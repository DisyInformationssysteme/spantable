package net.disy.oss.spantable;

import java.util.List;
import java.util.Optional;

public interface ISpanTableModel {
  /**
   * Returns a CellSpan which contains the cell specified by row and column if there is one.
   * CellSpans are not allowed to intersect each other. So this method returns either one or none.
   * This method is called during painting of the component and should be as fast as possible.
   * @param row An index specifying the row.
   * @param column An index specifying the column.
   * @return The CellSpan containing row and column or Optional.empty().
   */
  Optional<CellSpan> getCellSpanContaining(int row, int column);

  /**
   * Finds all CellSpans that intersect with the given CellSpan.
   * This method is called during painting of the component and should be as fast as possible.
   * @param cellSpan The CellSpan for which intersecting CellSpans are searched.
   * @return All intersecting CellSpans or an empty list.
   */
  List<CellSpan> getIntersectingCellSpans(CellSpan cellSpan);

  /**
   * Returns true if the given column could contain CellSpans.
   * @param column An index specifying the column.
   * @return True if the column could contain CellSpans.
   */
  boolean containsSpans(int column);
}
