package com.gpxmanager.component;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Titre : Cave à vin
 * Description : Votre description
 * Copyright : Copyright (c) 2011
 * Société : Seb Informatique
 *
 * @author Sébastien Duché
 * @version 0.8
 * @since 23/05/21
 */

public final class MyPasswordLabel extends JLabel {

	private static final long serialVersionUID = 4972622436840497820L;
	private static final Font FONT = new Font("Arial", Font.PLAIN, 12);


	public MyPasswordLabel() {
		setFont(FONT);
	}

	private void hide(boolean visible) {
		if (visible) { 
			setText("");
		} else {
			setVisible(false);
		}
	}

	public void setText(String text, boolean autoHide) {
		setText(text, autoHide, 5000, true);
	}

	public void setText(String text, boolean autoHide, int delay, boolean visible) {
		super.setText(text);
		if (autoHide) {
			new Timer().schedule(
					new TimerTask() {
						@Override
						public void run() {
							SwingUtilities.invokeLater(() -> hide(visible));
						}
					},
					delay
			);
		}
	}
}
