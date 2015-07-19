package com.automation.javauto.test;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 * Creates a simple panel for testing purposes.
 * 
 * @author henry.tejera
 *
 */
public class TestPanel extends JFrame {

    private static final long serialVersionUID = 1L;
    private String clickedButton;
    private int clickCount;

    /**
     * Create a new TestPanel instance.
     * 
     * @param width
     *            - The Width of the panel.
     * @param height
     *            - The Height of the panel.
     * @param x
     *            - x location.
     * @param y
     *            - y location.
     */
    public TestPanel(int width, int height, int x, int y) {
	setSize(width, height);
	setLocation(x, y);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	final JTextArea textArea = new JTextArea();

	textArea.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.NOBUTTON) {
		    textArea.setText("No button clicked...");
		    setClickedButton("NoButton");
		} else if (e.getButton() == MouseEvent.BUTTON1) {
		    textArea.setText("Button left clicked...");
		    setClickedButton("left");
		} else if (e.getButton() == MouseEvent.BUTTON2) {
		    textArea.setText("Button left middle...");
		    setClickedButton("middle");
		} else if (e.getButton() == MouseEvent.BUTTON3) {
		    textArea.setText("Button left right...");
		    setClickedButton("right");
		}
		setClickCount(getClickCount());
	    }
	});

	getContentPane().add(textArea);
	setVisible(true);
    }

    /**
     * @return the clickedButton
     */
    public String getClickedButton() {
	return clickedButton;
    }

    /**
     * @param clickedButton
     *            the clickedButton to set
     */
    public void setClickedButton(String clickedButton) {
	this.clickedButton = clickedButton;
    }

    /**
     * @return the clickCount
     */
    public int getClickCount() {
	return clickCount;
    }

    /**
     * @param clickCount
     *            the clickCount to set
     */
    public void setClickCount(int clickCount) {
	this.clickCount = clickCount;
    }
}