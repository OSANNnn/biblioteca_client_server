package zekusan.ui.components;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import zekusan.models.loans.PendingLoanInfo;
import zekusan.services.LibraryClient;

public class PendingBorrowTable {
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final DefaultTableModel tableModel;
	private final JTable table;
	private final JScrollPane scrollPane;
	private final List<PendingLoanInfo> rows = new ArrayList<>();
	private final PendingBorrowActionCell actionsCell;

	public PendingBorrowTable(LibraryClient libraryClient, Consumer<String> statusUpdater) {
		tableModel = new DefaultTableModel(
				new Object[] { "Nome", "Categoria", "Richiesto il", "Azioni" }, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 3;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 3) {
					return Object.class;
				}
				return super.getColumnClass(columnIndex);
			}
		};

		table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFillsViewportHeight(true);

		actionsCell = new PendingBorrowActionCell(table, tableModel, rows, libraryClient, statusUpdater);
		table.setRowHeight(actionsCell.getPreferredHeight() + 2);

		scrollPane = new JScrollPane(
				table,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

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

	public int getPreferredRowHeight() {
		return actionsCell.getPreferredHeight() + 2;
	}

	public void setRowHeight(int height) {
		if (height > 0) {
			table.setRowHeight(height);
		}
	}

	private void installActionColumn() {
		if (table.getColumnModel().getColumnCount() > 3) {
			table.getColumnModel().getColumn(3).setCellRenderer(actionsCell);
			table.getColumnModel().getColumn(3).setCellEditor(actionsCell);
		}
	}

	private void configureColumnWidths() {
		if (table.getColumnModel().getColumnCount() >= 4) {
			table.getColumnModel().getColumn(0).setPreferredWidth(240);
			table.getColumnModel().getColumn(1).setPreferredWidth(120);
			table.getColumnModel().getColumn(2).setPreferredWidth(140);
			table.getColumnModel().getColumn(3).setPreferredWidth(140);
		}
	}

	private String formatDate(java.time.LocalDate date) {
		if (date == null) {
			return "-";
		}
		return DATE_FMT.format(date);
	}
}
