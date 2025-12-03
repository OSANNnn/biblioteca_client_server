package zekusan.ui.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import zekusan.enums.ItemType;
import zekusan.enums.Status;
import zekusan.comms.responses.CatalogoResponse;
import zekusan.models.items.CD;
import zekusan.models.items.Item;
import zekusan.models.items.Libro;
import zekusan.models.items.Rivista;
import zekusan.services.LibraryClient;
import zekusan.interfaces.PanelLifecycle;

public class CatalogPanel extends JPanel implements PanelLifecycle {
	private static final long serialVersionUID = 1L;

	private final transient LibraryClient libraryClient;
	private final JComboBox<ItemType> categorySelect;
	private final JLabel statusLabel;
	private final JButton refreshButton;
	private final DefaultTableModel tableModel;

	public CatalogPanel(LibraryClient libraryClient) {
		super(new BorderLayout(8, 8));
		this.libraryClient = libraryClient;

		categorySelect = new JComboBox<>(new ItemType[] { ItemType.LIBRO, ItemType.CD, ItemType.RIVISTA });
		refreshButton = new JButton("Aggiorna catalogo");
		statusLabel = new JLabel(" ");

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
		controls.add(new JLabel("Categoria:"));
		controls.add(categorySelect);
		controls.add(refreshButton);
		controls.add(statusLabel);

		tableModel = new DefaultTableModel(new Object[] { "ID", "Titolo", "Quantita", "Tipo", "Dettagli" }, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable table = new JTable(tableModel);

		add(controls, BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);

		refreshButton.addActionListener(e -> loadCatalog());
	}

	@Override
	public void onShow() {
		loadCatalog();
	}

	@Override
	public void onHide() {
		// nothing to clean up
	}

	private void loadCatalog() {
		if (!libraryClient.isLoggedIn()) {
			statusLabel.setText("Non sei autenticato.");
			tableModel.setRowCount(0);
			return;
		}

		refreshButton.setEnabled(false);
		statusLabel.setText("Caricamento in corso...");

		ItemType selected = (ItemType) categorySelect.getSelectedItem();

		new SwingWorker<CatalogoResponse, Void>() {
			@Override
			protected CatalogoResponse doInBackground() throws Exception {
				return libraryClient.loadCatalog(selected);
			}

			@Override
			protected void done() {
				refreshButton.setEnabled(true);
				try {
					CatalogoResponse response = get();
					if (response.getStatus() == Status.SUCCESS) {
						updateTable(response.getCatalogo());
						statusLabel.setText("Catalogo aggiornato.");
					} else {
						statusLabel.setText("Errore dal server: " + response.getStatus());
						tableModel.setRowCount(0);
					}
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					statusLabel.setText("Caricamento interrotto.");
					tableModel.setRowCount(0);
				} catch (ExecutionException e) {
					statusLabel.setText("Impossibile caricare il catalogo: " + e.getCause().getMessage());
					tableModel.setRowCount(0);
				}
			}
		}.execute();
	}

	private void updateTable(List<Item> items) {
		tableModel.setRowCount(0);
		if (items == null) {
			return;
		}

		for (Item item : items) {
			tableModel.addRow(new Object[] {
					item.getId(),
					item.getTitolo(),
					item.getQuantita(),
					item.getTipo(),
					describe(item)
			});
		}
	}

	private String describe(Item item) {
		if (item instanceof Libro libro) {
			return "Autore: " + nullToEmpty(libro.getAutore()) + ", Genere: " + nullToEmpty(libro.getGenere())
					+ ", ISBN: " + nullToEmpty(libro.getIsbn());
		}
		if (item instanceof CD cd) {
			return "Artista: " + nullToEmpty(cd.getArtista()) + ", Genere: " + nullToEmpty(cd.getGenere());
		}
		if (item instanceof Rivista rivista) {
			return "Anno: " + rivista.getAnno() + ", Numero: " + rivista.getNumero();
		}
		return "";
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
