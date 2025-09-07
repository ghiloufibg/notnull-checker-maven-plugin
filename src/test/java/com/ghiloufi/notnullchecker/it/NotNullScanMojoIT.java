package com.ghiloufi.notnullchecker.it;

import com.ghiloufi.notnullchecker.plugin.NotNullScanMojo;
import com.ghiloufi.notnullchecker.plugin.PluginSamplesProvider;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotNullScanMojoIT {

  private NotNullScanMojo mojo;

  @TempDir private Path tempDir;

  @BeforeEach
  void setUp() throws IOException {
    Path srcMainJava = tempDir.resolve(PluginSamplesProvider.getSrcDir());
    Files.createDirectories(srcMainJava);

    MavenProject project = new MavenProject();
    Reflect.on(project).set("compileSourceRoots", List.of(srcMainJava.toString()));

    mojo = new NotNullScanMojo();

    Reflect.on(mojo).set("project", project);
    Reflect.on(mojo).set("failOnViolation", true);
  }

  @Test
  void should_allow_build_while_there_is_no_violation() throws IOException {
    String code =
        """
                package com.ghiloufi.notnullchecker.samples;
                import jakarta.validation.constraints.NotNull;
                public class Foo {
                   private @NotNull String name;
                }
                """;
    Path javaFile = tempDir.resolve(PluginSamplesProvider.resolve("Foo.java"));
    Files.writeString(javaFile, code);

    assertDoesNotThrow(() -> mojo.execute());
  }

  @Test
  void should_fail_build_when_there_is_violation() throws IOException {
    String code =
        """
                package com.ghiloufi.notnullchecker.samples;
                public class Bar {
                   private String missingNotNull;
                }
                """;
    Path javaFile = tempDir.resolve(PluginSamplesProvider.resolve("Bar.java"));
    Files.writeString(javaFile, code);

    assertThrows(MojoFailureException.class, () -> mojo.execute());
  }

  @Test
  void shoul_allow_when_there_is_violation_and_violation_is_allowed() throws Exception {
    Reflect.on(mojo).set("failOnViolation", false);

    String code =
        """
                package com.ghiloufi.notnullchecker.samples;
                public class Baz {
                   private String missingNotNull;
                }
                """;
    Path javaFile = tempDir.resolve(PluginSamplesProvider.resolve("Baz.java"));
    Files.writeString(javaFile, code);

    assertDoesNotThrow(() -> mojo.execute());
  }
}
