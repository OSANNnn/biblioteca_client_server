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
import zekusan.services.LibraryClient;

class CatalogActionCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private static final long serialVersionUID = 1L;

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;
    private final transient Supplier<ItemType> currentTypeSupplier;
    private final transient LibraryClient libraryClient;
    private final transient IntConsumer onSuccessfulBorrow;

    // Renderer components (non-interactive)
    private final JPanel renderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    private final JButton renderBorrow = new JButton();

    // Editor components (interactive)
    private final JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    private final JButton editBorrow = new JButton();

    CatalogActionCell(
            JTable table,
            DefaultTableModel tableModel,
            JLabel statusLabel,
            Supplier<ItemType> currentTypeSupplier,
            LibraryClient libraryClient,
            IntConsumer onSuccessfulBorrow) {
        this.table = table;
        this.tableModel = tableModel;
        this.statusLabel = statusLabel;
        this.currentTypeSupplier = currentTypeSupplier;
        this.libraryClient = libraryClient;
        this.onSuccessfulBorrow = onSuccessfulBorrow;

        renderBorrow.setText(buttonText());
        renderBorrow.setFocusable(false);
        renderPanel.add(renderBorrow);

        editBorrow.setText(buttonText());
        editPanel.add(editBorrow);

        editBorrow.addActionListener(e -> handleBorrowAction());
    }

    private void handleBorrowAction() {
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
            statusLabel.setText("Funzione modifica non implementata.");
            fireEditingStopped();
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

    private boolean isLibrarian() {
        if (libraryClient == null || !libraryClient.isLoggedIn() || libraryClient.getSession() == null) {
            return false;
        }
        return libraryClient.getSession().userType() != UserType.STUDENTE;
    }

    private String buttonText() {
        return isLibrarian() ? "Modifica" : "Richiedi prestito";
    }
}
