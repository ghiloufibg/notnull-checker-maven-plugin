package com.ghiloufi.notnullchecker.plugin;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(PluginContainerExtension.class)
public @interface PluginTestConfig {}
