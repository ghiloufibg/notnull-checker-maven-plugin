package com.ghiloufi.notnullchecker.scanner;

import com.ghiloufi.notnullchecker.validation.ViolationCandidate;
import com.ghiloufi.notnullchecker.validation.ViolationCandidateEnum;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.google.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class NodeScanner {

  List<ViolationCandidate> findAllField(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(FieldDeclaration.class).stream()
        .parallel()
        .map(
            field ->
                new ViolationCandidate(
                    ViolationCandidateEnum.FIELD,
                    getFieldName(field.getVariables()),
                    getFieldType(field.getVariables()),
                    getFieldPosition(field.getVariables()),
                    sourceFile.toString(),
                    field.getAnnotations()))
        .toList();
  }

  List<ViolationCandidate> findAllMethodParameter(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(MethodDeclaration.class).stream()
        .parallel()
        .map(MethodDeclaration::getParameters)
        .flatMap(Collection::stream)
        .map(
            parameter ->
                new ViolationCandidate(
                    ViolationCandidateEnum.PARAMETER,
                    parameter.getName().asString(),
                    parameter.getType().asString(),
                    parameter.getRange().map(Range::toString).orElse(""),
                    sourceFile.toString(),
                    parameter.getAnnotations()))
        .toList();
  }

  List<ViolationCandidate> findAllLambdaParameter(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(LambdaExpr.class).stream()
        .parallel()
        .map(LambdaExpr::getParameters)
        .flatMap(Collection::stream)
        .map(
            parameter ->
                new ViolationCandidate(
                    ViolationCandidateEnum.LAMBDA_PARAMETER,
                    parameter.getName().asString(),
                    parameter.resolve().getType().describe(),
                    parameter.getRange().map(Range::toString).orElse(""),
                    sourceFile.toString(),
                    parameter.getAnnotations()))
        .toList();
  }

  List<ViolationCandidate> findAllConstructorParameter(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(ConstructorDeclaration.class).stream()
        .parallel()
        .map(ConstructorDeclaration::getParameters)
        .flatMap(Collection::stream)
        .map(
            parameter ->
                new ViolationCandidate(
                    ViolationCandidateEnum.CONSTRUCTOR_PARAMETER,
                    parameter.getName().asString(),
                    parameter.getType().asString(),
                    sourceFile.toString(),
                    parameter.getRange().map(Range::toString).orElse(""),
                    parameter.getAnnotations()))
        .toList();
  }

  List<ViolationCandidate> findAllTypeParameters(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(TypeParameter.class).stream()
        .parallel()
        .map(
            typeParam ->
                new ViolationCandidate(
                    ViolationCandidateEnum.TYPE_PARAMETER,
                    typeParam.getNameAsString(),
                    typeParam.getTypeBound().isEmpty()
                        ? "unbounded"
                        : typeParam.getTypeBound().stream()
                            .map(Type::asString)
                            .collect(Collectors.joining(" & ")),
                    sourceFile.toString(),
                    typeParam.getRange().map(Range::toString).orElse(""),
                    typeParam.getAnnotations()))
        .toList();
  }

  List<ViolationCandidate> findAllGenericElements(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {
    return cu.findAll(FieldDeclaration.class).stream()
        .parallel()
        .flatMap(
            fieldDeclaration ->
                fieldDeclaration.getVariables().stream()
                    .map(
                        variableDeclarator ->
                            getGenericElements(
                                variableDeclarator.getType(),
                                variableDeclarator.getNameAsString(),
                                sourceFile,
                                variableDeclarator.getRange().map(Range::toString).orElse(""))))
        .flatMap(Collection::stream)
        .toList();
  }

  List<ViolationCandidate> findAllMethodReturnType(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(MethodDeclaration.class).stream()
        .parallel()
        .filter(
            method -> {
              var type = method.getType();
              return !type.isVoidType() && !type.isPrimitiveType() && !isOptionalType(type);
            })
        .map(
            method ->
                new ViolationCandidate(
                    ViolationCandidateEnum.RETURN_TYPE,
                    method.getNameAsString(),
                    method.getType().asString(),
                    sourceFile.toString(),
                    method.getRange().map(Range::toString).orElse(""),
                    method.getAnnotations()))
        .toList();
  }

  List<ViolationCandidate> findAllMethodReturnNullLiteral(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(MethodDeclaration.class).stream()
        .parallel()
        .filter(method -> !method.getType().isVoidType() && !method.getType().isPrimitiveType())
        .filter(
            method ->
                method.findAll(ReturnStmt.class).stream()
                    .anyMatch(
                        ret ->
                            ret.getExpression().map(Expression::isNullLiteralExpr).orElse(false)))
        .map(
            method ->
                new ViolationCandidate(
                    ViolationCandidateEnum.RETURN_NULL_LITERAL,
                    method.getNameAsString(),
                    method.getType().asString(),
                    sourceFile.toString(),
                    method.getRange().map(Range::toString).orElse(""),
                    method.getAnnotations()))
        .toList();
  }

  List<ViolationCandidate> findAllLocalNullAssignments(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    final var localNullAssignments =
        cu.findAll(VariableDeclarator.class).stream()
            .parallel()
            .filter(
                variableDeclarator ->
                    variableDeclarator.getInitializer().isPresent()
                        && variableDeclarator.getInitializer().get().isNullLiteralExpr())
            .map(
                var ->
                    new ViolationCandidate(
                        ViolationCandidateEnum.LOCAL_NULL_ASSIGNMENT,
                        var.getNameAsString(),
                        var.getType().asString(),
                        sourceFile.toString(),
                        var.getRange().map(Range::toString).orElse(""),
                        List.of()))
            .toList();

    final var localNullReassignments =
        cu.findAll(AssignExpr.class).stream()
            .parallel()
            .filter(assign -> assign.getValue().isNullLiteralExpr())
            .map(
                assign ->
                    new ViolationCandidate(
                        ViolationCandidateEnum.LOCAL_NULL_ASSIGNMENT,
                        assign.getTarget().toString(),
                        "Object",
                        sourceFile.toString(),
                        assign.getRange().map(Range::toString).orElse(""),
                        List.of()))
            .toList();

    return Stream.concat(localNullAssignments.stream(), localNullReassignments.stream()).toList();
  }

  List<ViolationCandidate> findAllMethodNullArguments(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(MethodCallExpr.class).stream()
        .parallel()
        .flatMap(
            call ->
                call.getArguments().stream()
                    .filter(Expression::isNullLiteralExpr)
                    .map(
                        arg ->
                            new ViolationCandidate(
                                ViolationCandidateEnum.METHOD_NULL_ARGUMENT,
                                call.getNameAsString(),
                                "Object",
                                sourceFile.toString(),
                                arg.getRange().map(Range::toString).orElse(""),
                                List.of())))
        .toList();
  }

  List<ViolationCandidate> findAllConstructorNullArguments(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(ObjectCreationExpr.class).stream()
        .parallel()
        .flatMap(
            ctor ->
                ctor.getArguments().stream()
                    .filter(Expression::isNullLiteralExpr)
                    .map(
                        arg ->
                            new ViolationCandidate(
                                ViolationCandidateEnum.CONSTRUCTOR_NULL_ARGUMENT,
                                ctor.getType().asString(),
                                "Object",
                                sourceFile.toString(),
                                arg.getRange().map(Range::toString).orElse(""),
                                List.of())))
        .toList();
  }

  List<ViolationCandidate> findAllLambdaReturnNullLiteral(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {

    return cu.findAll(LambdaExpr.class).stream()
        .parallel()
        .filter(
            lambda -> {
              if (lambda.getBody().isExpressionStmt()) {
                return lambda.getBody().asExpressionStmt().getExpression().isNullLiteralExpr();
              } else if (lambda.getBody().isBlockStmt()) {
                return lambda.getBody().asBlockStmt().getStatements().stream()
                    .filter(Statement::isReturnStmt)
                    .map(Statement::asReturnStmt)
                    .map(ReturnStmt::getExpression)
                    .anyMatch(
                        opExpression ->
                            opExpression.map(Expression::isNullLiteralExpr).orElse(false));
              }
              return false;
            })
        .map(
            lambda ->
                new ViolationCandidate(
                    ViolationCandidateEnum.LAMBDA_RETURN_NULL_LITERAL,
                    lambda
                        .getParentNode()
                        .filter(VariableDeclarator.class::isInstance)
                        .map(VariableDeclarator.class::cast)
                        .map(VariableDeclarator::getName)
                        .map(SimpleName::asString)
                        .orElse(""),
                    lambda.calculateResolvedType().describe(),
                    sourceFile.toString(),
                    lambda.getRange().map(Range::toString).orElse(""),
                    List.of()))
        .toList();
  }

  List<ViolationCandidate> findAllRecordComponents(
      @NotNull final CompilationUnit cu, @NotNull final Path sourceFile) {
    return cu.findAll(RecordDeclaration.class).stream()
        .parallel()
        .flatMap(recordDecl -> recordDecl.getParameters().stream())
        .map(
            param ->
                new ViolationCandidate(
                    ViolationCandidateEnum.RECORD_COMPONENT,
                    param.getName().asString(),
                    param.getType().asString(),
                    sourceFile.toString(),
                    param.getRange().map(Range::toString).orElse(""),
                    param.getAnnotations()))
        .toList();
  }

  private List<ViolationCandidate> getGenericElements(
      final @NotNull Type type,
      final @NotNull String name,
      final @NotNull Path sourceFile,
      final @NotNull String range) {

    if (!type.isClassOrInterfaceType()) {
      return List.of();
    }

    return type.asClassOrInterfaceType()
        .getTypeArguments()
        .map(
            typeArgs ->
                typeArgs.stream()
                    .map(
                        typeArg -> {
                          final var candidates = new ArrayList<ViolationCandidate>();

                          candidates.add(
                              new ViolationCandidate(
                                  ViolationCandidateEnum.COLLECTION_ELEMENT,
                                  name,
                                  typeArg.asString(),
                                  range,
                                  sourceFile.toString(),
                                  typeArg.getAnnotations()));

                          candidates.addAll(getGenericElements(typeArg, name, sourceFile, range));

                          return candidates;
                        })
                    .flatMap(Collection::stream)
                    .toList())
        .orElse(List.of());
  }

  private String getFieldName(@NotNull List<VariableDeclarator> declarators) {
    return declarators.stream()
        .map(VariableDeclarator::getName)
        .map(SimpleName::asString)
        .findFirst()
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format("Unable to get the variable name from %s", declarators)));
  }

  private String getFieldType(@NotNull List<VariableDeclarator> declarators) {
    return declarators.stream()
        .map(VariableDeclarator::getType)
        .map(Type::asString)
        .findFirst()
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format("Unable to get the variable type from %s", declarators)));
  }

  private String getFieldPosition(@NotNull List<VariableDeclarator> declarators) {
    return declarators.stream()
        .map(VariableDeclarator::getRange)
        .map(opRange -> opRange.map(Range::toString).orElse(""))
        .findFirst()
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format("Unable to get the variable position from %s", declarators)));
  }

  private boolean isOptionalType(@NotNull Type type) {
    return type.resolve().asReferenceType().getQualifiedName().equals(Optional.class.getName());
  }
}
