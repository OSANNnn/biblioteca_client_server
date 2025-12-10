package zekusan.ui.components;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import zekusan.models.loans.LoanInfo;
import zekusan.models.loans.PendingLoanInfo;
import zekusan.services.LibraryClient;

public class PendingLoanTable {
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final DefaultTableModel tableModel;
	private final JTable table;
	private final JScrollPane scrollPane;
	private final List<PendingLoanInfo> rows = new ArrayList<>();
	private final PendingLoanActionCell actionsCell;

	public PendingLoanTable(
			LibraryClient libraryClient,
			Consumer<String> statusUpdater,
			Consumer<LoanInfo> onAcceptedLoan) {
		tableModel = new DefaultTableModel(
				new Object[] { "Nome", "Categoria", "Richiedente", "Richiesto il", "Azioni" }, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 4;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 4) {
					return Object.class;
				}
				return super.getColumnClass(columnIndex);
			}
		};

		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFillsViewportHeight(true);

		scrollPane = new JScrollPane(
				table,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		actionsCell = new PendingLoanActionCell(table, tableModel, rows, libraryClient, statusUpdater, onAcceptedLoan);
		table.setRowHeight(actionsCell.getPreferredHeight() + 2);

		configureColumnWidths();
	}

	public void setRows(List<PendingLoanInfo> data) {
		clearRows();
		if (data == null) {
			return;
		}

		for (PendingLoanInfo pending : data) {
			rows.add(pending);
			tableModel.addRow(new Object[] {
					pending.getItemName(),
					pending.getCategory(),
					pending.getBorrower(),
					formatDate(pending.getRequestedOn()),
					null
			});
		}
		installActionColumn();
	}

	public void clearRows() {
		rows.clear();
		tableModel.setRowCount(0);
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	private void configureColumnWidths() {
		if (table.getColumnModel().getColumnCount() >= 5) {
			table.getColumnModel().getColumn(0).setPreferredWidth(240);
			table.getColumnModel().getColumn(1).setPreferredWidth(120);
			table.getColumnModel().getColumn(2).setPreferredWidth(160);
			table.getColumnModel().getColumn(3).setPreferredWidth(140);
			table.getColumnModel().getColumn(4).setPreferredWidth(200);
		}
	}

	private void installActionColumn() {
		if (table.getColumnModel().getColumnCount() > 4) {
			table.getColumnModel().getColumn(4).setCellRenderer(actionsCell);
			table.getColumnModel().getColumn(4).setCellEditor(actionsCell);
		}
	}

	public int getPreferredRowHeight() {
		return actionsCell.getPreferredHeight() + 2;
	}

	private String formatDate(java.time.LocalDate date) {
		if (date == null) {
			return "-";
		}
		return DATE_FMT.format(date);
	}
}
