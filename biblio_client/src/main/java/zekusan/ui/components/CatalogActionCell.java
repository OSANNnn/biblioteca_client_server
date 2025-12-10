package zekusan.ui.components;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.concurrent.ExecutionException;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import zekusan.comms.responses.PrenotazioneResponse;
import zekusan.enums.ItemType;
import zekusan.enums.UserType;
import zekusan.enums.Status;
import zekusan.interfaces.Navigator;
import zekusan.enums.Route;
import zekusan.services.LibraryClient;
import zekusan.models.items.CD;
import zekusan.models.items.Item;
import zekusan.models.items.Libro;
import zekusan.models.items.Rivista;

class CatalogActionCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private static final long serialVersionUID = 1L;

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;
    private final transient Supplier<ItemType> currentTypeSupplier;
    private final transient LibraryClient libraryClient;
    private final transient Navigator navigator;
    private final transient IntConsumer onSuccessfulBorrow;

    // Renderer components (non-interactive)
    private final JPanel renderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    private final JButton renderBorrow = new JButton();
    private final JButton renderDelete = new JButton();

    // Editor components (interactive)
    private final JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    private final JButton editBorrow = new JButton();
    private final JButton editDelete = new JButton();

    CatalogActionCell(
            JTable table,
            DefaultTableModel tableModel,
            JLabel statusLabel,
            Supplier<ItemType> currentTypeSupplier,
            LibraryClient libraryClient,
            Navigator navigator,
            IntConsumer onSuccessfulBorrow) {
        this.table = table;
        this.tableModel = tableModel;
        this.statusLabel = statusLabel;
        this.currentTypeSupplier = currentTypeSupplier;
        this.libraryClient = libraryClient;
        this.navigator = navigator;
        this.onSuccessfulBorrow = onSuccessfulBorrow;

        renderBorrow.setText(buttonText());
        renderBorrow.setFocusable(false);
        renderPanel.add(renderBorrow);

        renderDelete.setText("Elimina");
        renderDelete.setFocusable(false);
        renderPanel.add(renderDelete);

        editBorrow.setText(buttonText());
        editPanel.add(editBorrow);

        editDelete.setText("Elimina");
        editPanel.add(editDelete);

        editBorrow.addActionListener(e -> handleBorrowAction());
        editDelete.addActionListener(e -> handleDeleteAction());
        refreshButtonsForRole();
    }

    private void handleBorrowAction() {
        refreshButtonsForRole();
        int viewRow = table.getEditingRow();
        if (viewRow < 0) {
            viewRow = table.getSelectedRow();
        }

        int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
        if (modelRow < 0) {
            statusLabel.setText("Seleziona un elemento per il prestito.");
            fireEditingCanceled();
            return;
        }

        Object idValue = tableModel.getValueAt(modelRow, 0);
        if (!(idValue instanceof Number idNumber)) {
            statusLabel.setText("ID non valido per il prestito.");
            fireEditingCanceled();
            return;
        }

        ItemType type = currentTypeSupplier.get();
        if (type == null || type == ItemType.NONE) {
            statusLabel.setText("Categoria non valida per il prestito.");
            fireEditingCanceled();
            return;
        }

        int itemId = idNumber.intValue();

        if (isLibrarian()) {
            statusLabel.setText("Modifica elemento...");
            openEditPanel(modelRow, type);
            return;
        }

        statusLabel.setText("Richiesta prestito per ID " + itemId + "...");
        editBorrow.setEnabled(false);

        new SwingWorker<PrenotazioneResponse, Void>() {
            @Override
            protected PrenotazioneResponse doInBackground() throws Exception {
                return libraryClient.prenota(itemId, type);
            }

            @Override
            protected void done() {
                editBorrow.setEnabled(true);
                try {
                    PrenotazioneResponse response = get();
                    if (response.getStatus() == Status.SUCCESS) {
                        statusLabel.setText("Prestito richiesto per ID " + itemId + ".");
                        onSuccessfulBorrow.accept(modelRow);
                    } else {
                        statusLabel.setText("Impossibile richiedere prestito: " + response.getStatus());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Richiesta prestito interrotta.");
                } catch (ExecutionException e) {
                    statusLabel.setText("Errore nella richiesta di prestito: " + e.getCause().getMessage());
                }
            }
        }.execute();

        if (viewRow >= 0) {
            table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
        }

        fireEditingStopped();
    }

    private void handleDeleteAction() {
        refreshButtonsForRole();
        if (!isLibrarian()) {
            statusLabel.setText("Solo il bibliotecario può eliminare elementi.");
            fireEditingCanceled();
            return;
        }

        int viewRow = table.getEditingRow();
        if (viewRow < 0) {
            viewRow = table.getSelectedRow();
        }

        int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
        if (modelRow < 0) {
            statusLabel.setText("Seleziona un elemento da eliminare.");
            fireEditingCanceled();
            return;
        }

        Object idValue = tableModel.getValueAt(modelRow, 0);
        if (!(idValue instanceof Number idNumber)) {
            statusLabel.setText("ID non valido per l'eliminazione.");
            fireEditingCanceled();
            return;
        }

        int itemId = idNumber.intValue();
        statusLabel.setText("Eliminazione elemento ID " + itemId + "...");
        editBorrow.setEnabled(false);
        editDelete.setEnabled(false);

        final int rowToRemove = modelRow;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return libraryClient.deleteItem(itemId);
            }

            @Override
            protected void done() {
                editBorrow.setEnabled(true);
                editDelete.setEnabled(true);
                try {
                    Boolean removed = get();
                    if (Boolean.TRUE.equals(removed)) {
                        statusLabel.setText("Elemento eliminato.");
                        if (rowToRemove >= 0 && rowToRemove < tableModel.getRowCount()) {
                            tableModel.removeRow(rowToRemove);
                        }
                    } else {
                        statusLabel.setText("Elemento non trovato o già rimosso.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Eliminazione interrotta.");
                } catch (ExecutionException e) {
                    statusLabel.setText("Errore nell'eliminazione: " + e.getCause().getMessage());
                }
            }
        }.execute();

        if (viewRow >= 0) {
            table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
        }

        fireEditingStopped();
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        refreshButtonsForRole();
        renderPanel.setBackground(
                isSelected ? table.getSelectionBackground()
                           : table.getBackground());
        return renderPanel;
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected,
            int row, int column) {
        refreshButtonsForRole();
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

    private void refreshButtonsForRole() {
        String primaryText = buttonText();
        renderBorrow.setText(primaryText);
        editBorrow.setText(primaryText);

        boolean librarian = isLibrarian();
        renderDelete.setVisible(librarian);
        editDelete.setVisible(librarian);
    }

    private boolean isLibrarian() {
        if (libraryClient == null || !libraryClient.isLoggedIn() || libraryClient.getSession() == null) {
            return false;
        }
        return libraryClient.getSession().userType() != UserType.STUDENTE;
    }

    private String buttonText() {
        return isLibrarian() ? "Modifica" : "Richiedi prestito";
    }

    private void openEditPanel(int modelRow, ItemType type) {
        Item item = extractItem(modelRow, type);
        libraryClient.setPendingEditItem(item);
        if (navigator != null) {
            navigator.navigate(Route.LIBRARIAN_EDIT_ITEM);
        }
        fireEditingStopped();
    }

    private Item extractItem(int modelRow, ItemType type) {
        Object idVal = tableModel.getValueAt(modelRow, 0);
        int id = idVal instanceof Number n ? n.intValue() : -1;
        Item item;
        if (type == ItemType.LIBRO) {
            Libro libro = new Libro();
            libro.setAutore(toStr(tableModel.getValueAt(modelRow, 3)));
            libro.setGenere(toStr(tableModel.getValueAt(modelRow, 4)));
            libro.setIsbn(toStr(tableModel.getValueAt(modelRow, 5)));
            item = libro;
        } else if (type == ItemType.CD) {
            CD cd = new CD();
            cd.setArtista(toStr(tableModel.getValueAt(modelRow, 3)));
            cd.setGenere(toStr(tableModel.getValueAt(modelRow, 4)));
            item = cd;
        } else if (type == ItemType.RIVISTA) {
            Rivista rivista = new Rivista();
            rivista.setAnno(toInt(tableModel.getValueAt(modelRow, 3)));
            rivista.setNumero(toInt(tableModel.getValueAt(modelRow, 4)));
            item = rivista;
        } else {
            item = new Item();
            item.setTipo(type);
        }

        item.setId(id);
        item.setTitolo(toStr(tableModel.getValueAt(modelRow, 1)));
        item.setQuantita(toInt(tableModel.getValueAt(modelRow, 2)));
        item.setTipo(type);
        return item;
    }

    private void updateTableRow(int modelRow, Item item) {
        tableModel.setValueAt(item.getTitolo(), modelRow, 1);
        tableModel.setValueAt(item.getQuantita(), modelRow, 2);

        if (item instanceof Libro libro) {
            tableModel.setValueAt(nullToEmpty(libro.getAutore()), modelRow, 3);
            tableModel.setValueAt(nullToEmpty(libro.getGenere()), modelRow, 4);
            tableModel.setValueAt(nullToEmpty(libro.getIsbn()), modelRow, 5);
        } else if (item instanceof CD cd) {
            tableModel.setValueAt(nullToEmpty(cd.getArtista()), modelRow, 3);
            tableModel.setValueAt(nullToEmpty(cd.getGenere()), modelRow, 4);
        } else if (item instanceof Rivista rivista) {
            tableModel.setValueAt(rivista.getAnno(), modelRow, 3);
            tableModel.setValueAt(rivista.getNumero(), modelRow, 4);
        }
    }

    private String toStr(Object value) {
        return value == null ? "" : value.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }
}
