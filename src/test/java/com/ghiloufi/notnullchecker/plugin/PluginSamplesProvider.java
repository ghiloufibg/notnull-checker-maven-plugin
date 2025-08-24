package com.ghiloufi.notnullchecker.plugin;

import jakarta.validation.constraints.NotBlank;

import java.nio.file.Paths;

public class PluginSamplesProvider {

  private static final String srcDir = "src/test/java/com/ghiloufi/notnullchecker/samples";

  public static String resolve(@NotBlank final String filename) {
    return Paths.get(srcDir, filename).toString();
  }

  public static String getSrcDir() {
    return srcDir;
  }
}
