package com.ghiloufi.notnullchecker.scanner;

import static org.junit.jupiter.api.Assertions.*;

import com.ghiloufi.notnullchecker.infrastructure.ioc.PluginContainer;
import com.ghiloufi.notnullchecker.plugin.PluginTestConfig;
import com.ghiloufi.notnullchecker.plugin.SourceRoot;
import com.ghiloufi.notnullchecker.validation.ViolationCandidateEnum;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@PluginTestConfig
public class SourceScannerTest {

  private SourceScanner scanner;

  @SourceRoot private Path srcDir;

  @BeforeEach
  void setUp() {
    scanner = PluginContainer.lookup(SourceScanner.class);
  }

  @Test
  void should_find_fields_and_params() throws Exception {
    int fieldAndMethodParameterCount =
        scanner.scan(srcDir).stream()
            .filter(
                candidate ->
                    candidate.kind() == ViolationCandidateEnum.FIELD
                        || candidate.kind() == ViolationCandidateEnum.PARAMETER)
            .toList()
            .size();

    assertEquals(6, fieldAndMethodParameterCount, "Should detect fields and parameters");
  }

  @Test
  void should_find_lambda_params() throws Exception {
    int lambdaParameterCount =
        scanner.scan(srcDir).stream()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.LAMBDA_PARAMETER)
            .toList()
            .size();

    assertEquals(1, lambdaParameterCount, "Should detect lambda parameters");
  }

  @Test
  void should_find_constructor_params() throws Exception {
    int constructorParameterCount =
        scanner.scan(srcDir).stream()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.CONSTRUCTOR_PARAMETER)
            .toList()
            .size();

    assertEquals(1, constructorParameterCount, "Should detect constructor parameters");
  }

  @Test
  void should_find_type_parameters() throws Exception {
    int typeParameterCount =
        scanner.scan(srcDir).stream()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.TYPE_PARAMETER)
            .toList()
            .size();

    assertEquals(1, typeParameterCount, "Should detect generic parameters");
  }

  @Test
  void should_find_collection_fields() throws Exception {
    int collectionFieldElementCount =
        scanner.scan(srcDir).stream()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.COLLECTION_ELEMENT)
            .toList()
            .size();

    assertEquals(1, collectionFieldElementCount, "Should detect collection element");
  }

  @Test
  void should_find_methods_return_types() throws Exception {
    int methodReturnTypeCount =
        scanner.scan(srcDir).stream()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.RETURN_TYPE)
            .toList()
            .size();

    assertEquals(2, methodReturnTypeCount, "Should detect method return type");
  }

  @Test
  void should_find_methods_return_null_literal() throws Exception {
    int methodReturnNullLiteralCount =
        scanner.scan(srcDir).stream()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.RETURN_NULL_LITERAL)
            .toList()
            .size();

    assertEquals(1, methodReturnNullLiteralCount, "Should detect method return null literal");
  }

  @Test
  void should_find_local_null_assignments() throws Exception {
    int localNullAssignmentCount =
        scanner.scan(srcDir).stream()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.LOCAL_NULL_ASSIGNMENT)
            .toList()
            .size();

    assertEquals(
        4, localNullAssignmentCount, "Should detect both declaration and reassignment to null");
  }

  @Test
  void should_find_method_null_arguments() throws Exception {
    int methodNullArgumentCount =
        scanner.scan(srcDir).stream()
            .filter(candidate -> candidate.kind() == ViolationCandidateEnum.METHOD_NULL_ARGUMENT)
            .toList()
            .size();

    assertEquals(2, methodNullArgumentCount, "Should detect null passed as method argument");
  }

  @Test
  void should_find_constructor_null_arguments() throws Exception {
    int constructorNullArgumentCount =
        scanner.scan(srcDir).stream()
            .filter(
                candidate -> candidate.kind() == ViolationCandidateEnum.CONSTRUCTOR_NULL_ARGUMENT)
            .toList()
            .size();

    assertEquals(
        1, constructorNullArgumentCount, "Should detect null passed as constructor argument");
  }

  @Test
  void should_find_lambda_return_null_literal() throws Exception {
    int count =
        scanner.scan(srcDir).stream()
            .filter(
                candidate -> candidate.kind() == ViolationCandidateEnum.LAMBDA_RETURN_NULL_LITERAL)
            .toList()
            .size();

    assertEquals(2, count, "Should detect lambda returning null");
  }
}
