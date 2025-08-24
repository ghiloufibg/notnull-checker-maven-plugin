package com.ghiloufi.notnullchecker.validation;

import com.ghiloufi.notnullchecker.infrastructure.ioc.PluginContainer;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.google.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.plugin.logging.Log;

@Singleton
public class NotNullAnnotationValidator {

  private static final String NOTNULL_QUALIFIED_NAME = "jakarta.validation.constraints.NotNull";

  private static final List<String> PRIMITIVES =
      List.of("byte", "short", "int", "long", "float", "double", "char", "boolean");

  private static final Log LOG = PluginContainer.lookup(Log.class);

  public List<ViolationCandidate> getViolations(@NotNull List<ViolationCandidate> candidates) {

    final var methodsReturningNull =
        candidates.stream()
            .parallel()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.RETURN_NULL_LITERAL)
            .peek(
                candidate ->
                    LOG.warn(
                        String.format(
                            "method %s in %s return null literal at %s",
                            candidate.name(), candidate.fileName(), candidate.position())))
            .toList();

    final var nullableField =
        candidates.stream()
            .parallel()
            .filter(candidate -> candidate.kind() != ViolationCandidateEnum.RETURN_NULL_LITERAL)
            .filter(
                candidate -> !this.hasNotNullAnnotation(candidate) && !isPrimitiveType(candidate))
            .peek(
                candidate ->
                    LOG.warn(
                        String.format(
                            "%s without @NotNull: %s in %s at %s",
                            candidate.kind().value(),
                            candidate.name(),
                            candidate.fileName(),
                            candidate.position())))
            .toList();

    return Stream.concat(nullableField.stream(), methodsReturningNull.stream()).toList();
  }

  private boolean hasNotNullAnnotation(@NotNull ViolationCandidate candidate) {
    return candidate.annotations().stream().anyMatch(this::isNotNullAnnotation);
  }

  private boolean isPrimitiveType(@NotNull ViolationCandidate candidate) {
    return PRIMITIVES.contains(candidate.type());
  }

  private boolean isNotNullAnnotation(@NotNull AnnotationExpr expr) {
    try {
      return expr.resolve().getQualifiedName().equals(NOTNULL_QUALIFIED_NAME);
    } catch (UnsolvedSymbolException e) {
      LOG.debug(String.format("Could not resolve not null annotation %s", e));
      return false;
    }
  }
}
