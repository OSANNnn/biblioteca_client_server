package zekusan.ui.components;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import zekusan.models.loans.LoanInfo;
import zekusan.services.LibraryClient;

class BorrowedActionCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
	private static final long serialVersionUID = 1L;

	private final JTable table;
	private final DefaultTableModel tableModel;
	private final List<LoanInfo> rows;
	private final LibraryClient libraryClient;
	private final Consumer<String> statusUpdater;

	private final JPanel renderPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
	private final JButton renderReturn = new JButton("Restituisci");

	private final JPanel editPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
	private final JButton editReturn = new JButton("Restituisci");
	private final JLabel spacer = new JLabel();

	BorrowedActionCell(
			JTable table,
			DefaultTableModel tableModel,
			List<LoanInfo> rows,
			LibraryClient libraryClient,
			Consumer<String> statusUpdater) {
		this.table = table;
		this.tableModel = tableModel;
		this.rows = rows;
		this.libraryClient = libraryClient;
		this.statusUpdater = statusUpdater;

		renderReturn.setFocusable(false);
		renderPanel.add(renderReturn);

		editPanel.add(editReturn);
		editPanel.add(spacer);

		editReturn.addActionListener(e -> handleReturn());
	}

	private void handleReturn() {
		int viewRow = table.getEditingRow();
		if (viewRow < 0) {
			viewRow = table.getSelectedRow();
		}

		int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
		if (modelRow < 0 || modelRow >= rows.size()) {
			updateStatus("Seleziona un prestito valido.");
			fireEditingCanceled();
			return;
		}

		LoanInfo loan = rows.get(modelRow);
		updateStatus("Restituzione di " + loan.getItemName() + "...");
		editReturn.setEnabled(false);

		new SwingWorker<Boolean, Void>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				return libraryClient.returnLoan(loan.getId());
			}

			@Override
			protected void done() {
				editReturn.setEnabled(true);
				try {
					Boolean removed = get();
					if (Boolean.TRUE.equals(removed)) {
						rows.remove(modelRow);
						tableModel.removeRow(modelRow);
						updateStatus("Restituzione completata.");
					} else {
						updateStatus("Prestito non trovato.");
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					updateStatus("Restituzione interrotta.");
				} catch (ExecutionException e) {
					updateStatus("Errore nella restituzione: " + e.getCause().getMessage());
				}
			}
		}.execute();

		if (viewRow >= 0) {
			table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
		}

		fireEditingStopped();
	}

	private void updateStatus(String message) {
		if (statusUpdater != null) {
			statusUpdater.accept(message);
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		renderPanel.setBackground(
				isSelected ? table.getSelectionBackground()
						: table.getBackground());
		return renderPanel;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
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
