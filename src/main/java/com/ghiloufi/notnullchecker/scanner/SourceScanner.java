package com.ghiloufi.notnullchecker.scanner;

import com.ghiloufi.notnullchecker.infrastructure.ioc.PluginContainer;
import com.ghiloufi.notnullchecker.validation.ViolationCandidate;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.Singleton;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class SourceScanner {

  private final String JAVA_FILE_EXTENTION = ".java";

  private final JavaParser parser;
  private final NodeScanner nodeScanner;

  public SourceScanner() {
    nodeScanner = PluginContainer.lookup(NodeScanner.class);
    parser = PluginContainer.lookup(JavaParser.class);
  }

  public List<ViolationCandidate> scan(@NotNull Path root) throws IOException {
    try (Stream<Path> allFiles = Files.walk(root)) {
      return allFiles
          .parallel()
          .filter(this::isJavaFile)
          .map(this::getViolationCandidateFromFile)
          .flatMap(Collection::stream)
          .toList();
    }
  }

  private boolean isJavaFile(@NotNull Path filePath) {
    return filePath.toString().endsWith(JAVA_FILE_EXTENTION);
  }

  private CompilationUnit parse(@NotNull Path filePath) throws IOException {
    return parser
        .parse(filePath)
        .getResult()
        .orElseThrow(() -> new IOException(String.format("Cannot parse %s", filePath.toString())));
  }

  private List<ViolationCandidate> getViolationCandidateFromFile(@NotNull Path filePath) {
    final var candidates = new ArrayList<ViolationCandidate>();
    try {
      final CompilationUnit cu = parse(filePath);

      final var fileName = filePath.getFileName();

      // Collect fields
      candidates.addAll(nodeScanner.findAllField(cu, fileName));

      // Collect parameters
      candidates.addAll(nodeScanner.findAllMethodParameter(cu, fileName));

      // Collect lambda parameters
      candidates.addAll(nodeScanner.findAllLambdaParameter(cu, fileName));

      // Collect constructor parameters
      candidates.addAll(nodeScanner.findAllConstructorParameter(cu, fileName));

      // Collect type parameters
      candidates.addAll(nodeScanner.findAllTypeParameters(cu, fileName));

      // Collect collection elements
      candidates.addAll(nodeScanner.findAllGenericElements(cu, fileName));

      // Collect methods returns
      candidates.addAll(nodeScanner.findAllMethodReturnType(cu, fileName));

      // Collect methods return null literal
      candidates.addAll(nodeScanner.findAllMethodReturnNullLiteral(cu, fileName));

      // Collect local null assignments
      candidates.addAll(nodeScanner.findAllLocalNullAssignments(cu, fileName));

      // Collect method null arguments
      candidates.addAll(nodeScanner.findAllMethodNullArguments(cu, fileName));

      // Collect constructor null arguments
      candidates.addAll(nodeScanner.findAllConstructorNullArguments(cu, fileName));

      // Collect lambda return null literal
      candidates.addAll(nodeScanner.findAllLambdaReturnNullLiteral(cu, fileName));

      // Collect record parameters
      candidates.addAll(nodeScanner.findAllRecordComponents(cu, fileName));

    } catch (IOException | ParseProblemException e) {
      throw new RuntimeException(
          String.format("Failed to parse %s: %s", filePath.toString(), e.getMessage()));
    }
    return candidates;
  }
}
