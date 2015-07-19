package com.automation.javauto;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * 
 * Purpose: Javauto can emulate keyboard/mouse input and perform other functions
 * similar to the Windows automation language AutoIt, however, being written in
 * java, Javauto is cross platform.
 * 
 * @author matthew.downey
 */
public class Javauto {
    /*
     * Define the environmental constants & class variables to be made available
     * for convenient access
     */

    /**
     * The directory the program is being executed from.
     */
    public final String WORKING_DIR = System.getProperty("user.dir");

    /**
     * The temporary directory on the system.
     */
    public final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * The file separator on the system, usually '/' on *nix based systems and
     * '\' on Windows.
     */
    public final String FILE_SEP = File.separator;

    /**
     * The width of the screen in pixels.
     */
    public final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;

    /**
     * The height of the screen in pixels.
     */
    public final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit()
	    .getScreenSize().height;

    /**
     * The operating system in use.
     */
    public final String SYSTEM_OS = System.getProperty("os.name");

    /**
     * The user name the program is being run under.
     */
    public final String USER_NAME = System.getProperty("user.name");

    /**
     * The path to the user's home directory.
     */
    public final String USER_HOME_DIR = System.getProperty("user.home");

    /**
     * The speed (0.00-1.00) at which events occur. This includes mouse clicks,
     * mouse click-and-drags, mouse movements, and simulated keyboard input.
     */
    private double simulatedMotionSpeed = 0.95D;

    /*
     * Define the public functions to be offered by the library
     */

    /**
     * Get the current speed. The speed controls how quickly mouse and keyboard
     * actions happen. The default speed is .95 (95%). A speed of 1.00 (100%)
     * makes mouse and keyboard actions instant.
     * 
     * @return The speed as a decimal from 0.00 to 1.00.
     */
    public double getSpeed() {
	return simulatedMotionSpeed;
    }

    /**
     * Set the current speed. The speed controls how quickly mouse and keyboard
     * actions happen. The default speed is .95 (95%). A speed of 1.00 (100%)
     * makes mouse and keyboard actions instant.
     * 
     * @param spd
     *            The new speed as a double. This value must be between 0.00 and
     *            1.00, if it isn't no action will be taken. A speed of 1.00
     *            will make mouse and keyboard actions instant.
     */
    public void setSpeed(double spd) {
	if (spd > 1.0D) {
	    simulatedMotionSpeed = 1.0D;
	} else if (Double.compare(spd, 0.0) < 0) {
	    simulatedMotionSpeed = 0.0D;
	} else {
	    simulatedMotionSpeed = spd;
	}
    }

    /**
     * Simulate keyboard input by "typing" the contents of str. Special keys can
     * also be typed. The speed at which this typing occurs can be set with
     * {@link setSpeed}.
     * 
     * @param str
     *            The string to type, one letter (or special key) at a time.
     */
    public void send(String str) {
	/* replace special characters like {CTRL} with unicode place holders */
	String s = handleSpecialChars(str);

	/* iterate through the charcters in the string and simulate each one */
	for (int i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    sendChar(c);
	    if (simulatedMotionSpeed < .945D)
		sleep((int) (400 - (400 * (simulatedMotionSpeed + .05D))));
	}
    }

    /**
     * Hold down a key until {@link keyUp} is called on the same key. If keyUp
     * is not called the key will become "stuck." Special keys can also be used
     * here.
     * 
     * @param str
     *            The key that will be held down. If there are multiple letters
     *            or keys specified only the first will be held down.
     */
    public void keyDown(String str) {
	/* replace special characters like {CTRL} with unicode place holders */
	String s = handleSpecialChars(str);

	/* if there is more than one key specified just use the first */
	char c = s.charAt(0);

	/* try to recognize the key and press it down */
	try {
	    Robot r = new Robot();
	    switch (c) {
	    case '\u0259':
		r.keyPress(KeyEvent.VK_WINDOWS);
		break;
	    case '\u025A':
		r.keyPress(KeyEvent.VK_SHIFT);
		break;
	    case '\u025B':
		r.keyPress(KeyEvent.VK_DELETE);
		break;
	    case '\u025C':
		r.keyPress(KeyEvent.VK_CONTROL);
		break;
	    case '\u025D':
		r.keyPress(KeyEvent.VK_ALT);
		break;
	    case '\u025E':
		r.keyPress(KeyEvent.VK_ESCAPE);
		break;
	    case '\u025F':
		r.keyPress(KeyEvent.VK_META);
		break;
	    case '\u1E90':
		r.keyPress(KeyEvent.VK_F1);
		break;
	    case '\u1E91':
		r.keyPress(KeyEvent.VK_F2);
		break;
	    case '\u1E92':
		r.keyPress(KeyEvent.VK_F3);
		break;
	    case '\u1E93':
		r.keyPress(KeyEvent.VK_F4);
		break;
	    case '\u1E94':
		r.keyPress(KeyEvent.VK_F5);
		break;
	    case '\u1E95':
		r.keyPress(KeyEvent.VK_F6);
		break;
	    case '\u1E96':
		r.keyPress(KeyEvent.VK_F7);
		break;
	    case '\u1E97':
		r.keyPress(KeyEvent.VK_F8);
		break;
	    case '\u1E98':
		r.keyPress(KeyEvent.VK_F9);
		break;
	    case '\u1E99':
		r.keyPress(KeyEvent.VK_F10);
		break;
	    case '\u1E9A':
		r.keyPress(KeyEvent.VK_F11);
		break;
	    case '\u1E9B':
		r.keyPress(KeyEvent.VK_F12);
		break;
	    case '\u1E9C':
		r.keyPress(KeyEvent.VK_F13);
		break;
	    case '\u1E9D':
		r.keyPress(KeyEvent.VK_F14);
		break;
	    case '\u1E9E':
		r.keyPress(KeyEvent.VK_F15);
		break;
	    case '\u1E9F':
		r.keyPress(KeyEvent.VK_F16);
		break;
	    case '\u1EA0':
		r.keyPress(KeyEvent.VK_F17);
		break;
	    case '\u1EA1':
		r.keyPress(KeyEvent.VK_F18);
		break;
	    case '\u1EA2':
		r.keyPress(KeyEvent.VK_F19);
		break;
	    case '\u1EA3':
		r.keyPress(KeyEvent.VK_F20);
		break;
	    case '\u1EA4':
		r.keyPress(KeyEvent.VK_F21);
		break;
	    case '\u1EA5':
		r.keyPress(KeyEvent.VK_F22);
		break;
	    case '\u1EA6':
		r.keyPress(KeyEvent.VK_F23);
		break;
	    case '\u1EA7':
		r.keyPress(KeyEvent.VK_F24);
		break;
	    case '\u1EA8':
		r.keyPress(KeyEvent.VK_HOME);
		break;
	    case '\u1EA9':
		r.keyPress(KeyEvent.VK_END);
		break;
	    case '\u2C00':
		r.keyPress(KeyEvent.VK_LEFT);
		break;
	    case '\u2C01':
		r.keyPress(KeyEvent.VK_RIGHT);
		break;
	    case '\u2C02':
		r.keyPress(KeyEvent.VK_UP);
		break;
	    case '\u2C03':
		r.keyPress(KeyEvent.VK_DOWN);
		break;
	    case '\u2C04':
		r.keyPress(KeyEvent.VK_CAPS_LOCK);
		break;
	    case 'a':
		r.keyPress(KeyEvent.VK_A);
		break;
	    case 'b':
		r.keyPress(KeyEvent.VK_B);
		break;
	    case 'c':
		r.keyPress(KeyEvent.VK_C);
		break;
	    case 'd':
		r.keyPress(KeyEvent.VK_D);
		break;
	    case 'e':
		r.keyPress(KeyEvent.VK_E);
		break;
	    case 'f':
		r.keyPress(KeyEvent.VK_F);
		break;
	    case 'g':
		r.keyPress(KeyEvent.VK_G);
		break;
	    case 'h':
		r.keyPress(KeyEvent.VK_H);
		break;
	    case 'i':
		r.keyPress(KeyEvent.VK_I);
		break;
	    case 'j':
		r.keyPress(KeyEvent.VK_J);
		break;
	    case 'k':
		r.keyPress(KeyEvent.VK_K);
		break;
	    case 'l':
		r.keyPress(KeyEvent.VK_L);
		break;
	    case 'm':
		r.keyPress(KeyEvent.VK_M);
		break;
	    case 'n':
		r.keyPress(KeyEvent.VK_N);
		break;
	    case 'o':
		r.keyPress(KeyEvent.VK_O);
		break;
	    case 'p':
		r.keyPress(KeyEvent.VK_P);
		break;
	    case 'q':
		r.keyPress(KeyEvent.VK_Q);
		break;
	    case 'r':
		r.keyPress(KeyEvent.VK_R);
		break;
	    case 's':
		r.keyPress(KeyEvent.VK_S);
		break;
	    case 't':
		r.keyPress(KeyEvent.VK_T);
		break;
	    case 'u':
		r.keyPress(KeyEvent.VK_U);
		break;
	    case 'v':
		r.keyPress(KeyEvent.VK_V);
		break;
	    case 'w':
		r.keyPress(KeyEvent.VK_W);
		break;
	    case 'x':
		r.keyPress(KeyEvent.VK_X);
		break;
	    case 'y':
		r.keyPress(KeyEvent.VK_Y);
		break;
	    case 'z':
		r.keyPress(KeyEvent.VK_Z);
		break;
	    case '`':
		r.keyPress(KeyEvent.VK_BACK_QUOTE);
		break;
	    case '0':
		r.keyPress(KeyEvent.VK_0);
		break;
	    case '1':
		r.keyPress(KeyEvent.VK_1);
		break;
	    case '2':
		r.keyPress(KeyEvent.VK_2);
		break;
	    case '3':
		r.keyPress(KeyEvent.VK_3);
		break;
	    case '4':
		r.keyPress(KeyEvent.VK_4);
		break;
	    case '5':
		r.keyPress(KeyEvent.VK_5);
		break;
	    case '6':
		r.keyPress(KeyEvent.VK_6);
		break;
	    case '7':
		r.keyPress(KeyEvent.VK_7);
		break;
	    case '8':
		r.keyPress(KeyEvent.VK_8);
		break;
	    case '9':
		r.keyPress(KeyEvent.VK_9);
		break;
	    case '-':
		r.keyPress(KeyEvent.VK_MINUS);
		break;
	    case '=':
		r.keyPress(KeyEvent.VK_EQUALS);
		break;
	    case '\t':
		r.keyPress(KeyEvent.VK_TAB);
		break;
	    case '\n':
		r.keyPress(KeyEvent.VK_ENTER);
		break;
	    case '[':
		r.keyPress(KeyEvent.VK_OPEN_BRACKET);
		break;
	    case ']':
		r.keyPress(KeyEvent.VK_CLOSE_BRACKET);
		break;
	    case '\\':
		r.keyPress(KeyEvent.VK_BACK_SLASH);
		break;
	    case ';':
		r.keyPress(KeyEvent.VK_SEMICOLON);
		break;
	    case '\'':
		r.keyPress(KeyEvent.VK_QUOTE);
		break;
	    case ',':
		r.keyPress(KeyEvent.VK_COMMA);
		break;
	    case '.':
		r.keyPress(KeyEvent.VK_PERIOD);
		break;
	    case '/':
		r.keyPress(KeyEvent.VK_SLASH);
		break;
	    case ' ':
		r.keyPress(KeyEvent.VK_SPACE);
		break;
	    case '\b':
		r.keyPress(KeyEvent.VK_BACK_SPACE);
		break;
	    default:
		throw new IllegalArgumentException("Cannot type " + s);
	    }
	} catch (AWTException e) { // throw an error
	    throw new RuntimeException(e);
	}
    }

    /**
     * Release a key that has been held down. Special keys can also be used
     * here.
     * 
     * @param str
     *            The single key that will be released. If more than one key is
     *            specified only the first will be released.
     */
    public void keyUp(String str) {
	/* replace special characters like {CTRL} with unicode place holders */
	String s = handleSpecialChars(str);

	/* if there is more than one key specified just use the first */
	char c = s.charAt(0);

	/* try to recognize the key and un-press it */
	try {
	    Robot r = new Robot();
	    switch (c) {
	    case '\u0259':
		r.keyRelease(KeyEvent.VK_WINDOWS);
		break;
	    case '\u025A':
		r.keyRelease(KeyEvent.VK_SHIFT);
		break;
	    case '\u025B':
		r.keyRelease(KeyEvent.VK_DELETE);
		break;
	    case '\u025C':
		r.keyRelease(KeyEvent.VK_CONTROL);
		break;
	    case '\u025D':
		r.keyRelease(KeyEvent.VK_ALT);
		break;
	    case '\u025E':
		r.keyRelease(KeyEvent.VK_ESCAPE);
		break;
	    case '\u025F':
		r.keyRelease(KeyEvent.VK_META);
		break;
	    case '\u1E90':
		r.keyRelease(KeyEvent.VK_F1);
		break;
	    case '\u1E91':
		r.keyRelease(KeyEvent.VK_F2);
		break;
	    case '\u1E92':
		r.keyRelease(KeyEvent.VK_F3);
		break;
	    case '\u1E93':
		r.keyRelease(KeyEvent.VK_F4);
		break;
	    case '\u1E94':
		r.keyRelease(KeyEvent.VK_F5);
		break;
	    case '\u1E95':
		r.keyRelease(KeyEvent.VK_F6);
		break;
	    case '\u1E96':
		r.keyRelease(KeyEvent.VK_F7);
		break;
	    case '\u1E97':
		r.keyRelease(KeyEvent.VK_F8);
		break;
	    case '\u1E98':
		r.keyRelease(KeyEvent.VK_F9);
		break;
	    case '\u1E99':
		r.keyRelease(KeyEvent.VK_F10);
		break;
	    case '\u1E9A':
		r.keyRelease(KeyEvent.VK_F11);
		break;
	    case '\u1E9B':
		r.keyRelease(KeyEvent.VK_F12);
		break;
	    case '\u1E9C':
		r.keyRelease(KeyEvent.VK_F13);
		break;
	    case '\u1E9D':
		r.keyRelease(KeyEvent.VK_F14);
		break;
	    case '\u1E9E':
		r.keyRelease(KeyEvent.VK_F15);
		break;
	    case '\u1E9F':
		r.keyRelease(KeyEvent.VK_F16);
		break;
	    case '\u1EA0':
		r.keyRelease(KeyEvent.VK_F17);
		break;
	    case '\u1EA1':
		r.keyRelease(KeyEvent.VK_F18);
		break;
	    case '\u1EA2':
		r.keyRelease(KeyEvent.VK_F19);
		break;
	    case '\u1EA3':
		r.keyRelease(KeyEvent.VK_F20);
		break;
	    case '\u1EA4':
		r.keyRelease(KeyEvent.VK_F21);
		break;
	    case '\u1EA5':
		r.keyRelease(KeyEvent.VK_F22);
		break;
	    case '\u1EA6':
		r.keyRelease(KeyEvent.VK_F23);
		break;
	    case '\u1EA7':
		r.keyRelease(KeyEvent.VK_F24);
		break;
	    case '\u1EA8':
		r.keyRelease(KeyEvent.VK_HOME);
		break;
	    case '\u1EA9':
		r.keyRelease(KeyEvent.VK_END);
		break;
	    case '\u2C00':
		r.keyRelease(KeyEvent.VK_LEFT);
		break;
	    case '\u2C01':
		r.keyRelease(KeyEvent.VK_RIGHT);
		break;
	    case '\u2C02':
		r.keyRelease(KeyEvent.VK_UP);
		break;
	    case '\u2C03':
		r.keyRelease(KeyEvent.VK_DOWN);
		break;
	    case '\u2C04':
		r.keyRelease(KeyEvent.VK_CAPS_LOCK);
		break;
	    case 'a':
		r.keyRelease(KeyEvent.VK_A);
		break;
	    case 'b':
		r.keyRelease(KeyEvent.VK_B);
		break;
	    case 'c':
		r.keyRelease(KeyEvent.VK_C);
		break;
	    case 'd':
		r.keyRelease(KeyEvent.VK_D);
		break;
	    case 'e':
		r.keyRelease(KeyEvent.VK_E);
		break;
	    case 'f':
		r.keyRelease(KeyEvent.VK_F);
		break;
	    case 'g':
		r.keyRelease(KeyEvent.VK_G);
		break;
	    case 'h':
		r.keyRelease(KeyEvent.VK_H);
		break;
	    case 'i':
		r.keyRelease(KeyEvent.VK_I);
		break;
	    case 'j':
		r.keyRelease(KeyEvent.VK_J);
		break;
	    case 'k':
		r.keyRelease(KeyEvent.VK_K);
		break;
	    case 'l':
		r.keyRelease(KeyEvent.VK_L);
		break;
	    case 'm':
		r.keyRelease(KeyEvent.VK_M);
		break;
	    case 'n':
		r.keyRelease(KeyEvent.VK_N);
		break;
	    case 'o':
		r.keyRelease(KeyEvent.VK_O);
		break;
	    case 'p':
		r.keyRelease(KeyEvent.VK_P);
		break;
	    case 'q':
		r.keyRelease(KeyEvent.VK_Q);
		break;
	    case 'r':
		r.keyRelease(KeyEvent.VK_R);
		break;
	    case 's':
		r.keyRelease(KeyEvent.VK_S);
		break;
	    case 't':
		r.keyRelease(KeyEvent.VK_T);
		break;
	    case 'u':
		r.keyRelease(KeyEvent.VK_U);
		break;
	    case 'v':
		r.keyRelease(KeyEvent.VK_V);
		break;
	    case 'w':
		r.keyRelease(KeyEvent.VK_W);
		break;
	    case 'x':
		r.keyRelease(KeyEvent.VK_X);
		break;
	    case 'y':
		r.keyRelease(KeyEvent.VK_Y);
		break;
	    case 'z':
		r.keyRelease(KeyEvent.VK_Z);
		break;
	    case '`':
		r.keyRelease(KeyEvent.VK_BACK_QUOTE);
		break;
	    case '0':
		r.keyRelease(KeyEvent.VK_0);
		break;
	    case '1':
		r.keyRelease(KeyEvent.VK_1);
		break;
	    case '2':
		r.keyRelease(KeyEvent.VK_2);
		break;
	    case '3':
		r.keyRelease(KeyEvent.VK_3);
		break;
	    case '4':
		r.keyRelease(KeyEvent.VK_4);
		break;
	    case '5':
		r.keyRelease(KeyEvent.VK_5);
		break;
	    case '6':
		r.keyRelease(KeyEvent.VK_6);
		break;
	    case '7':
		r.keyRelease(KeyEvent.VK_7);
		break;
	    case '8':
		r.keyRelease(KeyEvent.VK_8);
		break;
	    case '9':
		r.keyRelease(KeyEvent.VK_9);
		break;
	    case '-':
		r.keyRelease(KeyEvent.VK_MINUS);
		break;
	    case '=':
		r.keyRelease(KeyEvent.VK_EQUALS);
		break;
	    case '\t':
		r.keyRelease(KeyEvent.VK_TAB);
		break;
	    case '\n':
		r.keyRelease(KeyEvent.VK_ENTER);
		break;
	    case '[':
		r.keyRelease(KeyEvent.VK_OPEN_BRACKET);
		break;
	    case ']':
		r.keyRelease(KeyEvent.VK_CLOSE_BRACKET);
		break;
	    case '\\':
		r.keyRelease(KeyEvent.VK_BACK_SLASH);
		break;
	    case ';':
		r.keyRelease(KeyEvent.VK_SEMICOLON);
		break;
	    case '\'':
		r.keyRelease(KeyEvent.VK_QUOTE);
		break;
	    case '\"':
		r.keyRelease(KeyEvent.VK_QUOTEDBL);
		break;
	    case ',':
		r.keyRelease(KeyEvent.VK_COMMA);
		break;
	    case '.':
		r.keyRelease(KeyEvent.VK_PERIOD);
		break;
	    case '/':
		r.keyRelease(KeyEvent.VK_SLASH);
		break;
	    case ' ':
		r.keyRelease(KeyEvent.VK_SPACE);
		break;
	    case '\b':
		r.keyRelease(KeyEvent.VK_BACK_SPACE);
		break;
	    default:
		throw new IllegalArgumentException("Cannot type" + s);
	    }
	} catch (AWTException e) { // throw an error
	    throw new RuntimeException(e);
	}
    }

    /**
     * Simulate the press and release of a single key. Special keys can be used
     * here.
     * 
     * @param s
     *            The single key to press or release. If more than one key is
     *            specified only one key will be pressed.
     */
    public void keyPress(String s) {
	keyDown(s);
	keyUp(s);
    }

    /**
     * Hold down a key until {@link keyUp} is called on the same key. If keyUp
     * is not called the key will become "stuck." The helper can be used to find
     * the integer value of a key.
     * 
     * @param i
     *            Integer value of key to be pressed.
     */
    public void keyDown(int i) {
	try {
	    Robot r = new Robot();
	    r.keyPress(i);
	} catch (AWTException e) { // throw an error
	    throw new RuntimeException(e);
	}
    }

    /**
     * Release a pressed key. The helper can be used to find the integer value
     * of a key.
     * 
     * @param i
     *            Integer representation of the key that will be released.
     */
    public void keyUp(int i) {
	try {
	    Robot r = new Robot();
	    r.keyRelease(i);
	} catch (AWTException e) { // throw an error
	    throw new RuntimeException(e);
	}
    }

    /**
     * Simulate the press and release of a single key.
     * 
     * @param s
     *            The integer value of the key to press.
     */
    public void keyPress(int i) {
	keyDown(i);
	keyUp(i);
    }

    /**
     * Send a key combination such as Ctrl + Alt + Delete. The keys must be
     * passed in as a string array.
     * 
     * @param keys
     *            The key combination to be executed as a String array. The keys
     *            in this array must be in order and may contain special keys.
     *            The array can be of any size.
     */
    public void keyCombo(String... keys) {
	/* hold down each key except for the last */
	for (int i = 0; i < keys.length - 1; i++)
	    keyDown(keys[i]);

	/* press the last key */
	keyPress(keys[keys.length - 1]);

	/* unpress each key in reverse order */
	for (int i = keys.length - 2; i >= 0; i--)
	    keyUp(keys[i]);
    }

    /**
     * Send a key combination such as Ctrl + Alt + Delete.
     * 
     * @param keys
     *            The key combination to be executed as an int array. The keys
     *            in this array must be in order. The array can be of any size.
     */
    public void keyCombo(int... keys) {
	/* hold down each key except for the last */
	for (int i = 0; i < keys.length - 1; i++)
	    keyDown(keys[i]);

	/* press the last key */
	keyPress(keys[keys.length - 1]);

	/* unpress each key in reverse order */
	for (int i = keys.length - 2; i >= 0; i--)
	    keyUp(keys[i]);
    }

    /**
     * Delay execution for an amount of milliseconds (1000 milliseconds = 1
     * second).
     * 
     * @param milliseconds
     *            Time to delay; must be between 0 and 60,000ms. Only takes
     *            integer values.
     */
    public void sleep(int milliseconds) {
	try {
	    Robot r = new Robot();
	    r.delay(milliseconds);
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Move the mouse cursor to a certain position. The speed of the mouse
     * movement can be set with {@link setSpeed}. Note that mouse coordinates
     * can be found with the Javauto Helper.
     * 
     * @param xFinal
     *            The X value of the coordinate to move the mouse to.
     * @param yFinal
     *            The Y value of the coordinate to move the mouse to.
     */
    public void mouseMove(int xFinal, int yFinal) {

	try {
	    /*
	     * if they have speed set to highest (everything happens instantly)
	     * just move the mouse
	     */
	    if (simulatedMotionSpeed == 1) {
		new Robot().mouseMove(xFinal, yFinal);
		return;
	    }

	    /* calculate delay time in milliseconds based on overall speed */
	    int delay = (int) ((1 - simulatedMotionSpeed) * 100) / 4;

	    /* set how many pixels we want to move at a time */
	    final int pixelInterval = 7;

	    /* get starting position */
	    int xInitial = MouseInfo.getPointerInfo().getLocation().x;
	    int yInitial = MouseInfo.getPointerInfo().getLocation().y;

	    /* keep track of where we are */
	    int xCurrent = xInitial;
	    int yCurrent = yInitial;

	    /* keep track of where we'd like to be (as double) */
	    /*
	     * this ensures that adding decimal intervals repeatedly doesn't
	     * result in a loss of accuracy over time since we can always refer
	     * back to this ideal value and round from there to get the closest
	     * pixel
	     */
	    double xIdeal = (double) xCurrent;
	    double yIdeal = (double) yCurrent;

	    /* get the change that must occur in each direction */
	    int deltaX = xFinal - xInitial;
	    int deltaY = yFinal - yInitial;

	    /* get how many x pixels to move for each y and vice versa */
	    double xForEachY = 0;
	    if (deltaY != 0)
		xForEachY = (double) deltaX / deltaY;
	    double yForEachX = 0;
	    if (deltaX != 0)
		yForEachX = (double) deltaY / deltaX;

	    /* get the closest point that allows for our pixel interval */
	    int xClosest = xFinal;
	    int yClosest = yFinal;
	    if (deltaX > 0) {
		while ((xClosest - xInitial) % pixelInterval != 0)
		    xClosest = xClosest - 1;
	    } else {
		while ((xClosest - xInitial) % pixelInterval != 0)
		    xClosest = xClosest + 1;
	    }
	    if (deltaY > 0) {
		while ((yClosest - yInitial) % pixelInterval != 0)
		    yClosest = yClosest - 1;
	    } else {
		while ((yClosest - yInitial) % pixelInterval != 0)
		    yClosest = yClosest + 1;
	    }

	    /* initialize Robot that will make mouse movements */
	    Robot r = new Robot();

	    /* if x is larger than y, move x value in whole numbers */
	    if (Math.abs(deltaX) > Math.abs(deltaY)) {
		while (xCurrent != xClosest) {
		    if (deltaX < 0)
			xCurrent = xCurrent - pixelInterval;
		    else
			xCurrent = xCurrent + pixelInterval;
		    if (deltaY < 0)
			yIdeal = yIdeal - (Math.abs(yForEachX) * pixelInterval);
		    else
			yIdeal = yIdeal + (Math.abs(yForEachX) * pixelInterval);
		    yCurrent = (int) Math.round(yIdeal);
		    r.mouseMove(xCurrent, yCurrent);
		    r.delay(delay);
		}
	    }

	    /* if y is larger than x, move y value in whole numbers */
	    else {
		while (yCurrent != yClosest) {
		    if (deltaY < 0)
			yCurrent = yCurrent - pixelInterval;
		    else
			yCurrent = yCurrent + pixelInterval;
		    if (deltaX < 0)
			xIdeal = xIdeal - (Math.abs(xForEachY) * pixelInterval);
		    else
			xIdeal = xIdeal + (Math.abs(xForEachY) * pixelInterval);
		    xCurrent = (int) Math.round(xIdeal);
		    r.mouseMove(xCurrent, yCurrent);
		    r.delay(delay);
		}
	    }

	    r.mouseMove(xFinal, yFinal);
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Perform a mouse click.
     * 
     * @param button
     *            either "left", "right", or "middle"
     * @param x
     *            The X value of the coordinate that will be clicked on.
     * @param y
     *            The Y value of the coordinate that willl be clicked on.
     */
    public void mouseClick(String button, int x, int y) {
	try {
	    Robot r = new Robot();

	    /* move the mouse to where we want to click */
	    mouseMove(x, y);

	    /* perform a mouse click with the specified button */
	    if (button.equals("left")) {
		r.mousePress(InputEvent.BUTTON1_MASK);
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	    } else if (button.equals("middle")) {
		r.mousePress(InputEvent.BUTTON2_MASK);
		r.mouseRelease(InputEvent.BUTTON2_MASK);
	    } else if (button.equals("right")) {
		r.mousePress(InputEvent.BUTTON3_MASK);
		r.mouseRelease(InputEvent.BUTTON3_MASK);
	    }
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Perform a mouse click.
     * 
     * @param button
     *            The mouse button to simulate. This value must be either
     *            "left", "right", or "middle".
     */
    public void mouseClick(String button) {
	try {
	    Robot r = new Robot();
	    /* perform a mouse click with the specified button */
	    if (button.equals("left")) {
		r.mousePress(InputEvent.BUTTON1_MASK);
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	    } else if (button.equals("middle")) {
		r.mousePress(InputEvent.BUTTON2_MASK);
		r.mouseRelease(InputEvent.BUTTON2_MASK);
	    } else if (button.equals("right")) {
		r.mousePress(InputEvent.BUTTON3_MASK);
		r.mouseRelease(InputEvent.BUTTON3_MASK);
	    }
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Press down a mouse button. If {@link mouseUp} is not called the mouse
     * will be stuck down.
     * 
     * @param button
     *            The mouse button to simulate. This value must be either
     *            "left", "right", or "middle".
     * @param x
     *            The X value of the coordinate that will be clicked on.
     * @param y
     *            The Y value of the coordinate that will be clicked on.
     */
    public void mouseDown(String button, int x, int y) {
	try {
	    Robot r = new Robot();

	    /* move the mouse to where we want it to be */
	    mouseMove(x, y);

	    /* press down the appropriate button */
	    if (button.equals("left")) {
		r.mousePress(InputEvent.BUTTON1_MASK);
	    } else if (button.equals("middle")) {
		r.mousePress(InputEvent.BUTTON2_MASK);
	    } else if (button.equals("right")) {
		r.mousePress(InputEvent.BUTTON3_MASK);
	    }
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Press down a mouse button. If {@link mouseUp} is not called the mouse
     * will be stuck down.
     * 
     * @param button
     *            The mouse button to simulate. This value must be either
     *            "left", "right", or "middle".
     */
    public void mouseDown(String button) {
	try {
	    Robot r = new Robot();

	    /* press down the appropriate button */
	    if (button.equals("left")) {
		r.mousePress(InputEvent.BUTTON1_MASK);
	    } else if (button.equals("middle")) {
		r.mousePress(InputEvent.BUTTON2_MASK);
	    } else if (button.equals("right")) {
		r.mousePress(InputEvent.BUTTON3_MASK);
	    }
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Release a pressed mouse button.
     * 
     * @param button
     *            The mouse button to simulate. This value must be either
     *            "left", "right", or "middle".
     */
    public void mouseUp(String button) {
	try {
	    Robot r = new Robot();

	    /* release the appropriate button */
	    if (button.equals("left")) {
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	    } else if (button.equals("middle")) {
		r.mouseRelease(InputEvent.BUTTON2_MASK);
	    } else if (button.equals("right")) {
		r.mouseRelease(InputEvent.BUTTON3_MASK);
	    }
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Click and drag the mouse from one coordinate to another.
     * 
     * @param button
     *            The mouse button to simulate. This value must be either
     *            "left", "right", or "middle".
     * @param x1
     *            First coordinate X value.
     * @param y1
     *            First coordinate Y value.
     * @param x2
     *            Second coordinate X value.
     * @param y2
     *            Second coordinate Y value.
     */
    public void mouseClickDrag(String button, int x1, int y1, int x2, int y2) {
	mouseDown(button, x1, y1);
	sleep(10);
	mouseMove(x2, y2);
	sleep(10);
	mouseUp(button);
    }

    /**
     * Scroll the mouse wheel up once.
     */
    public void mouseScrollUp() {
	try {
	    Robot r = new Robot();
	    r.mouseWheel(-100);
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Scroll the mouse wheel down once.
     */
    public void mouseScrollDown() {
	try {
	    Robot r = new Robot();
	    r.mouseWheel(100);
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Display a message box with custom button options.
     * 
     * @param Text
     *            Text to display as a prompt.
     * @param Title
     *            Title of the dialog window.
     * @param buttons
     *            String array of button names to be available.
     * @param defaultButton
     *            The default button to be selected when "Enter" is pressed.
     *            defaultButton should be the text of the desired default
     *            button.
     * @return The index of selected button within the buttons array.
     */
    public int optionsBox(String Text, String Title, String[] buttons,
	    String defaultButton) {
	return JOptionPane.showOptionDialog(null, Text, Title,
		JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
		buttons, defaultButton);
    }

    /**
     * Display a message box with custom button options.
     * 
     * @param Text
     *            Text to display as a prompt.
     * @param Title
     *            Title of the dialog window.
     * @param buttons
     *            String array of button names to be available.
     * @return The index of selected button within the buttons array.
     */
    public int optionsBox(String Text, String Title, String[] buttons) {
	return optionsBox(Text, Title, buttons, buttons[0]);
    }

    /**
     * Display a message dialog box until the user closes it or it times out.
     * 
     * @param Text
     *            Text to be displayed as the prompt in the dialog box.
     * @param Title
     *            Title of the dialog window.
     * @param Timeout
     *            Time (in milliseconds) to wait until until the window times
     *            out automatically.
     */
    public void msgBox(String Text, String Title, int Timeout) {
	JOptionPane jOptionPane = new JOptionPane(Text,
		JOptionPane.PLAIN_MESSAGE);
	final JDialog jDialog = jOptionPane.createDialog(Title);
	final int t = Timeout;
	new Thread(new Runnable() {
	    public void run() {
		try {
		    Thread.sleep(t);
		    jDialog.dispose();
		} catch (Throwable th) {

		}
	    }
	}).start();
	jDialog.setVisible(true);
    }

    /**
     * Display a message dialog box until the user closes it or it times out.
     * 
     * @param Text
     *            Text to be displayed as the prompt in the dialog box.
     * @param Title
     *            Title of the dialog window.
     */
    public void msgBox(String Text, String Title) {
	JOptionPane.showMessageDialog(null, Text, Title,
		JOptionPane.PLAIN_MESSAGE); // Show message box
    }

    /**
     * Display a message dialog box until the user closes it or it times out.
     * 
     * @param Text
     *            Text to be displayed as the prompt in the dialog box.
     */
    public void msgBox(String Text) {
	msgBox(Text, "");
    }

    /**
     * Print to the terminal.
     * 
     * @param b
     *            boolean to print
     */
    public void print(boolean b) {
	System.out.println(b);
    }

    /**
     * Print to the terminal.
     * 
     * @param c
     *            char to print
     */
    public void print(char c) {
	System.out.println(c);
    }

    /**
     * Print to the terminal.
     * 
     * @param s
     *            char[] to print
     */
    public void print(char[] s) {
	System.out.println(s);
    }

    /**
     * Print to the terminal.
     * 
     * @param d
     *            double to print
     */
    public void print(double d) {
	System.out.println(d);
    }

    /**
     * Print to the terminal.
     * 
     * @param f
     *            float to print
     */
    public void print(float f) {
	System.out.println(f);
    }

    /**
     * Print to the terminal.
     * 
     * @param i
     *            int to print
     */
    public void print(int i) {
	System.out.println(i);
    }

    /**
     * Print to the terminal.
     * 
     * @param l
     *            Long to print
     */
    public void print(long l) {
	System.out.println(l);
    }

    /**
     * Print to the terminal.
     * 
     * @param obj
     *            Object to print
     */
    public void print(Object obj) {
	System.out.println(obj);
    }

    /**
     * Print to the terminal.
     * 
     * @param s
     *            String to print
     */
    public void print(String s) {
	System.out.println(s);
    }

    /**
     * Convert to a double.
     */
    public double toDouble(String s) throws NumberFormatException {
	return Double.parseDouble(s);
    }

    /**
     * Convert to a double.
     */
    public double toDouble(char c) throws NumberFormatException {
	return Double.parseDouble(String.valueOf(c));
    }

    /**
     * Convert to a double.
     */
    public double toDouble(boolean b) {
	return b ? 1.0D : 0.0D;
    }

    /**
     * Convert to a double.
     */
    public double toDouble(int i) {
	return (double) i;
    }

    /**
     * Convert to a double.
     */
    public double toDouble(long l) {
	return (double) l;
    }

    /**
     * Convert to a double.
     */
    public double toDouble(short s) {
	return (double) s;
    }

    /**
     * Convert to a double.
     */
    public double toDouble(float f) {
	return (double) f;
    }

    /**
     * Convert to a double.
     */
    public double toDouble(double d) {
	return d;
    }

    /**
     * Convert to a double.
     */
    public double toDouble(byte b) {
	int i = b;
	return (double) i;
    }

    /**
     * Convert to a float.
     */
    public float toFloat(String s) throws NumberFormatException {
	return Float.parseFloat(s);
    }

    /**
     * Convert to a float.
     */
    public float toFloat(char c) throws NumberFormatException {
	return Float.parseFloat(String.valueOf(c));
    }

    /**
     * Convert to a float.
     */
    public float toFloat(boolean b) {
	return b ? 1.0F : 0.0F;
    }

    /**
     * Convert to a float.
     */
    public float toFloat(int i) {
	return (float) i;
    }

    /**
     * Convert to a float.
     */
    public float toFloat(long l) {
	float f = l;
	return f;
    }

    /**
     * Convert to a float.
     */
    public float toFloat(short s) {
	float f = s;
	return f;
    }

    /**
     * Convert to a float.
     */
    public float toFloat(float f) {
	return f;
    }

    /**
     * Convert to a float.
     */
    public float toFloat(double d) {
	return (float) d;
    }

    /**
     * Convert to a float.
     */
    public float toFloat(byte b) {
	int i = b;
	return (float) i;
    }

    /**
     * Convert to an int.
     */
    public int toInt(String s) throws NumberFormatException {
	return Integer.parseInt(s);
    }

    /**
     * Convert to an int.
     */
    public int toInt(char c) throws NumberFormatException {
	return Integer.parseInt(String.valueOf(c));
    }

    /**
     * Convert to an int.
     */
    public int toInt(boolean b) {
	return b ? 1 : 0;
    }

    /**
     * Convert to an int.
     */
    public int toInt(int i) {
	return i;
    }

    /**
     * Convert to an int.
     */
    public int toInt(long l) {
	if (l > Integer.MAX_VALUE) {
	    throw new NumberFormatException(
		    "Cannot convert from long to integer because long exceeds Integer.MAX_VALUE ("
			    + Integer.MAX_VALUE + ")");
	} else if (l < Integer.MIN_VALUE) {
	    throw new NumberFormatException(
		    "Cannot convert from long to integer because long is smaller than Integer.MIN_VALUE ("
			    + Integer.MIN_VALUE + ")");
	}
	return (int) l;
    }

    /**
     * Convert to an int.
     */
    public int toInt(short s) {
	return (int) s;
    }

    /**
     * Convert to an int.
     */
    public int toInt(float f) {
	return (int) f;
    }

    /**
     * Convert to an int.
     */
    public int toInt(double d) {
	return (int) d;
    }

    /**
     * Convert to an int.
     */
    public int toInt(byte b) {
	int i = b;
	return i;
    }

    /**
     * Convert to a char.
     */
    public char toChar(String s) {
	return s.toCharArray()[0];
    }

    /**
     * Convert to a char.
     */
    public char toChar(char c) {
	return c;
    }

    /**
     * Convert to string.
     */
    public String toString(String s) {
	return s;
    }

    /**
     * Convert to string.
     */
    public String toString(char c) {
	return String.valueOf(c);
    }

    /**
     * Convert to string.
     */
    public String toString(boolean b) {
	if (b)
	    return "true";
	else
	    return "false";
    }

    /**
     * Convert to string.
     */
    public String toString(int i) {
	return String.valueOf(i);
    }

    /**
     * Convert to string.
     */
    public String toString(long l) {
	return Long.toString(l);
    }

    /**
     * Convert to string.
     */
    public String toString(short s) {
	return String.valueOf(s);
    }

    /**
     * Convert to string.
     */
    public String toString(float f) {
	return Float.toString(f);
    }

    /**
     * Convert to string.
     */
    public String toString(double d) {
	return Double.toString(d);
    }

    /**
     * Convert to string.
     */
    public String toString(byte b) {
	int i = b;
	return String.valueOf(i);
    }

    /**
     * Take a screenshot and save to a PNG file.
     * 
     * @param fileName
     *            Filename to save the PNG screenshot to.
     * @param x1
     *            X value of top left coordinate.
     * @param y1
     *            Y value of top left coordinate.
     * @param x2
     *            X value of bottom right coordinate.
     * @param y2
     *            Y value of bottom right coordinate.
     */
    public void screenShot(String fileName, int x1, int y1, int x2, int y2) {
	/* figure out our coordinates */
	int width = 0, height = 0;
	int[] topLeft = new int[] { 0, 0 };

	/*
	 * possible user inputs (top left, bottom right) (top right, bottom
	 * left) (bottom left, top right) (bottom right, top left)
	 */
	/* if it's already in the (top left, bottom right) format */
	if ((x1 < x2) && (y1 < y2)) {
	    topLeft[0] = x1;
	    topLeft[1] = y1;
	    width = x2 - x1;
	    height = y2 - y1;
	}

	/* if it's in the unfortunate format of (top right, bottom left) */
	else if ((x1 > x2) && (y1 < y2)) {
	    topLeft[0] = x2;
	    topLeft[1] = y1;
	    width = x1 - x2;
	    height = y2 - y1;
	}

	/* if it's in the (still unfortunate) format of (bottom left, top right) */
	else if ((x1 < x2) && (y1 > y2)) {
	    topLeft[0] = x1;
	    topLeft[1] = y2;
	    width = x2 - x1;
	    height = y1 - y2;
	}

	/*
	 * if it's in the incredibly backwards format of (bottom right, top
	 * left)
	 */
	else if ((x1 > x2) && (y1 > y2)) {
	    topLeft[0] = x2;
	    topLeft[1] = y2;
	    width = x1 - x2;
	    height = y1 - y2;
	}

	/*
	 * if it's some other god forsaken format just quit this would only
	 * happen if one of the dimesions was 0 ... so it's fine
	 */
	else {
	    return;
	}

	try {
	    if (!fileName.toLowerCase().endsWith(".png")) {
		fileName = fileName + ".png";
	    }
	    Robot robot = new Robot();
	    BufferedImage img = robot.createScreenCapture(new Rectangle(
		    topLeft[0], topLeft[1], width, height));
	    File save_path = new File(fileName);
	    ImageIO.write(img, "png", save_path);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Take a screenshot and save to a PNG file.
     * 
     * @param fileName
     *            Filename to save the PNG screenshot to.
     */
    public void screenShot(String fileName) {
	try {
	    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	    screenShot(fileName, 0, 0, screen.width, screen.height);

	} catch (Exception e) {

	}
    }

    /**
     * Open a file or URL in the default program. (This will be the default
     * browser if it's a URL.)
     * 
     * @param path
     *            The URL or file to open. URLs must begin with http:// or
     *            https://
     * @return Returns true on success. Returns false on failure.
     */
    public boolean open(String path) {
	if (path.toLowerCase().startsWith("http://")
		|| path.toLowerCase().startsWith("https://")) {
	    try {
		Desktop d = Desktop.getDesktop();
		URI p = new URI(path);
		d.browse(p);
		return true;
	    } catch (Throwable t) {
		return false;
	    }
	} else {
	    try {
		Desktop d = Desktop.getDesktop();
		File file = new File(path);
		d.open(file);
		return true;
	    } catch (Throwable t) {
		return false;
	    }
	}
    }

    /**
     * Get the system date and time in yyyy/MM/dd HH:mm:ss format.
     * 
     * @return The date and time as a string in yyyy/MM/dd HH:mm:ss format.
     */
    public String getDateTime() {
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	return dateFormat.format(date);
    }

    /**
     * Gets the current time in milliseconds. There are <code>1000</code>
     * milliseconds in 1 second, so divide by 1000 to find seconds.
     * 
     * @return This method returns the difference, measured in milliseconds,
     *         between the current time and midnight, January 1, 1970
     *         UTC(coordinated universal time).
     */
    public long getMilliTime() {
	return System.currentTimeMillis();
    }

    /**
     * Gets the current value from the most precise system timer on a computer,
     * in nanoseconds. It returns nanoseconds since some fixed but arbitrary
     * time. This fixed time could even be in the future, resulting in a
     * negative value. <code>getNanoTime</code> should be used to measure the
     * time between events, not as a wall clock.<br>
     * There are <code>1,000,000,000</code> nanoseconds in a second, and
     * <code>1,000,000</code> nanoseconds in a millisecond.
     * 
     * @return The current value of the system timer, in nanoseconds.
     */
    public long getNanoTime() {
	return System.nanoTime();
    }

    /**
     * Execute a shell command and return the output. This will not display a
     * visable process and it will wait for the command to finish before
     * continuing. On Windows systems (the os can be checked with the
     * {@link OS_SYSTEM} constant) the command string must be of the format
     * "cmd /c command".
     * 
     * @param cmd
     *            The command to execute.
     * @return The output of the command.
     */
    public String exec(String cmd) {
	try {
	    String result = "";
	    String line;
	    Process p = Runtime.getRuntime().exec(cmd);
	    BufferedReader bri = new BufferedReader(new InputStreamReader(
		    p.getInputStream()));
	    BufferedReader bre = new BufferedReader(new InputStreamReader(
		    p.getErrorStream()));
	    while ((line = bri.readLine()) != null) {
		result = result + line + "\n";
	    }
	    bri.close();
	    while ((line = bre.readLine()) != null) {
		result = result + line + "\n";
	    }
	    bre.close();
	    p.waitFor();
	    return result;
	} catch (Exception err) {
	    return null;
	}

    }

    /**
     * Get user input from the console.
     * 
     * @param prompt
     *            The prompt to display to the user.
     * @return The user's response.
     */
    public String input(String prompt) {
	System.out.print(prompt);
	Scanner sc = new Scanner(System.in);
	String in = sc.nextLine();
	return in;
    }

    /**
     * Get user input from the console.
     * 
     * @return The user's response.
     */
    public String input() {
	return input("");
    }

    /**
     * Display an input dialog box to the user.
     * 
     * @param Text
     *            Text to be displayed in the dialog box.
     * @param Title
     *            Title of the dialog box.
     * @return User's input as a string.
     */
    public String inputBox(String Text, String Title) {
	String result = JOptionPane.showInputDialog(null, Text, Title, 1);
	return result;
    }

    /**
     * Display an input dialog box to the user.
     * 
     * @param Text
     *            Text to be displayed in the dialog box.
     * @return User's input as a string.
     */
    public String inputBox(String Text) {
	String result = JOptionPane.showInputDialog(null, Text, "", 1);
	return result;
    }

    /**
     * Show a dialog allowing the user to choose from a drop-down list of items.
     * 
     * @param Text
     *            Text to be displayed in the dialog box.
     * @param Title
     *            Title of the dialog box.
     * @param Choices
     *            The options in the drop-down as a string array.
     * @param Default
     *            The index of the default option in the Choices array.
     * @return The user's choice as a string.
     */
    public String inputList(String Text, String Title, String[] Choices,
	    int Default) {
	String choice = (String) JOptionPane.showInputDialog(null, Text, Title,
		JOptionPane.QUESTION_MESSAGE, null, Choices, Choices[Default]);
	return choice;
    }

    /**
     * Show a dialog allowing the user to choose from a drop-down list of items.
     * 
     * @param Text
     *            Text to be displayed in the dialog box.
     * @param Title
     *            Title of the dialog box.
     * @param Choices
     *            The options in the drop-down as a string array.
     * @return The user's choice as a string.
     */
    public String inputList(String Text, String Title, String[] Choices) {
	return inputList(Text, Title, Choices, 0);
    }

    /**
     * Get a password from the user. Displays a dialog box where the input is
     * masked.
     * 
     * @param Text
     *            Text to be displayed in the dialog box.
     * @param Title
     *            Title of the dialog box.
     * @param Okay
     *            text of the okay button, eg. okay, submit, go, login, etc.
     * @return The user's input
     */
    public String inputPassword(String Text, String Title, String Okay) {
	JPasswordField passwordField = new JPasswordField();
	Object[] obj = { Text + "\n\n", passwordField };
	Object[] stringArray = { Okay };
	if (JOptionPane.showOptionDialog(null, obj, Title,
		JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
		stringArray, obj) == JOptionPane.YES_OPTION) {
	    return new String(passwordField.getPassword());
	} else {
	    return null;
	}
    }

    /**
     * Get a password from the user. Displays a dialog box where the input is
     * masked.
     * 
     * @param Text
     *            Text to be displayed in the dialog box.
     * @param Title
     *            Title of the dialog box.
     * @return The user's input
     */
    public String inputPassword(String Text, String Title) {
	return inputPassword(Text, Title, "Submit");
    }

    /**
     * Get a password from the user. Displays a dialog box where the input is
     * masked.
     * 
     * @param Text
     *            Text to be displayed in the dialog box.
     * @return The user's input
     */
    public String inputPassword(String Text) {
	return inputPassword(Text, "Password Input");
    }

    /**
     * Get a random integer between two inclusive values.
     * 
     * @param min
     *            The minimum integer value to be returned (inclusive).
     * @param max
     *            The minimum integer value to be returned (inclusive).
     * @return A random integer between min and max.
     */
    public int intGetRandom(int min, int max) {
	Random rand = new Random();
	// nextInt is normally exclusive of the top value,
	// so add 1 to make it inclusive
	int randomNum = rand.nextInt((max - min) + 1) + min;
	return randomNum;
    }

    /**
     * Get the current position of the mouse cursor in an integer array.
     * 
     * @return Integer array formatted like [x position, y position].
     */
    public int[] cursorGetPos() {
	int X = MouseInfo.getPointerInfo().getLocation().x; // get the X
							    // coordinate of the
							    // mouse
	int Y = MouseInfo.getPointerInfo().getLocation().y; // get the Y
							    // coordinate of the
							    // mouse
	int[] coords = { X, Y };
	return coords;
    }

    /**
     * Get the color of a certain pixel as an integer.
     * 
     * @param x
     *            X coordinate of pixel.
     * @param y
     *            Y coordinate of pixel.
     * @return The integer representation of the pixel's color.
     */
    public int pixelGetColor(int x, int y) {
	try {
	    Robot r = new Robot();
	    Color pixel = r.getPixelColor(x, y);
	    return pixel.getRGB();
	} catch (AWTException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Get an integer representation of a color based on R,G,B values.
     * 
     * @param r
     *            r value.
     * @param g
     *            g value.
     * @param b
     *            b value.
     * @return The integer representation of the color.
     */
    public int rgbGetInt(int r, int g, int b) {
	Color c = new Color(r, g, b);
	return c.getRGB();
    }

    /**
     * Get the RGB values of an integer color value.
     * 
     * @param i
     *            The integer value of the color.
     * @return An array with three values: [red, green, blue].
     */
    public int[] intGetRGB(int i) {
	Color c = new Color(i);
	return new int[] { c.getRed(), c.getGreen(), c.getBlue() };
    }

    /**
     * Search for the coordinates of a pixel of a certain color within an area.
     * This will return the coordinates of the first pixel found within the area
     * that matches the search color.
     * 
     * @param colorInt
     *            Integer representation of the color to search for.
     * @param x1
     *            X value of top left coordinate.
     * @param y1
     *            Y value of top left coordinate.
     * @param x2
     *            X value of bottom right coordinate.
     * @param y2
     *            Y value of bottom right coordinate.
     * @param speed
     *            A value of 1-5 to describe how fast to search. A speed of 2
     *            searches twice as fast as speed 1 except it only checks every
     *            other pixel, which is fine if the color you're trying to find
     *            is more than two pixels wide.
     * @return If the color is found coordinates are returned in an int array
     *         formatted like: [x, y]. If the color is not found it will return
     *         [-1,-1]. If there is an error executing the search it will return
     *         [-3, -3].
     */
    public int[] pixelSearch(int colorInt, int x1, int y1, int x2, int y2,
	    int speed) {
	try {
	    Color color = new Color(colorInt);
	    Robot r = new Robot(); // create robot to capture the screen
	    BufferedImage screen = r.createScreenCapture(new Rectangle(x1, y1,
		    x2 + 1, y2 + 1)); // read the screen into a buffered image
				      // (+1's to avoid index out of range)

	    int cVal = color.getRGB(); // get value of color to compare to
				       // pixels

	    // speed 1 - xMod = 1 yMod = 1
	    // 10201 - 100%
	    // speed 2 - xMod = 2 yMod = 1
	    // 5050 - 50%
	    // speed 3 - xMod = 2 yMod = 2
	    // 2500 - 25%
	    // speed 4 - xMod = 3 yMod = 2
	    // 1650 - 16%
	    // speed 5 - xMod = 3 yMod = 3
	    // 1089 - 10%

	    int xMod;
	    int yMod;
	    switch (speed) {
	    case 1:
		xMod = 1;
		yMod = 1;
		break;
	    case 2:
		xMod = 2;
		yMod = 1;
		break;
	    case 3:
		xMod = 2;
		yMod = 2;
		break;
	    case 4:
		xMod = 3;
		yMod = 2;
		break;
	    case 5:
		xMod = 3;
		yMod = 3;
		break;
	    default:
		xMod = 1;
		yMod = 1;
		break;
	    }

	    int[] preXArray = new int[x2 - x1 + 1]; // create an array to hold
						    // all X values in image
	    int iterator = 0;
	    while (iterator <= x2) {
		preXArray[iterator] = x1 + iterator;
		iterator++;
	    }
	    int[] preYArray = new int[y2 - y1 + 1]; // create an array to hold
						    // all Y values in image
	    iterator = 0;
	    while (iterator <= y2) {
		preYArray[iterator] = y1 + iterator;
		iterator++;
	    }

	    int[] xArray = new int[(preXArray.length / xMod)];
	    int step = 0;
	    for (int i = 0; i < preXArray.length; i += xMod) {
		try {
		    xArray[step] = preXArray[i];
		} catch (Exception e) {
		}
		step++;
	    }

	    int[] yArray = new int[(preYArray.length / yMod)];
	    step = 0;
	    for (int i = 0; i < preYArray.length; i += yMod) {
		try {
		    yArray[step] = preYArray[i];
		} catch (Exception e) {
		}
		step++;
	    }

	    for (int yVal : yArray) {
		for (int xVal : xArray) {
		    int col = screen.getRGB(xVal, yVal); // get the color of
							 // pixel at coords
							 // (xVal, yVal)
		    if (col == cVal) { // if we find the color
			int[] cPos = { xVal, yVal };
			return cPos;
		    }
		}
	    }

	    int[] returnVal = { -1, -1 };
	    return returnVal;
	} catch (Exception e) {
	    int[] returnVal = { -3, -3 };
	    return returnVal;
	}
    }

    /**
     * Search for the coordinates of a pixel of a certain color within an area.
     * This will return the coordinates of the first pixel found within the area
     * that matches the search color.
     * 
     * @param colorInt
     *            Integer representation of the color to search for.
     * @param x1
     *            X value of top left coordinate.
     * @param y1
     *            Y value of top left coordinate.
     * @param x2
     *            X value of bottom right coordinate.
     * @param y2
     *            Y value of bottom right coordinate.
     * @return If the color is found coordinates are returned in an int array
     *         formatted like: [x, y]. If the color is not found it will return
     *         [-1,-1]. If there is an error executing the search it will return
     *         [-3, -3].
     */
    public int[] pixelSearch(int colorInt, int x1, int y1, int x2, int y2) {
	return pixelSearch(colorInt, x1, y1, x2, y2, 1);
    }

    /**
     * Search for the coordinates of a pixel of a certain color within an area.
     * This will return the coordinates of the first pixel found within the area
     * that matches the search color.
     * 
     * @param colorInt
     *            Integer representation of the color to search for.
     * @return If the color is found coordinates are returned in an int array
     *         formatted like: [x, y]. If the color is not found it will return
     *         [-1,-1]. If there is an error executing the search it will return
     *         [-3, -3].
     */
    public int[] pixelSearch(int colorInt) {
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	return pixelSearch(colorInt, 0, 0, screen.width, screen.height);
    }

    /**
     * Check if a file or directory exists.
     * 
     * @param fPath
     *            Path of the file or directory to check.
     * @return Will return true if the file or directory exists, otherwise
     *         returns false.
     */
    public boolean fileExists(String fPath) {
	return new File(fPath).exists();
    }

    /**
     * Delete a file. This will not work on a directory.
     * 
     * @param fPath
     *            Path of the file to delete.
     */
    public void fileDelete(String fPath) {
	new File(fPath).delete();
    }

    /**
     * Delete a directory and its contents.
     */
    public void rmDir(String filePath) {
	File file = new File(filePath);
	if (!file.exists())
	    return;
	if (file.isDirectory()) {
	    for (File f : file.listFiles()) {
		rmDir(f.getAbsolutePath());
	    }
	}
	file.delete();
    }

    /**
     * Create a directory.
     * 
     * @param dirName
     *            The name of the directory to create. If the directory already
     *            exists no action will be taken.
     */
    public void mkDir(String dirName) {
	File file = new File(dirName);
	if (!file.exists())
	    file.mkdir();
    }

    /**
     * Create a file. Similar to the unix touch command.
     * 
     * @param fPath
     *            Path of file to be created.
     */
    public void fileCreate(String fPath) {
	try {
	    new File(fPath).createNewFile();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * Get the full file path of a specific file or directory.
     * 
     * @param fPath
     *            Path of the file.
     * @return The full (absolute) path of the file or directory.
     */
    public String fileGetPath(String fPath) {
	return new File(fPath).getAbsolutePath();
    }

    /**
     * Get the base name of a file, including its extension.
     * 
     * @param fPath
     *            Path of the file.
     * @return Name of the file (including extension).
     */
    public String fileGetName(String fPath) {
	return new File(fPath).getName();
    }

    /**
     * Returns true for a file but not for a directory (or a non-existant file).
     * 
     * @param fPath
     *            Path to evaluate.
     * @return Returns true if fPath is a file, otherwise returns false.
     */
    public boolean isFile(String fPath) {
	return new File(fPath).isFile();
    }

    /**
     * Check if a path is a directory.
     * 
     * @param fPath
     *            Path to evaluate.
     * @return True if directory, false if not.
     */
    public boolean isDirectory(String fPath) {
	return new File(fPath).isDirectory();
    }

    /**
     * Read the contents of a file to a string.
     * 
     * @param fPath
     *            Path of the file to read.
     * @return The contents of the file as a string.
     */
    public String fileRead(String fPath) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(fPath));
	    StringBuilder data = new StringBuilder();

	    String line = br.readLine();
	    while (line != null) {
		data.append(line);
		data.append('\n');
		line = br.readLine();
	    }

	    String fileData = data.toString();
	    fileData = fileData.substring(0, fileData.length() - 1);
	    br.close();
	    return fileData;
	} catch (Exception e) {
	    return "null";
	}
    }

    /**
     * Write data to a file -- this will overwrite the file. If the file does
     * not exist it will be created.
     * 
     * @param fPath
     *            Path of the file to write to.
     * @param data
     *            Data to write to the file as a string.
     * @return Returns false if the file write failed. Otherwise returns true.
     */
    public boolean fileWrite(String fPath, String data) {
	BufferedWriter bufferedWriter = null;
	try {
	    File myFile = new File(fPath);
	    if (!myFile.exists()) {
		myFile.createNewFile();
	    }
	    Writer writer = new FileWriter(myFile);
	    bufferedWriter = new BufferedWriter(writer);
	    bufferedWriter.write(data);
	    try {
		if (bufferedWriter != null)
		    bufferedWriter.close();
	    } catch (Exception ex) {
	    }
	    return true;
	} catch (IOException e) {
	    return false;
	}
    }

    /**
     * Append data to a file. If the file does not exist it will be created.
     * 
     * @param fPath
     *            The path of the file to which we are appending data.
     * @param data
     *            The data to be appended to the file.
     * @return If appending to the file failed false will be returned. Otherwise
     *         will return true.
     */
    public boolean fileAppend(String fPath, String data) {
	try {
	    File oFile = new File(fPath);
	    if (!oFile.exists()) {
		oFile.createNewFile();
	    }
	    FileWriter fileWriter = new FileWriter(oFile, true);
	    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	    bufferedWriter.write(data);
	    bufferedWriter.close();
	    return false;
	} catch (IOException e) {
	    return false;
	}
    }

    /**
     * Get a list of file names in a directory as an array.
     * 
     * @param dir
     *            Directory to list files from.
     * @return String array of file names.
     */
    public String[] fileList(String dir) {
	try {
	    File[] fileList = new File(dir).listFiles(); // java code to get a
							 // list of files
	    int fileNum = fileList.length; // store length of file list
	    String[] files = new String[fileNum];

	    for (int i = 0; i < fileNum; i++) {
		files[i] = fileList[i].toString();
	    }
	    return files;
	} catch (Exception e) {
	    String[] files = { "null" };
	    return files;
	}
    }

    /**
     * Put a string in the system clipboard. On Ubuntu Linux the way the
     * clipboard is handled only allows information in the clipboard to be
     * accessable while the application that put it there is running.
     * 
     * @param s
     *            String data to put in the clip board.
     */
    public void clipboardPut(String s) {
	StringSelection stringSelection = new StringSelection(s);
	Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
	clpbrd.setContents(stringSelection, null);
    }

    /**
     * Get the contents of the clip board (as string).
     * 
     * @return The clip board contents as a string. Returns null if the data
     *         inside the clip board cannot be read to a string.
     */
    public String clipboardGet() {
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Clipboard clipboard = toolkit.getSystemClipboard();
	String result;
	try {
	    result = (String) clipboard.getData(DataFlavor.stringFlavor);
	    return result;
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Get the value of a flagged argument. So if the program is executed like
     * java program -v --file aFile.txt -o outFile.txt getFlaggedArg(args,
     * "-f|--file") would return aFile.txt.
     * 
     * @param args
     *            The String[] args from the main function. Pass them in by just
     *            saying args.
     * @param flag
     *            The flags to get values from, separated by | like -f|--file.
     * @return The argument directly after the detected flag.
     */
    public String getFlaggedArg(String[] args, String flag) {
	ArrayList<String> flags = new ArrayList<String>();
	if (flag.contains("|")) {
	    /* we need to user [|] because .split() takes a regex value */
	    for (String f : flag.split("[|]"))
		flags.add(f.trim());
	} else {
	    flags.add(flag.trim());
	}
	for (int i = 0; i < args.length; i++) {
	    if (flags.contains(args[i])) {
		if (i == args.length - 1)
		    return "";
		else
		    return args[i + 1];
	    }
	}
	return "";
    }

    /**
     * Check if a flag is present within the command line arguments.
     * 
     * @param args
     *            The String[] args from the main function. Pass them in by just
     *            saying args.
     * @param flag
     *            The flag(s) to check for, separate possible values with a pipe
     *            | For instance -h|--help could match either one.
     * @return True or false based on whether the flag is present.
     */
    public boolean isFlagged(String[] args, String flag) {
	ArrayList<String> flags = new ArrayList<String>();
	if (flag.contains("|")) {
	    /* we need to user [|] because .split() takes a regex value */
	    for (String f : flag.split("[|]"))
		flags.add(f.trim());
	} else {
	    flags.add(flag.trim());
	}
	for (int i = 0; i < args.length; i++) {
	    if (flags.contains(args[i]))
		return true;
	}
	return false;
    }

    /**
     * Send a HTTP GET request to a URL.
     * 
     * @param url
     *            The URL to query, including the get parameters.
     * @return Page response as HTML.
     */
    public String httpGet(String url) {
	try {
	    URL obj = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

	    // optional default is GET
	    con.setRequestMethod("GET");

	    // add request header
	    con.setRequestProperty("User-Agent", "Mozilla/5.0");

	    BufferedReader in = new BufferedReader(new InputStreamReader(
		    con.getInputStream()));
	    String inputLine;
	    StringBuffer response = new StringBuffer();

	    while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	    }
	    in.close();

	    return response.toString();
	} catch (Exception e) {
	    e.printStackTrace();
	    return "";
	}

    }

    /**
     * Send a HTTP POST request to a given URL with the specified parameters.
     * 
     * @param postUrl
     *            URL to send the post request to.
     * @param parameters
     *            Post parameters to send to URL. Parameters must be defined in
     *            a two dimesional array like [ [key, value], [key, value],
     *            [key, value] ].
     * @return Page response as HTML.
     */
    public String httpPost(String postUrl, String[][] parameters) {
	try {
	    URL url = new URL(postUrl);
	    Map<String, Object> params = new LinkedHashMap<>();
	    for (String[] param : parameters) {
		params.put(param[0], param[1]);
	    }
	    StringBuilder postData = new StringBuilder();
	    for (Map.Entry<String, Object> param : params.entrySet()) {
		if (postData.length() != 0)
		    postData.append('&');
		postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
		postData.append('=');
		postData.append(URLEncoder.encode(
			String.valueOf(param.getValue()), "UTF-8"));
	    }
	    byte[] postDataBytes = postData.toString().getBytes("UTF-8");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type",
		    "application/x-www-form-urlencoded");
	    conn.setRequestProperty("Content-Length",
		    String.valueOf(postDataBytes.length));
	    conn.setDoOutput(true);
	    conn.getOutputStream().write(postDataBytes);
	    Reader in = new BufferedReader(new InputStreamReader(
		    conn.getInputStream(), "UTF-8"));
	    int ch;
	    String response = "";
	    while ((ch = in.read()) >= 0)
		response = response + String.valueOf((char) ch);
	    return response;
	} catch (Exception e) {
	    return "Error";
	}
    }

    /**
     * Get the source of a web page as HTML.
     * 
     * @param url
     *            The page to fetch.
     * @return The page source (HTML) as a string.
     */
    public String getPage(String url) {
	try {
	    // get URL content
	    URL page = new URL(url);
	    URLConnection conn = page.openConnection();
	    // open the stream and put it into BufferedReader
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    conn.getInputStream()));
	    String inputLine;
	    String pageData = "";
	    while ((inputLine = br.readLine()) != null) {
		pageData = pageData + inputLine + "\n";
	    }
	    br.close();
	    /* get rid of the last newline we added on */
	    return pageData.substring(0, pageData.length() - 1);
	} catch (Exception e) {
	    return "Error";
	}
    }

    /**
     * Join a string array into a string with a delimeter.
     * 
     * @param joinString
     *            The delimeter with which to join the string array.
     * @param strArray
     *            The array to join.
     * @return The array as a string with the delimeter between each index.
     */
    public String join(String joinString, String[] strArray) {
	String joined = "";
	for (String s : strArray)
	    joined = joined + s + joinString;
	return joined.substring(0, joined.length() - joinString.length());
    }

    /*
     * Define the private, internally used functions that support the public
     * functions
     */

    /**
     * Replace special characters like {CTRL} or {ENTER} with arbitrary unicode
     * characters that can be recognized by other functions
     * 
     * @param s
     *            String to replace special characters in
     * @return String with special characters replaced by appropriate unicode
     *         chracters
     */
    private String handleSpecialChars(String s) {
	// compensate for special characters
	s = s.replace("{ENTER}", "\n");
	s = s.replace("{TAB}", "\t");
	s = s.replace("{WIN}", "\u0259");
	s = s.replace("{SHIFT}", "\u025A");
	s = s.replace("{DELETE}", "\u025B");
	s = s.replace("{CTRL}", "\u025C");
	s = s.replace("{ALT}", "\u025D");
	s = s.replace("{ESC}", "\u025E");
	s = s.replace("{APPLE}", "\u025F");
	s = s.replace("{F1}", "\u1E90");
	s = s.replace("{F2}", "\u1E91");
	s = s.replace("{F3}", "\u1E92");
	s = s.replace("{F4}", "\u1E93");
	s = s.replace("{F5}", "\u1E94");
	s = s.replace("{F6}", "\u1E95");
	s = s.replace("{F7}", "\u1E96");
	s = s.replace("{F8}", "\u1E97");
	s = s.replace("{F9}", "\u1E98");
	s = s.replace("{F10}", "\u1E99");
	s = s.replace("{F11}", "\u1E9A");
	s = s.replace("{F12}", "\u1E9B");
	s = s.replace("{F13}", "\u1E9C");
	s = s.replace("{F14}", "\u1E9D");
	s = s.replace("{F15}", "\u1E9E");
	s = s.replace("{F16}", "\u1E9F");
	s = s.replace("{F17}", "\u1EA0");
	s = s.replace("{F18}", "\u1EA1");
	s = s.replace("{F19}", "\u1EA2");
	s = s.replace("{F20}", "\u1EA3");
	s = s.replace("{F21}", "\u1EA4");
	s = s.replace("{F22}", "\u1EA5");
	s = s.replace("{F23}", "\u1EA6");
	s = s.replace("{F24}", "\u1EA7");
	s = s.replace("{HOME}", "\u1EA8");
	s = s.replace("{END}", "\u1EA9");
	s = s.replace("{LEFT}", "\u2C00");
	s = s.replace("{RIGHT}", "\u2C01");
	s = s.replace("{UP}", "\u2C02");
	s = s.replace("{DOWN}", "\u2C03");
	s = s.replace("{CAPS}", "\u2C04");
	s = s.replace("{BACKSPACE}", "\b");
	return s;
    }

    /**
     * Figure out which virtual key codes are associated with each character and
     * then call {@link keyType} with the right value
     * 
     * @param c
     *            character to send
     */
    private void sendChar(char c) {
	switch (c) {
	case 'a':
	    keyType(KeyEvent.VK_A);
	    break;
	case 'b':
	    keyType(KeyEvent.VK_B);
	    break;
	case 'c':
	    keyType(KeyEvent.VK_C);
	    break;
	case 'd':
	    keyType(KeyEvent.VK_D);
	    break;
	case 'e':
	    keyType(KeyEvent.VK_E);
	    break;
	case 'f':
	    keyType(KeyEvent.VK_F);
	    break;
	case 'g':
	    keyType(KeyEvent.VK_G);
	    break;
	case 'h':
	    keyType(KeyEvent.VK_H);
	    break;
	case 'i':
	    keyType(KeyEvent.VK_I);
	    break;
	case 'j':
	    keyType(KeyEvent.VK_J);
	    break;
	case 'k':
	    keyType(KeyEvent.VK_K);
	    break;
	case 'l':
	    keyType(KeyEvent.VK_L);
	    break;
	case 'm':
	    keyType(KeyEvent.VK_M);
	    break;
	case 'n':
	    keyType(KeyEvent.VK_N);
	    break;
	case 'o':
	    keyType(KeyEvent.VK_O);
	    break;
	case 'p':
	    keyType(KeyEvent.VK_P);
	    break;
	case 'q':
	    keyType(KeyEvent.VK_Q);
	    break;
	case 'r':
	    keyType(KeyEvent.VK_R);
	    break;
	case 's':
	    keyType(KeyEvent.VK_S);
	    break;
	case 't':
	    keyType(KeyEvent.VK_T);
	    break;
	case 'u':
	    keyType(KeyEvent.VK_U);
	    break;
	case 'v':
	    keyType(KeyEvent.VK_V);
	    break;
	case 'w':
	    keyType(KeyEvent.VK_W);
	    break;
	case 'x':
	    keyType(KeyEvent.VK_X);
	    break;
	case 'y':
	    keyType(KeyEvent.VK_Y);
	    break;
	case 'z':
	    keyType(KeyEvent.VK_Z);
	    break;
	case 'A':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_A);
	    break;
	case 'B':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_B);
	    break;
	case 'C':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_C);
	    break;
	case 'D':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_D);
	    break;
	case 'E':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_E);
	    break;
	case 'F':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_F);
	    break;
	case 'G':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_G);
	    break;
	case 'H':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_H);
	    break;
	case 'I':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_I);
	    break;
	case 'J':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_J);
	    break;
	case 'K':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_K);
	    break;
	case 'L':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_L);
	    break;
	case 'M':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_M);
	    break;
	case 'N':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_N);
	    break;
	case 'O':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_O);
	    break;
	case 'P':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_P);
	    break;
	case 'Q':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_Q);
	    break;
	case 'R':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_R);
	    break;
	case 'S':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_S);
	    break;
	case 'T':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_T);
	    break;
	case 'U':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_U);
	    break;
	case 'V':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_V);
	    break;
	case 'W':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_W);
	    break;
	case 'X':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_X);
	    break;
	case 'Y':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_Y);
	    break;
	case 'Z':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_Z);
	    break;
	case '`':
	    keyType(KeyEvent.VK_BACK_QUOTE);
	    break;
	case '0':
	    keyType(KeyEvent.VK_0);
	    break;
	case '1':
	    keyType(KeyEvent.VK_1);
	    break;
	case '2':
	    keyType(KeyEvent.VK_2);
	    break;
	case '3':
	    keyType(KeyEvent.VK_3);
	    break;
	case '4':
	    keyType(KeyEvent.VK_4);
	    break;
	case '5':
	    keyType(KeyEvent.VK_5);
	    break;
	case '6':
	    keyType(KeyEvent.VK_6);
	    break;
	case '7':
	    keyType(KeyEvent.VK_7);
	    break;
	case '8':
	    keyType(KeyEvent.VK_8);
	    break;
	case '9':
	    keyType(KeyEvent.VK_9);
	    break;
	case '-':
	    keyType(KeyEvent.VK_MINUS);
	    break;
	case '=':
	    keyType(KeyEvent.VK_EQUALS);
	    break;
	case '~':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE);
	    break;
	case '!':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_1);
	    break;
	case '@':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_2);
	    break;
	case '#':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_3);
	    break;
	case '$':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_4);
	    break;
	case '%':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_5);
	    break;
	case '^':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_6);
	    break;
	case '&':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_7);
	    break;
	case '*':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_8);
	    break;
	case '(':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_9);
	    break;
	case ')':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_0);
	    break;
	case '_':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS);
	    break;
	case '+':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS);
	    break;
	case '\t':
	    keyType(KeyEvent.VK_TAB);
	    break;
	case '\n':
	    keyType(KeyEvent.VK_ENTER);
	    break;
	case '[':
	    keyType(KeyEvent.VK_OPEN_BRACKET);
	    break;
	case ']':
	    keyType(KeyEvent.VK_CLOSE_BRACKET);
	    break;
	case '\\':
	    keyType(KeyEvent.VK_BACK_SLASH);
	    break;
	case '{':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET);
	    break;
	case '}':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET);
	    break;
	case '|':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH);
	    break;
	case ';':
	    keyType(KeyEvent.VK_SEMICOLON);
	    break;
	case ':':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON);
	    break;
	case '\'':
	    keyType(KeyEvent.VK_QUOTE);
	    break;
	case '\"':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE);
	    break;
	case ',':
	    keyType(KeyEvent.VK_COMMA);
	    break;
	case '<':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA);
	    break;
	case '.':
	    keyType(KeyEvent.VK_PERIOD);
	    break;
	case '>':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD);
	    break;
	case '/':
	    keyType(KeyEvent.VK_SLASH);
	    break;
	case '?':
	    keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH);
	    break;
	case ' ':
	    keyType(KeyEvent.VK_SPACE);
	    break;
	case '\u0259':
	    keyType(KeyEvent.VK_WINDOWS);
	    break;
	case '\u025A':
	    keyType(KeyEvent.VK_SHIFT);
	    break;
	case '\u025B':
	    keyType(KeyEvent.VK_DELETE);
	    break;
	case '\u025C':
	    keyType(KeyEvent.VK_CONTROL);
	    break;
	case '\u025D':
	    keyType(KeyEvent.VK_ALT);
	    break;
	case '\u025E':
	    keyType(KeyEvent.VK_ESCAPE);
	    break;
	case '\u025F':
	    keyType(KeyEvent.VK_META);
	    break;
	case '\u1E90':
	    keyType(KeyEvent.VK_F1);
	    break;
	case '\u1E91':
	    keyType(KeyEvent.VK_F2);
	    break;
	case '\u1E92':
	    keyType(KeyEvent.VK_F3);
	    break;
	case '\u1E93':
	    keyType(KeyEvent.VK_F4);
	    break;
	case '\u1E94':
	    keyType(KeyEvent.VK_F5);
	    break;
	case '\u1E95':
	    keyType(KeyEvent.VK_F6);
	    break;
	case '\u1E96':
	    keyType(KeyEvent.VK_F7);
	    break;
	case '\u1E97':
	    keyType(KeyEvent.VK_F8);
	    break;
	case '\u1E98':
	    keyType(KeyEvent.VK_F9);
	    break;
	case '\u1E99':
	    keyType(KeyEvent.VK_F10);
	    break;
	case '\u1E9A':
	    keyType(KeyEvent.VK_F11);
	    break;
	case '\u1E9B':
	    keyType(KeyEvent.VK_F12);
	    break;
	case '\u1E9C':
	    keyType(KeyEvent.VK_F13);
	    break;
	case '\u1E9D':
	    keyType(KeyEvent.VK_F14);
	    break;
	case '\u1E9E':
	    keyType(KeyEvent.VK_F15);
	    break;
	case '\u1E9F':
	    keyType(KeyEvent.VK_F16);
	    break;
	case '\u1EA0':
	    keyType(KeyEvent.VK_F17);
	    break;
	case '\u1EA1':
	    keyType(KeyEvent.VK_F18);
	    break;
	case '\u1EA2':
	    keyType(KeyEvent.VK_F19);
	    break;
	case '\u1EA3':
	    keyType(KeyEvent.VK_F20);
	    break;
	case '\u1EA4':
	    keyType(KeyEvent.VK_F21);
	    break;
	case '\u1EA5':
	    keyType(KeyEvent.VK_F22);
	    break;
	case '\u1EA6':
	    keyType(KeyEvent.VK_F23);
	    break;
	case '\u1EA7':
	    keyType(KeyEvent.VK_F24);
	    break;
	case '\u1EA8':
	    keyType(KeyEvent.VK_HOME);
	    break;
	case '\u1EA9':
	    keyType(KeyEvent.VK_END);
	    break;
	case '\u2C00':
	    keyType(KeyEvent.VK_LEFT);
	    break;
	case '\u2C01':
	    keyType(KeyEvent.VK_RIGHT);
	    break;
	case '\u2C02':
	    keyType(KeyEvent.VK_UP);
	    break;
	case '\u2C03':
	    keyType(KeyEvent.VK_DOWN);
	    break;
	case '\u2C04':
	    keyType(KeyEvent.VK_CAPS_LOCK);
	    break;
	case '\b':
	    keyType(KeyEvent.VK_BACK_SPACE);
	    break;
	default:
	    throw new IllegalArgumentException("Cannot type character " + c);
	}
    }

    /**
     * Use the java.awt.Robot class to perform a key press for each int in int[]
     * keyCodes
     * 
     * @param keyCodes
     *            int array of virtual key codes to press
     */
    private void keyType(int... keyCodes) {
	keyType(keyCodes, 0, keyCodes.length);
    }

    /**
     * Use the java.awt.Robot class to perform a key press for each int in int[]
     * keyCodes
     * 
     * @param keyCodes
     *            int array of virtual key codes to press
     * @param offset
     *            used to recursively call each virtual key code in array
     * @param length
     *            used to manage the recursive base case in calling each virtual
     *            key code in the array
     */
    private void keyType(int[] keyCodes, int offset, int length) {
	if (length == 0) {
	    return;
	}
	try {
	    Robot r = new Robot();
	    r.keyPress(keyCodes[offset]);
	    keyType(keyCodes, offset + 1, length - 1);
	    r.keyRelease(keyCodes[offset]);
	} catch (AWTException e) { // throw an error
	    throw new RuntimeException(e);
	}
    }

    /**
     * Terminates the currently running Java Virtual Machine. The argument
     * serves as a status code. This code can be used by Windows or the DOS
     * variable %ERRORLEVEL%. On *nix to check the exit code we can simply print
     * the $? special variable in bash. This variable will print the exit code
     * of the last run command.
     * 
     * @param code
     *            - Integer that sets the script's return code. Exit code is
     *            <ul>
     *            <li>0 when execution went fine</li>
     *            <li>1, -1, whatever != 0 when some error occurred</li>
     *            </ul>
     * 
     * @return
     */
    public void exit(final int code) {
	System.exit(code);
    }

    /**
     * Terminates the currently running Java Virtual Machine with exit code 0
     * (Scripts normally set a exit status code of 0 if the script executed
     * properly).
     */
    public void exit() {
	System.exit(0);
    }
}
