package net.disy.oss.spantable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.disy.oss.spantable.CellSpan.NonOverlappingRowComparator;

/**
 * This class uses a <code>Map<List<CellSpan>></code> to store the CellSpans.
 * The Map is indexed by column. If a CellSpan spans multiple columns it will
 * be stored in the list for each spanned column.
 * The lists for each column are sorted by the first row in the CellSpan. To find
 * a CellSpan that intersects with a given CellSpan a binary search is used.
 * There could be performance gains by using an interval tree.
 */
public class SpanTableModel  implements ISpanTableModel {
  private  final int [] cellSpanColumns;
  private final Map<Integer, List<CellSpan>> columnIndexedCellSpans = new HashMap<>();

  /**
   * Creates an instance of DelegatingSpanTableModel.
   * The CellSpans are assumed to
   * not intersect each other. This has to be checked before construction by the caller.
   * @param spans A List of not intersecting CellSpans.
   * @param cellSpanColumns An array containing all column indices, which could contain CellSpans.
   */
  public SpanTableModel(List<CellSpan> spans, int[] cellSpanColumns) {
    this.cellSpanColumns = cellSpanColumns;
    for (var span : spans) {
      var columns = span.getSpannedColumns();
      for (var column : columns) {
        if (columnIndexedCellSpans.containsKey(column)) {
          columnIndexedCellSpans.get(column).add(span);
        } else {
          List<CellSpan> list = new ArrayList<>();
          list.add(span);
          columnIndexedCellSpans.put(column, list);
        }
      }
    }
    for (var lists : columnIndexedCellSpans.values()) {
      lists.sort(Comparator.comparingInt(CellSpan::getStartRow));
    }
  }

  @Override
  public Optional<CellSpan> getCellSpanContaining(int row, int column) {
    var sortedSpansInColumn = columnIndexedCellSpans.get(column);
    if (sortedSpansInColumn == null) {
      return Optional.empty();
    }
    var key = new CellSpan(row, column, 1, 1);
    int i = Collections.binarySearch(sortedSpansInColumn, key, new NonOverlappingRowComparator()) ;
    if (i < 0) {
      return Optional.empty();
    }
    return Optional.of(sortedSpansInColumn.get(i));
  }

  @Override
  public List<CellSpan> getIntersectingCellSpans(CellSpan cellSpan) {
    var columns = cellSpan.getSpannedColumns();
    var result = new ArrayList<CellSpan>();
    for (var column : columns) {
      var spans = columnIndexedCellSpans.get(column);
      if (spans == null) {
        continue;
      }
      int i = Collections.binarySearch(spans, cellSpan, new NonOverlappingRowComparator());
      if (i < 0) {
        continue;
      }
      result.add(spans.get(i));
    }
    return result;
  }

  @Override
  public boolean containsSpans(int column) {
    for (int cellSpanColumn : cellSpanColumns) {
      if (cellSpanColumn == column) {
        return true;
      }
    }
    return false;
  }
}
