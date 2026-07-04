package dev.kreaker.hile.infrastructure.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimpleReadOnlyQueryValidatorTest {

  private final SimpleReadOnlyQueryValidator validator = new SimpleReadOnlyQueryValidator();

  @Test
  void shouldAcceptSelectStatements() {
    assertTrue(validator.validateReadOnly("SELECT * FROM sales").valid());
  }

  @Test
  void shouldRejectDeleteStatements() {
    assertFalse(validator.validateReadOnly("DELETE FROM sales").valid());
  }

  @Test
  void shouldExtractNamedParameters() {
    assertEquals(
        2,
        validator
            .extractNamedParameters(
                """
            SELECT *
            FROM sales
            WHERE customer_id = :customerId
              AND order_date >= :dateFrom
            """)
            .size());
  }
}
