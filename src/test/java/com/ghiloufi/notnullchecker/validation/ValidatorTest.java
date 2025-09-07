package com.ghiloufi.notnullchecker.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.ghiloufi.notnullchecker.infrastructure.ioc.PluginContainer;
import com.ghiloufi.notnullchecker.plugin.PluginTestConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@PluginTestConfig
public class ValidatorTest {

  private NotNullAnnotationValidator validator;

  @BeforeEach
  void setUp() {
    validator = PluginContainer.lookup(NotNullAnnotationValidator.class);
  }

  @Test
  void should_detects_missing_not_null_annotation() {
    final var candidate =
        new ViolationCandidate(
            ViolationCandidateEnum.FIELD,
            "name",
            "String",
            "com/ghiloufi/notnullchecker/samples/SampleClass.java",
            "line 4",
            List.of());

    int count = validator.getViolations(List.of(candidate)).size();
    assertEquals(1, count, "Should flag missing @NotNull");
  }

  @Test
  void should_not_validate_primitives_types() {
    final var candidate =
        new ViolationCandidate(
            ViolationCandidateEnum.FIELD,
            "age",
            "int",
            "com/ghiloufi/notnullchecker/samples/SampleClass.java",
            "line 4",
            List.of());

    final var count = validator.getViolations(List.of(candidate)).size();

    assertEquals(0, count, "Primitives should not be flagged");
  }

  @Test
  void should_detects_method_returns_null_literal() {
    final var candidate =
        new ViolationCandidate(
            ViolationCandidateEnum.RETURN_NULL_LITERAL,
            "getName",
            "String",
            "com/ghiloufi/notnullchecker/samples/SampleClass.java",
            "line 4",
            List.of());

    final var count = validator.getViolations(List.of(candidate)).size();

    assertEquals(1, count, "Should flag method return null literal");
  }
}
