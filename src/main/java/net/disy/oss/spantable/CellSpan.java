package net.disy.oss.spantable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CellSpan {
  private final int row;
  private final int column;
  private final int rowSpan;
  private final int columnSpan;
  private List<int[]> spans;

  public CellSpan(int row, int column, int rowSpan, int columnSpan) {
    this.row = row;
    this.column = column;
    this.rowSpan = rowSpan;
    this.columnSpan = columnSpan;
  }

  public int getStartRow() {
    return row;
  }

  public int getStartColumn() {
    return column;
  }

  public int getEndRow() {
    return row + rowSpan - 1;
  }

  public int getEndColumn() {
    return column + columnSpan - 1;
  }

  public int getRowSpan() {
    return rowSpan;
  }

  public int getColumnSpan() {
    return columnSpan;
  }

  public boolean contains(int row, int column) {
    return row >= getStartRow() &&
        row < getStartRow() + getRowSpan() &&
        column >= getStartColumn() &&
        column < getStartColumn() + getColumnSpan();
  }

  public List<int[]> getSpannedCells() {
    if (spans != null) {
      return spans;
    }
    spans = new ArrayList<>();
    for (int r = getStartRow(); r <= getEndRow(); r++) {
      for (int c = getStartColumn(); c <= getEndColumn(); c++) {
        spans.add(new int[]{r, c});
      }
    }
    return spans;
  }

  public int[] getSpannedColumns() {
    var result = new int[getColumnSpan()];
    var col = getStartColumn();
    for (int i = 0; i < getColumnSpan(); i++) {
      result[i] = col;
      col++;
    }
    return result;
  }

  public boolean intersects(CellSpan other) {
    return getStartRow() <= other.getEndRow() &&
            getEndRow() >= other.getStartRow() &&
            getStartColumn() <= other.getEndColumn() &&
            getEndColumn() >= other.getStartColumn();

  }

  @Override
  public String toString() {
    return "CellSpan{" +
            "row=" + row +
            ", column=" + column +
            ", rowSpan=" + rowSpan +
            ", columnSpan=" + columnSpan +
            '}';
  }

  public static CellSpan fromStartEnd(int startRow, int startColumn, int endRow, int endColumn) {
    return new CellSpan(
        startRow,
        startColumn,
        endRow - startRow + 1,
        endColumn - startColumn + 1);
  }

  /**
   * Compares two CellSpans.
   * Returns 0 if the CellSpans intersect, otherwise the startRows are compared.
   */
  public static class NonOverlappingRowComparator implements Comparator<CellSpan> {
    @Override
    public int compare(CellSpan o1, CellSpan o2) {
      if (o1.intersects(o2)) {
        return 0;
      }
      if (o1.getStartRow() < o2.getStartRow()) {
        return -1;
      }
      return 1;
    }
  }
}
