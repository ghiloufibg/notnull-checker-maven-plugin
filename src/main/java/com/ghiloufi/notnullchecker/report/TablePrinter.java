package com.ghiloufi.notnullchecker.report;

import jakarta.validation.constraints.NotNull;
import org.apache.maven.plugin.logging.Log;

import java.util.*;

final class TablePrinter<T> {

  private final List<T> rows;
  private final List<Column<T>> columns;
  private final Log log;

  public TablePrinter(@NotNull List<T> rows, @NotNull List<Column<T>> columns, @NotNull Log log) {
    this.rows = rows;
    this.columns = columns;
    this.log = log;
  }

  public void print() {
    int[] colWidths = new int[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      Column<T> col = columns.get(i);
      int width = Math.max(col.name().length(), col.minWidth());
      for (T row : rows) {
        String cell = col.format(row).trim();
        width = Math.max(width, cell.length());
      }
      colWidths[i] = Math.min(width, col.maxWidth());
    }

    log.info(buildBorder(colWidths));

    log.info(buildRow(colWidths, columns.stream().map(Column::name).toList()));
    log.info(buildBorder(colWidths));

    for (T row : rows) {
      List<String> cells = new ArrayList<>();
      for (int i = 0; i < columns.size(); i++) {
        cells.add(columns.get(i).format(row).trim());
      }
      log.info(buildRow(colWidths, cells));
    }

    log.info(buildBorder(colWidths));
  }

  private String buildBorder(int[] colWidths) {
    StringBuilder sb = new StringBuilder("+");
    for (int w : colWidths) {
      sb.append("-".repeat(w + 2)).append("+");
    }
    return sb.toString();
  }

  private String buildRow(int[] colWidths, List<String> cells) {
    StringBuilder sb = new StringBuilder("|");
    for (int i = 0; i < cells.size(); i++) {
      sb.append(" ").append(String.format("%-" + colWidths[i] + "s", cells.get(i))).append(" |");
    }
    return sb.toString();
  }
}
