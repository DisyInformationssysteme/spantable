/*
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Disy Informationssysteme GmbH. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package net.disy.oss.spantable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class SpanTableUi extends BasicTableUI {


  @Override
  public void paint(Graphics g, JComponent c) {
    Rectangle clip = g.getClipBounds();
    Rectangle bounds = table.getBounds();
    // account for the fact that the graphics has already been translated
    // into the table's bounds
    bounds.x = bounds.y = 0;

    if (table.getRowCount() <= 0 || table.getColumnCount() <= 0 ||
        // this check prevents us from painting the entire table
        // when the clip doesn't intersect our bounds at all
        !bounds.intersects(clip)) {

      paintDropLines(g);
      return;
    }

    boolean ltr = table.getComponentOrientation().isLeftToRight();
    Point upperLeft, lowerRight;
    // compute the visible part of table which needs to be painted
    Rectangle visibleBounds = clip.intersection(bounds);
    upperLeft = visibleBounds.getLocation();
    lowerRight = new Point(
        visibleBounds.x + visibleBounds.width - 1,
        visibleBounds.y + visibleBounds.height - 1);

    int rMin = table.rowAtPoint(upperLeft);
    int rMax = table.rowAtPoint(lowerRight);
    // This should never happen (as long as our bounds intersect the clip,
    // which is why we bail above if that is the case).
    if (rMin == -1) {
      rMin = 0;
    }
    // If the table does not have enough rows to fill the view we'll get -1.
    // (We could also get -1 if our bounds don't intersect the clip,
    // which is why we bail above if that is the case).
    // Replace this with the index of the last row.
    if (rMax == -1) {
      rMax = table.getRowCount() - 1;
    }

    // For FIT_WIDTH, all columns should be printed irrespective of
    // how many columns are visible. So, we used clip which is already set to
    // total col width instead of visible region
    // Since JTable.PrintMode is not accessible
    // from here, we aet "Table.printMode" in TablePrintable#print and
    // access from here.
    Object printMode = table.getClientProperty("Table.printMode");
    if ((printMode == JTable.PrintMode.FIT_WIDTH)) {
      upperLeft = clip.getLocation();
      lowerRight = new Point(
          clip.x + clip.width - 1,
          clip.y + clip.height - 1);
    }
    int cMin = table.columnAtPoint(ltr ? upperLeft : lowerRight);
    int cMax = table.columnAtPoint(ltr ? lowerRight : upperLeft);
    // This should never happen.
    if (cMin == -1) {
      cMin = 0;
    }
    // If the table does not have enough columns to fill the view we'll get -1.
    // Replace this with the index of the last column.
    if (cMax == -1) {
      cMax = table.getColumnCount() - 1;
    }

    Container comp = SwingUtilities.getUnwrappedParent(table);
    if (comp != null) {
      comp = comp.getParent();
    }

    if (comp != null && !(comp instanceof JViewport) && !(comp instanceof JScrollPane)) {
      // We did rMax-1 to paint the same number of rows that are drawn on console
      // otherwise 1 extra row is printed per page than that are displayed
      // when there is no scrollPane and we do printing of table
      // but not when rmax is already pointing to index of last row
      // and if there is any selected rows
      if (rMax != (table.getRowCount() - 1) &&
          (table.getSelectedRow() == -1)) {
        rMax = rMax - 1;
      }
    }

    // Paint the grid.
    paintGrid(g, rMin, rMax, cMin, cMax);

    // Paint the cells.
    paintCells(g, rMin, rMax, cMin, cMax);

    paintDropLines(g);
  }

  private void paintDropLines(Graphics g) {
    JTable.DropLocation loc = table.getDropLocation();
    if (loc == null) {
      return;
    }

    Color color = UIManager.getColor("Table.dropLineColor");
    Color shortColor = UIManager.getColor("Table.dropLineShortColor");
    if (color == null && shortColor == null) {
      return;
    }

    Rectangle rect;

    rect = getHDropLineRect(loc);
    if (rect != null) {
      int x = rect.x;
      int w = rect.width;
      if (color != null) {
        extendRect(rect, true);
        g.setColor(color);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
      }
      if (!loc.isInsertColumn() && shortColor != null) {
        g.setColor(shortColor);
        g.fillRect(x, rect.y, w, rect.height);
      }
    }

    rect = getVDropLineRect(loc);
    if (rect != null) {
      int y = rect.y;
      int h = rect.height;
      if (color != null) {
        extendRect(rect, false);
        g.setColor(color);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
      }
      if (!loc.isInsertRow() && shortColor != null) {
        g.setColor(shortColor);
        g.fillRect(rect.x, y, rect.width, h);
      }
    }
  }

  private Rectangle getHDropLineRect(JTable.DropLocation loc) {
    if (!loc.isInsertRow()) {
      return null;
    }

    int row = loc.getRow();
    int col = loc.getColumn();
    if (col >= table.getColumnCount()) {
      col--;
    }

    Rectangle rect = table.getCellRect(row, col, true);

    if (row >= table.getRowCount()) {
      row--;
      Rectangle prevRect = table.getCellRect(row, col, true);
      rect.y = prevRect.y + prevRect.height;
    }

    if (rect.y == 0) {
      rect.y = -1;
    } else {
      rect.y -= 2;
    }

    rect.height = 3;

    return rect;
  }

  private Rectangle getVDropLineRect(JTable.DropLocation loc) {
    if (!loc.isInsertColumn()) {
      return null;
    }

    boolean ltr = table.getComponentOrientation().isLeftToRight();
    int col = loc.getColumn();
    Rectangle rect = table.getCellRect(loc.getRow(), col, true);

    if (col >= table.getColumnCount()) {
      col--;
      rect = table.getCellRect(loc.getRow(), col, true);
      if (ltr) {
        rect.x = rect.x + rect.width;
      }
    } else if (!ltr) {
      rect.x = rect.x + rect.width;
    }

    if (rect.x == 0) {
      rect.x = -1;
    } else {
      rect.x -= 2;
    }

    rect.width = 3;

    return rect;
  }

  private Rectangle extendRect(Rectangle rect, boolean horizontal) {
    if (rect == null) {
      return rect;
    }

    if (horizontal) {
      rect.x = 0;
      rect.width = table.getWidth();
    } else {
      rect.y = 0;

      if (table.getRowCount() != 0) {
        Rectangle lastRect = table.getCellRect(table.getRowCount() - 1, 0, true);
        rect.height = lastRect.y + lastRect.height;
      } else {
        rect.height = table.getHeight();
      }
    }

    return rect;
  }

  /*
   * Paints the grid lines within <I>aRect</I>, using the grid
   * color set with <I>setGridColor</I>. Paints vertical lines
   * if <code>getShowVerticalLines()</code> returns true and paints
   * horizontal lines if <code>getShowHorizontalLines()</code>
   * returns true.
   */
  private void paintGrid(Graphics g, int rMin, int rMax, int cMin, int cMax) {
    g.setColor(table.getGridColor());

    Rectangle minCell = table.getCellRect(rMin, cMin, true);
    Rectangle maxCell = table.getCellRect(rMax, cMax, true);
    Rectangle damagedArea = minCell.union(maxCell);

    if (table.getModel() instanceof ISpanTableModel) {
      paintGridWithSpans(g, (ISpanTableModel) table.getModel(), rMin, rMax, cMin, cMax);
    } else {
      paintCompleteGrid(g, damagedArea, rMin, rMax, cMin, cMax);
    }

  }

  private void paintGridWithSpans(
      Graphics g,
      ISpanTableModel model,
      int rMin,
      int rMax,
      int cMin,
      int cMax) {
    if (!(table.getShowHorizontalLines() ||
        table.getShowVerticalLines())) {
      return;
    }

    for (int row = rMin; row <= rMax; row++) {
      for (int col = cMin; col <= cMax; col++) {
        Rectangle cell = table.getCellRect(row, col, false);
        Optional<CellSpan> cellSpan = model.getCellSpanContaining(row, col);
        if (cellSpan.isPresent()) {
          CellSpan span = cellSpan.get();
          if (row == span.getEndRow()) {
            SpanTableUi.drawHLine(g, cell.x, cell.x + cell.width, cell.y + cell.height);
          }

          if (col == span.getEndColumn()) {
            SpanTableUi.drawVLine(g, cell.x + cell.width, cell.y, cell.y + cell.height);
          }

        } else {
          if (table.getShowHorizontalLines()) {
            SpanTableUi.drawHLine(g, cell.x, cell.x + cell.width, cell.y + cell.height);
          }

          if (table.getShowVerticalLines()) {
            SpanTableUi.drawVLine(g, cell.x + cell.width, cell.y, cell.y + cell.height);
          }
        }
      }
    }
  }

  private void paintCompleteGrid(
      Graphics g,
      Rectangle damagedArea,
      int rMin,
      int rMax,
      int cMin,
      int cMax) {
    if (table.getShowHorizontalLines()) {
      int tableWidth = damagedArea.x + damagedArea.width;
      int y = damagedArea.y;
      for (int row = rMin; row <= rMax; row++) {
        y += table.getRowHeight(row);
        SpanTableUi.drawHLine(g, damagedArea.x, tableWidth - 1, y - 1);
      }
    }
    if (table.getShowVerticalLines()) {
      TableColumnModel cm = table.getColumnModel();
      int tableHeight = damagedArea.y + damagedArea.height;
      int x;
      if (table.getComponentOrientation().isLeftToRight()) {
        x = damagedArea.x;
        for (int column = cMin; column <= cMax; column++) {
          int w = cm.getColumn(column).getWidth();
          x += w;
          SpanTableUi.drawVLine(g, x - 1, 0, tableHeight - 1);
        }
      } else {
        x = damagedArea.x;
        for (int column = cMax; column >= cMin; column--) {
          int w = cm.getColumn(column).getWidth();
          x += w;
          SpanTableUi.drawVLine(g, x - 1, 0, tableHeight - 1);
        }
      }
    }
  }

  private int viewIndexForColumn(TableColumn aColumn) {
    TableColumnModel cm = table.getColumnModel();
    for (int column = 0; column < cm.getColumnCount(); column++) {
      if (cm.getColumn(column) == aColumn) {
        return column;
      }
    }
    return -1;
  }

  private void paintCells(Graphics g, int rMin, int rMax, int cMin, int cMax) {
    JTableHeader header = table.getTableHeader();
    TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();

    TableColumnModel cm = table.getColumnModel();
    int columnMargin = cm.getColumnMargin();

    Rectangle cellRect;
    TableColumn aColumn;
    int columnWidth;
    ISpanTableModel tableModel = null;
    if (table.getModel() instanceof ISpanTableModel) {
      tableModel = (ISpanTableModel) table.getModel();
    }
    if (table.getComponentOrientation().isLeftToRight()) {
      for (int row = rMin; row <= rMax; row++) {
        for (int column = cMin; column <= cMax; column++) {
          cellRect = table.getCellRect(row, column, false);
          if (tableModel != null && tableModel.containsSpans(column)) {
            paintSpanColumnCell(tableModel, g, cellRect, row, column, rMin, cMin);
          } else {
            paintCell(g, cellRect, row, column);
          }
        }
      }
    } else {
      for (int row = rMin; row <= rMax; row++) {
        cellRect = table.getCellRect(row, cMin, false);
        aColumn = cm.getColumn(cMin);
        if (aColumn != draggedColumn) {
          columnWidth = aColumn.getWidth();
          cellRect.width = columnWidth - columnMargin;
          paintCell(g, cellRect, row, cMin);
        }
        for (int column = cMin + 1; column <= cMax; column++) {
          aColumn = cm.getColumn(column);
          columnWidth = aColumn.getWidth();
          cellRect.width = columnWidth - columnMargin;
          cellRect.x -= columnWidth;
          if (aColumn != draggedColumn) {
            paintCell(g, cellRect, row, column);
          }
        }
      }
    }

    // Paint the dragged column if we are dragging.
    if (draggedColumn != null) {
      paintDraggedArea(g, rMin, rMax, draggedColumn, header.getDraggedDistance());
    }

    // Remove any renderers that may be left in the rendererPane.
    rendererPane.removeAll();
  }

  private void paintDraggedArea(
      Graphics g,
      int rMin,
      int rMax,
      TableColumn draggedColumn,
      int distance) {
    int draggedColumnIndex = viewIndexForColumn(draggedColumn);

    Rectangle minCell = table.getCellRect(rMin, draggedColumnIndex, true);
    Rectangle maxCell = table.getCellRect(rMax, draggedColumnIndex, true);

    Rectangle vacatedColumnRect = minCell.union(maxCell);

    // Paint a gray well in place of the moving column.
    g.setColor(table.getParent().getBackground());
    g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
        vacatedColumnRect.width, vacatedColumnRect.height);

    // Move to the where the cell has been dragged.
    vacatedColumnRect.x += distance;

    // Fill the background.
    g.setColor(table.getBackground());
    g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
        vacatedColumnRect.width, vacatedColumnRect.height);

    // Paint the vertical grid lines if necessary.
    if (table.getShowVerticalLines()) {
      g.setColor(table.getGridColor());
      int x1 = vacatedColumnRect.x;
      int y1 = vacatedColumnRect.y;
      int x2 = x1 + vacatedColumnRect.width - 1;
      int y2 = y1 + vacatedColumnRect.height - 1;
      // Left
      g.drawLine(x1 - 1, y1, x1 - 1, y2);
      // Right
      g.drawLine(x2, y1, x2, y2);
    }

    for (int row = rMin; row <= rMax; row++) {
      // Render the cell value
      Rectangle r = table.getCellRect(row, draggedColumnIndex, false);
      r.x += distance;
      paintCell(g, r, row, draggedColumnIndex);

      // Paint the (lower) horizontal grid line if necessary.
      if (table.getShowHorizontalLines()) {
        g.setColor(table.getGridColor());
        Rectangle rcr = table.getCellRect(row, draggedColumnIndex, true);
        rcr.x += distance;
        int x1 = rcr.x;
        int y1 = rcr.y;
        int x2 = x1 + rcr.width - 1;
        int y2 = y1 + rcr.height - 1;
        g.drawLine(x1, y2, x2, y2);
      }
    }
  }

  private void paintCell(Graphics g, Rectangle cellRect, int row, int column) {
    if (table.isEditing() && table.getEditingRow() == row &&
        table.getEditingColumn() == column) {
      Component component = table.getEditorComponent();
      component.setBounds(cellRect);
      component.validate();
    } else {
      TableCellRenderer renderer = table.getCellRenderer(row, column);
      Component component = table.prepareRenderer(renderer, row, column);
      rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y,
          cellRect.width, cellRect.height, true);
    }
  }

  private void paintSpanColumnCell(ISpanTableModel model, Graphics g, Rectangle realCellRect, int row, int column, int rowMin, int colMin) {
    Optional<CellSpan> span = model.getCellSpanContaining(row, column);
    if (span.isPresent()) {
      paintCellSpan(model, span.get(), g, realCellRect, row, column);
    } else {
      paintSingleCell(g, realCellRect, row, column);
    }
  }

  private void paintCellSpan(ISpanTableModel model, CellSpan span, Graphics g, Rectangle realCellRect, int row, int column) {
    Rectangle spanRect = SpanTableHelper.getCellRect(table, model, row, column, false);

    Container parent = SwingUtilities.getUnwrappedParent(table);
    boolean isFirstCellInSpan = span.getStartRow() == row && span.getStartColumn() == column;
    boolean isFirstInVisibleArea = false;
    if (parent instanceof JViewport) {
      Rectangle viewRect = ((JViewport) parent).getViewRect();

      int rowMin = table.rowAtPoint(viewRect.getLocation());
      int colMin = table.columnAtPoint(viewRect.getLocation());
      isFirstInVisibleArea = (span.getStartRow() < rowMin && row == rowMin) ||
          (span.getStartColumn() < colMin && column == colMin);
    }
    if (!(isFirstCellInSpan || isFirstInVisibleArea)) {
      // already painted
      return;
    }
    var renderer = table.getCellRenderer(span.getStartRow(), span.getStartColumn());

    Object value = table.getValueAt(span.getStartRow(), span.getStartColumn());
    boolean isSelected = false;
    boolean hasFocus = false;
    if (!table.isPaintingForPrint()) {

      isSelected = span.getSpannedCells().stream()
          .anyMatch(s -> table.isCellSelected(s[0], s[1]));

      var cellIsLead = span.contains(
          table.getSelectionModel().getLeadSelectionIndex(),
          table.getColumnModel().getSelectionModel().getLeadSelectionIndex()
      );

      hasFocus = cellIsLead && table.isFocusOwner();
    }

    var component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, span.getStartRow(), span.getStartColumn());
    if (!isSelected) {
      component.setBackground(Color.WHITE);
    }
    g.setColor(component.getBackground());
    g.fillRect(spanRect.x, spanRect.y, spanRect.width, spanRect.height);
    rendererPane.paintComponent(g, component, table, realCellRect.x, realCellRect.y,
        realCellRect.width, realCellRect.height, true);

  }

  private void paintSingleCell(Graphics g, Rectangle cellRect, int row, int column) {
    TableCellRenderer cellRenderer = table.getCellRenderer(row, column);

    Object value = table.getValueAt(row, column);

    boolean isSelected = false;
    boolean hasFocus = false;

    // Only indicate the selection and focused cell if not printing
    if (!table.isPaintingForPrint()) {
      isSelected = table.isCellSelected(row, column);

      boolean rowIsLead =
          (table.getSelectionModel().getLeadSelectionIndex() == row);
      boolean colIsLead =
        (table.getColumnModel().getSelectionModel().getLeadSelectionIndex() == column);

      hasFocus = (rowIsLead && colIsLead) && table.isFocusOwner();
    }

    Component component = cellRenderer.getTableCellRendererComponent(table, value,
        isSelected, hasFocus,
        row, column);
    // all cells in a column which could contain spans should be bg=white
    if (!isSelected) {
      component.setBackground(Color.WHITE);
    }
    rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true);
  }

  public static int getAdjustedLead(JTable table,
                                     boolean row,
                                     ListSelectionModel model) {

    int index = model.getLeadSelectionIndex();
    int compare = row ? table.getRowCount() : table.getColumnCount();
    return index < compare ? index : -1;
  }


  public static int getAdjustedLead(JTable table, boolean row) {
    return row ? getAdjustedLead(table, row, table.getSelectionModel())
            : getAdjustedLead(table, row, table.getColumnModel().getSelectionModel());
  }

  /**
   * Register all keyboard actions on the JTable.
   */
  @Override
  protected void installKeyboardActions() {
    super.installKeyboardActions();
    LazyActionMap lazyActionMap = new LazyActionMap(SpanTableUi::loadActionMap);
    ActionMap map = (ActionMap)UIManager.get("Table.actionMap");
    if (map == null) {
      throw new IllegalStateException("BasicTableUI should have installed its ActionMap.");
    }
    var originalKeys = Arrays.stream(map.keys()).map(Object::toString).collect(Collectors.toSet());
    for (var key : SpanTableActions.keys) {
      if (!originalKeys.contains(key)) {
        throw new IllegalStateException("Key: " + key + " is not in the ActionMap");
      }
    }
    lazyActionMap.setParent(map);

    SwingUtilities.replaceUIActionMap(table, lazyActionMap);

    InputMap inputMap = SwingUtilities.getUIInputMap(table, JComponent.
            WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    SwingUtilities.replaceUIInputMap(table,
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
            inputMap);
  }

  static void loadActionMap(LazyActionMap map) {
    // IMPORTANT: There is a very close coupling between the parameters
    // passed to the Actions constructor. Only certain parameter
    // combinations are supported. For example, the following Action would
    // not work as expected:
    //     new Actions(Actions.NEXT_ROW_CELL, 1, 4, false, true)
    // Actions which move within the selection only (having a true
    // inSelection parameter) require that one of dx or dy be
    // zero and the other be -1 or 1. The point of this warning is
    // that you should be very careful about making sure a particular
    // combination of parameters is supported before changing or
    // adding anything here.

    map.put(new SpanTableActions(SpanTableActions.NEXT_COLUMN, 1, 0,
            false, false));
    map.put(new SpanTableActions(SpanTableActions.NEXT_COLUMN_CHANGE_LEAD, 1, 0,
            false, false));
    map.put(new SpanTableActions(SpanTableActions.PREVIOUS_COLUMN, -1, 0,
            false, false));
    map.put(new SpanTableActions(SpanTableActions.PREVIOUS_COLUMN_CHANGE_LEAD, -1, 0,
            false, false));
    map.put(new SpanTableActions(SpanTableActions.NEXT_ROW, 0, 1,
            false, false));
    map.put(new SpanTableActions(SpanTableActions.NEXT_ROW_CHANGE_LEAD, 0, 1,
            false, false));
    map.put(new SpanTableActions(SpanTableActions.PREVIOUS_ROW, 0, -1,
            false, false));
    map.put(new SpanTableActions(SpanTableActions.PREVIOUS_ROW_CHANGE_LEAD, 0, -1,
            false, false));
    map.put(new SpanTableActions(SpanTableActions.NEXT_COLUMN_EXTEND_SELECTION,
            1, 0, true, false));
    map.put(new SpanTableActions(SpanTableActions.PREVIOUS_COLUMN_EXTEND_SELECTION,
            -1, 0, true, false));
    map.put(new SpanTableActions(SpanTableActions.NEXT_ROW_EXTEND_SELECTION,
            0, 1, true, false));
    map.put(new SpanTableActions(SpanTableActions.PREVIOUS_ROW_EXTEND_SELECTION,
            0, -1, true, false));
    map.put(new SpanTableActions(SpanTableActions.SCROLL_UP_CHANGE_SELECTION,
            false, false, true, false));
    map.put(new SpanTableActions(SpanTableActions.SCROLL_DOWN_CHANGE_SELECTION,
            false, true, true, false));
    map.put(new SpanTableActions(SpanTableActions.FIRST_COLUMN,
            false, false, false, true));
    map.put(new SpanTableActions(SpanTableActions.LAST_COLUMN,
            false, true, false, true));

    map.put(new SpanTableActions(SpanTableActions.SCROLL_UP_EXTEND_SELECTION,
            true, false, true, false));
    map.put(new SpanTableActions(SpanTableActions.SCROLL_DOWN_EXTEND_SELECTION,
            true, true, true, false));
    map.put(new SpanTableActions(SpanTableActions.FIRST_COLUMN_EXTEND_SELECTION,
            true, false, false, true));
    map.put(new SpanTableActions(SpanTableActions.LAST_COLUMN_EXTEND_SELECTION,
            true, true, false, true));

    map.put(new SpanTableActions(SpanTableActions.FIRST_ROW, false, false, true, true));
    map.put(new SpanTableActions(SpanTableActions.LAST_ROW, false, true, true, true));

    map.put(new SpanTableActions(SpanTableActions.FIRST_ROW_EXTEND_SELECTION,
            true, false, true, true));
    map.put(new SpanTableActions(SpanTableActions.LAST_ROW_EXTEND_SELECTION,
            true, true, true, true));

    map.put(new SpanTableActions(SpanTableActions.NEXT_COLUMN_CELL,
            1, 0, false, true));
    map.put(new SpanTableActions(SpanTableActions.PREVIOUS_COLUMN_CELL,
            -1, 0, false, true));
    map.put(new SpanTableActions(SpanTableActions.NEXT_ROW_CELL, 0, 1, false, true));
    map.put(new SpanTableActions(SpanTableActions.PREVIOUS_ROW_CELL,
            0, -1, false, true));

    map.put(new SpanTableActions(SpanTableActions.SCROLL_LEFT_CHANGE_SELECTION,
            false, false, false, false));
    map.put(new SpanTableActions(SpanTableActions.SCROLL_RIGHT_CHANGE_SELECTION,
            false, true, false, false));
    map.put(new SpanTableActions(SpanTableActions.SCROLL_LEFT_EXTEND_SELECTION,
            true, false, false, false));
    map.put(new SpanTableActions(SpanTableActions.SCROLL_RIGHT_EXTEND_SELECTION,
            true, true, false, false));

    map.put(new SpanTableActions(SpanTableActions.ADD_TO_SELECTION));
    map.put(new SpanTableActions(SpanTableActions.TOGGLE_AND_ANCHOR));
    map.put(new SpanTableActions(SpanTableActions.EXTEND_TO));
    map.put(new SpanTableActions(SpanTableActions.MOVE_SELECTION_TO));
    map.put(new SpanTableActions(SpanTableActions.FOCUS_HEADER));
  }

  // copied from SwingUtilities2
  /**
   * This method should be used for drawing a borders over a filled rectangle.
   * Draws horizontal line, using the current color, between the points {@code
   * (x1, y)} and {@code (x2, y)} in graphics context's coordinate system.
   * Note: it use {@code Graphics.fillRect()} internally.
   *
   * @param g  Graphics to draw the line to.
   * @param x1 the first point's <i>x</i> coordinate.
   * @param x2 the second point's <i>x</i> coordinate.
   * @param y  the <i>y</i> coordinate.
   */
  public static void drawHLine(Graphics g, int x1, int x2, int y) {
    if (x2 < x1) {
      final int temp = x2;
      x2 = x1;
      x1 = temp;
    }
    g.fillRect(x1, y, x2 - x1 + 1, 1);
  }

  // copied from SwingUtilities2
  /**
   * This method should be used for drawing a borders over a filled rectangle.
   * Draws vertical line, using the current color, between the points {@code
   * (x, y1)} and {@code (x, y2)} in graphics context's coordinate system.
   * Note: it use {@code Graphics.fillRect()} internally.
   *
   * @param g  Graphics to draw the line to.
   * @param x  the <i>x</i> coordinate.
   * @param y1 the first point's <i>y</i> coordinate.
   * @param y2 the second point's <i>y</i> coordinate.
   */
  public static void drawVLine(Graphics g, int x, int y1, int y2) {
    if (y2 < y1) {
      final int temp = y2;
      y2 = y1;
      y1 = temp;
    }
    g.fillRect(x, y1, 1, y2 - y1 + 1);
  }
}
