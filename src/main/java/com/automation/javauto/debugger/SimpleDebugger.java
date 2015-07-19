package com.automation.javauto.debugger;

import java.util.ArrayList;
import java.util.regex.*;
import java.util.Collections;
import java.io.*;

import com.automation.javauto.parser.Create;

/**
 * Takes Java code and does a preliminary debugging to make sure things are
 * ready to be compiled.
 * 
 * @author matthew.downey
 *
 */
public class SimpleDebugger {

    /* list to hold errors */
    private static ArrayList<String[]> errors = new ArrayList<String[]>();
    private static ArrayList<Integer> errorLines = new ArrayList<Integer>();

    private static String userCodeFileName = "";
    private static String javautoFile = "Javauto.java";

    public static void main(String[] args) {
	String userCode = fileRead("thisHasErrors.ja");
	debug(userCode, "thisHasErrors.ja");
    }

    /**
     * Does some simple debugging of a user code file.
     * 
     * @param userCode
     *            the user code to debug
     * @return true only if there are no errors, false otherwise
     */
    public static boolean debug(String userCode, String fileName) {
	/*
	 * set the global file name so errors can be specific about where an
	 * error happens
	 */
	userCodeFileName = fileName;

	/*
	 * build ignore lists for the data -- this step also handles unclosed
	 * String literals, character literals, and comments.
	 */
	ArrayList<Integer[]> commentIgnores = getIgnoreIndexes(userCode).get(0);

	/* get rid of all the comments */
	String t = "";
	for (int i = 0; i < userCode.length(); i++) {
	    if (!inIgnoredText(i, commentIgnores))
		t = t + userCode.substring(i, i + 1);
	}

	userCode = t;

	/* build a list of literal ignores */
	ArrayList<Integer[]> allIgnores = getIgnoreIndexes(userCode).get(1);

	/* we're gonna need a character array and a line array */
	char[] userChars = userCode.toCharArray();
	String[] userLines = userCode.split("\n");

	/* first we check to make sure every { has a close } */
	for (int i = 0; i < userChars.length; i++) {
	    if (userChars[i] == '{' && !inIgnoredText(i, allIgnores)) {
		findEndBracket(i, userCode, allIgnores);
	    }
	}

	/* now make sure every } has a { */
	for (int i = userChars.length - 1; i >= 0; i--) {
	    if (userChars[i] == '}' && !inIgnoredText(i, allIgnores)) {
		findStartBracket(i, userCode, allIgnores);
	    }
	}

	/* make sure every line with a ( has a corresponding ) */
	for (int l = 0; l < userLines.length; l++) {
	    /* use these indexes to populate a list of general ignore indexes */
	    ArrayList<Integer[]> allIgs = getIgnoreIndexes(userLines[l]).get(1);

	    char[] lineChars = userLines[l].toCharArray();

	    /* make sure each ( has a ) */
	    for (int i = 0; i < lineChars.length; i++) {
		if (lineChars[i] == '(' && !inIgnoredText(i, allIgs)) {
		    findEndParen(i, userLines[l], allIgs, l);
		}

		if (lineChars[i] == ')' && !inIgnoredText(i, allIgs)) {
		    findStartParen(i, userLines[l], allIgs, l);
		}
	    }
	}

	/* make sure string replacements are all right */
	checkStringReplaceFormat(userCode);

	/* make sure any javauto functions/constants are in the right case */
	checkJavautoCase(userCode, allIgnores);

	/* make sure the user doesn't try to override a built in function */
	checkFunctionDeclarations(userCode, allIgnores);

	/* make sure the user doesn't override a javauto variable */
	checkVariableDeclarations(userCode, allIgnores);

	/* if there are no errors return true */
	if (errors.size() == 0)
	    return false;

	/* remove duplicate line numbers from errorLines */
	ArrayList<Integer> temp = new ArrayList<Integer>();
	temp.addAll(errorLines);
	errorLines = new ArrayList<Integer>();
	for (Integer j : temp)
	    if (!errorLines.contains(j))
		errorLines.add(j);

	/* at the end we display the errors in order of line number */
	Collections.sort(errorLines);
	for (int el : errorLines) {
	    for (String[] e : errors) {
		if (el == Integer.parseInt(e[0]))
		    System.out.println(e[1] + "\n");
	    }
	}

	/* show how many errors */
	int errorCount = errors.size();
	if (errorCount == 1)
	    System.out.println(errors.size() + " error.");
	else
	    System.out.println(errors.size() + " errors.");

	return true;
    }

    /**
     * Find the line that a character appears on based on the character
     * position.
     * 
     * @param pos
     *            the position of the character
     * @param generatedCode
     *            the string the character position is in
     * @return the line number containing the character
     */
    private static int getLineNumber(int pos, String generatedCode) {
	int line = 1;
	char[] genChars = generatedCode.toCharArray();
	for (int i = 0; i < pos; i++)
	    if (genChars[i] == '\n')
		line++;
	return line;
    }

    /**
     * Make sure the python file String replacement is formatted like: String s
     * = "Hello, my name is %s" % ("John") with the parenthesis after the %
     * 
     * @param userCode
     *            the code we're auditing
     */
    private static void checkStringReplaceFormat(String userCode) {
	/* regex to find bad replacements */
	String regexp = "\".*\"\\s*%\\s+";
	Pattern pattern = Pattern.compile(regexp);
	Matcher matcher = pattern.matcher(userCode);

	while (matcher.find()) {
	    /* get the next char that's supposed to be a ( */
	    int end = matcher.end();
	    String nextChar = userCode.substring(end, end + 1);

	    /* add an error if it isn't a ( */
	    if (!nextChar.equals("(")) {
		int lineNum = getLineNumber(end, userCode);
		errorLines.add(lineNum);
		String message = "Error in "
			+ userCodeFileName
			+ " line "
			+ lineNum
			+ "\n"
			+ "\t"
			+ userCode.split("\n")[lineNum - 1]
			+ "\n"
			+ "Expected a ( after the %.\nLike \"Hello %s\" % (\"John\")";
		errors.add(new String[] { String.valueOf(lineNum), message });

	    }
	}

    }

    private static void checkVariableDeclarations(String userCode,
	    ArrayList<Integer[]> allIgnores) {
	ArrayList<String> vars = getVariables();
	ArrayList<String> lowerVars = new ArrayList<String>();
	for (String s : vars)
	    lowerVars.add(s.toLowerCase());

	String[] lines = userCode.split("\n");
	for (int i = 0; i < lines.length; i++) {
	    /* regex to search for variable declaration */
	    String regexp = "^\\s*[\\S+\\s+]*\\s+(\\w+)\\s*=|^\\s*(\\w+)\\s*=";
	    Pattern pattern = Pattern.compile(regexp);
	    Matcher matcher = pattern.matcher(lines[i]);

	    while (matcher.find()) {
		String found = matcher.group(1);

		/* if it's not part of the first half it's the second */
		if (found == null)
		    found = matcher.group(2);

		if (lowerVars.contains(found.toLowerCase())
			|| found.toLowerCase().equals("args")) {
		    errorLines.add(i + 1);
		    String message = "Error in " + userCodeFileName + " line "
			    + (i + 1) + "\n" + "\t" + userCode.split("\n")[i]
			    + "\n" + "Cannot override \"" + found
			    + "\", use a different name.";
		    errors.add(new String[] { String.valueOf(i + 1), message });
		}
	    }

	}

    }

    private static void checkFunctionDeclarations(String userCode,
	    ArrayList<Integer[]> allIgnores) {
	ArrayList<String> funcs = getFunctions();
	ArrayList<String> lowerFuncs = new ArrayList<String>();
	for (String s : funcs)
	    lowerFuncs.add(s.toLowerCase());

	String[] lines = userCode.split("\n");
	for (int i = 0; i < lines.length; i++) {
	    /* regex to search for function declaration */
	    String regexp = "^\\s*func[\\s+\\w+]+\\s(\\w+)\\s*\\(";
	    Pattern pattern = Pattern.compile(regexp);
	    Matcher matcher = pattern.matcher(lines[i]);

	    while (matcher.find()) {
		String found = matcher.group(1);
		if (lowerFuncs.contains(found.toLowerCase())) {
		    errorLines.add(i + 1);
		    String message = "Error in "
			    + userCodeFileName
			    + " line "
			    + (i + 1)
			    + "\n"
			    + "\t"
			    + userCode.split("\n")[i]
			    + "\n"
			    + "Cannot override this function, use a different name.";
		    errors.add(new String[] { String.valueOf(i + 1), message });
		}
	    }
	}
    }

    private static void checkJavautoCase(String userCode,
	    ArrayList<Integer[]> allIgnores) {
	ArrayList<String> funcs = getJavauto();
	for (String s : funcs) {

	    /* regex to search for function s within code */
	    String regexp = "(?i)([^a-zA-Z0-9]+)(" + s + ")([^a-zA-Z0-9]+)";
	    Pattern pattern = Pattern.compile(regexp);
	    Matcher matcher = pattern.matcher(userCode);

	    while (matcher.find()) {
		String foundFunc = matcher.group(2);
		int index = matcher.start() + matcher.group(1).length();
		if (!foundFunc.equals(s)
			&& foundFunc.toLowerCase().equals(s.toLowerCase())
			&& !inIgnoredText(index, allIgnores)) {
		    int lineNum = getLineNumber(index, userCode);
		    errorLines.add(lineNum);
		    String message = "Error in " + userCodeFileName + " line "
			    + lineNum + "\n" + "\t"
			    + userCode.split("\n")[lineNum - 1] + "\n"
			    + "Case of \"" + foundFunc
			    + "\" is incorrect, it should be \"" + s + "\".";
		    errors.add(new String[] { String.valueOf(lineNum), message });
		}
	    }

	    /*
	     * do the whole regex again looking for func at the beginning of
	     * file
	     */
	    regexp = "(?i)^(" + s + ")[^a-zA-Z0-9]+";
	    pattern = Pattern.compile(regexp);
	    matcher = pattern.matcher(userCode);

	    while (matcher.find()) {
		String foundFunc = matcher.group(1);
		if (!foundFunc.equals(s)
			&& foundFunc.toLowerCase().equals(s.toLowerCase())) {
		    int lineNum = 1;
		    errorLines.add(lineNum);
		    String message = "Error in " + userCodeFileName + " line "
			    + lineNum + "\n" + "\t"
			    + userCode.split("\n")[lineNum - 1] + "\n"
			    + "Case of \"" + foundFunc
			    + "\" is incorrect, it should be \"" + s + "\".";
		    errors.add(new String[] { String.valueOf(lineNum), message });
		}
	    }
	}
    }

    /**
     * Search the javautoFile for all class variables and return them
     * 
     * @return class variables that have been generated
     */
    private static ArrayList<String> getVariables() {
	/* get raw file contents of the file containing our variables */
	String[] variablesContents = resourceRead(javautoFile).split("\n");

	/* variable to store our generated class vars */
	ArrayList<String> classVars = new ArrayList<String>();

	/* add each line that has a class varaible to our classVars */
	for (String line : variablesContents) {
	    if ((line.trim().startsWith("public ") || line.trim().startsWith(
		    "private "))
		    && line.trim().endsWith(";")) {
		String[] parts = line.split("=")[0].split(" ");
		String name = parts[parts.length - 1].trim();
		classVars.add(name);
	    }
	}

	/* return our class vars */
	return classVars;
    }

    /**
     * Gets a list of functions and their source code from the javautoFile
     */
    private static ArrayList<String> getFunctions() {
	/* define the list where we'll store all the data */
	ArrayList<String> functionDataList = new ArrayList<String>();

	/* get raw file contents of the file containing our functions */
	String functionContents = resourceRead(javautoFile);

	/* split into lines for evaluation */
	String[] functionContentsLines = functionContents.split("\n");

	/* check each line and extract function names */
	for (String line : functionContentsLines) {
	    /*
	     * if the line is like "public *{" or "private *{" but isn't like
	     * "public class"
	     */
	    if ((line.trim().startsWith("public ") || line.trim().startsWith(
		    "private "))
		    && (line.trim().endsWith("{"))
		    && (!line.trim().startsWith("public class"))) {
		/*
		 * if it meets the above criteria it's a function & we add it to
		 * the list
		 */

		/* get function name & code */
		String functionName = line.trim().split(" ")[2].split("[(]")[0];

		/*
		 * add it to the list unless it's the "run" function used in a
		 * thread
		 */
		if (!functionName.toLowerCase().equals("run")
			&& !functionDataList.contains(functionName.trim())) {
		    functionDataList.add(functionName.trim());
		}
	    }
	}

	/* return our list without duplicates */
	return functionDataList;
    }

    private static ArrayList<String> getJavauto() {
	/* define the list where we'll store all the data */
	ArrayList<String> functionDataList = new ArrayList<String>();

	/* get raw file contents of the file containing our functions */
	String functionContents = resourceRead(javautoFile);

	/* split into lines for evaluation */
	String[] functionContentsLines = functionContents.split("\n");

	/* check each line and extract function names */
	for (String line : functionContentsLines) {
	    /*
	     * if the line is like "public *{" or "private *{" but isn't like
	     * "public class"
	     */
	    if ((line.trim().startsWith("public ") || line.trim().startsWith(
		    "private "))
		    && (line.trim().endsWith("{"))
		    && (!line.trim().startsWith("public class"))) {
		/*
		 * if it meets the above criteria it's a function & we add it to
		 * the list
		 */

		/* get function name & code */
		String functionName = line.trim().split(" ")[2].split("[(]")[0];

		/*
		 * add it to the list unless it's the "run" function used in a
		 * thread
		 */
		if (!functionName.toLowerCase().equals("run")) {
		    String function = functionName.trim();
		    if (!functionDataList.contains(function))
			functionDataList.add(function);
		}
	    }

	    /* if it's a variable */
	    else if ((line.trim().startsWith("public ") || line.trim()
		    .startsWith("private ")) && line.trim().endsWith(";")) {
		String[] parts = line.split("=")[0].split(" ");
		String name = parts[parts.length - 1].trim();
		functionDataList.add(name);
	    }
	}

	/* return our list without duplicates */
	return functionDataList;
    }

    /**
     * Reads file contents (from a resource inside the JAR) into string
     * 
     * @param resourcePath
     *            the path to the resource within the JAR file
     * @return file contents as string
     */
    private static String resourceRead(String resourcePath) {
	try {
	    InputStream inputStream = Thread.currentThread()
		    .getContextClassLoader().getResourceAsStream(resourcePath);

	    BufferedReader bufferedReader = new BufferedReader(
		    new InputStreamReader(inputStream, "UTF-8"));
	    StringBuilder stringBuilder = new StringBuilder();
	    String line = bufferedReader.readLine();
	    while (line != null) {
		stringBuilder.append(line);
		if (line != null) {
		    stringBuilder.append("\n");
		}
		line = bufferedReader.readLine();
	    }
	    return stringBuilder.toString();
	} catch (Exception e) {
	    System.out
		    .println("Compiler encountered an error reading JAR resource \""
			    + resourcePath
			    + "\" -- the JAR file may be corrupt.");
	    e.printStackTrace();
	    System.exit(1);
	    return "";
	}
    }

    /**
     * Reads file contents into a string
     * 
     * @param filePath
     *            path of file to read
     * @return file contents as string
     */
    private static String fileRead(String filePath) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(filePath));
	    StringBuilder data = new StringBuilder();
	    String line = br.readLine();
	    while (line != null) {
		data.append(line);
		data.append('\n');
		line = br.readLine();
	    }
	    String fileData = data.toString();
	    br.close();
	    return fileData;
	} catch (Exception e) {
	    System.out.println("Compiler encountered an error reading file \""
		    + filePath + "\"");
	    e.printStackTrace();
	    System.exit(1);
	    return "null";
	}
    }

    /**
     * Given the index of a '{', find the corresponding '}'.
     * 
     * @param startBracket
     *            the index of the '{'.
     * @param context
     *            the string within which the parens are located.
     * @param indexesToIgnore
     *            A list of Integers like [start, end] that specify indexes that
     *            should be ignored, this list usually contains the start and
     *            end bounds of each comment and string literal in the context.
     * @return the index of the closing paren
     */
    private static int findEndBracket(int startBracket, String context,
	    ArrayList<Integer[]> indexesToIgnore) {
	char[] contextChars = context.toCharArray();
	int index = startBracket + 1;
	int open = 1;
	int close = 0;
	try {
	    while (open > close) {
		/*
		 * if the current character is a ( and it's not inside a comment
		 * or literal
		 */
		if (contextChars[index] == '{'
			&& !inIgnoredText(index, indexesToIgnore)) {
		    open++;
		}
		/*
		 * if the current character is a ) and it's not inside a comment
		 * or literal
		 */
		else if (contextChars[index] == '}'
			&& !inIgnoredText(index, indexesToIgnore)) {
		    close++;
		}
		/* on to the next one */
		index++;
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    int lineNum = getLineNumber(startBracket, context);
	    errorLines.add(lineNum);
	    String message = "Error in " + userCodeFileName + " line "
		    + lineNum + "\n" + "\t" + context.split("\n")[lineNum - 1]
		    + "\n" + "Expected corresponding '}'";
	    errors.add(new String[] { String.valueOf(lineNum), message });
	}
	return index;
    }

    private static int findStartBracket(int endBracket, String context,
	    ArrayList<Integer[]> indexesToIgnore) {
	char[] contextChars = context.toCharArray();
	int index = endBracket - 1;
	int open = 0;
	int close = 1;
	try {
	    while (close > open) {
		/*
		 * if the current character is a ( and it's not inside a comment
		 * or literal
		 */
		if (contextChars[index] == '{'
			&& !inIgnoredText(index, indexesToIgnore)) {
		    open++;
		}
		/*
		 * if the current character is a ) and it's not inside a comment
		 * or literal
		 */
		else if (contextChars[index] == '}'
			&& !inIgnoredText(index, indexesToIgnore)) {
		    close++;
		}
		/* on to the next one */
		index--;
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    int lineNum = getLineNumber(endBracket, context);
	    errorLines.add(lineNum);
	    String message = "Error in " + userCodeFileName + " line "
		    + lineNum + "\n" + "\t" + context.split("\n")[lineNum - 1]
		    + "\n" + "Unexpected '}'";
	    errors.add(new String[] { String.valueOf(lineNum), message });
	}
	return index;
    }

    /**
     * Given the index of a '(', find the corresponding ')'.
     * 
     * @param startParen
     *            the index of the '('.
     * @param context
     *            the string within which the parens are located.
     * @param indexesToIgnore
     *            A list of Integers like [start, end] that specify indexes that
     *            should be ignored, this list usually contains the start and
     *            end bounds of each comment and string literal in the context.
     * @return the index of the closing paren
     */
    private static int findEndParen(int startParen, String context,
	    ArrayList<Integer[]> indexesToIgnore, int lineNumber) {
	char[] contextChars = context.toCharArray();
	int index = startParen + 1;
	int open = 1;
	int close = 0;
	try {
	    while (open > close) {
		/*
		 * if the current character is a ( and it's not inside a comment
		 * or literal
		 */
		if (contextChars[index] == '('
			&& !inIgnoredText(index, indexesToIgnore)) {
		    open++;
		}
		/*
		 * if the current character is a ) and it's not inside a comment
		 * or literal
		 */
		else if (contextChars[index] == ')'
			&& !inIgnoredText(index, indexesToIgnore)) {
		    close++;
		}
		/* on to the next one */
		index++;
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    errorLines.add(lineNumber);
	    String message = "Error in " + userCodeFileName + " line "
		    + (lineNumber + 1) + "\n" + "\t" + context + "\n"
		    + "Unclosed '(', expected corresponding ')'";
	    errors.add(new String[] { String.valueOf(lineNumber), message });
	}
	return index;
    }

    private static int findStartParen(int endParen, String context,
	    ArrayList<Integer[]> indexesToIgnore, int lineNumber) {
	char[] contextChars = context.toCharArray();
	int index = endParen - 1;
	int open = 0;
	int close = 1;
	try {
	    while (close > open) {
		/*
		 * if the current character is a ( and it's not inside a comment
		 * or literal
		 */
		if (contextChars[index] == '('
			&& !inIgnoredText(index, indexesToIgnore)) {
		    open++;
		}
		/*
		 * if the current character is a ) and it's not inside a comment
		 * or literal
		 */
		else if (contextChars[index] == ')'
			&& !inIgnoredText(index, indexesToIgnore)) {
		    close++;
		}
		/* on to the next one */
		index--;
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    errorLines.add(lineNumber);
	    String message = "Error in " + userCodeFileName + " line "
		    + (lineNumber + 1) + "\n" + "\t" + context + "\n"
		    + "Unexpected ')'";
	    errors.add(new String[] { String.valueOf(lineNumber), message });
	}
	return index;
    }

    /**
     * Determines if a character is inside any of the bounds of a list of blocks
     * of text to ignore.
     * 
     * @param charIndex
     *            the index of the character whose position is being checked
     * @param ignoredIndex
     *            a list of items like [int startIgnoreIndex, int
     *            endIgnoreIndex] that specifies which indexes are supposed to
     *            be ignored
     * @return returns true if the character index is within any of the ignore
     *         bounds, otherwise returns false.
     */
    private static boolean inIgnoredText(int charIndex,
	    ArrayList<Integer[]> ignoredIndexes) {
	/* check each set of bounds to see if they hold our charIndex */
	for (Integer[] i : ignoredIndexes) {
	    if (charIndex >= i[0] && charIndex <= i[1])
		return true;
	}

	/* if we got to here we haven't found it inside any of the bounds */
	return false;
    }

    /**
     * Build a list of character indexes that should be ignored during
     * processing. Characters to be ignored are those inside of comments, string
     * literals, and character literals. This function returns the bounds of
     * indexes of characters and literals in two separate lists.
     * 
     * @param code
     *            the string to generate this list of bounds for
     * @return returns two ArrayList<Integer[]> objects, wrapped inside of one
     *         ArrayList, with the format [comment indexes, literal indexes].
     *         Then each of those elements is a list with the format [start
     *         ignore index, end ignore index].
     */
    private static ArrayList<ArrayList<Integer[]>> getIgnoreIndexes(String code) {
	/* Lists to hold bounds for both comments and literals */
	ArrayList<Integer[]> commentIndexes = new ArrayList<Integer[]>();
	ArrayList<Integer[]> literalIndexes = new ArrayList<Integer[]>();

	/*
	 * List to hold our other two lists, so that the user can separate them
	 * at will
	 */
	ArrayList<ArrayList<Integer[]>> finalValues = new ArrayList<ArrayList<Integer[]>>();

	/*
	 * iterate through character by character looking for comments and
	 * literals
	 */
	char[] codeChars = code.toCharArray();
	int index = 0;
	while (index < codeChars.length) {
	    if (codeChars[index] == '/') {
		Integer startIndex = -1;
		Integer endIndex = -1;

		index++;

		if (codeChars[index] == '/') {
		    startIndex = index - 1;
		    index++;
		    while (codeChars[index] != '\n') {
			index++;
			if (index == codeChars.length) {
			    index--;
			    break;
			}
		    }
		    endIndex = index - 1;
		    index++;
		} else if (codeChars[index] == '*') {
		    startIndex = index - 1;
		    index++;

		    try {
			boolean done = false;
			while (!done) {
			    if (codeChars[index] == '*') {
				index++;
				if (codeChars[index] == '/') {
				    done = true;
				    endIndex = index;
				}
			    }
			    index++;
			}
		    } catch (ArrayIndexOutOfBoundsException e) {
			int lineNum = getLineNumber(startIndex, code);
			errorLines.add(lineNum);

			String message = "Error in " + userCodeFileName
				+ " line " + lineNum + "\n" + "\t"
				+ code.split("\n")[lineNum - 1] + "\n"
				+ "Unclosed comment.";
			errors.add(new String[] { String.valueOf(lineNum),
				message });
		    }
		}

		/* if we found stuff add to array */
		if (startIndex != -1)
		    commentIndexes.add(new Integer[] { startIndex, endIndex });
	    } else if (codeChars[index] == '"') {
		Integer startIndex = index;
		Integer endIndex = -1;
		index++;

		try {
		    while (codeChars[index] != '"') {
			if (codeChars[index] == '\\') {
			    index++;
			}
			index++;
		    }
		    endIndex = index;
		    index++;
		} catch (ArrayIndexOutOfBoundsException e) {
		    int lineNum = getLineNumber(startIndex, code);
		    errorLines.add(lineNum);

		    String message = "Error in " + userCodeFileName + " line "
			    + lineNum + "\n" + "\t"
			    + code.split("\n")[lineNum - 1] + "\n"
			    + "Unclosed string literal.";
		    errors.add(new String[] { String.valueOf(lineNum), message });
		}

		/* add to array */
		literalIndexes.add(new Integer[] { startIndex, endIndex });
	    } else if (codeChars[index] == '\'') {
		Integer startIndex = index;
		Integer endIndex = -1;
		index++;

		try {
		    while (codeChars[index] != '\'') {
			if (codeChars[index] == '\\') {
			    index++;
			}
			index++;
		    }
		    endIndex = index;
		    index++;
		} catch (ArrayIndexOutOfBoundsException e) {
		    int lineNum = getLineNumber(startIndex, code);
		    errorLines.add(lineNum);

		    String message = "Error in " + userCodeFileName + " line "
			    + lineNum + "\n" + "\t"
			    + code.split("\n")[lineNum - 1] + "\n"
			    + "Unclosed character literal.";
		    errors.add(new String[] { String.valueOf(lineNum), message });
		}

		/* add to array */
		literalIndexes.add(new Integer[] { startIndex, endIndex });
	    } else {
		index++;
	    }
	}

	/* put our two lists inside of a third list */
	finalValues.add(commentIndexes);
	finalValues.add(literalIndexes);

	/* return our final list */
	return finalValues;
    }
}
