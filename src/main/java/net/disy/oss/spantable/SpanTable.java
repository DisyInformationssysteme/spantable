package net.disy.oss.spantable;

import javax.swing.*;
import java.awt.*;

public class SpanTable extends JTable {
  @Override
  public void changeSelection(int row, int column, boolean toggle, boolean extend) {
    if (getModel() instanceof ISpanTableModel) {
      var spanModel = (ISpanTableModel) getModel();
      var cellSpan = spanModel.getCellSpanContaining(row, column);
      if (cellSpan.isPresent()) {
        var span = cellSpan.get();
        super.changeSelection(span.getStartRow(), span.getStartColumn(), toggle, extend);
        return;
      }
    }
    super.changeSelection(row, column, toggle, extend);
  }

  @Override
  public void repaint(Rectangle r) {
    if (getModel() instanceof ISpanTableModel) {
      var spanModel = (ISpanTableModel) getModel();
      SpanTableHelper.adjustDirtyRect(this, spanModel, r);
    }
    super.repaint(r);
  }
}
