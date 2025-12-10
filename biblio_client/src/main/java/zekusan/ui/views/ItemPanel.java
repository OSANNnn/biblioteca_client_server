package zekusan.ui.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import zekusan.enums.ItemType;
import zekusan.enums.Route;
import zekusan.interfaces.Navigator;
import zekusan.interfaces.PanelLifecycle;
import zekusan.models.items.CD;
import zekusan.models.items.Item;
import zekusan.models.items.Libro;
import zekusan.models.items.Rivista;
import zekusan.services.LibraryClient;

public class ItemPanel extends JPanel implements PanelLifecycle {
	private static final long serialVersionUID = 1L;

	private static final int LABEL_COLUMN_WIDTH = 90;

	private final transient LibraryClient libraryClient;
	private final transient Navigator navigator;

	private final JLabel headingLabel = new JLabel("Modifica elemento");
	private final JLabel statusLabel = new JLabel(" ");
	private final JLabel typeValueLabel = new JLabel("-");
	private final JTextField titoloField = new JTextField(22);
	private final JSpinner quantitaSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

	private final JPanel extraCardHost = new JPanel(new CardLayout());
	private final Map<ItemType, ItemDetailForm<? extends Item>> detailForms = new EnumMap<>(ItemType.class);
	private final ItemDetailForm<Item> emptyForm = new EmptyDetailForm();

	private final JButton cancelButton = new JButton("Annulla");
	private final JButton saveButton = new JButton("Salva");

	private ItemType type = ItemType.NONE;
	private Item baseItem;

	public ItemPanel(LibraryClient libraryClient, Navigator navigator) {
		super(new GridBagLayout());
		this.libraryClient = libraryClient;
		this.navigator = navigator;

		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		headingLabel.setFont(
				headingLabel.getFont().deriveFont(
						headingLabel.getFont().getSize2D() + 3f));
		headingLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		registerForm(ItemType.LIBRO, new LibroForm());
		registerForm(ItemType.CD, new CdForm());
		registerForm(ItemType.RIVISTA, new RivistaForm());
		extraCardHost.add(emptyForm.getComponent(), ItemType.NONE.name());

		cancelButton.addActionListener(e -> handleCancel());
		saveButton.addActionListener(e -> saveChanges());

		JPanel baseForm = buildBaseForm();

		JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
		actions.add(cancelButton);
		actions.add(saveButton);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 8, 0);
		add(headingLabel, gbc);

		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 8, 0);
		add(baseForm, gbc);

		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 8, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0;
		add(extraCardHost, gbc);

		gbc.gridy = 3;
		gbc.insets = new Insets(6, 0, 4, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(actions, gbc);

		gbc.gridy = 4;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(statusLabel, gbc);

		gbc.gridy = 5;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(Box.createVerticalGlue(), gbc);
	}

	@Override
	public void onShow() {
		Item item = libraryClient.consumePendingEditItem();
		if (item == null) {
			statusLabel.setText("Nessun elemento da modificare.");
			if (navigator != null) {
				navigator.navigate(Route.LIBRARIAN_CATALOG);
			}
			return;
		}
		this.baseItem = item;
		this.type = resolveType(item);
		typeValueLabel.setText(typeLabel(type));
		headingLabel.setText(item.getId() > 0
				? "Modifica " + nullToEmpty(item.getTitolo())
				: "Nuovo " + typeLabel(type).toLowerCase());
		loadValues(item);
		statusLabel.setText(" ");
	}

	@Override
	public void onHide() {
		// no-op
	}

	private void loadValues(Item item) {
		titoloField.setText(nullToEmpty(item.getTitolo()));
		int qty = item.getQuantita();
		if (qty < 0) {
			qty = 0;
		}
		quantitaSpinner.setValue(qty);

		showFormFor(type);
		ItemDetailForm<? extends Item> form = detailForms.getOrDefault(type, emptyForm);
		bindIntoForm(form, item);
	}

	private void saveChanges() {
		Item updated = buildItem();
		boolean isNew = updated.getId() <= 0;
		statusLabel.setText(isNew ? "Creazione in corso..." : "Aggiornamento in corso...");
		saveButton.setEnabled(false);
		cancelButton.setEnabled(false);

		new SwingWorker<Item, Void>() {
			@Override
			protected Item doInBackground() throws Exception {
				return libraryClient.saveItem(updated);
			}

			@Override
			protected void done() {
				saveButton.setEnabled(true);
				cancelButton.setEnabled(true);
				try {
					get();
					statusLabel.setText(isNew ? "Elemento creato." : "Elemento aggiornato.");
					if (navigator != null) {
						navigator.navigate(Route.LIBRARIAN_CATALOG);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					statusLabel.setText("Operazione interrotta.");
				} catch (ExecutionException e) {
					statusLabel.setText("Errore salvataggio: " + e.getCause().getMessage());
				}
			}
		}.execute();
	}

	private void handleCancel() {
		statusLabel.setText("Modifica annullata.");
		if (navigator != null) {
			if (navigator != null) {
				navigator.back();
			}
		}
	}

	private Item buildItem() {
		Item target = newItemForType(type);
		if (baseItem != null && baseItem.getId() > 0) {
			target.setId(baseItem.getId());
		}
		target.setTipo(type);
		target.setTitolo(titoloField.getText().trim());
		target.setQuantita(((Number) quantitaSpinner.getValue()).intValue());

		ItemDetailForm<? extends Item> form = detailForms.getOrDefault(type, emptyForm);
		applyFromForm(form, target);
		return target;
	}

	private static JLabel createFormLabel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		Dimension pref = label.getPreferredSize();
		pref = new Dimension(LABEL_COLUMN_WIDTH, pref.height);
		label.setPreferredSize(pref);
		return label;
	}

	private JPanel buildBaseForm() {
		JPanel base = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(4, 0, 2, 0);
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Titolo
		gbc.weightx = 0;
		base.add(createFormLabel("Titolo"), gbc);
		gbc.gridy++;
		gbc.weightx = 1.0;
		base.add(titoloField, gbc);

		// Quantità
		gbc.gridy++;
		gbc.weightx = 0;
		base.add(createFormLabel("Quantità"), gbc);
		gbc.gridy++;
		gbc.weightx = 1.0;
		base.add(quantitaSpinner, gbc);

		// Tipo
		gbc.gridy++;
		gbc.weightx = 0;
		base.add(createFormLabel("Tipo"), gbc);
		gbc.gridy++;
		gbc.weightx = 1.0;
		typeValueLabel.setFont(typeValueLabel.getFont()
				.deriveFont(typeValueLabel.getFont().getStyle() | java.awt.Font.BOLD));
		base.add(typeValueLabel, gbc);

		return base;
	}

	private void registerForm(ItemType type, ItemDetailForm<? extends Item> form) {
		detailForms.put(type, form);
		extraCardHost.add(form.getComponent(), type.name());
	}

	private void showFormFor(ItemType type) {
		CardLayout layout = (CardLayout) extraCardHost.getLayout();
		layout.show(extraCardHost, detailForms.containsKey(type) ? type.name() : ItemType.NONE.name());
	}

	private ItemType resolveType(Item item) {
		if (item == null) {
			return ItemType.NONE;
		}
		if (item.getTipo() != null && item.getTipo() != ItemType.NONE) {
			return item.getTipo();
		}
		if (item instanceof Libro) {
			return ItemType.LIBRO;
		}
		if (item instanceof CD) {
			return ItemType.CD;
		}
		if (item instanceof Rivista) {
			return ItemType.RIVISTA;
		}
		return ItemType.NONE;
	}

	private String typeLabel(ItemType type) {
		return switch (type) {
		case LIBRO -> "Libro";
		case CD -> "CD";
		case RIVISTA -> "Rivista";
		default -> "Nessuna categoria";
		};
	}

	private Item newItemForType(ItemType type) {
		return switch (type) {
		case LIBRO -> new Libro();
		case CD -> new CD();
		case RIVISTA -> new Rivista();
		default -> new Item();
		};
	}

	@SuppressWarnings("unchecked")
	private void bindIntoForm(ItemDetailForm<? extends Item> form, Item item) {
		try {
			((ItemDetailForm<Item>) form).bindFrom(item);
		} catch (ClassCastException ignored) {
			// ignore mismatched form
		}
	}

	@SuppressWarnings("unchecked")
	private void applyFromForm(ItemDetailForm<? extends Item> form, Item item) {
		try {
			((ItemDetailForm<Item>) form).applyTo(item);
		} catch (ClassCastException ignored) {
			// ignore mismatched form
		}
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private interface ItemDetailForm<T extends Item> {
		void bindFrom(T item);

		void applyTo(T item);

		JComponent getComponent();
	}

	/**
	 * LibroForm: now uses vertical Label/Input layout for each field.
	 */
	private static class LibroForm extends JPanel implements ItemDetailForm<Libro> {
		private static final long serialVersionUID = 1L;

		private final JTextField autoreField = new JTextField(22);
		private final JTextField genereField = new JTextField(22);
		private final JTextField isbnField = new JTextField(22);

		LibroForm() {
			super(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(4, 0, 2, 0);
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			// Autore
			gbc.weightx = 0;
			add(createFormLabel("Autore"), gbc);
			gbc.gridy++;
			gbc.weightx = 1.0;
			add(autoreField, gbc);

			// Genere
			gbc.gridy++;
			gbc.weightx = 0;
			add(createFormLabel("Genere"), gbc);
			gbc.gridy++;
			gbc.weightx = 1.0;
			add(genereField, gbc);

			// ISBN
			gbc.gridy++;
			gbc.weightx = 0;
			add(createFormLabel("ISBN"), gbc);
			gbc.gridy++;
			gbc.weightx = 1.0;
			add(isbnField, gbc);
		}

		@Override
		public void bindFrom(Libro item) {
			autoreField.setText(item == null ? "" : nullToEmpty(item.getAutore()));
			genereField.setText(item == null ? "" : nullToEmpty(item.getGenere()));
			isbnField.setText(item == null ? "" : nullToEmpty(item.getIsbn()));
		}

		@Override
		public void applyTo(Libro item) {
			item.setAutore(autoreField.getText().trim());
			item.setGenere(genereField.getText().trim());
			item.setIsbn(isbnField.getText().trim());
		}

		@Override
		public JComponent getComponent() {
			return this;
		}
	}

	/**
	 * CdForm: vertical Label/Input layout.
	 */
	private static class CdForm extends JPanel implements ItemDetailForm<CD> {
		private static final long serialVersionUID = 1L;

		private final JTextField artistaField = new JTextField(22);
		private final JTextField genereField = new JTextField(22);

		CdForm() {
			super(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(4, 0, 2, 0);
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			// Artista
			gbc.weightx = 0;
			add(createFormLabel("Artista"), gbc);
			gbc.gridy++;
			gbc.weightx = 1.0;
			add(artistaField, gbc);

			// Genere
			gbc.gridy++;
			gbc.weightx = 0;
			add(createFormLabel("Genere"), gbc);
			gbc.gridy++;
			gbc.weightx = 1.0;
			add(genereField, gbc);
		}

		@Override
		public void bindFrom(CD item) {
			artistaField.setText(item == null ? "" : nullToEmpty(item.getArtista()));
			genereField.setText(item == null ? "" : nullToEmpty(item.getGenere()));
		}

		@Override
		public void applyTo(CD item) {
			item.setArtista(artistaField.getText().trim());
			item.setGenere(genereField.getText().trim());
		}

		@Override
		public JComponent getComponent() {
			return this;
		}
	}

	/**
	 * RivistaForm: vertical Label/Input layout.
	 */
	private static class RivistaForm extends JPanel implements ItemDetailForm<Rivista> {
		private static final long serialVersionUID = 1L;

		private final JSpinner annoSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		private final JSpinner numeroSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

		RivistaForm() {
			super(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.insets = new Insets(4, 0, 2, 0);
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			// Anno
			gbc.weightx = 0;
			add(createFormLabel("Anno"), gbc);
			gbc.gridy++;
			gbc.weightx = 1.0;
			add(annoSpinner, gbc);

			// Numero
			gbc.gridy++;
			gbc.weightx = 0;
			add(createFormLabel("Numero"), gbc);
			gbc.gridy++;
			gbc.weightx = 1.0;
			add(numeroSpinner, gbc);
		}

		@Override
		public void bindFrom(Rivista item) {
			annoSpinner.setValue(item == null ? 0 : Math.max(0, item.getAnno()));
			numeroSpinner.setValue(item == null ? 0 : Math.max(0, item.getNumero()));
		}

		@Override
		public void applyTo(Rivista item) {
			item.setAnno(((Number) annoSpinner.getValue()).intValue());
			item.setNumero(((Number) numeroSpinner.getValue()).intValue());
		}

		@Override
		public JComponent getComponent() {
			return this;
		}
	}

	private static class EmptyDetailForm extends JPanel implements ItemDetailForm<Item> {
		private static final long serialVersionUID = 1L;

		EmptyDetailForm() {
			super(new BorderLayout());
			setBorder(new EmptyBorder(0, 0, 0, 0));
			// no label -> visually seamless when no extra details
		}

		@Override
		public void bindFrom(Item item) {
			// nothing to bind
		}

		@Override
		public void applyTo(Item item) {
			// nothing to apply
		}

		@Override
		public JComponent getComponent() {
			return this;
		}
	}
}
