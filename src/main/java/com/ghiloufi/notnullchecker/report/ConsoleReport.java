package com.ghiloufi.notnullchecker.report;

import com.ghiloufi.notnullchecker.validation.ViolationCandidate;
import com.ghiloufi.notnullchecker.validation.ViolationCandidateEnum;
import com.google.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.maven.plugin.logging.Log;

@Singleton
public record ConsoleReport(@NotNull Log log) {

  public void reportViolations(@NotNull Collection<ViolationCandidate> violations) {

    log.info("============================================================");
    log.info("              @NotNull VIOLATION REPORT");
    log.info("============================================================");
    log.info(String.format("Total violations: %d", violations.size()));

    if (violations.isEmpty()) {
      log.info("No violations found !");
      return;
    }

    var byKind =
        violations.stream()
            .collect(
                Collectors.groupingBy(ViolationCandidate::kind, TreeMap::new, Collectors.toList()));

    for (var entry : byKind.entrySet()) {
      ViolationCandidateEnum kind = entry.getKey();
      List<ViolationCandidate> list = entry.getValue();

      log.info(String.format("-- %s (%d)", kind.value(), list.size()));

      var printer =
          new TablePrinter<>(
              list,
              List.of(
                  new Column<>("Name", ViolationCandidate::name, 10, 30),
                  new Column<>("Type", ViolationCandidate::type, 10, 40),
                  new Column<>("File", ViolationCandidate::fileName, 15, 60),
                  new Column<>("Position", ViolationCandidate::position, 6, 20)),
              log);

      printer.print();
    }
  }
}
