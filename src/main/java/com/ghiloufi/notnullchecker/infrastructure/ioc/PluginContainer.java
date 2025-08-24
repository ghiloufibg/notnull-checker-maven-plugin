package com.ghiloufi.notnullchecker.infrastructure.ioc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import org.apache.maven.plugin.logging.Log;

public class PluginContainer {

  private static Injector INJECTOR;

  private PluginContainer() {}

  public static <T> T lookup(final Class<T> clazz) {
    Objects.requireNonNull(INJECTOR, "The injector should be initialized before utilization.");
    return INJECTOR.getInstance(clazz);
  }

  public static synchronized void setUpContext(
      final @NotNull Log log, final @NotNull List<String> roots) {

    if (roots.isEmpty()) {
      throw new IllegalArgumentException("At least one source folder is required !");
    }

    if (INJECTOR != null) {
      log.warn("An injector is already created !");
      return;
    }
    INJECTOR = Guice.createInjector(new PluginModule(log, roots));
  }
}
