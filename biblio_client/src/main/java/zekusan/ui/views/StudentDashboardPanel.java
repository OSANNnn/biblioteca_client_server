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
import zekusan.ui.components.BorrowedTable;
import zekusan.ui.components.PendingBorrowTable;
import zekusan.ui.components.ScrollUtil;
import zekusan.ui.components.ScrollablePanel;

public class StudentDashboardPanel extends JPanel implements PanelLifecycle {
	private static final long serialVersionUID = 1L;

	private final transient LibraryClient libraryClient;
	private final JLabel greetingLabel;
	private final JLabel borrowedStatusLabel;
	private final JLabel pendingStatusLabel;
	private final BorrowedTable borrowedTable;
	private final PendingBorrowTable pendingBorrowTable;
	private final JButton refreshBorrowedButton;
	private final JButton refreshPendingButton;

	public StudentDashboardPanel(LibraryClient libraryClient) {
		super(new BorderLayout(8, 8));
		this.libraryClient = libraryClient;

		greetingLabel = new JLabel(" ", SwingConstants.LEFT);
		greetingLabel.setFont(greetingLabel.getFont().deriveFont(Font.BOLD, greetingLabel.getFont().getSize2D() + 2));
		borrowedStatusLabel = new JLabel(" ");
		pendingStatusLabel = new JLabel(" ");

		borrowedTable = new BorrowedTable(libraryClient, this::setBorrowedStatusMessage);
		pendingBorrowTable = new PendingBorrowTable(libraryClient, this::setPendingStatusMessage);
		int preferredRowHeight = Math.max(
				borrowedTable.getPreferredRowHeight(),
				pendingBorrowTable.getPreferredRowHeight());
		borrowedTable.setRowHeight(preferredRowHeight);
		pendingBorrowTable.setRowHeight(preferredRowHeight);

		JScrollPane borrowedScroll = borrowedTable.getScrollPane();
		JScrollPane pendingScroll = pendingBorrowTable.getScrollPane();

		refreshBorrowedButton = new JButton("Aggiorna");
		refreshPendingButton = new JButton("Aggiorna");
		refreshBorrowedButton.addActionListener(e -> loadData());
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
		content.add(sectionHeader("Prestiti attivi", refreshBorrowedButton, borrowedStatusLabel), gbc);

		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.5;
		content.add(wrapTable(borrowedScroll), gbc);

		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		content.add(sectionHeader("Richieste in attesa", refreshPendingButton, pendingStatusLabel), gbc);

		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.5;
		content.add(wrapTable(pendingScroll), gbc);

		JScrollPane outerScroll = new JScrollPane(content);
		outerScroll.setBorder(BorderFactory.createEmptyBorder());
		outerScroll.getViewport().setBackground(content.getBackground());
		outerScroll.getVerticalScrollBar().setUnitIncrement(16);

		ScrollUtil.enableEdgeWheelPropagation(borrowedScroll, outerScroll);
		ScrollUtil.enableEdgeWheelPropagation(pendingScroll, outerScroll);

		add(outerScroll, BorderLayout.CENTER);
	}

	@Override
	public void onShow() {
		if (!libraryClient.isLoggedIn()) {
			greetingLabel.setText("Accedi per iniziare.");
			setBorrowedStatusMessage(" ");
			setPendingStatusMessage(" ");
			borrowedTable.clearRows();
			pendingBorrowTable.clearRows();
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
		setBorrowedStatusMessage("Caricamento prestiti in corso...");
		setPendingStatusMessage("Caricamento in corso...");
		borrowedTable.clearRows();
		pendingBorrowTable.clearRows();
		setRefreshEnabled(false);

		new SwingWorker<BorrowSnapshot, Void>() {
			@Override
			protected BorrowSnapshot doInBackground() throws Exception {
				List<LoanInfo> borrowed = libraryClient.loadMyBorrowedItems();
				List<PendingLoanInfo> pending = libraryClient.loadMyPendingBorrows();
				return new BorrowSnapshot(borrowed, pending);
			}

			@Override
			protected void done() {
				try {
					BorrowSnapshot data = get();
					borrowedTable.setRows(data.borrowed());
					pendingBorrowTable.setRows(data.pending());
					setBorrowedStatusMessage("Dati aggiornati.");
					setPendingStatusMessage("Dati aggiornati.");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					setBorrowedStatusMessage("Caricamento interrotto.");
					setPendingStatusMessage("Caricamento interrotto.");
					borrowedTable.clearRows();
					pendingBorrowTable.clearRows();
				} catch (ExecutionException e) {
					setBorrowedStatusMessage("Errore: " + e.getCause().getMessage());
					setPendingStatusMessage("Errore: " + e.getCause().getMessage());
					borrowedTable.clearRows();
					pendingBorrowTable.clearRows();
				} finally {
					setRefreshEnabled(true);
				}
			}
		}.execute();
	}

	private void setBorrowedStatusMessage(String message) {
		borrowedStatusLabel.setText(message == null || message.isBlank() ? " " : message);
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
		refreshBorrowedButton.setEnabled(enabled);
		refreshPendingButton.setEnabled(enabled);
	}

	private record BorrowSnapshot(List<LoanInfo> borrowed, List<PendingLoanInfo> pending) {
	}
}
