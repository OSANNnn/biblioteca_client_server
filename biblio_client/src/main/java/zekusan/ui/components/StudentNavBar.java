package zekusan.ui.components;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import zekusan.interfaces.Navigator;
import zekusan.enums.Route;

public class StudentNavBar extends JPanel {
	private static final long serialVersionUID = 1L;

	public StudentNavBar(Navigator navigator) {
		setLayout(new GridLayout(1, 3, 8, 0));
		setBorder(new EmptyBorder(8, 8, 8, 8));

		JButton homeBtn = new JButton("Home");
		JButton catalogBtn = new JButton("Catalogo");
		JButton settingsBtn = new JButton("Impostazioni");

		homeBtn.addActionListener(e -> navigator.navigate(Route.STUDENT_DASHBOARD));
		catalogBtn.addActionListener(e -> navigator.navigate(Route.STUDENT_CATALOG));
		settingsBtn.addActionListener(e -> navigator.navigate(Route.STUDENT_SETTINGS));

		add(homeBtn);
		add(catalogBtn);
		add(settingsBtn);
	}
}
