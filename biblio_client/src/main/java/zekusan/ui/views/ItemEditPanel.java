package zekusan.ui.views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import zekusan.enums.ItemType;
import zekusan.enums.Route;
import zekusan.interfaces.Navigator;
import zekusan.interfaces.PanelLifecycle;
import zekusan.models.items.CD;
import zekusan.models.items.Item;
import zekusan.models.items.Libro;
import zekusan.models.items.Rivista;
import zekusan.services.LibraryClient;

public class ItemEditPanel extends JPanel implements PanelLifecycle {
    private static final long serialVersionUID = 1L;

    private final transient LibraryClient libraryClient;
    private final transient Navigator navigator;

    private final List<FieldRow> dynamicRows = new ArrayList<>();
    private final JLabel headingLabel = new JLabel("Modifica elemento");
    private final JLabel statusLabel = new JLabel(" ");
    private ItemType type = ItemType.NONE;
    private Item baseItem;

    private final JTextField titoloField = new JTextField(22);
    private final JTextField quantitaField = new JTextField(12);
    private final JTextField autoreField = new JTextField(22);
    private final JTextField genereField = new JTextField(22);
    private final JTextField isbnField = new JTextField(22);
    private final JTextField artistaField = new JTextField(22);
    private final JTextField annoField = new JTextField(12);
    private final JTextField numeroField = new JTextField(12);

    private JPanel formContainer;

    public ItemEditPanel(LibraryClient libraryClient, Navigator navigator) {
        super(new GridBagLayout());
        this.libraryClient = libraryClient;
        this.navigator = navigator;

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        headingLabel.setFont(
            headingLabel.getFont().deriveFont(
                headingLabel.getFont().getSize2D() + 3f
            )
        );
        headingLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        formContainer = new JPanel(new GridBagLayout());

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton cancel = new JButton("Annulla");
        JButton save = new JButton("Salva");
        cancel.addActionListener(e -> {
            statusLabel.setText("Modifica annullata.");
            if (navigator != null) {
                navigator.back();
            }
        });
        save.addActionListener(e -> saveChanges());
        actions.add(cancel);
        actions.add(save);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        add(headingLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 12, 0);
        add(formContainer, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        add(actions, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(statusLabel, gbc);

        gbc.gridy = 4;
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
        this.type = item.getTipo() == null ? ItemType.NONE : item.getTipo();
        headingLabel.setText("Modifica " + (item.getTitolo() == null ? "" : item.getTitolo()));
        rebuildForm();
        loadValues(item);
        statusLabel.setText(" ");
    }

    @Override
    public void onHide() {
        // no-op
    }

    private void rebuildForm() {
        formContainer.removeAll();
        dynamicRows.clear();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(4, 4, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        addRow(form, gbc, "Titolo", titoloField);
        addRow(form, gbc, "Quantità", quantitaField);

        if (type == ItemType.LIBRO) {
            addRow(form, gbc, "Autore", autoreField);
            addRow(form, gbc, "Genere", genereField);
            addRow(form, gbc, "ISBN", isbnField);
        } else if (type == ItemType.CD) {
            addRow(form, gbc, "Artista", artistaField);
            addRow(form, gbc, "Genere", genereField);
        } else if (type == ItemType.RIVISTA) {
            addRow(form, gbc, "Anno", annoField);
            addRow(form, gbc, "Numero", numeroField);
        }

        GridBagConstraints containerGbc = new GridBagConstraints();
        containerGbc.gridx = 0;
        containerGbc.gridy = 0;
        containerGbc.weightx = 1.0;
        containerGbc.fill = GridBagConstraints.HORIZONTAL;
        formContainer.add(form, containerGbc);

        revalidate();
        repaint();
    }

    private void addRow(JPanel form, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(field, gbc);
        dynamicRows.add(new FieldRow(label, field));
        gbc.gridy++;
    }

    private void loadValues(Item item) {
        if (item == null) {
            return;
        }
        titoloField.setText(nullToEmpty(item.getTitolo()));
        quantitaField.setText(String.valueOf(item.getQuantita()));

        if (item instanceof Libro libro) {
            autoreField.setText(nullToEmpty(libro.getAutore()));
            genereField.setText(nullToEmpty(libro.getGenere()));
            isbnField.setText(nullToEmpty(libro.getIsbn()));
        } else if (item instanceof CD cd) {
            artistaField.setText(nullToEmpty(cd.getArtista()));
            genereField.setText(nullToEmpty(cd.getGenere()));
        } else if (item instanceof Rivista rivista) {
            annoField.setText(String.valueOf(rivista.getAnno()));
            numeroField.setText(String.valueOf(rivista.getNumero()));
        }
    }

    private void saveChanges() {
        Item updated = buildItem();
        statusLabel.setText("Aggiornamento in corso...");

        new javax.swing.SwingWorker<Item, Void>() {
            @Override
            protected Item doInBackground() throws Exception {
                return libraryClient.updateItem(updated);
            }

            @Override
            protected void done() {
                try {
                    Item saved = get();
                    statusLabel.setText("Elemento aggiornato.");
                    if (navigator != null) {
                        navigator.navigate(Route.LIBRARIAN_CATALOG);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Aggiornamento interrotto.");
                } catch (ExecutionException e) {
                    statusLabel.setText("Errore aggiornamento: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private Item buildItem() {
        Item result;
        if (type == ItemType.LIBRO) {
            Libro libro = new Libro();
            libro.setAutore(autoreField.getText().trim());
            libro.setGenere(genereField.getText().trim());
            libro.setIsbn(isbnField.getText().trim());
            result = libro;
        } else if (type == ItemType.CD) {
            CD cd = new CD();
            cd.setArtista(artistaField.getText().trim());
            cd.setGenere(genereField.getText().trim());
            result = cd;
        } else if (type == ItemType.RIVISTA) {
            Rivista rivista = new Rivista();
            rivista.setAnno(parseIntSafe(annoField.getText()));
            rivista.setNumero(parseIntSafe(numeroField.getText()));
            result = rivista;
        } else {
            result = new Item();
            result.setTipo(type);
        }

        if (baseItem != null) {
            result.setId(baseItem.getId());
            result.setTipo(type);
        }
        result.setTitolo(titoloField.getText().trim());
        result.setQuantita(parseIntSafe(quantitaField.getText()));
        return result;
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record FieldRow(String label, JTextField field) {
    }
}
