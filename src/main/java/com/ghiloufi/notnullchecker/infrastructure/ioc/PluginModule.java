package com.ghiloufi.notnullchecker.infrastructure.ioc;

import com.ghiloufi.notnullchecker.report.ConsoleReport;
import com.ghiloufi.notnullchecker.scanner.NodeScanner;
import com.ghiloufi.notnullchecker.scanner.SourceScanner;
import com.ghiloufi.notnullchecker.validation.NotNullAnnotationValidator;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.inject.AbstractModule;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

public class PluginModule extends AbstractModule {

  private final Log log;
  private final List<String> srcDirs;

  public PluginModule(final @NotNull Log log, final @NotNull List<String> srcDirs) {
    this.log = log;
    this.srcDirs = srcDirs;
  }

  @Override
  protected void configure() {
    bind(NodeScanner.class);
    bind(Log.class).toInstance(log);
    bind(JavaParser.class).toInstance(buildParser());
    bind(SourceScanner.class);
    bind(NotNullAnnotationValidator.class);
    bind(ConsoleReport.class).toInstance(new ConsoleReport(log));
  }

  private JavaParser buildParser() {

    CombinedTypeSolver typeSolver = new CombinedTypeSolver();

    typeSolver.add(new ReflectionTypeSolver());

    srcDirs.forEach(srcDir -> typeSolver.add(new JavaParserTypeSolver(srcDir)));

    typeSolver.add(new ClassLoaderTypeSolver(getClass().getClassLoader()));

    ParserConfiguration config = new ParserConfiguration();

    config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

    config.setSymbolResolver(new JavaSymbolSolver(typeSolver));

    return new JavaParser(config);
  }
}
