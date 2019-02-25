package net.disy.oss.spantable;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JTable;

public interface SpanTableHelper {

  /**
   * Calculates the CellRect for a given row and column taking CellSpans into account.
   * If row and column are contained in a CellSpan the returned Rectangle represents the CellSpan.
   * Otherwise we delegate to the getCellRect implementation of the table.
   * @param table A JTable for which we are calculating the Rectangle.
   * @param model An ISpanTableModel to get the CellSpan containing row and column.
   * @param row The row index.
   * @param column The column index.
   * @param includeSpacing Not sure what this does.
   * @return A Rectangle representing the CellSpan.
   */
  static Rectangle getCellRect(JTable table, ISpanTableModel model, int row, int column, boolean includeSpacing) {
      var cellSpan = model.getCellSpanContaining(row, column);
      if (cellSpan.isPresent()) {
        var span = cellSpan.get();
        var upperLeft = table.getCellRect(span.getStartRow(), span.getStartColumn(), includeSpacing);
        var lowerRight = table.getCellRect(span.getEndRow(), span.getEndColumn(), includeSpacing);
        return upperLeft.union(lowerRight);
      }
    return table.getCellRect(row, column, includeSpacing);
  }

  /**
   * Given a dirtyRect r the dirtyRect is extended to contain all CellSpans.
   * If part of a CellSpan is contained in the dirtyRect we have to extend it so that it contains
   * the whole CellSpan. The passed Rectangle is modified in place.
   * @param table The table for which we want to adjust the dirtyRect.
   * @param model An ISpantTableModel to calculate CellSpans.
   * @param r A dirtyRect as calculated by Swing.
   */
  static void adjustDirtyRect(JTable table, ISpanTableModel model, Rectangle r) {
    var upperRow = table.rowAtPoint(new Point(r.x, r.y));
    var lowerRow = table.rowAtPoint(new Point(r.x, r.y + r.height));
    var leftColumn = table.columnAtPoint(new Point(r.x, r.y));
    var rightColumn = table.columnAtPoint(new Point(r.x + r.width, r.y));
    List<CellSpan> intersectingCellSpans = model.getIntersectingCellSpans(CellSpan.fromStartEnd(
        upperRow,
        leftColumn,
        lowerRow,
        rightColumn));
    for (var span : intersectingCellSpans) {
      upperRow = Math.min(span.getStartRow(), upperRow);
      lowerRow = Math.max(span.getEndRow(), lowerRow);
    }
    var upperLeft = table.getCellRect(upperRow, leftColumn, false);
    var lowerRight = table.getCellRect(lowerRow, rightColumn, false);
    r.add(upperLeft);
    r.add(lowerRight);
  }
}
