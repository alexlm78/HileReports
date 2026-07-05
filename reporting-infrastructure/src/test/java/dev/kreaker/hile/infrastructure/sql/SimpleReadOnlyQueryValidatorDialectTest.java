package dev.kreaker.hile.infrastructure.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Dialect-specific cases for {@link SimpleReadOnlyQueryValidator}. Covers PostgreSQL and MySQL SQL
 * surface that may appear in user-authored report queries.
 */
class SimpleReadOnlyQueryValidatorDialectTest {

  private final SimpleReadOnlyQueryValidator validator = new SimpleReadOnlyQueryValidator();

  // -------------------------------------------------------------------------
  // PostgreSQL — acceptance
  // -------------------------------------------------------------------------

  @Nested
  class PostgreSqlAccept {

    @Test
    void castOperatorDoubleColon() {
      assertTrue(
          validator
              .validateReadOnly("SELECT id::TEXT, amount::NUMERIC FROM orders WHERE year = :year")
              .valid());
    }

    @Test
    void ilikeOperator() {
      assertTrue(
          validator.validateReadOnly("SELECT * FROM products WHERE name ILIKE :pattern").valid());
    }

    @Test
    void nullsLastOrdering() {
      assertTrue(
          validator.validateReadOnly("SELECT * FROM tasks ORDER BY due_date NULLS LAST").valid());
    }

    @Test
    void fetchNextPagination() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT * FROM orders ORDER BY created_at OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY")
              .valid());
    }

    @Test
    void atTimeZone() {
      assertTrue(
          validator
              .validateReadOnly("SELECT * FROM events WHERE ts AT TIME ZONE 'UTC' > :cutoff")
              .valid());
    }

    @Test
    void recursiveCte() {
      assertTrue(
          validator
              .validateReadOnly(
                  """
                  WITH RECURSIVE subordinates(id, name, manager_id) AS (
                    SELECT id, name, manager_id FROM employees WHERE id = :rootId
                    UNION ALL
                    SELECT e.id, e.name, e.manager_id
                    FROM employees e JOIN subordinates s ON e.manager_id = s.id
                  )
                  SELECT * FROM subordinates
                  """)
              .valid());
    }

    @Test
    void jsonArrowOperator() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT data->>'name' AS name, data->'address'->>'city' AS city"
                      + " FROM documents WHERE user_id = :uid")
              .valid());
    }

    @Test
    void qualifiedSchemaName() {
      assertTrue(
          validator
              .validateReadOnly("SELECT * FROM reporting.orders WHERE status = :status")
              .valid());
    }

    @Test
    void coalesceWithCast() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT COALESCE(manager_name::TEXT, 'N/A') AS manager FROM employees"
                      + " WHERE dept_id = :deptId")
              .valid());
    }

    @Test
    void arrayAnyWithParam() {
      assertTrue(
          validator
              .validateReadOnly("SELECT * FROM reports WHERE category_id = ANY(:ids)")
              .valid());
    }
  }

  // -------------------------------------------------------------------------
  // PostgreSQL — rejection
  // -------------------------------------------------------------------------

  @Nested
  class PostgreSqlReject {

    @Test
    void pgSleepBlocked() {
      assertFalse(validator.validateReadOnly("SELECT pg_sleep(5)").valid());
    }

    @Test
    void pgSleepInSubqueryBlocked() {
      assertFalse(
          validator
              .validateReadOnly("SELECT * FROM t WHERE 1 = (SELECT 1 FROM pg_sleep(2))")
              .valid());
    }

    @Test
    void pgReadFileBlocked() {
      assertFalse(validator.validateReadOnly("SELECT pg_read_file('/etc/passwd')").valid());
    }

    @Test
    void pgLsDirBlocked() {
      assertFalse(validator.validateReadOnly("SELECT * FROM pg_ls_dir('/var')").valid());
    }

    @Test
    void pgCatalogBlocked() {
      assertFalse(validator.validateReadOnly("SELECT * FROM pg_catalog.pg_class").valid());
    }
  }

  // -------------------------------------------------------------------------
  // MySQL — acceptance
  // -------------------------------------------------------------------------

  @Nested
  class MySqlAccept {

    @Test
    void backtickQuotedIdentifiers() {
      assertTrue(
          validator.validateReadOnly("SELECT * FROM `orders` WHERE `user_id` = :uid").valid());
    }

    @Test
    void limitOffsetPagination() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT * FROM sales ORDER BY created_at LIMIT :pageSize OFFSET :offset")
              .valid());
    }

    @Test
    void ifFunction() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT IF(status = 'active', 1, 0) AS is_active FROM users WHERE dept_id = :deptId")
              .valid());
    }

    @Test
    void ifnullFunction() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT IFNULL(manager_name, 'N/A') AS manager FROM employees WHERE id = :id")
              .valid());
    }

    @Test
    void dateFormatFunction() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, SUM(total) FROM orders"
                      + " WHERE year = :year GROUP BY month")
              .valid());
    }

    @Test
    void groupConcatFunction() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT dept_id, GROUP_CONCAT(name ORDER BY name SEPARATOR ', ') AS members"
                      + " FROM employees WHERE active = :active GROUP BY dept_id")
              .valid());
    }

    @Test
    void useIndexHint() {
      assertTrue(
          validator
              .validateReadOnly(
                  "SELECT * FROM orders USE INDEX (idx_status) WHERE status = :status")
              .valid());
    }
  }

  // -------------------------------------------------------------------------
  // MySQL — rejection
  // -------------------------------------------------------------------------

  @Nested
  class MySqlReject {

    @Test
    void sleepBlocked() {
      assertFalse(validator.validateReadOnly("SELECT SLEEP(5)").valid());
    }

    @Test
    void benchmarkBlocked() {
      assertFalse(validator.validateReadOnly("SELECT BENCHMARK(1000000, MD5(1))").valid());
    }

    @Test
    void loadFileBlocked() {
      assertFalse(validator.validateReadOnly("SELECT LOAD_FILE('/etc/passwd')").valid());
    }

    @Test
    void intoOutfileBlocked() {
      assertFalse(
          validator.validateReadOnly("SELECT * FROM t INTO OUTFILE '/tmp/data.csv'").valid());
    }
  }

  // -------------------------------------------------------------------------
  // Named-parameter extraction — dialect edge cases
  // -------------------------------------------------------------------------

  @Nested
  class ParameterExtractionDialect {

    @Test
    void pgCastSyntaxDoesNotYieldSpuriousParams() {
      // "id::TEXT" must not extract "TEXT" as a named parameter
      List<String> params =
          validator.extractNamedParameters("SELECT id::TEXT, amount::NUMERIC FROM orders");
      assertEquals(0, params.size());
    }

    @Test
    void pgCastMixedWithRealParams() {
      List<String> params =
          validator.extractNamedParameters(
              "SELECT id::TEXT FROM orders WHERE status = :status AND year = :year");
      assertEquals(2, params.size());
      assertTrue(params.contains("status"));
      assertTrue(params.contains("year"));
    }

    @Test
    void mysqlBacktickWithParam() {
      List<String> params =
          validator.extractNamedParameters(
              "SELECT * FROM `orders` WHERE `user_id` = :uid AND `status` = :status");
      assertEquals(2, params.size());
      assertTrue(params.contains("uid"));
      assertTrue(params.contains("status"));
    }

    @Test
    void pgRecursiveCteParamsExtracted() {
      List<String> params =
          validator.extractNamedParameters(
              "WITH RECURSIVE sub AS (SELECT * FROM t WHERE id = :rootId) SELECT * FROM sub"
                  + " WHERE depth < :maxDepth");
      assertEquals(2, params.size());
      assertTrue(params.contains("rootId"));
      assertTrue(params.contains("maxDepth"));
    }
  }
}
