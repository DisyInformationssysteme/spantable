package net.disy.oss.spantable;

import java.util.List;
import java.util.Optional;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class DelegatingSpanTableModel implements ISpanTableModel, TableModel {
  private final TableModel tableModelDelegate;
  private final ISpanTableModel spanModelDelegate;

  public DelegatingSpanTableModel(TableModel tableModelDelegate, ISpanTableModel spanModelDelegate) {
    this.tableModelDelegate = tableModelDelegate;
    this.spanModelDelegate = spanModelDelegate;
  }

  @Override
  public Optional<CellSpan> getCellSpanContaining(int row, int column) {
    return spanModelDelegate.getCellSpanContaining(row, column);
  }

  @Override
  public List<CellSpan> getIntersectingCellSpans(CellSpan cellSpan) {
    return spanModelDelegate.getIntersectingCellSpans(cellSpan);
  }

  @Override
  public boolean containsSpans(int column) {
    return spanModelDelegate.containsSpans(column);
  }

  @Override
  public int getRowCount() {
    return tableModelDelegate.getRowCount();
  }

  @Override
  public int getColumnCount() {
    return tableModelDelegate.getColumnCount();
  }

  @Override
  public String getColumnName(int columnIndex) {
    return tableModelDelegate.getColumnName(columnIndex);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return tableModelDelegate.getColumnClass(columnIndex);
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return tableModelDelegate.isCellEditable(rowIndex, columnIndex);
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return tableModelDelegate.getValueAt(rowIndex, columnIndex);
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    tableModelDelegate.setValueAt(aValue, rowIndex, columnIndex);
  }

  @Override
  public void addTableModelListener(TableModelListener l) {
    tableModelDelegate.addTableModelListener(l);
  }

  @Override
  public void removeTableModelListener(TableModelListener l) {
    tableModelDelegate.removeTableModelListener(l);
  }
}
