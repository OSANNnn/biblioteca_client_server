package zekusan.ui.views;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class NotFoundPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public NotFoundPanel() {
		super(new BorderLayout());
		add(new JLabel("La pagina richiesta non esiste.", SwingConstants.CENTER), BorderLayout.CENTER);
	}
}
