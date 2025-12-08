package zekusan.ui.views;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

class CatalogActionCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private static final long serialVersionUID = 1L;

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;

    // Renderer components (non-interactive)
    private final JPanel renderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    private final JButton renderFoo = new JButton("Foo");
    private final JButton renderBar = new JButton("Bar");

    // Editor components (interactive)
    private final JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    private final JButton editFoo = new JButton("Foo");
    private final JButton editBar = new JButton("Bar");

    CatalogActionCell(JTable table, DefaultTableModel tableModel, JLabel statusLabel) {
        this.table = table;
        this.tableModel = tableModel;
        this.statusLabel = statusLabel;

        renderFoo.setFocusable(false);
        renderBar.setFocusable(false);
        renderPanel.add(renderFoo);
        renderPanel.add(renderBar);

        editPanel.add(editFoo);
        editPanel.add(editBar);

        editFoo.addActionListener(e -> handleAction("Foo"));
        editBar.addActionListener(e -> handleAction("Bar"));
    }

    private void handleAction(String actionLabel) {
        int viewRow = table.getEditingRow();
        if (viewRow < 0) {
            viewRow = table.getSelectedRow();
        }

        int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
        Object id = modelRow >= 0 ? tableModel.getValueAt(modelRow, 0) : "?";

        statusLabel.setText(actionLabel + " su ID " + id);

        if (viewRow >= 0) {
            table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
        }

        fireEditingStopped();
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        renderPanel.setBackground(
                isSelected ? table.getSelectionBackground()
                           : table.getBackground());
        return renderPanel;
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected,
            int row, int column) {
        editPanel.setBackground(table.getSelectionBackground());
        return editPanel;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    int getPreferredHeight() {
        return renderPanel.getPreferredSize().height;
    }
}
