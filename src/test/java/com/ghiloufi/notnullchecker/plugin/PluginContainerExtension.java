package com.ghiloufi.notnullchecker.plugin;

import com.ghiloufi.notnullchecker.infrastructure.ioc.PluginContainer;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.extension.*;

public class PluginContainerExtension implements BeforeAllCallback, TestInstancePostProcessor {

  @Override
  public void beforeAll(ExtensionContext context) {
    PluginContainer.setUpContext(new SystemStreamLog(), List.of(PluginSamplesProvider.getSrcDir()));
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context)
      throws Exception {
    for (Field field : testInstance.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(SourceRoot.class) && field.getType().equals(Path.class)) {
        field.setAccessible(true);
        field.set(testInstance, Path.of(PluginSamplesProvider.getSrcDir()));
      }
    }
  }
}
