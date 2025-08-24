package com.ghiloufi.notnullchecker.report;

import java.util.function.Function;

public record Column<T>(String name, Function<T, String> extractor, int minWidth, int maxWidth) {
  public String format(T item) {
    String value = extractor.apply(item);
    if (value == null) value = "";
    if (value.length() > maxWidth) value = value.substring(0, maxWidth);
    return String.format("%-" + minWidth + "s", value);
  }
}
