package dev.kreaker.hile.infrastructure.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SimpleReadOnlyQueryValidatorTest {

  private final SimpleReadOnlyQueryValidator validator = new SimpleReadOnlyQueryValidator();

  // --- Basic acceptance ---

  @Test
  void shouldAcceptSelectStatement() {
    assertTrue(validator.validateReadOnly("SELECT * FROM sales").valid());
  }

  @Test
  void shouldAcceptWithCteStatement() {
    assertTrue(validator.validateReadOnly("WITH cte AS (SELECT 1 AS n) SELECT n FROM cte").valid());
  }

  @Test
  void shouldAcceptSelectWithNamedParams() {
    assertTrue(
        validator
            .validateReadOnly("SELECT * FROM orders WHERE id = :id AND status = :status")
            .valid());
  }

  // --- DML / DDL blocking ---

  @Test
  void shouldRejectDeleteStatement() {
    assertFalse(validator.validateReadOnly("DELETE FROM sales").valid());
  }

  @Test
  void shouldRejectInsertStatement() {
    assertFalse(validator.validateReadOnly("INSERT INTO sales VALUES (1)").valid());
  }

  @Test
  void shouldRejectUpdateStatement() {
    assertFalse(validator.validateReadOnly("UPDATE sales SET x = 1").valid());
  }

  @Test
  void shouldRejectDropStatement() {
    assertFalse(validator.validateReadOnly("DROP TABLE sales").valid());
  }

  @Test
  void shouldRejectSemicolon() {
    assertFalse(validator.validateReadOnly("SELECT 1; SELECT 2").valid());
  }

  @Test
  void shouldRejectQueryNotStartingWithSelectOrWith() {
    assertFalse(validator.validateReadOnly("SHOW TABLES").valid());
  }

  // --- Comment stripping: comments must not affect valid SQL ---

  @Test
  void shouldAcceptSelectWithLineComment() {
    assertTrue(validator.validateReadOnly("SELECT * FROM sales -- filter active only").valid());
  }

  @Test
  void shouldAcceptSelectWithBlockComment() {
    assertTrue(validator.validateReadOnly("SELECT /* all rows */ * FROM sales").valid());
  }

  @Test
  void shouldAcceptSelectWithMultilineBlockComment() {
    assertTrue(
        validator
            .validateReadOnly(
                """
                SELECT
                  /* report: monthly sales
                     author: team */
                  month, total
                FROM sales_summary
                """)
            .valid());
  }

  // --- Comment stripping: hidden commands must be stripped and then NOT trigger false positives
  // ---

  @Test
  void shouldNotBlockForbiddenTokenInsideBlockComment() {
    // "DELETE" is inside a comment — after stripping it disappears; remaining SQL is valid
    assertTrue(validator.validateReadOnly("SELECT /* DELETE FROM users */ * FROM sales").valid());
  }

  @Test
  void shouldNotBlockForbiddenTokenInsideLineComment() {
    assertTrue(
        validator.validateReadOnly("SELECT * FROM sales -- DROP TABLE not executed").valid());
  }

  // --- Stacked query hiding via comments ---

  @Test
  void shouldNotPanicOnSemicolonInsideBlockComment() {
    // Semicolon is inside a comment; after stripping the remaining SQL has no semicolon
    assertTrue(validator.validateReadOnly("SELECT * FROM t /* ; DROP TABLE t */").valid());
  }

  // --- Dangerous pattern blocking ---

  @Test
  void shouldBlockSleepFunction() {
    assertFalse(validator.validateReadOnly("SELECT SLEEP(5)").valid());
  }

  @Test
  void shouldBlockBenchmarkFunction() {
    assertFalse(validator.validateReadOnly("SELECT BENCHMARK(1000000, MD5(1))").valid());
  }

  @Test
  void shouldBlockWaitfor() {
    assertFalse(validator.validateReadOnly("SELECT 1; WAITFOR DELAY '0:0:5'").valid());
  }

  @Test
  void shouldBlockLoadFile() {
    assertFalse(validator.validateReadOnly("SELECT LOAD_FILE('/etc/passwd')").valid());
  }

  @Test
  void shouldBlockIntoOutfile() {
    assertFalse(validator.validateReadOnly("SELECT * FROM t INTO OUTFILE '/tmp/data.csv'").valid());
  }

  @Test
  void shouldBlockIntoDumpfile() {
    assertFalse(validator.validateReadOnly("SELECT * FROM t INTO DUMPFILE '/tmp/data'").valid());
  }

  @Test
  void shouldBlockInformationSchemaAccess() {
    assertFalse(validator.validateReadOnly("SELECT * FROM information_schema.tables").valid());
  }

  @Test
  void shouldBlockPgCatalogAccess() {
    assertFalse(validator.validateReadOnly("SELECT * FROM pg_catalog.pg_tables").valid());
  }

  @Test
  void shouldBlockPgReadFile() {
    assertFalse(validator.validateReadOnly("SELECT pg_read_file('/etc/passwd')").valid());
  }

  @Test
  void shouldBlockPgLsDir() {
    assertFalse(validator.validateReadOnly("SELECT pg_ls_dir('/tmp')").valid());
  }

  @Test
  void shouldBlockXpCmdshell() {
    assertFalse(validator.validateReadOnly("EXEC xp_cmdshell 'whoami'").valid());
  }

  @Test
  void shouldBlockExecuteKeyword() {
    assertFalse(validator.validateReadOnly("SELECT 1; EXECUTE 'DROP TABLE users'").valid());
  }

  // --- Dangerous pattern hidden in comment must not bypass after stripping ---

  @Test
  void shouldBlockSleepHiddenInLineCommentIfItRemains() {
    // SLEEP is not in a comment here — it's in the actual SQL
    assertFalse(validator.validateReadOnly("SELECT SLEEP(1) -- looks safe").valid());
  }

  @Test
  void shouldNotBlockSleepWhenFullyInsideComment() {
    // SLEEP is entirely inside the comment; after stripping it's gone
    assertTrue(validator.validateReadOnly("SELECT 1 /* SLEEP(999) injected */").valid());
  }

  // --- Named parameter extraction ---

  @Test
  void shouldExtractNamedParameters() {
    List<String> params =
        validator.extractNamedParameters(
            """
            SELECT *
            FROM sales
            WHERE customer_id = :customerId
              AND order_date >= :dateFrom
            """);
    assertEquals(2, params.size());
    assertTrue(params.contains("customerId"));
    assertTrue(params.contains("dateFrom"));
  }

  @Test
  void shouldNotExtractNamedParametersFromLineComments() {
    List<String> params =
        validator.extractNamedParameters("SELECT * FROM t WHERE id = :id -- :commentParam ignored");
    assertEquals(1, params.size());
    assertEquals("id", params.get(0));
  }

  @Test
  void shouldNotExtractNamedParametersFromBlockComments() {
    List<String> params =
        validator.extractNamedParameters("SELECT * FROM t WHERE id = :id /* :blockParam */");
    assertEquals(1, params.size());
    assertEquals("id", params.get(0));
  }

  @Test
  void shouldDeduplicateNamedParameters() {
    List<String> params =
        validator.extractNamedParameters("SELECT * FROM t WHERE a = :x OR b = :x OR c = :y");
    assertEquals(2, params.size());
  }

  @Test
  void shouldReturnEmptyListForNullSql() {
    assertEquals(0, validator.extractNamedParameters(null).size());
  }
}
