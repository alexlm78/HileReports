/**
 * HileReports load test suite — k6
 *
 * Usage:
 *   BASE_URL=http://localhost:8080 \
 *   TEST_USER=admin \
 *   TEST_PASS=admin123 \
 *   k6 run --scenario smoke hile-reports-load.js
 *
 * Scenarios: smoke | load | stress
 * Install k6: https://k6.io/docs/get-started/installation/
 */

import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

// ---------------------------------------------------------------------------
// Config
// ---------------------------------------------------------------------------
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const TEST_USER = __ENV.TEST_USER || "admin";
const TEST_PASS = __ENV.TEST_PASS || "admin123";

const errorRate = new Rate("errors");
const executionDuration = new Trend("report_execution_duration", true);
const catalogDuration = new Trend("catalog_duration", true);

// ---------------------------------------------------------------------------
// Scenarios
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    smoke: {
      executor: "constant-vus",
      vus: 1,
      duration: "30s",
      tags: { scenario: "smoke" },
    },
    load: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "1m", target: 50 },
        { duration: "3m", target: 50 },
        { duration: "1m", target: 0 },
      ],
      tags: { scenario: "load" },
    },
    stress: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "2m", target: 100 },
        { duration: "5m", target: 200 },
        { duration: "2m", target: 0 },
      ],
      tags: { scenario: "stress" },
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<2000"],
    report_execution_duration: ["p(95)<5000"],
    catalog_duration: ["p(95)<500"],
    errors: ["rate<0.05"],
  },
};

// ---------------------------------------------------------------------------
// Setup — runs once per test run, returns shared data
// ---------------------------------------------------------------------------
export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ username: TEST_USER, password: TEST_PASS }),
    { headers: { "Content-Type": "application/json" } }
  );

  check(loginRes, { "login 200": (r) => r.status === 200 });

  const token = loginRes.json("token");
  if (!token) {
    throw new Error(`Login failed: ${loginRes.body}`);
  }

  // Discover a published report ID to use in execute tests
  const catalogRes = http.get(`${BASE_URL}/api/v1/catalog`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  let reportId = null;
  if (catalogRes.status === 200) {
    const reports = catalogRes.json();
    if (reports && reports.length > 0) {
      reportId = reports[0].id;
    }
  }

  return { token, reportId };
}

// ---------------------------------------------------------------------------
// Default function — main VU loop
// ---------------------------------------------------------------------------
export default function (data) {
  const headers = {
    Authorization: `Bearer ${data.token}`,
    "Content-Type": "application/json",
  };

  // 1. Health check
  const health = http.get(`${BASE_URL}/actuator/health`);
  check(health, { "health 200": (r) => r.status === 200 });
  errorRate.add(health.status !== 200);

  sleep(0.5);

  // 2. Catalog browse
  const start = Date.now();
  const catalog = http.get(`${BASE_URL}/api/v1/catalog`, { headers });
  check(catalog, { "catalog 200": (r) => r.status === 200 });
  catalogDuration.add(Date.now() - start);
  errorRate.add(catalog.status !== 200);

  sleep(0.5);

  // 3. Report list
  const reports = http.get(`${BASE_URL}/api/v1/reports`, { headers });
  check(reports, { "reports 200": (r) => r.status === 200 });
  errorRate.add(reports.status !== 200);

  sleep(0.5);

  // 4. Report execution (only if a published report is available)
  if (data.reportId) {
    const execStart = Date.now();
    const exec = http.post(
      `${BASE_URL}/api/v1/reports/${data.reportId}/execute`,
      JSON.stringify({ parameters: {}, page: 1, pageSize: 10 }),
      { headers }
    );
    check(exec, { "execute 200": (r) => r.status === 200 });
    executionDuration.add(Date.now() - execStart);
    errorRate.add(exec.status !== 200);
  }

  sleep(1);
}
