package com.ghiloufi.notnullchecker.samples;

import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SampleClass<T> {
  private String name;
  private String age = null;

  private List<String> list;

  public SampleClass(String name) {}

  public void greet(String message) {
    Consumer<String> fn = (v) -> System.out.println(v);

    Supplier<String> supplier = () -> null;

    Supplier<Integer> integerSupplier =
        () -> {
          System.out.println("supplier");
          return null;
        };

    System.out.println(message);
  }

  public String findById(@NotNull String id) {
    return "Hello";
  }

  public String find(@NotNull String id) {
    return null;
  }

  public Optional<String> value() {
    return Optional.empty();
  }

  void test() {
    String s = null;
    s = null;
    find(null);
    var sample = new SampleClass<String>(null);
  }

  void testCollections() {
    List<String> list = Arrays.asList("a", null, "b");
  }

  void testArray() {
    String[] arr = new String[2];
    arr[0] = null;
  }
}

record  Person(String name) {}
