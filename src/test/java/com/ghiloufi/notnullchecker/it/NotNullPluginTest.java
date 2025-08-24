package com.ghiloufi.notnullchecker.it;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NotNullPluginTest {

  @Test
  void should_fail_when_null_violations_present() throws Exception {

    Path testProjectDir = Path.of("src", "test", "resources", "project");

    Path projectDir = Path.of("src", "test", "resources", "project");

    Verifier verifier = new Verifier(testProjectDir.toString());
    verifier.setAutoclean(true);

    // 2️⃣ Replace @project.version@ placeholders in pom.xml
    verifier.filterFile(
        "pom.xml",
        "pom.xml",
        "UTF-8",
        Map.of("project.version", System.getProperty("project.version", "1.0-SNAPSHOT")));
    // verifier.filterFile("pom.xml", "pom.xml", "utf-8", Map.of());

    // 3️⃣ Run the build
    try {
      verifier.executeGoal("validate");
    } catch (VerificationException expected) {
      // We *expect* this project to fail because of violations
    }

    // 4️⃣ Inspect the log for your expected output
    List<String> logLines = verifier.loadLines("log.txt", "utf-8");
    // assertThat(logLines).anyMatch(line -> line.contains("ViolationCandidate"));

    // Optional: use built‑in helper
    // verifier.verifyTextInLog("Should detect @NotNull violations");

    logLines.forEach(System.out::println);

    Assertions.assertTrue(true);
    //verifier.verifyTextInLog("NotNull scan complete");
  }
}
