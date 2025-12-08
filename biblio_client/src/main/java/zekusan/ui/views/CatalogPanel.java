package zekusan.ui.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseWheelEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

import zekusan.comms.responses.CatalogoResponse;
import zekusan.enums.ItemType;
import zekusan.enums.Status;
import zekusan.interfaces.PanelLifecycle;
import zekusan.services.LibraryClient;

public class CatalogPanel extends JPanel implements PanelLifecycle {
    private static final long serialVersionUID = 1L;

    private final transient LibraryClient libraryClient;
    private final JComboBox<ItemType> categorySelect;
    private final JLabel statusLabel;
    private final JButton refreshButton;
    private final transient CatalogTable catalogTable;

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

        catalogTable = new CatalogTable(statusLabel);
        JScrollPane tableScroll = catalogTable.getScrollPane();

        ScrollablePage page = new ScrollablePage();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.insets = new Insets(0, 0, 8, 0);

        gbc.gridy = 0;
        page.add(controls, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        page.add(tableScroll, gbc);

        JScrollPane outerScroll = new JScrollPane(
                page,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        enableEdgeWheelPropagation(tableScroll, outerScroll);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        outerScroll.getVerticalScrollBar().setBlockIncrement(240);
        outerScroll.setBorder(BorderFactory.createEmptyBorder());
        outerScroll.getViewport().setOpaque(true);
        outerScroll.getViewport().setBackground(page.getBackground());

        add(outerScroll, BorderLayout.CENTER);

        catalogTable.configureColumnsForType((ItemType) categorySelect.getSelectedItem());

        refreshButton.addActionListener(e -> loadCatalog());
        categorySelect.addActionListener(e -> loadCatalog());
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
            catalogTable.clearRows();
            return;
        }

        refreshButton.setEnabled(false);
        categorySelect.setEnabled(false);
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
                categorySelect.setEnabled(true);
                try {
                    CatalogoResponse response = get();
                    if (response.getStatus() == Status.SUCCESS) {
                        catalogTable.clearRows();
                        catalogTable.configureColumnsForType(selected);
                        catalogTable.updateRows(response.getCatalogo(), selected);
                        statusLabel.setText("Catalogo aggiornato.");
                    } else {
                        statusLabel.setText("Errore dal server: " + response.getStatus());
                        catalogTable.clearRows();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Caricamento interrotto.");
                    catalogTable.clearRows();
                } catch (ExecutionException e) {
                    statusLabel.setText("Impossibile caricare il catalogo: " + e.getCause().getMessage());
                    catalogTable.clearRows();
                }
            }
        }.execute();
    }

    private void enableEdgeWheelPropagation(JScrollPane inner, JScrollPane outer) {
        inner.addMouseWheelListener(e -> {
            if (e.isShiftDown()) {
                return;
            }

            JScrollBar verticalBar = inner.getVerticalScrollBar();
            if (verticalBar == null || !verticalBar.isVisible()) {
                return;
            }

            BoundedRangeModel model = verticalBar.getModel();
            int value = model.getValue();
            int extent = model.getExtent();
            int min = model.getMinimum();
            int max = model.getMaximum();

            boolean atTop = value <= min;
            boolean atBottom = value + extent >= max;

            int direction = e.getWheelRotation() > 0 ? 1 : -1;
            boolean shouldBubbleUp = (direction < 0 && atTop) || (direction > 0 && atBottom);

            if (shouldBubbleUp) {
                MouseWheelEvent forwarded = new MouseWheelEvent(
                        outer,
                        e.getID(),
                        e.getWhen(),
                        e.getModifiersEx(),
                        e.getX(),
                        e.getY(),
                        e.getClickCount(),
                        e.isPopupTrigger(),
                        e.getScrollType(),
                        e.getScrollAmount(),
                        e.getWheelRotation());
                outer.dispatchEvent(forwarded);
                e.consume();
            }
        });
    }
}
