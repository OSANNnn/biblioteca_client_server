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
import zekusan.models.loans.PendingLoanInfo;
import zekusan.services.LibraryClient;

class PendingLoanActionCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
	private static final long serialVersionUID = 1L;

	private final JTable table;
	private final DefaultTableModel tableModel;
	private final List<PendingLoanInfo> rows;
	private final LibraryClient libraryClient;
	private final Consumer<String> statusUpdater;
	private final Consumer<LoanInfo> onAcceptedLoan;

	private final JPanel renderPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
	private final JButton renderAccept = new JButton("Accetta");
	private final JButton renderCancel = new JButton("Annulla");

	private final JPanel editPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
	private final JButton editAccept = new JButton("Accetta");
	private final JButton editCancel = new JButton("Annulla");
	private final JLabel spacer = new JLabel(); // keeps height consistent

	PendingLoanActionCell(
			JTable table,
			DefaultTableModel tableModel,
			List<PendingLoanInfo> rows,
			LibraryClient libraryClient,
			Consumer<String> statusUpdater,
			Consumer<LoanInfo> onAcceptedLoan) {
		this.table = table;
		this.tableModel = tableModel;
		this.rows = rows;
		this.libraryClient = libraryClient;
		this.statusUpdater = statusUpdater;
		this.onAcceptedLoan = onAcceptedLoan;

		renderAccept.setFocusable(false);
		renderCancel.setFocusable(false);
		renderPanel.add(renderAccept);
		renderPanel.add(renderCancel);

		editPanel.add(editAccept);
		editPanel.add(editCancel);
		editPanel.add(spacer);

		editAccept.addActionListener(e -> handleAccept());
		editCancel.addActionListener(e -> handleCancel());
	}

	private void handleAccept() {
		int viewRow = table.getEditingRow();
		if (viewRow < 0) {
			viewRow = table.getSelectedRow();
		}

		int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
		if (modelRow < 0 || modelRow >= rows.size()) {
			updateStatus("Seleziona una richiesta valida.");
			fireEditingCanceled();
			return;
		}

		PendingLoanInfo pending = rows.get(modelRow);
		updateStatus("Accetto richiesta per " + pending.getItemName() + "...");
		setButtonsEnabled(false);

		new SwingWorker<LoanInfo, Void>() {
			@Override
			protected LoanInfo doInBackground() throws Exception {
				return libraryClient.acceptPendingLoan(pending.getId());
			}

			@Override
			protected void done() {
				setButtonsEnabled(true);
				try {
					LoanInfo loan = get();
					rows.remove(modelRow);
					tableModel.removeRow(modelRow);
					if (onAcceptedLoan != null) {
						onAcceptedLoan.accept(loan);
					}
					updateStatus("Prestito accettato per " + pending.getItemName() + ".");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					updateStatus("Accettazione interrotta.");
				} catch (ExecutionException e) {
					updateStatus("Errore durante l'accettazione: " + e.getCause().getMessage());
				}
			}
		}.execute();

		if (viewRow >= 0) {
			table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
		}

		fireEditingStopped();
	}

	private void handleCancel() {
		int viewRow = table.getEditingRow();
		if (viewRow < 0) {
			viewRow = table.getSelectedRow();
		}

		int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;
		if (modelRow < 0 || modelRow >= rows.size()) {
			updateStatus("Seleziona una richiesta valida.");
			fireEditingCanceled();
			return;
		}

		PendingLoanInfo pending = rows.get(modelRow);
		updateStatus("Annullamento per " + pending.getItemName() + "...");
		setButtonsEnabled(false);

		new SwingWorker<Boolean, Void>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				return libraryClient.cancelPendingLoan(pending.getId());
			}

			@Override
			protected void done() {
				setButtonsEnabled(true);
				try {
					Boolean removed = get();
					if (Boolean.TRUE.equals(removed)) {
						rows.remove(modelRow);
						tableModel.removeRow(modelRow);
						updateStatus("Richiesta annullata.");
					} else {
						updateStatus("Nessuna richiesta trovata.");
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					updateStatus("Annullamento interrotto.");
				} catch (ExecutionException e) {
					updateStatus("Errore nell'annullamento: " + e.getCause().getMessage());
				}
			}
		}.execute();

		if (viewRow >= 0) {
			table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
		}

		fireEditingStopped();
	}

	private void setButtonsEnabled(boolean enabled) {
		editAccept.setEnabled(enabled);
		editCancel.setEnabled(enabled);
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
