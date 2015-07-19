package com.automation.javauto.compiler;

import javax.tools.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Attempt to make any compiler errors from {@link CustomJavaCompiler} relevant
 * to the user's original Javauto code and display these errors.
 * 
 * @author matthew.downey
 *
 */
public class DealWithCompilerErrors {
    /**
     * Handle errors that may crop up during compilation. This entails letting
     * the user know what caused the error and where.
     */
    public static void dealWithIt(
	    DiagnosticCollector<JavaFileObject> compileErrors,
	    String generatedCode, String userCode, String userCodeFileName) {
	for (Diagnostic diagnostic : compileErrors.getDiagnostics()) {
	    /* get the text of the line the error appears on */
	    String line = getLine(diagnostic.getPosition(), generatedCode);

	    /*
	     * get all line numbers in the original code that contain the error
	     * line
	     */
	    ArrayList<Integer> originalLineNums = getOriginalLineNums(line,
		    userCode);

	    /* if we only found it once in the original code we're all good! */
	    if (originalLineNums.size() == 1) {
		System.out
			.println(userCodeFileName
				+ " line "
				+ (originalLineNums.get(0) + 1)
				+ "\n"
				+ line
				+ "\n"
				+ diagnostic.getKind().toString().toLowerCase()
				+ ": "
				+ diagnostic.getMessage(null).replace(
					"java.lang.", ""));
	    }

	    /*
	     * if we found it more than once specify where in the generated code
	     * to look for it and tell them to use --generate
	     */
	    else {
		String className = new File(userCodeFileName).getName().split(
			"\\.(?=[^\\.]+$)")[0];
		System.out
			.println("Generated file: "
				+ className
				+ ".java line "
				+ getLineNumber((int) diagnostic.getPosition(),
					generatedCode)
				+ "\n"
				+ line
				+ "\n"
				+ diagnostic.getKind().toString().toLowerCase()
				+ ": "
				+ diagnostic.getMessage(null).replace(
					"java.lang.", ""));
		System.out
			.println("This was an error in generated code -- to understand what caused the error please compile with the --generate flag to enable the writing of generated files. During compilation a directory called \""
				+ className
				+ "/gen/\" will be created, and this directory will contain "
				+ className
				+ ".java, the file that caused the error.");
	    }
	}
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
     * Gets the line numbers in the user code of lines that exactly match the
     * given line of generated code. That way if a line of generated code causes
     * an error and appears only once in the user code we can inform the user of
     * the line of their code that causes an error.
     * 
     * @param line
     *            the line of generated code that causes an error
     * @param userCode
     *            the user code file to match the line against
     * @return an ArrayList of lines that match the error line
     */
    private static ArrayList<Integer> getOriginalLineNums(String line,
	    String userCode) {
	ArrayList<Integer> lineNums = new ArrayList<Integer>();
	Integer l = 0;
	for (String userLine : userCode.split("\n")) {
	    if (userLine.trim().equals(line.trim())
		    || removeComments(userLine).trim().equals(line.trim())) {
		lineNums.add(l);
	    }
	    l = l + 1;
	}
	return lineNums;
    }

    /**
     * Get the whole line that contains the error.
     * 
     * @param startPosition
     *            the Diagnostic.getPosition() value for the error
     * @param generatedCode
     *            the code that caused the error
     * @return the line containing the error
     */
    private static String getLine(long startPosition, String generatedCode) {
	/* get the line bounds */
	int[] bounds = getLineBounds((int) startPosition, generatedCode);

	/* return the line based on the bounds */
	return generatedCode.substring(bounds[0], bounds[1]);
    }

    private static int[] getLineBounds(int pos, String generatedCode) {
	/*
	 * find the indexes of the "\n" characters on either side of the
	 * position
	 */
	return new int[] {
		generatedCode.substring(0, pos).lastIndexOf("\n") + 1,
		pos + generatedCode.substring(pos).indexOf("\n") };
    }

    private static String removeComments(String line) {
	ArrayList<Integer[]> commentIgnores = getIgnoreIndexes(line).get(0);
	String temp = "";
	for (int i = 0; i < line.length(); i++) {
	    if (!inIgnoredText(i, commentIgnores))
		temp = temp + line.substring(i, i + 1);
	}
	return temp;
    }

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
		if (index >= codeChars.length)
		    break;

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
		    endIndex = index;
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
			endIndex = codeChars.length - 1;
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
