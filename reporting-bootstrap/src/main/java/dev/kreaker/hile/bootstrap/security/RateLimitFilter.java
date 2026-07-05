package dev.kreaker.hile.bootstrap.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * IP-based sliding-window rate limiter for expensive endpoints (execute, export). Uses a single
 * AtomicLong per IP packing the current minute window (high 32 bits) and the request count (low 32
 * bits) to enable lock-free CAS updates.
 *
 * <p>Not distributed — for multi-instance deployments, replace with a Redis-backed counter.
 */
@Component
@Order(100)
public class RateLimitFilter extends OncePerRequestFilter {

  private final int limitRpm;
  private final boolean enabled;
  private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

  public RateLimitFilter(
      @Value("${hile.reports.rate-limit.execute-rpm:30}") int limitRpm,
      @Value("${hile.reports.rate-limit.enabled:true}") boolean enabled) {
    this.limitRpm = limitRpm;
    this.enabled = enabled;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if (!enabled) return true;
    String uri = request.getRequestURI();
    return !(uri.endsWith("/execute") || uri.endsWith("/export"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String ip = resolveClientIp(request);
    if (!tryConsume(ip)) {
      response.setStatus(429);
      response.setContentType("application/json;charset=UTF-8");
      response
          .getWriter()
          .write("{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests\"}");
      return;
    }
    chain.doFilter(request, response);
  }

  private boolean tryConsume(String ip) {
    long currentMinute = System.currentTimeMillis() / 60_000L;
    AtomicLong slot = counters.computeIfAbsent(ip, k -> new AtomicLong(0L));
    while (true) {
      long packed = slot.get();
      long window = packed >>> 32;
      long count = packed & 0xFFFFFFFFL;
      if (window != currentMinute) {
        long reset = (currentMinute << 32) | 1L;
        if (slot.compareAndSet(packed, reset)) return true;
      } else if (count < limitRpm) {
        if (slot.compareAndSet(packed, packed + 1L)) return true;
      } else {
        return false;
      }
    }
  }

  private static String resolveClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].strip();
    }
    return request.getRemoteAddr();
  }
}
