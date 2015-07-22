package com.automation.javauto.test;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.assertj.core.api.StrictAssertions.fail;
import static org.assertj.core.api.Assertions.*;

import java.awt.MouseInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.automation.javauto.Javauto;

/**
 * Some test for the {@link Javauto} class.
 * 
 * @author henry.tejera
 *
 */
public class JavautoTest {

    public final String SEP = File.separator;
    public final String TEST_RESOURCES_PATH = "src" + SEP + "test" + SEP
	    + "resources" + SEP;
    public final String TEST_FILE = "fermat.txt";

    @Test
    public void testGetAndSetSpeed() {
	Javauto javauto = new Javauto();
	double defaultSpeed = 0.95D;
	double newSpeed = 0.5D;
	double maxLimitSpeed = 1.0D;
	double minLimitSpeed = 0.0D;
	double upLimitSpeed = 1.1D;
	double underLimitSpeed = -1.0D;

	assertThat(javauto.getSpeed()).isEqualTo(defaultSpeed);

	javauto.setSpeed(newSpeed);
	assertThat(javauto.getSpeed()).isEqualTo(newSpeed);

	javauto.setSpeed(maxLimitSpeed);
	assertThat(javauto.getSpeed()).isEqualTo(maxLimitSpeed);

	javauto.setSpeed(minLimitSpeed);
	assertThat(javauto.getSpeed()).isEqualTo(minLimitSpeed);

	javauto.setSpeed(upLimitSpeed);
	assertThat(javauto.getSpeed()).isEqualTo(maxLimitSpeed);

	javauto.setSpeed(underLimitSpeed);
	assertThat(javauto.getSpeed()).isEqualTo(minLimitSpeed);
    }

    @Test()
    public void testKeyDownShouldBeReturnRuntimeExceptionWithBadKey() {
	Javauto javauto = new Javauto();
	String badKey = "{INVALIDENTER}";

	try {
	    javauto.keyDown(badKey);
	    fail("RuntimeException expected because the Key is invalid.");
	} catch (RuntimeException e) {
	    assertThat(e).hasMessage("Cannot type " + badKey);
	}
    }

    @Test()
    public void testKeyDownShouldBeReturnRuntimeExceptionWithInvalidKeyCode() {
	Javauto javauto = new Javauto();
	int badKeyCode = 0;

	try {
	    javauto.keyDown(badKeyCode);
	    fail("RuntimeException expected because the keycode is not a valid key.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test()
    public void testKeyUpShouldBeReturnRuntimeExceptionWithBadKey() {
	Javauto javauto = new Javauto();
	String badKey = "{INVALIDENTER}";

	try {
	    javauto.keyUp(badKey);
	    fail("RuntimeException expected because the Key is invalid.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test()
    public void testKeyUpShouldBeReturnRuntimeExceptionWithInvalidKeyCode() {
	Javauto javauto = new Javauto();
	int badKey = 0;

	try {
	    javauto.keyUp(badKey);
	    fail("RuntimeException expected because the keycode is not a valid key.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test()
    public void testKeyPressShouldBeReturnRuntimeExceptionWithInvalidKeyCode() {
	Javauto javauto = new Javauto();
	int badKey = 0;

	try {
	    javauto.keyPress(badKey);
	    fail("RuntimeException expected because the keycode is not a valid key.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    public void testSleephouldBeReturnRuntimeExceptionWithInvalidTimeout() {
	Javauto javauto = new Javauto();
	int timeout = 600001;

	try {
	    javauto.sleep(timeout);
	    fail("RuntimeException expected because the timeout is not a valid timeout.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    // Do not move your mouse :).
    public void testMouseMove() throws InterruptedException {
	Javauto javauto = new Javauto();

	// if x is larger than y.
	int x1 = 40;
	int y1 = 10;
	javauto.mouseMove(x1, y1);
	TimeUnit.SECONDS.sleep(1);

	int actualX = MouseInfo.getPointerInfo().getLocation().x;
	int actualY = MouseInfo.getPointerInfo().getLocation().y;

	assertThat(x1).isEqualTo(actualX);
	assertThat(y1).isEqualTo(actualY);

	// if y is larger than x.
	int x2 = 10;
	int y2 = 40;
	javauto.mouseMove(x2, y2);
	TimeUnit.SECONDS.sleep(1);

	actualX = MouseInfo.getPointerInfo().getLocation().x;
	actualY = MouseInfo.getPointerInfo().getLocation().y;

	assertThat(x2).isEqualTo(actualX);
	assertThat(y2).isEqualTo(actualY);

	// equals.
	int x3 = 40;
	int y3 = 40;
	javauto.mouseMove(x3, y3);
	TimeUnit.SECONDS.sleep(1);

	actualX = MouseInfo.getPointerInfo().getLocation().x;
	actualY = MouseInfo.getPointerInfo().getLocation().y;

	assertThat(x3).isEqualTo(actualX);
	assertThat(y3).isEqualTo(actualY);
    }

    @Test
    public void testMouseClickStringIntInt() throws InterruptedException {
	Javauto javauto = new Javauto();
	int locationX = (javauto.SCREEN_HEIGHT / 2) - (600 / 2);
	int locationY = (javauto.SCREEN_WIDTH / 2) - (800 / 2);

	TestPanel panel = new TestPanel(800, 600, locationX, locationY);
	javauto.setSpeed(0.5D);

	// Left
	int x = locationX + 120;
	int y = locationY + 120;
	javauto.mouseClick("left", x, y);
	TimeUnit.SECONDS.sleep(2);

	int actualX = MouseInfo.getPointerInfo().getLocation().x;
	int actualY = MouseInfo.getPointerInfo().getLocation().y;

	assertThat(panel.getClickedButton()).isEqualToIgnoringCase("left");
	assertThat(x).isEqualTo(actualX);
	assertThat(y).isEqualTo(actualY);

	// middle
	x = locationX + 130;
	y = locationY + 130;
	javauto.mouseMove(x, y);
	javauto.mouseClick("middle");
	TimeUnit.SECONDS.sleep(2);

	actualX = MouseInfo.getPointerInfo().getLocation().x;
	actualY = MouseInfo.getPointerInfo().getLocation().y;

	assertThat(panel.getClickedButton()).isEqualToIgnoringCase("middle");
	assertThat(x).isEqualTo(actualX);
	assertThat(y).isEqualTo(actualY);

	// right
	x = locationX + 140;
	y = locationY + 140;
	javauto.mouseMove(x, y);
	javauto.mouseClick("right");
	TimeUnit.SECONDS.sleep(2);

	actualX = MouseInfo.getPointerInfo().getLocation().x;
	actualY = MouseInfo.getPointerInfo().getLocation().y;

	assertThat(panel.getClickedButton()).isEqualToIgnoringCase("right");
	assertThat(x).isEqualTo(actualX);
	assertThat(y).isEqualTo(actualY);

	// Invalid
	x = locationX + 150;
	y = locationY + 150;
	javauto.mouseMove(x, y);
	javauto.mouseClick("INVALID");
	TimeUnit.SECONDS.sleep(2);

	actualX = MouseInfo.getPointerInfo().getLocation().x;
	actualY = MouseInfo.getPointerInfo().getLocation().y;

	assertThat(panel.getClickedButton()).isEqualToIgnoringCase("right");
	assertThat(x).isEqualTo(actualX);
	assertThat(y).isEqualTo(actualY);
    }

    @Test
    public void testMouseClickString() throws InterruptedException {
	Javauto javauto = new Javauto();
	int locationX = (javauto.SCREEN_HEIGHT / 2) - (600 / 2);
	int locationY = (javauto.SCREEN_WIDTH / 2) - (800 / 2);

	TestPanel panel = new TestPanel(800, 600, locationX, locationY);
	javauto.setSpeed(0.5D);

	// Left
	javauto.mouseMove(locationX + 120, locationY + 120);
	javauto.mouseClick("left");
	TimeUnit.SECONDS.sleep(2);
	assertThat(panel.getClickedButton()).isEqualToIgnoringCase("left");

	// middle
	javauto.mouseMove(locationX + 120, locationY + 120);
	javauto.mouseClick("middle");
	TimeUnit.SECONDS.sleep(2);
	assertThat(panel.getClickedButton()).isEqualToIgnoringCase("middle");

	// right
	javauto.mouseMove(locationX + 120, locationY + 120);
	javauto.mouseClick("right");
	TimeUnit.SECONDS.sleep(2);
	assertThat(panel.getClickedButton()).isEqualToIgnoringCase("right");

	// Nothing
	javauto.mouseMove(locationX + 120, locationY + 120);
	javauto.mouseClick("INVALID");
	TimeUnit.SECONDS.sleep(2);
	assertThat(panel.getClickedButton()).isEqualToIgnoringCase("right");
    }

    @Test
    public void testToDoubleString() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toDouble("0.0")).isEqualTo(0.0D);
    }

    @Test
    public void testToDoubleChar() {
	Javauto javauto = new Javauto();
	char c = "0".charAt(0);
	assertThat(javauto.toDouble(c)).isEqualTo(0.0D);
    }

    @Test
    public void testToDoubleBoolean() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toDouble(false)).isEqualTo(0.0D);
    }

    @Test
    public void testToDoubleInt() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toDouble(1)).isEqualTo(1.0D);
    }

    @Test
    public void testToDoubleLong() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toDouble(new Long(198546L))).isEqualTo(198546.0D);
    }

    @Test
    public void testToDoubleShort() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toDouble(new Short("10"))).isEqualTo(10.0D);
    }

    @Test
    public void testToDoubleFloat() {
	Javauto javauto = new Javauto();
	float f = 1F;
	assertThat(javauto.toDouble(f)).isEqualTo(1.0D);
    }

    @Test
    public void testToDoubleByte() {
	Javauto javauto = new Javauto();
	byte b = 10;
	assertThat(javauto.toDouble(b)).isEqualTo(10.0D);
    }

    @Test
    public void testToFloatString() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toFloat("10")).isEqualTo(10.0F);
    }

    @Test
    public void testToFloatChar() {
	Javauto javauto = new Javauto();
	char c = "0".charAt(0);
	assertThat(javauto.toFloat(c)).isEqualTo(0.0F);
    }

    @Test
    public void testToFloatBoolean() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toFloat(true)).isEqualTo(1.0F);
    }

    @Test
    public void testToFloatInt() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toFloat(1)).isEqualTo(1.0F);
    }

    @Test
    public void testToFloatLong() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toFloat(new Long(198546L))).isEqualTo(198546.0F);
    }

    @Test
    public void testToFloatShort() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toFloat(new Short("10"))).isEqualTo(10.0F);
    }

    @Test
    public void testToFloatDouble() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toFloat(new Double(10D))).isEqualTo(10.0F);
    }

    @Test
    public void testToFloatByte() {
	Javauto javauto = new Javauto();
	byte b = 10;
	assertThat(javauto.toFloat(b)).isEqualTo(10.0F);
    }

    @Test
    public void testToIntString() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toInt("1831")).isEqualTo(1831);
    }

    @Test
    public void testToIntChar() {
	Javauto javauto = new Javauto();
	char c = "1".charAt(0);
	assertThat(javauto.toInt(c)).isEqualTo(1);
    }

    @Test
    public void testToIntBoolean() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toInt(true)).isEqualTo(1);
    }

    @Test
    public void testToIntLong() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toInt(new Long(198546L))).isEqualTo(198546);
    }

    @Test
    public void testToIntShort() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toInt(new Short("10"))).isEqualTo(10);
    }

    @Test
    public void testToIntFloat() {
	Javauto javauto = new Javauto();
	float f = 1F;
	assertThat(javauto.toInt(f)).isEqualTo(1);
    }

    @Test
    public void testToIntDouble() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toInt(1.0D)).isEqualTo(1);
    }

    @Test
    public void testToIntByte() {
	Javauto javauto = new Javauto();
	byte b = 10;
	assertThat(javauto.toInt(b)).isEqualTo(10);
    }

    @Test
    public void testToCharString() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toChar("Fermat")).isEqualTo('F');
    }

    @Test
    public void testToStringChar() {
	Javauto javauto = new Javauto();
	char c = "1".charAt(0);
	assertThat(javauto.toString(c)).isEqualTo("1");
    }

    @Test
    public void testToStringBoolean() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toString(true)).isEqualTo("true");
    }

    @Test
    public void testToStringInt() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toString(1)).isEqualTo("1");
    }

    @Test
    public void testToStringLong() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toString(new Long(198546L))).isEqualTo("198546");
    }

    @Test
    public void testToStringShort() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toString(new Short("10"))).isEqualTo("10");
    }

    @Test
    public void testToStringFloat() {
	Javauto javauto = new Javauto();
	float f = 1F;
	assertThat(javauto.toString(f)).isEqualTo("1.0");
    }

    @Test
    public void testToStringDouble() {
	Javauto javauto = new Javauto();
	assertThat(javauto.toString(1.0D)).isEqualTo("1.0");
    }

    @Test
    public void testToStringByte() {
	Javauto javauto = new Javauto();
	byte b = 10;
	assertThat(javauto.toString(b)).isEqualTo("10");
    }

    @Test
    public void testFileExists() {
	Javauto javauto = new Javauto();
	String file = TEST_RESOURCES_PATH + TEST_FILE;
	assertThat(javauto.fileExists(file)).isEqualTo(true);
    }

    @Test
    public void testIsFile() {
	Javauto javauto = new Javauto();
	String file = TEST_RESOURCES_PATH + TEST_FILE;
	assertThat(javauto.isFile(file)).isEqualTo(true);
    }

    @Test
    public void testFileDelete() throws FileNotFoundException,
	    UnsupportedEncodingException {
	Javauto javauto = new Javauto();

	String file = TEST_RESOURCES_PATH + "lagrange";
	PrintWriter writer = new PrintWriter(file, "UTF-8");
	writer.println("The first line");
	writer.println("The second line");
	writer.close();

	javauto.fileDelete(file);
	assertThat(new File(file)).doesNotExist();

    }

    @Test
    public void testRmDir() {
	Javauto javauto = new Javauto();
	String dir = TEST_RESOURCES_PATH + "DIR";
	new File(dir).mkdirs();

	javauto.rmDir(dir);
	assertThat(new File(dir)).doesNotExist();
    }

    @Test
    public void testMkDir() {
	Javauto javauto = new Javauto();
	String dir = TEST_RESOURCES_PATH + "NEW";
	javauto.mkDir(dir);

	File theDir = new File(dir);
	assertThat(theDir).exists().isDirectory();
	theDir.delete();
    }

    @Test
    public void testFileCreate() {
	Javauto javauto = new Javauto();
	String filePath = TEST_RESOURCES_PATH + "evariste.txt";
	javauto.fileCreate(filePath);

	File file = new File(filePath);
	assertThat(file).exists().isFile();
	file.delete();
    }

    @Test
    public void testFileGetPath() {
	Javauto javauto = new Javauto();
	String filePath = TEST_RESOURCES_PATH + TEST_FILE;
	File file = new File(filePath);
	assertThat(javauto.fileGetPath(filePath)).isEqualToIgnoringCase(
		file.getAbsolutePath());
    }

    @Test
    public void testFileGetName() {
	Javauto javauto = new Javauto();
	String filePath = TEST_RESOURCES_PATH + TEST_FILE;
	File file = new File(filePath);
	assertThat(javauto.fileGetName(filePath)).isEqualToIgnoringCase(
		file.getName());
    }

    @Test
    public void testIsDirectory() {
	Javauto javauto = new Javauto();
	assertThat(javauto.isDirectory(TEST_RESOURCES_PATH)).isEqualTo(true);
	assertThat(javauto.isDirectory(TEST_RESOURCES_PATH + TEST_FILE))
		.isEqualTo(false);
	assertThat(javauto.isDirectory("NO")).isEqualTo(false);
    }

    @Test
    public void testFileRead() {
	Javauto javauto = new Javauto();
	String path = TEST_RESOURCES_PATH + TEST_FILE;
	assertThat(javauto.fileRead(path)).isEqualToIgnoringCase(
		"France Toulouse");
    }

    @Test
    public void testFileWrite() {
	Javauto javauto = new Javauto();
	String path = TEST_RESOURCES_PATH + "poincare.txt";
	String content = "France";

	javauto.fileWrite(path, content);
	File file = new File(path);
	assertThat(contentOf(file)).contains(content);
	file.delete();
    }

    @Test
    public void testFileAppend() throws FileNotFoundException,
	    UnsupportedEncodingException {
	Javauto javauto = new Javauto();

	String filePath = TEST_RESOURCES_PATH + "massera.txt";
	String content = "2";
	PrintWriter writer = new PrintWriter(filePath, "UTF-8");
	writer.println("1");
	writer.close();

	javauto.fileAppend(filePath, content);
	File file = new File(filePath);
	assertThat(contentOf(file)).contains(content);
	file.delete();
    }

    @Test
    public void testFileList() {
	Javauto javauto = new Javauto();
	String[] files = javauto.fileList(TEST_RESOURCES_PATH);
	assertThat(files.length).isGreaterThan(0);
    }

    @Test
    public void testClipboardPutAndGet() {
	Javauto javauto = new Javauto();
	String content = "buttner";
	javauto.clipboardPut(content);
	assertThat(javauto.clipboardGet()).isEqualTo(content);
    }

    @Test
    public void testHttpGet() {
	Javauto javauto = new Javauto();
	String urlHttp = "https://en.wikipedia.org/wiki/Rudolf_Carnap";
	String urlHttps = "https://en.wikipedia.org/wiki/Alonzo_Church";

	assertThat(javauto.httpGet(urlHttp)).contains("Logical positivism");
	assertThat(javauto.httpGet(urlHttps)).contains("lambda");
    }

    @Test
    public void testHttpGetShouldBeReturnEmpty() {
	Javauto javauto = new Javauto();
	String url = "http://noerterfasdpeor.com";
	assertThat(javauto.httpGet(url)).isEmpty();
    }

    @Test
    public void testGetPage() {
	Javauto javauto = new Javauto();
	String urlHttp = "https://en.wikipedia.org/wiki/Gary_R._Mar";
	String urlHttps = "https://en.wikipedia.org/wiki/Raymond_Smullyan";

	assertThat(javauto.getPage(urlHttp)).contains(
		"Philosophy of Mathematics");
	assertThat(javauto.getPage(urlHttps))
		.contains("American mathematician");
    }

    @Test
    public void testGetPageShouldBeReturnError() {
	Javauto javauto = new Javauto();
	String urlHttp = "https://seguad/logical/positivism";
	assertThat(javauto.getPage(urlHttp)).contains("Error");
    }

    @Test
    public void testHttpPost() {
	Javauto javauto = new Javauto();
	String url = "https://posttestserver.com/post.php";
	String[][] parameters = new String[][] { { "a", "1" }, { "b", "2" },
		{ "c", "3" } };

	assertThat(javauto.httpPost(url, parameters)).contains(
		"Successfully dumped 3 post variables.");
    }

    @Test
    public void testJoin() {
	Javauto javauto = new Javauto();
	String[] strArray = { "Jack Copeland", "Sebastian Guadagna" };
	String delimiter = "|";

	assertThat(javauto.join(delimiter, strArray)).isEqualToIgnoringCase(
		"Jack Copeland|Sebastian Guadagna");
    }

    @Test
    public void testArrayAsList() {
	Javauto javauto = new Javauto();
	String[] strArray = { "Jack Copeland", "Sebastian Gudagn" };
	List<String> expected = Arrays.asList(strArray);
	List<String> theList = javauto.arrayAsList(strArray);

	assertThat(theList).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void testArrayBinarySearchByte() {
	Javauto javauto = new Javauto();
	byte searchVal = 35;
	byte arr[] = { 10, 12, 34, searchVal, 5 };
	int index = javauto.arrayBinarySearch(arr, searchVal);

	assertThat(index).isEqualTo(4);
    }

    @Test
    public void testArrayBinarySearchChar() {
	Javauto javauto = new Javauto();
	char searchVal = 'c';
	char arr[] = { 'a', 'c', 'b', 'e', 'd' };
	int index = javauto.arrayBinarySearch(arr, searchVal);

	assertThat(index).isEqualTo(2);
    }

    @Test
    public void testArrayBinarySearchDouble() {
	Javauto javauto = new Javauto();
	double searchVal = 4.6;
	double arr[] = { 5.4, 49.2, 9.2, 35.4, 4.6 };
	int index = javauto.arrayBinarySearch(arr, searchVal);

	assertThat(index).isEqualTo(0);
    }

    @Test
    public void testArrayBinarySearchFloat() {
	Javauto javauto = new Javauto();
	float searchVal = 42.9f;
	float arr[] = { 5.2f, 46.1f, 42.9f, 22.3f };
	int index = javauto.arrayBinarySearch(arr, searchVal);

	assertThat(index).isEqualTo(2);
    }

    @Test
    public void testArrayBinarySearchInt() {
	Javauto javauto = new Javauto();
	int searchVal = 5;
	int arr[] = { 30, 20, 5, 12, 55 };
	int index = javauto.arrayBinarySearch(arr, searchVal);

	assertThat(index).isEqualTo(0);
    }

    @Test
    public void testArrayBinarySearchLong() {
	Javauto javauto = new Javauto();
	long searchVal = 46464;
	long arr[] = { 56, 46464, 3342, 232, 3445 };
	int index = javauto.arrayBinarySearch(arr, searchVal);

	assertThat(index).isEqualTo(4);
    }

    @Test
    public void testArrayBinarySearchShort() {
	Javauto javauto = new Javauto();
	short searchVal = 52;
	short arr[] = { 5, 2, 15, 52, 10 };
	int index = javauto.arrayBinarySearch(arr, searchVal);

	assertThat(index).isEqualTo(4);
    }

    @Test
    public void testArrayCopyOfBoolean() {
	Javauto javauto = new Javauto();
	boolean[] a1 = new boolean[] { true, false };
	boolean[] a2 = javauto.arrayCopyOf(a1, 4);

	assertThat(a2).hasSize(4).contains(true, atIndex(0))
		.contains(false, atIndex(1)).contains(false, atIndex(2))
		.contains(false, atIndex(3));
    }

    @Test
    public void testArrayCopyOfBooleanShouldBeReturnRuntimeExceptionWithNegativeLength() {
	Javauto javauto = new Javauto();
	boolean[] a1 = new boolean[] { true, false };

	try {
	    javauto.arrayCopyOf(a1, -1);
	    fail("RuntimeException expected because the new length is negative.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    public void testArrayCopyOfBytes() {
	Javauto javauto = new Javauto();
	byte[] a1 = new byte[] { 5, 62, 15 };
	byte[] a2 = javauto.arrayCopyOf(a1, 4);

	assertThat(a2).hasSize(4).contains((byte) 5, atIndex(0))
		.contains((byte) 62, atIndex(1))
		.contains((byte) 15, atIndex(2)).contains((byte) 0, atIndex(3));
    }

    @Test
    public void testArrayCopyOfBytesShouldBeReturnRuntimeExceptionWithNegativeLength() {
	Javauto javauto = new Javauto();
	byte[] a1 = new byte[] { 5, 62, 15 };

	try {
	    javauto.arrayCopyOf(a1, -1);
	    fail("RuntimeException expected because the new length is nagative.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    public void testArrayCopyOfChar() {
	Javauto javauto = new Javauto();
	char[] a1 = new char[] { 'p', 's', 'r' };
	char[] a2 = javauto.arrayCopyOf(a1, 4);

	assertThat(a2).hasSize(4).contains('p', atIndex(0))
		.contains('s', atIndex(1)).contains('r', atIndex(2));
    }

    @Test
    public void testArrayCopyOfCharShouldBeReturnRuntimeExceptionWithNegativeLength() {
	Javauto javauto = new Javauto();
	char[] a1 = new char[] { 'p', 's', 'r' };

	try {
	    javauto.arrayCopyOf(a1, -1);
	    fail("RuntimeException expected because the new length is negative.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    public void testArrayCopyOfDouble() {
	Javauto javauto = new Javauto();
	double[] a1 = new double[] { 12.5, 3.2, 37.5 };
	double[] a2 = javauto.arrayCopyOf(a1, 4);

	assertThat(a2).hasSize(4).contains(12.5, atIndex(0))
		.contains(3.2, atIndex(1)).contains(37.5, atIndex(2))
		.contains(0, atIndex(3));
    }

    @Test
    public void testArrayCopyOfDoubleShouldBeReturnRuntimeExceptionWithNegativeLength() {
	Javauto javauto = new Javauto();
	double[] a1 = new double[] { 12.5, 3.2, 37.5 };

	try {
	    javauto.arrayCopyOf(a1, -1);
	    fail("RuntimeException expected because the new length is negative.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    public void testArrayCopyOfFloat() {
	Javauto javauto = new Javauto();
	float[] a1 = new float[] { 10f, 32f, 25f };
	float[] a2 = javauto.arrayCopyOf(a1, 4);

	assertThat(a2).hasSize(4).contains(10f, atIndex(0))
		.contains(32f, atIndex(1)).contains(25f, atIndex(2))
		.contains(0f, atIndex(3));
    }

    @Test
    public void testArrayCopyOfFloatShouldBeReturnRuntimeExceptionWithNegativeLength() {
	Javauto javauto = new Javauto();
	float[] a1 = new float[] { 10f, 32f, 25f };

	try {
	    javauto.arrayCopyOf(a1, -1);
	    fail("RuntimeException expected because the new length is negative.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    public void testArrayCopyOfInt() {
	Javauto javauto = new Javauto();
	int[] a1 = new int[] { 45, 32, 75 };
	int[] a2 = javauto.arrayCopyOf(a1, 4);

	assertThat(a2).hasSize(4).contains(45, atIndex(0))
		.contains(32, atIndex(1)).contains(75, atIndex(2))
		.contains(0, atIndex(3));
    }

    @Test
    public void testArrayCopyOfIntShouldBeReturnRuntimeExceptionWithNegativeLength() {
	Javauto javauto = new Javauto();
	int[] a1 = new int[] { 45, 32, 75 };

	try {
	    javauto.arrayCopyOf(a1, -1);
	    fail("RuntimeException expected because the new length is negative.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    public void testArrayCopyOfLong() {
	Javauto javauto = new Javauto();
	long[] a1 = new long[] { 15, 10, 45 };
	long[] a2 = javauto.arrayCopyOf(a1, 4);

	assertThat(a2).hasSize(4).contains(15, atIndex(0))
		.contains(10, atIndex(1)).contains(45, atIndex(2))
		.contains(0L, atIndex(3));
    }

    @Test
    public void testArrayCopyOfLongShouldBeReturnRuntimeExceptionWithNegativeLength() {
	Javauto javauto = new Javauto();
	long[] a1 = new long[] { 15, 10, 45 };

	try {
	    javauto.arrayCopyOf(a1, -1);
	    fail("RuntimeException expected because the new length is negative.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }

    @Test
    public void testArrayCopyOfShort() {
	Javauto javauto = new Javauto();
	short[] a1 = new short[] { 15, 10, 45 };
	short[] a2 = javauto.arrayCopyOf(a1, 4);

	assertThat(a2).hasSize(4).contains((short) 15, atIndex(0))
		.contains((short) 10, atIndex(1))
		.contains((short) 45, atIndex(2))
		.contains((short) 0, atIndex(3));
    }

    @Test
    public void testArrayCopyOfShortShouldBeReturnRuntimeExceptionWithNegativeLength() {
	Javauto javauto = new Javauto();
	short[] a1 = new short[] { 15, 10, 45 };

	try {
	    javauto.arrayCopyOf(a1, -1);
	    fail("RuntimeException expected because the new length is negative.");
	} catch (RuntimeException e) {
	    assertThat(e);
	}
    }
}
