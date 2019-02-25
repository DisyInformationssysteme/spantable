/*
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
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

// copied from OpenJDKs BasicTableUI.Actions
package javax.swing.pla.basic;

private static class Actions extends UIAction {
  private static final String CANCEL_EDITING = "cancel";
  private static final String SELECT_ALL = "selectAll";
  private static final String CLEAR_SELECTION = "clearSelection";
  private static final String START_EDITING = "startEditing";

  private static final String NEXT_ROW = "selectNextRow";
  private static final String NEXT_ROW_CELL = "selectNextRowCell";
  private static final String NEXT_ROW_EXTEND_SELECTION =
      "selectNextRowExtendSelection";
  private static final String NEXT_ROW_CHANGE_LEAD =
      "selectNextRowChangeLead";
  private static final String PREVIOUS_ROW = "selectPreviousRow";
  private static final String PREVIOUS_ROW_CELL = "selectPreviousRowCell";
  private static final String PREVIOUS_ROW_EXTEND_SELECTION =
      "selectPreviousRowExtendSelection";
  private static final String PREVIOUS_ROW_CHANGE_LEAD =
      "selectPreviousRowChangeLead";

  private static final String NEXT_COLUMN = "selectNextColumn";
  private static final String NEXT_COLUMN_CELL = "selectNextColumnCell";
  private static final String NEXT_COLUMN_EXTEND_SELECTION =
      "selectNextColumnExtendSelection";
  private static final String NEXT_COLUMN_CHANGE_LEAD =
      "selectNextColumnChangeLead";
  private static final String PREVIOUS_COLUMN = "selectPreviousColumn";
  private static final String PREVIOUS_COLUMN_CELL =
      "selectPreviousColumnCell";
  private static final String PREVIOUS_COLUMN_EXTEND_SELECTION =
      "selectPreviousColumnExtendSelection";
  private static final String PREVIOUS_COLUMN_CHANGE_LEAD =
      "selectPreviousColumnChangeLead";

  private static final String SCROLL_LEFT_CHANGE_SELECTION =
      "scrollLeftChangeSelection";
  private static final String SCROLL_LEFT_EXTEND_SELECTION =
      "scrollLeftExtendSelection";
  private static final String SCROLL_RIGHT_CHANGE_SELECTION =
      "scrollRightChangeSelection";
  private static final String SCROLL_RIGHT_EXTEND_SELECTION =
      "scrollRightExtendSelection";

  private static final String SCROLL_UP_CHANGE_SELECTION =
      "scrollUpChangeSelection";
  private static final String SCROLL_UP_EXTEND_SELECTION =
      "scrollUpExtendSelection";
  private static final String SCROLL_DOWN_CHANGE_SELECTION =
      "scrollDownChangeSelection";
  private static final String SCROLL_DOWN_EXTEND_SELECTION =
      "scrollDownExtendSelection";

  private static final String FIRST_COLUMN =
      "selectFirstColumn";
  private static final String FIRST_COLUMN_EXTEND_SELECTION =
      "selectFirstColumnExtendSelection";
  private static final String LAST_COLUMN =
      "selectLastColumn";
  private static final String LAST_COLUMN_EXTEND_SELECTION =
      "selectLastColumnExtendSelection";

  private static final String FIRST_ROW =
      "selectFirstRow";
  private static final String FIRST_ROW_EXTEND_SELECTION =
      "selectFirstRowExtendSelection";
  private static final String LAST_ROW =
      "selectLastRow";
  private static final String LAST_ROW_EXTEND_SELECTION =
      "selectLastRowExtendSelection";

  // add the lead item to the selection without changing lead or anchor
  private static final String ADD_TO_SELECTION = "addToSelection";

  // toggle the selected state of the lead item and move the anchor to it
  private static final String TOGGLE_AND_ANCHOR = "toggleAndAnchor";

  // extend the selection to the lead item
  private static final String EXTEND_TO = "extendTo";

  // move the anchor to the lead and ensure only that item is selected
  private static final String MOVE_SELECTION_TO = "moveSelectionTo";

  // give focus to the JTableHeader, if one exists
  private static final String FOCUS_HEADER = "focusHeader";

  protected int dx;
  protected int dy;
  protected boolean extend;
  protected boolean inSelection;

  // horizontally, forwards always means right,
  // regardless of component orientation
  protected boolean forwards;
  protected boolean vertically;
  protected boolean toLimit;

  protected int leadRow;
  protected int leadColumn;

  Actions(String name) {
    super(name);
  }

  Actions(
      String name, int dx, int dy, boolean extend,
      boolean inSelection) {
    super(name);

    // Actions spcifying true for "inSelection" are
    // fairly sensitive to bad parameter values. They require
    // that one of dx and dy be 0 and the other be -1 or 1.
    // Bogus parameter values could cause an infinite loop.
    // To prevent any problems we massage the params here
    // and complain if we get something we can't deal with.
    if (inSelection) {
      this.inSelection = true;

      // look at the sign of dx and dy only
      dx = sign(dx);
      dy = sign(dy);

      // make sure one is zero, but not both
      assert (dx == 0 || dy == 0) && !(dx == 0 && dy == 0);
    }

    this.dx = dx;
    this.dy = dy;
    this.extend = extend;
  }

  Actions(
      String name, boolean extend, boolean forwards,
      boolean vertically, boolean toLimit) {
    this(name, 0, 0, extend, false);
    this.forwards = forwards;
    this.vertically = vertically;
    this.toLimit = toLimit;
  }

  private static int clipToRange(int i, int a, int b) {
    return Math.min(Math.max(i, a), b - 1);
  }

  private void moveWithinTableRange(JTable table, int dx, int dy) {
    leadRow = clipToRange(leadRow + dy, 0, table.getRowCount());
    leadColumn = clipToRange(leadColumn + dx, 0, table.getColumnCount());
  }

  private static int sign(int num) {
    return (num < 0) ? -1 : ((num == 0) ? 0 : 1);
  }

  /**
   * Called to move within the selected range of the given JTable.
   * This method uses the table's notion of selection, which is
   * important to allow the user to navigate between items visually
   * selected on screen. This notion may or may not be the same as
   * what could be determined by directly querying the selection models.
   * It depends on certain table properties (such as whether or not
   * row or column selection is allowed). When performing modifications,
   * it is recommended that caution be taken in order to preserve
   * the intent of this method, especially when deciding whether to
   * query the selection models or interact with JTable directly.
   */
  private boolean moveWithinSelectedRange(
      JTable table, int dx, int dy,
      ListSelectionModel rsm, ListSelectionModel csm) {

    // Note: The Actions constructor ensures that only one of
    // dx and dy is 0, and the other is either -1 or 1

    // find out how many items the table is showing as selected
    // and the range of items to navigate through
    int totalCount;
    int minX, maxX, minY, maxY;

    boolean rs = table.getRowSelectionAllowed();
    boolean cs = table.getColumnSelectionAllowed();

    // both column and row selection
    if (rs && cs) {
      totalCount = table.getSelectedRowCount() * table.getSelectedColumnCount();
      minX = csm.getMinSelectionIndex();
      maxX = csm.getMaxSelectionIndex();
      minY = rsm.getMinSelectionIndex();
      maxY = rsm.getMaxSelectionIndex();
      // row selection only
    } else if (rs) {
      totalCount = table.getSelectedRowCount();
      minX = 0;
      maxX = table.getColumnCount() - 1;
      minY = rsm.getMinSelectionIndex();
      maxY = rsm.getMaxSelectionIndex();
      // column selection only
    } else if (cs) {
      totalCount = table.getSelectedColumnCount();
      minX = csm.getMinSelectionIndex();
      maxX = csm.getMaxSelectionIndex();
      minY = 0;
      maxY = table.getRowCount() - 1;
      // no selection allowed
    } else {
      totalCount = 0;
      // A bogus assignment to stop javac from complaining
      // about unitialized values. In this case, these
      // won't even be used.
      minX = maxX = minY = maxY = 0;
    }

    // For some cases, there is no point in trying to stay within the
    // selected area. Instead, move outside the selection, wrapping at
    // the table boundaries. The cases are:
    boolean stayInSelection;

    // - nothing selected
    if (totalCount == 0 ||
        // - one item selected, and the lead is already selected
        (totalCount == 1 && table.isCellSelected(leadRow, leadColumn))) {

      stayInSelection = false;

      maxX = table.getColumnCount() - 1;
      maxY = table.getRowCount() - 1;

      // the mins are calculated like this in case the max is -1
      minX = Math.min(0, maxX);
      minY = Math.min(0, maxY);
    } else {
      stayInSelection = true;
    }

    // the algorithm below isn't prepared to deal with -1 lead/anchor
    // so massage appropriately here first
    if (dy == 1 && leadColumn == -1) {
      leadColumn = minX;
      leadRow = -1;
    } else if (dx == 1 && leadRow == -1) {
      leadRow = minY;
      leadColumn = -1;
    } else if (dy == -1 && leadColumn == -1) {
      leadColumn = maxX;
      leadRow = maxY + 1;
    } else if (dx == -1 && leadRow == -1) {
      leadRow = maxY;
      leadColumn = maxX + 1;
    }

    // In cases where the lead is not within the search range,
    // we need to bring it within one cell for the search
    // to work properly. Check these here.
    leadRow = Math.min(Math.max(leadRow, minY - 1), maxY + 1);
    leadColumn = Math.min(Math.max(leadColumn, minX - 1), maxX + 1);

    // find the next position, possibly looping until it is selected
    do {
      calcNextPos(dx, minX, maxX, dy, minY, maxY);
    } while (stayInSelection && !table.isCellSelected(leadRow, leadColumn));

    return stayInSelection;
  }

  /**
   * Find the next lead row and column based on the given
   * dx/dy and max/min values.
   */
  private void calcNextPos(
      int dx, int minX, int maxX,
      int dy, int minY, int maxY) {

    if (dx != 0) {
      leadColumn += dx;
      if (leadColumn > maxX) {
        leadColumn = minX;
        leadRow++;
        if (leadRow > maxY) {
          leadRow = minY;
        }
      } else if (leadColumn < minX) {
        leadColumn = maxX;
        leadRow--;
        if (leadRow < minY) {
          leadRow = maxY;
        }
      }
    } else {
      leadRow += dy;
      if (leadRow > maxY) {
        leadRow = minY;
        leadColumn++;
        if (leadColumn > maxX) {
          leadColumn = minX;
        }
      } else if (leadRow < minY) {
        leadRow = maxY;
        leadColumn--;
        if (leadColumn < minX) {
          leadColumn = maxX;
        }
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    String key = getName();
    JTable table = (JTable) e.getSource();

    ListSelectionModel rsm = table.getSelectionModel();
    leadRow = getAdjustedLead(table, true, rsm);

    ListSelectionModel csm = table.getColumnModel().getSelectionModel();
    leadColumn = getAdjustedLead(table, false, csm);

    if (key == SCROLL_LEFT_CHANGE_SELECTION ||        // Paging Actions
        key == SCROLL_LEFT_EXTEND_SELECTION ||
        key == SCROLL_RIGHT_CHANGE_SELECTION ||
        key == SCROLL_RIGHT_EXTEND_SELECTION ||
        key == SCROLL_UP_CHANGE_SELECTION ||
        key == SCROLL_UP_EXTEND_SELECTION ||
        key == SCROLL_DOWN_CHANGE_SELECTION ||
        key == SCROLL_DOWN_EXTEND_SELECTION ||
        key == FIRST_COLUMN ||
        key == FIRST_COLUMN_EXTEND_SELECTION ||
        key == FIRST_ROW ||
        key == FIRST_ROW_EXTEND_SELECTION ||
        key == LAST_COLUMN ||
        key == LAST_COLUMN_EXTEND_SELECTION ||
        key == LAST_ROW ||
        key == LAST_ROW_EXTEND_SELECTION) {
      if (toLimit) {
        if (vertically) {
          int rowCount = table.getRowCount();
          this.dx = 0;
          this.dy = forwards ? rowCount : -rowCount;
        } else {
          int colCount = table.getColumnCount();
          this.dx = forwards ? colCount : -colCount;
          this.dy = 0;
        }
      } else {
        if (!(SwingUtilities.getUnwrappedParent(table).getParent() instanceof
            JScrollPane)) {
          return;
        }

        Dimension delta = table.getParent().getSize();

        if (vertically) {
          Rectangle r = table.getCellRect(leadRow, 0, true);
          if (forwards) {
            // scroll by at least one cell
            r.y += Math.max(delta.height, r.height);
          } else {
            r.y -= delta.height;
          }

          this.dx = 0;
          int newRow = table.rowAtPoint(r.getLocation());
          if (newRow == -1 && forwards) {
            newRow = table.getRowCount();
          }
          this.dy = newRow - leadRow;
        } else {
          Rectangle r = table.getCellRect(0, leadColumn, true);

          if (forwards) {
            // scroll by at least one cell
            r.x += Math.max(delta.width, r.width);
          } else {
            r.x -= delta.width;
          }

          int newColumn = table.columnAtPoint(r.getLocation());
          if (newColumn == -1) {
            boolean ltr = table.getComponentOrientation().isLeftToRight();

            newColumn = forwards ? (ltr ? table.getColumnCount() : 0)
                : (ltr ? 0 : table.getColumnCount());

          }
          this.dx = newColumn - leadColumn;
          this.dy = 0;
        }
      }
    }
    if (key == NEXT_ROW ||  // Navigate Actions
        key == NEXT_ROW_CELL ||
        key == NEXT_ROW_EXTEND_SELECTION ||
        key == NEXT_ROW_CHANGE_LEAD ||
        key == NEXT_COLUMN ||
        key == NEXT_COLUMN_CELL ||
        key == NEXT_COLUMN_EXTEND_SELECTION ||
        key == NEXT_COLUMN_CHANGE_LEAD ||
        key == PREVIOUS_ROW ||
        key == PREVIOUS_ROW_CELL ||
        key == PREVIOUS_ROW_EXTEND_SELECTION ||
        key == PREVIOUS_ROW_CHANGE_LEAD ||
        key == PREVIOUS_COLUMN ||
        key == PREVIOUS_COLUMN_CELL ||
        key == PREVIOUS_COLUMN_EXTEND_SELECTION ||
        key == PREVIOUS_COLUMN_CHANGE_LEAD ||
        // Paging Actions.
        key == SCROLL_LEFT_CHANGE_SELECTION ||
        key == SCROLL_LEFT_EXTEND_SELECTION ||
        key == SCROLL_RIGHT_CHANGE_SELECTION ||
        key == SCROLL_RIGHT_EXTEND_SELECTION ||
        key == SCROLL_UP_CHANGE_SELECTION ||
        key == SCROLL_UP_EXTEND_SELECTION ||
        key == SCROLL_DOWN_CHANGE_SELECTION ||
        key == SCROLL_DOWN_EXTEND_SELECTION ||
        key == FIRST_COLUMN ||
        key == FIRST_COLUMN_EXTEND_SELECTION ||
        key == FIRST_ROW ||
        key == FIRST_ROW_EXTEND_SELECTION ||
        key == LAST_COLUMN ||
        key == LAST_COLUMN_EXTEND_SELECTION ||
        key == LAST_ROW ||
        key == LAST_ROW_EXTEND_SELECTION) {

      if (table.isEditing() &&
          !table.getCellEditor().stopCellEditing()) {
        return;
      }

      // Unfortunately, this strategy introduces bugs because
      // of the asynchronous nature of requestFocus() call below.
      // Introducing a delay with invokeLater() makes this work
      // in the typical case though race conditions then allow
      // focus to disappear altogether. The right solution appears
      // to be to fix requestFocus() so that it queues a request
      // for the focus regardless of who owns the focus at the
      // time the call to requestFocus() is made. The optimisation
      // to ignore the call to requestFocus() when the component
      // already has focus may ligitimately be made as the
      // request focus event is dequeued, not before.

      // boolean wasEditingWithFocus = table.isEditing() &&
      // table.getEditorComponent().isFocusOwner();

      boolean changeLead = false;
      if (key == NEXT_ROW_CHANGE_LEAD || key == PREVIOUS_ROW_CHANGE_LEAD) {
        changeLead = (rsm.getSelectionMode()
            == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      } else if (key == NEXT_COLUMN_CHANGE_LEAD || key == PREVIOUS_COLUMN_CHANGE_LEAD) {
        changeLead = (csm.getSelectionMode()
            == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      }

      if (changeLead) {
        moveWithinTableRange(table, dx, dy);
        if (dy != 0) {
          // casting should be safe since the action is only enabled
          // for DefaultListSelectionModel
          ((DefaultListSelectionModel) rsm).moveLeadSelectionIndex(leadRow);
          if (getAdjustedLead(table, false, csm) == -1
              && table.getColumnCount() > 0) {

            ((DefaultListSelectionModel) csm).moveLeadSelectionIndex(0);
          }
        } else {
          // casting should be safe since the action is only enabled
          // for DefaultListSelectionModel
          ((DefaultListSelectionModel) csm).moveLeadSelectionIndex(leadColumn);
          if (getAdjustedLead(table, true, rsm) == -1
              && table.getRowCount() > 0) {

            ((DefaultListSelectionModel) rsm).moveLeadSelectionIndex(0);
          }
        }

        Rectangle cellRect = table.getCellRect(leadRow, leadColumn, false);
        if (cellRect != null) {
          table.scrollRectToVisible(cellRect);
        }
      } else if (!inSelection) {
        moveWithinTableRange(table, dx, dy);
        table.changeSelection(leadRow, leadColumn, false, extend);
      } else {
        if (table.getRowCount() <= 0 || table.getColumnCount() <= 0) {
          // bail - don't try to move selection on an empty table
          return;
        }

        if (moveWithinSelectedRange(table, dx, dy, rsm, csm)) {
          // this is the only way we have to set both the lead
          // and the anchor without changing the selection
          if (rsm.isSelectedIndex(leadRow)) {
            rsm.addSelectionInterval(leadRow, leadRow);
          } else {
            rsm.removeSelectionInterval(leadRow, leadRow);
          }

          if (csm.isSelectedIndex(leadColumn)) {
            csm.addSelectionInterval(leadColumn, leadColumn);
          } else {
            csm.removeSelectionInterval(leadColumn, leadColumn);
          }

          Rectangle cellRect = table.getCellRect(leadRow, leadColumn, false);
          if (cellRect != null) {
            table.scrollRectToVisible(cellRect);
          }
        } else {
          table.changeSelection(leadRow, leadColumn,
              false, false);
        }
      }

                /*
                if (wasEditingWithFocus) {
                    table.editCellAt(leadRow, leadColumn);
                    final Component editorComp = table.getEditorComponent();
                    if (editorComp != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                editorComp.requestFocus();
                            }
                        });
                    }
                }
                */
    } else if (key == CANCEL_EDITING) {
      table.removeEditor();
    } else if (key == SELECT_ALL) {
      table.selectAll();
    } else if (key == CLEAR_SELECTION) {
      table.clearSelection();
    } else if (key == START_EDITING) {
      if (!table.hasFocus()) {
        CellEditor cellEditor = table.getCellEditor();
        if (cellEditor != null && !cellEditor.stopCellEditing()) {
          return;
        }
        table.requestFocus();
        return;
      }
      table.editCellAt(leadRow, leadColumn, e);
      Component editorComp = table.getEditorComponent();
      if (editorComp != null) {
        editorComp.requestFocus();
      }
    } else if (key == ADD_TO_SELECTION) {
      if (!table.isCellSelected(leadRow, leadColumn)) {
        int oldAnchorRow = rsm.getAnchorSelectionIndex();
        int oldAnchorColumn = csm.getAnchorSelectionIndex();
        rsm.setValueIsAdjusting(true);
        csm.setValueIsAdjusting(true);
        table.changeSelection(leadRow, leadColumn, true, false);
        rsm.setAnchorSelectionIndex(oldAnchorRow);
        csm.setAnchorSelectionIndex(oldAnchorColumn);
        rsm.setValueIsAdjusting(false);
        csm.setValueIsAdjusting(false);
      }
    } else if (key == TOGGLE_AND_ANCHOR) {
      table.changeSelection(leadRow, leadColumn, true, false);
    } else if (key == EXTEND_TO) {
      table.changeSelection(leadRow, leadColumn, false, true);
    } else if (key == MOVE_SELECTION_TO) {
      table.changeSelection(leadRow, leadColumn, false, false);
    } else if (key == FOCUS_HEADER) {
      JTableHeader th = table.getTableHeader();
      if (th != null) {
        //Set the header's selected column to match the table.
        int col = table.getSelectedColumn();
        if (col >= 0) {
          TableHeaderUI thUI = th.getUI();
          if (thUI instanceof BasicTableHeaderUI) {
            ((BasicTableHeaderUI) thUI).selectColumn(col);
          }
        }

        //Then give the header the focus.
        th.requestFocusInWindow();
      }
    }
  }

  @Override
  public boolean accept(Object sender) {
    String key = getName();

    if (sender instanceof JTable &&
        Boolean.TRUE.equals(((JTable) sender).getClientProperty("Table.isFileList"))) {
      if (key == NEXT_COLUMN ||
          key == NEXT_COLUMN_CELL ||
          key == NEXT_COLUMN_EXTEND_SELECTION ||
          key == NEXT_COLUMN_CHANGE_LEAD ||
          key == PREVIOUS_COLUMN ||
          key == PREVIOUS_COLUMN_CELL ||
          key == PREVIOUS_COLUMN_EXTEND_SELECTION ||
          key == PREVIOUS_COLUMN_CHANGE_LEAD ||
          key == SCROLL_LEFT_CHANGE_SELECTION ||
          key == SCROLL_LEFT_EXTEND_SELECTION ||
          key == SCROLL_RIGHT_CHANGE_SELECTION ||
          key == SCROLL_RIGHT_EXTEND_SELECTION ||
          key == FIRST_COLUMN ||
          key == FIRST_COLUMN_EXTEND_SELECTION ||
          key == LAST_COLUMN ||
          key == LAST_COLUMN_EXTEND_SELECTION ||
          key == NEXT_ROW_CELL ||
          key == PREVIOUS_ROW_CELL) {

        return false;
      }
    }

    if (key == CANCEL_EDITING && sender instanceof JTable) {
      return ((JTable) sender).isEditing();
    } else if (key == NEXT_ROW_CHANGE_LEAD ||
        key == PREVIOUS_ROW_CHANGE_LEAD) {
      // discontinuous selection actions are only enabled for
      // DefaultListSelectionModel
      return sender != null &&
          ((JTable) sender).getSelectionModel()
              instanceof DefaultListSelectionModel;
    } else if (key == NEXT_COLUMN_CHANGE_LEAD ||
        key == PREVIOUS_COLUMN_CHANGE_LEAD) {
      // discontinuous selection actions are only enabled for
      // DefaultListSelectionModel
      return sender != null &&
          ((JTable) sender).getColumnModel().getSelectionModel()
              instanceof DefaultListSelectionModel;
    } else if (key == ADD_TO_SELECTION && sender instanceof JTable) {
      // This action is typically bound to SPACE.
      // If the table is already in an editing mode, SPACE should
      // simply enter a space character into the table, and not
      // select a cell. Likewise, if the lead cell is already selected
      // then hitting SPACE should just enter a space character
      // into the cell and begin editing. In both of these cases
      // this action will be disabled.
      JTable table = (JTable) sender;
      int leadRow = getAdjustedLead(table, true);
      int leadCol = getAdjustedLead(table, false);
      return !(table.isEditing() || table.isCellSelected(leadRow, leadCol));
    } else if (key == FOCUS_HEADER && sender instanceof JTable) {
      JTable table = (JTable) sender;
      return table.getTableHeader() != null;
    }

    return true;
  }
}
