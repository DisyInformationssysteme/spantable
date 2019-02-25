package net.disy.oss.spantable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.List;

public class ExampleApp {
    public static void main(String[] args) {
        var panel = new JPanel();
        panel.add(createDefaultTable());
        panel.add(createSpanTable());
        var frame = new JFrame();
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.show();
    }

    private static JComponent createSpanTable() {
        var table = new SpanTable();
        var model = new SpanTableModel(
            List.of(new CellSpan(0, 0, 2, 2)),
            new int[]{0, 1});
        table.setModel(new DelegatingSpanTableModel(
                createModel(),
                model));
        table.setUI(new SpanTableUi());
        var scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        // If we don't do this BLIT_SCROLL_MODE is used.
        // When a CellSpan begins before the visible region we render it's value at the beginning
        // of the visible region. When we scroll now upwards the old visible region is blit accordingly,
        // leading to render artifacts.
        // This can be detrimental to scroll perf and should maybe be configurable.
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        return scrollPane;
    }

    private static JComponent createDefaultTable() {
        var table = new JTable();
        table.setModel(createModel());
        table.setUI(new SpanTableUi());
        var scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
        return scrollPane;
    }

    private static TableModel createModel() {
        return new AbstractTableModel() {
            private Object[][] data = {
                    { "foo", "bar", "0" },
                    { "foo", "bar", "1" },
                    { "foo", "asdf", "2" },
                    { "foo", "asdf", "3" },
                    { "foo2", "bar", "4" },
                    { "foo2", "bar", "5" },
                    { "foo2", "asdf", "6" },
                    { "foo2", "asdf", "7" },
                    { "foo3", "bar", "8" },
                    { "foo4", "bar", "9" },
            };

            @Override
            public int getRowCount() {
                return 10;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int i, int i1) {
                return data[i][i1];
            }
        };
    }
}
