package zekusan.ui.views;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JButton;
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
	private final JLabel loanedStatusLabel;
	private final JLabel pendingStatusLabel;
	private final LoanedTable loanedTable;
	private final PendingLoanTable pendingLoanTable;
	private final JButton refreshLoanedButton;
	private final JButton refreshPendingButton;

	public LibrarianDashboardPanel(LibraryClient libraryClient) {
		super(new BorderLayout(8, 8));
		this.libraryClient = libraryClient;

		greetingLabel = new JLabel(" ", SwingConstants.LEFT);
		greetingLabel.setFont(greetingLabel.getFont().deriveFont(Font.BOLD, greetingLabel.getFont().getSize2D() + 2));
		loanedStatusLabel = new JLabel(" ");
		pendingStatusLabel = new JLabel(" ");

		loanedTable = new LoanedTable();
		pendingLoanTable = new PendingLoanTable(libraryClient, this::setPendingStatusMessage, loanedTable::addRow);
		loanedTable.setRowHeight(pendingLoanTable.getPreferredRowHeight());
		JScrollPane loanedScroll = loanedTable.getScrollPane();
		JScrollPane pendingScroll = pendingLoanTable.getScrollPane();

		refreshLoanedButton = new JButton("Aggiorna");
		refreshPendingButton = new JButton("Aggiorna");
		refreshLoanedButton.addActionListener(e -> loadData());
		refreshPendingButton.addActionListener(e -> loadData());

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
		content.add(sectionHeader("Item in prestito", refreshLoanedButton, loanedStatusLabel), gbc);

		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.4;
		content.add(wrapTable(loanedScroll), gbc);

		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		content.add(sectionHeader("Prestiti in attesa", refreshPendingButton, pendingStatusLabel), gbc);

		gbc.gridy = 4;
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
			setLoanedStatusMessage(" ");
			setPendingStatusMessage(" ");
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
		setLoanedStatusMessage("Caricamento dei prestiti in corso...");
		setPendingStatusMessage("Caricamento in corso...");
		loanedTable.clearRows();
		pendingLoanTable.clearRows();
		setRefreshEnabled(false);

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
					setLoanedStatusMessage("Dati prestiti aggiornati.");
					setPendingStatusMessage("Dati prestiti aggiornati.");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					setLoanedStatusMessage("Caricamento interrotto.");
					setPendingStatusMessage("Caricamento interrotto.");
					loanedTable.clearRows();
					pendingLoanTable.clearRows();
				} catch (ExecutionException e) {
					setLoanedStatusMessage("Errore: " + e.getCause().getMessage());
					setPendingStatusMessage("Errore: " + e.getCause().getMessage());
					loanedTable.clearRows();
					pendingLoanTable.clearRows();
				} finally {
					setRefreshEnabled(true);
				}
			}
		}.execute();
	}

	private void setLoanedStatusMessage(String message) {
		loanedStatusLabel.setText(message == null || message.isBlank() ? " " : message);
	}

	private void setPendingStatusMessage(String message) {
		pendingStatusLabel.setText(message == null || message.isBlank() ? " " : message);
	}

	private JPanel sectionHeader(String text, JButton refreshButton, JLabel statusLabel) {
		JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 2));
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		panel.add(label);
		panel.add(refreshButton);
		panel.add(statusLabel);
		return panel;
	}

	private JComponent wrapTable(JScrollPane tableScroll) {
		tableScroll.setBorder(BorderFactory.createLineBorder(tableScroll.getBackground().darker(), 1));
		return tableScroll;
	}

	private void setRefreshEnabled(boolean enabled) {
		refreshLoanedButton.setEnabled(enabled);
		refreshPendingButton.setEnabled(enabled);
	}

	private record LoanSnapshot(List<LoanInfo> loaned, List<PendingLoanInfo> pending) {
	}
}
