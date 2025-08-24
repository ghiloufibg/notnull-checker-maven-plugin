package com.ghiloufi.notnullchecker.plugin;

import com.ghiloufi.notnullchecker.infrastructure.ioc.PluginContainer;
import com.ghiloufi.notnullchecker.report.ConsoleReport;
import com.ghiloufi.notnullchecker.scanner.SourceScanner;
import com.ghiloufi.notnullchecker.validation.NotNullAnnotationValidator;
import com.ghiloufi.notnullchecker.validation.ViolationCandidate;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "scan", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class NotNullScanMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(property = "notnull.failOnViolation", defaultValue = "true")
  private boolean failOnViolation;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    final var roots = project.getCompileSourceRoots();

    final var files = new AtomicInteger();

    final var allCandidates = new ArrayList<ViolationCandidate>();

    PluginContainer.setUpContext(getLog(), roots);

    SourceScanner scanner = PluginContainer.lookup(SourceScanner.class);

    ConsoleReport consoleReport = PluginContainer.lookup(ConsoleReport.class);

    for (String srcRoot : roots) {
      Path root = Paths.get(srcRoot);
      try {
        allCandidates.addAll(scanner.scan(root));
        files.incrementAndGet();
      } catch (IOException e) {
        throw new MojoExecutionException(
            String.format("Unable to traverse sources under %s", root), e);
      }
    }

    final var validator = PluginContainer.lookup(NotNullAnnotationValidator.class);

    final var violations = validator.getViolations(allCandidates);

    getLog()
        .info(
            String.format(
                "NotNull scan complete – checked %s source roots, found %s  violations.",
                files.get(), violations.size()));

    if (failOnViolation && !violations.isEmpty()) {
      consoleReport.reportViolations(violations);
      throw new MojoFailureException(
          String.format("Found %s  fields/parameters without @NotNull", violations));
    }
  }
}
