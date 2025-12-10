package zekusan.ui.components;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import zekusan.enums.ItemType;
import zekusan.models.items.CD;
import zekusan.models.items.Item;
import zekusan.models.items.Libro;
import zekusan.models.items.Rivista;
import zekusan.services.LibraryClient;

public class CatalogTable {
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JScrollPane scrollPane;
    private final CatalogActionCell actionsCell;

    // Index of the "Azioni" column, depends on current layout
    private int actionsColumnIndex = 5;
    private ItemType currentType = ItemType.NONE;

    public CatalogTable(JLabel statusLabel, LibraryClient libraryClient) {
        tableModel = new DefaultTableModel(
                new Object[] { "ID", "Titolo", "Quantita", "Tipo", "Dettagli", "Azioni" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == CatalogTable.this.actionsColumnIndex;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == CatalogTable.this.actionsColumnIndex) {
                    return Object.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);

        actionsCell = new CatalogActionCell(
                table,
                tableModel,
                statusLabel,
                this::getCurrentType,
                libraryClient,
                this::decrementQuantity);
        table.setRowHeight(actionsCell.getPreferredHeight() + 2);

        scrollPane = new JScrollPane(
                table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public void configureColumnsForType(ItemType type) {
        String[] columns;
        currentType = type;

        if (type == ItemType.LIBRO) {
            columns = new String[] { "ID", "Titolo", "Quantita", "Autore", "Genere", "ISBN", "Azioni" };
            actionsColumnIndex = 6;
        } else if (type == ItemType.CD) {
            columns = new String[] { "ID", "Titolo", "Quantita", "Artista", "Genere", "Azioni" };
            actionsColumnIndex = 5;
        } else if (type == ItemType.RIVISTA) {
            columns = new String[] { "ID", "Titolo", "Quantita", "Anno", "Numero", "Azioni" };
            actionsColumnIndex = 5;
        } else {
            columns = new String[] { "ID", "Titolo", "Quantita", "Tipo", "Dettagli", "Azioni" };
            actionsColumnIndex = 5;
        }

        tableModel.setColumnIdentifiers(columns);
        table.createDefaultColumnsFromModel();

        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        }
        if (table.getColumnModel().getColumnCount() > 1) {
            table.getColumnModel().getColumn(1).setPreferredWidth(240);  // Titolo
        }
        if (table.getColumnModel().getColumnCount() > 2) {
            table.getColumnModel().getColumn(2).setPreferredWidth(80);   // Quantita
        }

        for (int i = 3; i < actionsColumnIndex && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(140);
        }

        if (actionsColumnIndex < table.getColumnModel().getColumnCount()) {
            TableColumn actionsColumn = table.getColumnModel().getColumn(actionsColumnIndex);
            actionsColumn.setCellRenderer(actionsCell);
            actionsColumn.setCellEditor(actionsCell);
            actionsColumn.setMinWidth(180);
            actionsColumn.setPreferredWidth(200);
        }
    }

    public void updateRows(List<Item> items, ItemType type) {
        if (items == null) {
            return;
        }

        for (Item item : items) {
            if (type == ItemType.LIBRO && item instanceof Libro libro) {
                tableModel.addRow(new Object[] {
                        libro.getId(),
                        libro.getTitolo(),
                        libro.getQuantita(),
                        nullToEmpty(libro.getAutore()),
                        nullToEmpty(libro.getGenere()),
                        nullToEmpty(libro.getIsbn()),
                        null
                });
            } else if (type == ItemType.CD && item instanceof CD cd) {
                tableModel.addRow(new Object[] {
                        cd.getId(),
                        cd.getTitolo(),
                        cd.getQuantita(),
                        nullToEmpty(cd.getArtista()),
                        nullToEmpty(cd.getGenere()),
                        null
                });
            } else if (type == ItemType.RIVISTA && item instanceof Rivista rivista) {
                tableModel.addRow(new Object[] {
                        rivista.getId(),
                        rivista.getTitolo(),
                        rivista.getQuantita(),
                        rivista.getAnno(),
                        rivista.getNumero(),
                        null
                });
            } else {
                tableModel.addRow(new Object[] {
                        item.getId(),
                        item.getTitolo(),
                        item.getQuantita(),
                        item.getTipo(),
                        "-",
                        null
                });
            }
        }
    }

    public void clearRows() {
        tableModel.setRowCount(0);
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    ItemType getCurrentType() {
        return currentType;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void decrementQuantity(int modelRow) {
        Object qtyValue = tableModel.getValueAt(modelRow, 2);
        int currentQty = toInt(qtyValue);
        if (currentQty <= 0) {
            return;
        }
        tableModel.setValueAt(currentQty - 1, modelRow, 2);
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }
}
