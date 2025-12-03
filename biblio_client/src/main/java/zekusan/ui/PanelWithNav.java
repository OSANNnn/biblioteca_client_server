package zekusan.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import zekusan.interfaces.PanelLifecycle;

public class PanelWithNav extends JPanel implements PanelLifecycle {
	private static final long serialVersionUID = 1L;

	private final JComponent content;

	public PanelWithNav(JPanel content, JComponent bottomBar) {
		super(new BorderLayout());
		this.content = content;
		add(content, BorderLayout.CENTER);
		add(bottomBar, BorderLayout.SOUTH);
	}

	@Override
	public void onShow() {
		if (content instanceof PanelLifecycle lifecycle) {
			lifecycle.onShow();
		}
	}

	@Override
	public void onHide() {
		if (content instanceof PanelLifecycle lifecycle) {
			lifecycle.onHide();
		}
	}
}
