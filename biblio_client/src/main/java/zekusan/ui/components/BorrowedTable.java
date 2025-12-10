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
import zekusan.services.LibraryClient;

public class BorrowedTable {
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final DefaultTableModel tableModel;
	private final JTable table;
	private final JScrollPane scrollPane;
	private final List<LoanInfo> rows = new ArrayList<>();
	private final BorrowedActionCell actionsCell;

	public BorrowedTable(LibraryClient libraryClient, Consumer<String> statusUpdater) {
		tableModel = new DefaultTableModel(new Object[] { "Nome", "Categoria", "Scadenza", "Preso il", "Azioni" }, 0) {
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

		actionsCell = new BorrowedActionCell(table, tableModel, rows, libraryClient, statusUpdater);
		table.setRowHeight(actionsCell.getPreferredHeight() + 2);

		scrollPane = new JScrollPane(
				table,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		configureColumnWidths();
	}

	public void setRows(List<LoanInfo> data) {
		clearRows();
		if (data == null) {
			return;
		}

		for (LoanInfo loan : data) {
			rows.add(loan);
			tableModel.addRow(new Object[] {
					loan.getItemName(),
					loan.getCategory(),
					formatDate(loan.getDueDate()),
					formatDate(loan.getRequestedOn()),
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
		if (table.getColumnModel().getColumnCount() > 4) {
			table.getColumnModel().getColumn(4).setCellRenderer(actionsCell);
			table.getColumnModel().getColumn(4).setCellEditor(actionsCell);
		}
	}

	private void configureColumnWidths() {
		if (table.getColumnModel().getColumnCount() >= 5) {
			table.getColumnModel().getColumn(0).setPreferredWidth(240);
			table.getColumnModel().getColumn(1).setPreferredWidth(120);
			table.getColumnModel().getColumn(2).setPreferredWidth(140);
			table.getColumnModel().getColumn(3).setPreferredWidth(140);
			table.getColumnModel().getColumn(4).setPreferredWidth(160);
		}
	}

	private String formatDate(java.time.LocalDate date) {
		if (date == null) {
			return "-";
		}
		return DATE_FMT.format(date);
	}
}
