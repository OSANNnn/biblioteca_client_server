package zekusan.ui.views;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import zekusan.interfaces.PanelLifecycle;
import zekusan.models.loans.LoanInfo;
import zekusan.models.loans.PendingLoanInfo;
import zekusan.services.LibraryClient;
import zekusan.ui.components.LoanedTable;
import zekusan.ui.components.PendingLoanTable;
import zekusan.ui.components.ScrollUtil;
import zekusan.ui.components.ScrollablePanel;

public class LibrarianDashboardPanel extends JPanel implements PanelLifecycle {
	private static final long serialVersionUID = 1L;

	private final transient LibraryClient libraryClient;
	private final JLabel greetingLabel;
	private final JLabel statusLabel;
	private final LoanedTable loanedTable;
	private final PendingLoanTable pendingLoanTable;

	public LibrarianDashboardPanel(LibraryClient libraryClient) {
		super(new BorderLayout(8, 8));
		this.libraryClient = libraryClient;

		greetingLabel = new JLabel(" ", SwingConstants.LEFT);
		greetingLabel.setFont(greetingLabel.getFont().deriveFont(Font.BOLD, greetingLabel.getFont().getSize2D() + 2));
		statusLabel = new JLabel(" ");

		loanedTable = new LoanedTable();
		pendingLoanTable = new PendingLoanTable(libraryClient, this::setStatusMessage, loanedTable::addRow);
		loanedTable.setRowHeight(pendingLoanTable.getPreferredRowHeight());
		JScrollPane loanedScroll = loanedTable.getScrollPane();
		JScrollPane pendingScroll = pendingLoanTable.getScrollPane();

		ScrollablePanel content = new ScrollablePanel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.insets = new Insets(0, 0, 8, 0);

		gbc.gridy = 0;
		content.add(greetingLabel, gbc);

		gbc.gridy = 1;
		content.add(statusLabel, gbc);

		gbc.gridy = 2;
		content.add(sectionLabel("Item in prestito"), gbc);

		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.4;
		content.add(wrapTable(loanedScroll), gbc);

		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		content.add(sectionLabel("Prestiti in attesa"), gbc);

		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.6;
		content.add(wrapTable(pendingScroll), gbc);

		JScrollPane outerScroll = new JScrollPane(content);
		outerScroll.setBorder(BorderFactory.createEmptyBorder());
		outerScroll.getViewport().setBackground(content.getBackground());
		outerScroll.getVerticalScrollBar().setUnitIncrement(16);

		ScrollUtil.enableEdgeWheelPropagation(loanedScroll, outerScroll);
		ScrollUtil.enableEdgeWheelPropagation(pendingScroll, outerScroll);

		add(outerScroll, BorderLayout.CENTER);
	}

	@Override
	public void onShow() {
		if (!libraryClient.isLoggedIn()) {
			greetingLabel.setText("Accedi per iniziare.");
			setStatusMessage(" ");
			loanedTable.clearRows();
			pendingLoanTable.clearRows();
			return;
		}

		var session = libraryClient.getSession();
		greetingLabel.setText("Ciao " + session.username() + "!");
		loadData();
	}

	@Override
	public void onHide() {
		// nothing to clean up
	}

	private void loadData() {
		setStatusMessage("Caricamento dei prestiti in corso...");
		loanedTable.clearRows();
		pendingLoanTable.clearRows();

		new SwingWorker<LoanSnapshot, Void>() {
			@Override
			protected LoanSnapshot doInBackground() throws Exception {
				List<LoanInfo> loaned = libraryClient.loadLoanedItems();
				List<PendingLoanInfo> pending = libraryClient.loadPendingLoans();
				return new LoanSnapshot(loaned, pending);
			}

			@Override
			protected void done() {
				try {
					LoanSnapshot data = get();
					loanedTable.setRows(data.loaned());
					pendingLoanTable.setRows(data.pending());
					setStatusMessage("Dati prestiti aggiornati.");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					setStatusMessage("Caricamento interrotto.");
					loanedTable.clearRows();
					pendingLoanTable.clearRows();
				} catch (ExecutionException e) {
					setStatusMessage("Errore nel caricamento: " + e.getCause().getMessage());
					loanedTable.clearRows();
					pendingLoanTable.clearRows();
				}
			}
		}.execute();
	}

	private void setStatusMessage(String message) {
		statusLabel.setText(message == null || message.isBlank() ? " " : message);
	}

	private JLabel sectionLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
	}

	private JComponent wrapTable(JScrollPane tableScroll) {
		tableScroll.setBorder(BorderFactory.createLineBorder(tableScroll.getBackground().darker(), 1));
		return tableScroll;
	}

	private record LoanSnapshot(List<LoanInfo> loaned, List<PendingLoanInfo> pending) {
	}
}
