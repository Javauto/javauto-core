package com.automation.javauto.parser;

import java.io.*;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.*;

import javax.tools.*;

import com.automation.javauto.compiler.CustomJarCompiler;
import com.automation.javauto.compiler.CustomJavaCompiler;
import com.automation.javauto.debugger.SimpleDebugger;
import com.automation.javauto.compiler.DealWithCompilerErrors;

/**
 * 
 * This class handles the top level user code parsing. It takes a .ja or
 * .javauto file and parses a copy of the Javauto.java file in order to create
 * functions, it creates struct objects and class variables, and then it uses
 * the {@link SimpleDebugger} class to check its generated code. If everything
 * checks out, it runs the code through {@link CustomJavaCompiler} to generate
 * class files; any errors at this point are sent directly to
 * {@link DealWithCompilerErrors} to be displayed. Finally
 * {@link CustomJarCompiler} * is used to bundle the class files into a jar.
 * 
 * @author matthew.downey
 *
 */
public class Create {
    /* define constants for different terminal colors */
    private static String RED = "\033[91m";
    private static String GREEN = "\033[92m";
    private static String YELLOW = "\033[93m";
    private static String BLUE = "\033[96m";
    private static String NORMAL = "\033[0m";

    /* controls whether we're in verbose mode */
    private static boolean verbose = false;

    /* controls whether we're generating .java files */
    private static boolean genJava = false;

    /* controls whether we're keeping the generated .class files */
    private static boolean keepClass = false;

    /* the file our javauto functions are located in */
    private static String javautoFile = "Javauto.java";

    /* used by various functions to determine if they've printed to terminal */
    private static boolean printedResults = false;

    /* if this variable is true we will execute a search and then exit */
    private static boolean doSearch = false;

    /*
     * ArrayList to hold our struct objects, which are converted into java
     * objects and must be written to a separate file in the same directory.
     * Each index should have [struct name, struct code]
     */
    private static ArrayList<String[]> structFiles;

    /* the template used to organize generated code */
    private static String template = "<generatedImports>\n" + "<userImports>\n"
	    + "<userClassName>\n" + "<generatedVariables>\n"
	    + "<userGlobalVariables>\n" + "<userCode>\n" + "<userFunctions>\n"
	    + "<{_*_generatedFunctions_*_}>\n" + "<endFile>";

    public static void main(String[] args) {
	/* check to see if colors are in use */
	boolean useColors = true;
	try {
	    BufferedReader br = new BufferedReader(
		    new FileReader("colors.conf"));
	    StringBuilder data = new StringBuilder();
	    String line = br.readLine();
	    while (line != null) {
		data.append(line);
		data.append('\n');
		line = br.readLine();
	    }
	    String fileData = data.toString().trim();
	    br.close();
	    int c = Integer.parseInt(fileData);
	    if (c == 0)
		useColors = false;
	} catch (Exception e) {
	}
	if (!useColors) {
	    RED = "";
	    GREEN = "";
	    YELLOW = "";
	    BLUE = "";
	    NORMAL = "";
	}

	/* display error if no arguments are provided or if it's just a -v flag */
	if (args.length == 0) {
	    System.out.println("Usage: javauto <options> <source files>");
	    System.out.println("Options:");
	    System.out.println("\t-v\t--verbose\t\tcompile in verbose mode");
	    System.out
		    .println("\t-g\t--generate\t\tgenerate .java and .class files");
	    System.out
		    .println("\t-gj\t--generate-java\t\tgenerate .java files");
	    System.out
		    .println("\t-gc\t--generate-class\tgenerate .class files");
	}

	/* compile each file provided */
	for (String userCodeFile : args) {

	    /*
	     * initialize the struct files as an empty array for potential user
	     * structs in this userCodeFile
	     */
	    structFiles = new ArrayList<String[]>();

	    /* if it's not a file but a no-colors flag */
	    if (userCodeFile.equals("-nc")
		    || userCodeFile.equals("--no-colors")) {
		/* set all the colors to empty */
		RED = "";
		GREEN = "";
		YELLOW = "";
		BLUE = "";
		NORMAL = "";
		continue;
	    }

	    /* if it's not a file but a verbose flag */
	    if (userCodeFile.equals("-v") || userCodeFile.equals("--verbose")) {
		/* set verbose to true and continue with next iteration */
		verbose = true;
		continue;
	    }

	    /* if it's not a file but a flag */
	    else if (userCodeFile.equals("-g")
		    || userCodeFile.equals("--generate")) {
		/* set keeps to true and continue with next iteration */
		keepClass = true;
		genJava = true;
		continue;
	    }

	    /* if it's not a file but a flag */
	    else if (userCodeFile.equals("-gj")
		    || userCodeFile.equals("--generate-java")) {
		/* set keeps to true and continue with next iteration */
		genJava = true;
		continue;
	    }

	    /* if it's not a file but a flag */
	    else if (userCodeFile.equals("-gc")
		    || userCodeFile.equals("--generate-class")) {
		/* set keeps to true and continue with next iteration */
		keepClass = true;
		continue;
	    }

	    /* check to make sure it's a .javauto file */
	    if (!userCodeFile.toLowerCase().endsWith(".javauto")
		    && !userCodeFile.toLowerCase().endsWith(".ja")) {
		System.out.println("Not a .javauto file:  " + GREEN
			+ userCodeFile + NORMAL);
		continue;
	    }

	    /* check to make sure the file exists */
	    if (!new File(userCodeFile).exists()) {
		System.out.println("File not found:  " + GREEN + userCodeFile
			+ NORMAL);
		continue;
	    }

	    /* the class name of our main user code */
	    String className = new File(userCodeFile).getName().split(
		    "\\.(?=[^\\.]+$)")[0];

	    verbose("Generating " + GREEN + className + NORMAL + "...");

	    if (!className.matches("^[a-zA-Z].*")) {
		System.out.println(RED
			+ "Error: class names must start with a letter. "
			+ GREEN + className + RED
			+ " does not start with a letter." + NORMAL);
		System.exit(1);
	    }
	    if (!className.matches("^\\S+$")) {
		System.out.println(RED
			+ "Error: class names must not contain spaces. "
			+ GREEN + className + RED + " contains spaces."
			+ NORMAL);
		System.exit(1);
	    }

	    /* file path of our build folder (currentDir/build) */
	    String directory = new File(userCodeFile).getAbsolutePath()
		    .substring(
			    0,
			    new File(userCodeFile).getAbsolutePath()
				    .lastIndexOf(File.separator))
		    + File.separator + className;

	    /*
	     * if we're not keeping any of the generated files make the dir
	     * hidden
	     */
	    if (!keepClass && !genJava)
		directory = new File(userCodeFile).getAbsolutePath().substring(
			0,
			new File(userCodeFile).getAbsolutePath().lastIndexOf(
				File.separator))
			+ File.separator + "." + className;

	    /*
	     * file path of our gen directory (build/gen) for generated java
	     * files
	     */
	    String genDirectory = directory + File.separator + "gen";

	    /*
	     * file path of our class directory (build/class) for our compiled
	     * .class files
	     */
	    String classDirectory = directory + File.separator + "class";

	    /* get javauto code to compile */
	    String javautoCode = fileRead(userCodeFile);

	    /*
	     * perform rudimentary debugging and stop compiling if there's an
	     * error
	     */
	    boolean err = SimpleDebugger.debug(javautoCode, userCodeFile);
	    if (err)
		System.exit(1);

	    /* generate java code from the provided javauto code */
	    String generatedCode = generateJavaCode(javautoCode, className);
	    verbose("Generation complete... starting build");

	    /*** Now the build begins ***/

	    /* create our build folder if it doesn't exist */
	    File outDir = new File(directory);
	    if (!outDir.exists()) {
		verbose("Creating directory " + GREEN + directory + NORMAL
			+ "...");
		if (!outDir.mkdir()) {
		    System.out
			    .println("Error: couldn't create build directory "
				    + GREEN
				    + directory
				    + NORMAL
				    + "\nPlease create this directory manually.");
		    System.exit(1);
		}
	    }
	    /*
	     * if our build folder is temporary and we're on windows hide the
	     * folder manually
	     */
	    if (!keepClass
		    && !genJava
		    && System.getProperty("os.name").toLowerCase()
			    .contains("windows")) {
		String cmd = "Executing cmd /c attrib +s +h \"" + directory
			+ "\"...";
		verbose(cmd);
		try {
		    Process p = Runtime.getRuntime().exec(
			    "cmd /c attrib +s +h \"" + directory + "\"");
		} catch (Exception e) {
		    verbose(RED + "Failed." + NORMAL);
		}
	    }

	    /* create our gen folder if it doesn't exist */
	    if (genJava) {
		outDir = new File(genDirectory);
		if (!outDir.exists()) {
		    verbose("Creating directory " + GREEN + genDirectory
			    + NORMAL + "...");
		    if (!outDir.mkdir()) {
			System.out
				.println("Error: couldn't create gen directory "
					+ GREEN
					+ genDirectory
					+ NORMAL
					+ "\nPlease create this directory manually.");
			System.exit(1);
		    }
		}
	    }

	    /* create our class folder if it doesn't exist */
	    outDir = new File(classDirectory);
	    if (!outDir.exists()) {
		verbose("Creating directory " + GREEN + classDirectory + NORMAL
			+ "...");
		if (!outDir.mkdir()) {
		    System.out
			    .println("Error: couldn't create class directory "
				    + GREEN
				    + classDirectory
				    + NORMAL
				    + "\nPlease create this directory manually.");
		    System.exit(1);
		}
	    }

	    if (genJava) {
		/* build all of our user struct files */
		for (String[] structFile : structFiles) {
		    String outPath = genDirectory + File.separator
			    + structFile[0] + ".java";
		    verbose("Building " + GREEN + outPath + NORMAL + "...");
		    fileWrite(outPath, structFile[1] + "\n");
		}
	    }

	    if (genJava) {
		/* build our main file */
		String outputFile = genDirectory + File.separator + className
			+ ".java";
		verbose("Building " + GREEN + outputFile + NORMAL + "...");
		fileWrite(outputFile, generatedCode);
	    }

	    /*** Now it's time to generate class files ***/

	    /* first generate each struct */
	    for (String[] structFile : structFiles) {
		verbose("Building " + GREEN + classDirectory + File.separator
			+ structFile[0] + ".class" + NORMAL + "...");

		/*
		 * use our custom compiler {@link CustomJavaCompiler} to attempt
		 * to compile the file, if it doesn't work out errors will be
		 * stored in the DiagnosticCollector<JavaFileObject>
		 */
		DiagnosticCollector<JavaFileObject> compileErrors = null;
		try {
		    compileErrors = CustomJavaCompiler.compile(structFile[0],
			    structFile[1], classDirectory, classDirectory);
		} catch (IOException ioe) {
		    System.out.println(RED
			    + "Error: couldn't write to file in directory "
			    + classDirectory + NORMAL);
		    ioe.printStackTrace();
		}

		/* if there is an error */
		if (compileErrors != null) {

		    /* iterate through each error */
		    for (Diagnostic diagnostic : compileErrors.getDiagnostics()) {

			/*
			 * find the struct in the user code that is causing the
			 * issue
			 */
			String[] javautoCodeLines = javautoCode.split("\n");
			int structlinenum = -1;
			for (int k = 0; k < javautoCodeLines.length; k++) {
			    if (removeLiterals(
				    removeComments(javautoCodeLines[k])).trim()
				    .startsWith("struct " + structFile[0]))
				structlinenum = k;
			}

			/* if we found the struct declaration include that */
			if (structlinenum == -1)
			    /* print the error message */
			    System.out.println(diagnostic.getKind().toString()
				    .toLowerCase()
				    + " in struct " + structFile[0] + ":");
			else
			    /* print the error message */
			    System.out.println(diagnostic.getKind()
				    + " in struct declaration for "
				    + structFile[0] + " starting on line "
				    + structlinenum + ":\n\t"
				    + javautoCodeLines[structlinenum]);
			System.out.println(diagnostic.getMessage(Locale
				.getDefault()) + "\n");
		    }

		    /* clean up */
		    if (!keepClass && !genJava) {
			if (deleteDirectory(new File(directory))) {
			    verbose("Removed temporary dir " + GREEN
				    + directory + NORMAL);
			} else {
			    verbose(RED + "Failed to remove temporary dir "
				    + directory + NORMAL);
			}
		    }

		    /* exit because we found an error */
		    System.exit(1);
		}
	    }

	    /* now compile main user code */
	    verbose("Building " + GREEN + classDirectory + File.separator
		    + className + ".class" + NORMAL + "...");

	    /*
	     * use our custom compiler {@link CustomJavaCompiler.java} to
	     * attempt to compile the file, if it doesn't work out errors will
	     * be stored in the DiagnosticCollector<JavaFileObject>
	     */
	    DiagnosticCollector<JavaFileObject> compileErrors = null;
	    try {
		compileErrors = CustomJavaCompiler.compile(className,
			generatedCode, classDirectory, classDirectory);
	    } catch (IOException ioe) {
		System.out.println(RED
			+ "Error: couldn't write to file in directory "
			+ classDirectory + NORMAL);
		ioe.printStackTrace();
	    }

	    /* if there is an error */
	    if (compileErrors != null) {

		/*
		 * give the errors to our DealWithCompilerErrors.java to take
		 * care of
		 */
		DealWithCompilerErrors.dealWithIt(compileErrors, generatedCode,
			javautoCode, userCodeFile);

		/* clean up */
		if (!keepClass && !genJava) {
		    if (deleteDirectory(new File(directory))) {
			verbose("Removed temporary dir " + GREEN + directory
				+ NORMAL);
		    } else {
			verbose(RED + "Failed to remove temporary dir "
				+ directory + NORMAL);
		    }
		}

		/* exit because we found an error */
		System.exit(1);
	    }

	    /* now create a .jar file from our class files */
	    if (verbose)
		CustomJarCompiler.verbose = true;
	    if (GREEN.equals("")) // if colors are blank
		CustomJarCompiler.colors = false;
	    String jarOut = className + ".jar";
	    if (keepClass || genJava)
		jarOut = directory + File.separator + className + ".jar";

	    try {
		CustomJarCompiler.create(jarOut, className, classDirectory);
	    } catch (IOException ioe) {
		System.out.println(RED + "Error: could not write to file "
			+ jarOut + NORMAL);
		ioe.printStackTrace();
	    } finally {
		if (!keepClass && !genJava) {
		    if (deleteDirectory(new File(directory))) {
			verbose("Removed temporary dir " + GREEN + directory
				+ NORMAL);
		    } else {
			verbose(RED + "Failed to remove temporary dir "
				+ directory + NORMAL);
		    }
		}
	    }

	    /* set our jar file to be executable */
	    verbose("Setting executable permissions for " + GREEN + jarOut
		    + NORMAL + "...");
	    File ourJar = new File(jarOut);
	    ourJar.setExecutable(true);

	    /* we're done compiling */
	    verbose("Operation complete.");

	}

    }

    /**
     * Highlight a term inside of a string.
     * 
     * @param term
     *            the term to highlight (ingores anything thats not a letter or
     *            number)
     * @param context
     *            the string in which the term is found and highlighted
     * @param startHighlight
     *            the chracter to use to "highlight" the beginning
     * @param endHighlight
     *            the character to end the highlighting with
     * @return the highlighted string
     */
    private static String highlight(String term, String context,
	    String startHighlight, String endHighlight) {
	/*
	 * convert to lower case and replace non word characters with spaces so
	 * that indexes remain the same
	 */
	String indexedCon = context.replaceAll("[^a-zA-Z0-9]", " ")
		.toLowerCase();

	/* get our term without non words */
	String stripTerm = term.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

	/* build a regex to find the starting and ending indexes of our term */
	String regexp = "";
	String[] termParts = stripTerm.split("");
	for (int i = 1; i < termParts.length; i++) {
	    regexp = regexp + termParts[i] + "\\s*";
	}
	regexp = regexp.substring(0, regexp.length() - 3);

	/* search for our pattern in the string */
	Pattern pattern = Pattern.compile(regexp);
	Matcher matcher = pattern.matcher(indexedCon);

	/* get start and end indexes of our term inside the string */
	ArrayList<Integer[]> startEnd = new ArrayList<Integer[]>();
	while (matcher.find())
	    startEnd.add(new Integer[] { matcher.start(), matcher.end() });

	/* craft our highlighted result */
	int shiftLen = 0;
	String res = context;
	for (Integer[] coords : startEnd) {
	    int start = coords[0] + shiftLen;
	    int end = coords[1] + shiftLen;
	    res = res.substring(0, start) + startHighlight
		    + res.substring(start, end) + endHighlight
		    + res.substring(end, res.length());
	    shiftLen = shiftLen + startHighlight.length()
		    + endHighlight.length();
	}

	return res;
    }

    /**
     * Generate java code from javauto code with the help of our template
     * 
     * @param javautoCode
     *            raw javauto code to be converted
     * @param userClassName
     *            the name of the class/output file
     * @return java code as String
     */
    private static String generateJavaCode(String javautoCode,
	    String userClassName) {
	/* get a template for our generated code */
	String templateContents = template;

	/* get functions that might need to be implimented */
	ArrayList<String[]> javautoFunctions = getFunctions();

	/* define where we're gonna store our final code */
	String generatedCode = "";

	/* code we generate (supporting functions will be handled later on) */
	String generatedImports = generateImports();
	String generatedVariables = generateVariables();

	/* define different variables to categorize the user code */
	String userImports = "";
	String userGlobalVariables = "";
	String userCode = "";
	String userFunctions = "";

	/* handle python file string replacements in user code */
	javautoCode = doPythonStringReplacements(javautoCode);

	/* remove comments from the user code */
	javautoCode = removeComments(javautoCode);

	/* remove whitespace */
	String cleanedCode = "";
	for (String line : javautoCode.split("\n")) {
	    if (!line.equals("\n")) {
		cleanedCode = cleanedCode + line.trim() + "\n";
	    }
	}
	while (cleanedCode.contains("\n\n"))
	    cleanedCode = cleanedCode.replace("\n\n", "\n");
	javautoCode = cleanedCode;

	/* extract user imports */
	String[] jaCodeLines = javautoCode.split("\n");
	verbose("Getting user imports...  ", 0);
	printedResults = false;
	for (int i = 0; i < jaCodeLines.length; i++) {
	    if (jaCodeLines[i].trim().startsWith("import ")) {
		if (!alreadyImported(jaCodeLines[i].trim(), generatedImports)) {
		    userImports = userImports + jaCodeLines[i].trim() + "\n";
		    verbose(YELLOW
			    + jaCodeLines[i].trim().substring(
				    jaCodeLines[i].trim().lastIndexOf(".") + 1,
				    jaCodeLines[i].trim().lastIndexOf(";"))
			    + " " + NORMAL, 0);
		    printedResults = true;
		}
		jaCodeLines[i] = "";
	    }
	}
	if (!printedResults)
	    verbose("None", 0);
	verbose("");

	/* rebuild code without imports */
	javautoCode = "";
	for (String line : jaCodeLines)
	    javautoCode = javautoCode + line + "\n";

	/* extract user global/class level variables */
	verbose("Getting user global variables...  ", 0);
	printedResults = false;
	jaCodeLines = javautoCode.split("\n");
	for (int i = 0; i < jaCodeLines.length; i++) {
	    if (jaCodeLines[i].trim().startsWith("global ")) {
		boolean empty = false;
		if (!jaCodeLines[i].trim().contains("="))
		    empty = true; // they're just declaring it, not assigning
				  // value
		String[] declarationParts = jaCodeLines[i].trim().split(" ");
		if (declarationParts[2].endsWith(";"))
		    declarationParts[2] = declarationParts[2].substring(0,
			    declarationParts[2].length() - 1);
		verbose(YELLOW + declarationParts[2] + " " + NORMAL, 0);
		printedResults = true;
		userGlobalVariables = userGlobalVariables + "public static "
			+ declarationParts[1] + " " + declarationParts[2]
			+ ";\n";
		String restOfDeclaration = "";
		for (int j = 2; j < declarationParts.length; j++)
		    restOfDeclaration = restOfDeclaration + declarationParts[j]
			    + " ";
		jaCodeLines[i] = restOfDeclaration;
		if (empty)
		    jaCodeLines[i] = "";
	    }
	}
	javautoCode = "";
	for (String line : jaCodeLines)
	    javautoCode = javautoCode + line + "\n";
	if (!printedResults)
	    verbose("None", 0);
	verbose("");

	/* extract user defined functions from code */
	verbose("Getting user functions...  ", 0);
	String[] codeParts = splitUserFunctions(javautoCode);
	if (!printedResults)
	    verbose("None", 0);
	verbose("");
	userFunctions = codeParts[0];
	userCode = codeParts[1];

	/* clean up the main user code */
	while (userCode.contains("\n\n"))
	    userCode = userCode.replace("\n\n", "\n");
	userCode = indent(userCode, 2);

	/*
	 * insert ~most~ of our generated and user sourced code into the
	 * template
	 */
	generatedCode = templateContents;
	generatedCode = generatedCode.replace("<endFile>", "}");
	generatedCode = generatedCode.replace("<userImports>", userImports);
	generatedCode = generatedCode.replace("<userClassName>",
		"public class " + userClassName + " {");
	generatedCode = generatedCode.replace("<userGlobalVariables>",
		indent(userGlobalVariables));
	generatedCode = generatedCode.replace("<userCode>",
		"\tpublic static void main(String[] args) {\n" + userCode
			+ "\n\t}\n");
	generatedCode = generatedCode.replace("<userFunctions>",
		indent(userFunctions));

	/*
	 * now we generate javauto functions based on what the user has
	 * implemented until no replacements have been made
	 */
	int replacements = 1;
	ArrayList<String> alreadyGenerated = new ArrayList<String>();
	verbose("Generating functions...  ", 0);
	printedResults = false;
	String noLiteralsCode;
	while (replacements != 0) {
	    replacements = 0;
	    noLiteralsCode = removeLiterals(generatedCode);
	    for (String[] javautoFunction : javautoFunctions) {
		/*
		 * if a the function name + ( is present anywhere in our code
		 * AND we haven't already generated a function for it
		 */
		if (noLiteralsCode.contains(javautoFunction[0].trim())
			&& !alreadyGenerated
				.contains(javautoFunction[0].trim())) {
		    /* add code for that function to the file */
		    generatedCode = generatedCode.replace(
			    "<{_*_generatedFunctions_*_}>",
			    removeComments(javautoFunction[1])
				    + "\n<{_*_generatedFunctions_*_}>");
		    verbose(YELLOW + javautoFunction[0] + " " + NORMAL, 0);
		    printedResults = true;

		    /* add function to the list of already generated functions */
		    alreadyGenerated.add(javautoFunction[0]);

		    /* there's been a replacement => increment the variable */
		    replacements++;

		}
	    }
	}
	if (!printedResults)
	    verbose("None", 0);
	verbose("");

	/*
	 * we've generated all the functions we need => remove the
	 * <generatedFunctions> tag
	 */
	generatedCode = generatedCode.replace("<{_*_generatedFunctions_*_}>",
		"");

	/* build a list of class variable declarations and their variable names */
	ArrayList<String[]> classVariableData = new ArrayList<String[]>();
	for (String line : generatedVariables.split("\n")) {
	    String[] parts = line.split("=")[0].split(" ");
	    String name = parts[parts.length - 1].trim();

	    /* make class variables static */
	    String[] lineParts = line.trim().split(" ");
	    String modifiedLine = lineParts[0] + " static ";

	    for (int i = 1; i < lineParts.length; i++) {
		modifiedLine = modifiedLine + lineParts[i] + " ";
	    }

	    String[] data = { name, "\t" + modifiedLine.trim() };
	    classVariableData.add(data);
	}

	/* check if the names are in the code => add the variable if they are */
	String usedVariables = "";
	String skeletonCode = removeLiterals(generatedCode);
	verbose("Generating class variables...  ", 0);
	printedResults = false;
	for (String[] declaration : classVariableData) {
	    if (skeletonCode.contains(declaration[0])) {
		usedVariables = usedVariables + declaration[1] + "\n";
		verbose(YELLOW + declaration[0] + " " + NORMAL, 0);
		printedResults = true;
	    }
	}
	if (!printedResults)
	    verbose("None", 0);
	verbose("");

	/* add all the variables we determine have been used */
	generatedCode = generatedCode.replace("<generatedVariables>",
		usedVariables);

	/* update the skeletonCode to inclue the generated variables */
	skeletonCode = removeLiterals(generatedCode);

	/* build a list of imported classes */
	ArrayList<String[]> generatedImportData = new ArrayList<String[]>();
	for (String line : generatedImports.split("\n")) {
	    String importedClass = line.substring(line.lastIndexOf(".") + 1,
		    line.lastIndexOf(";"));
	    String[] data = { importedClass, line };
	    generatedImportData.add(data);
	}

	/* check if class names are in code => add the import if they are */
	generatedImports = "";
	verbose("Generating imports...  ", 0);
	printedResults = false;
	for (String[] d : generatedImportData) {
	    if (skeletonCode.contains(d[0]) || d[0].equals("*")) {
		generatedImports = generatedImports + d[1] + "\n";
		verbose(YELLOW + d[0] + " " + NORMAL, 0);
		printedResults = true;
	    }
	}
	if (!printedResults)
	    verbose("None", 0);
	verbose("");

	/* parse out user struct declarations */
	verbose("Generating struct objects... ", 0);
	printedResults = false;
	String structName = "";
	do {
	    String[] sParts = getStruct(generatedCode);
	    structName = sParts[1];
	    if (!structName.trim().equals("")) {
		structFiles.add(new String[] { sParts[1], sParts[2] });
		verbose(YELLOW + structName + " " + NORMAL, 0);
		printedResults = true;
		generatedCode = sParts[0];
	    }
	} while (!structName.trim().equals(""));
	if (!printedResults)
	    verbose("None", 0);
	verbose("");

	/* check if class names are in structs => add the import if they are */
	for (String[] d : generatedImportData) {
	    for (int j = 0; j < structFiles.size(); j++) {
		String[] structFile = structFiles.get(j);
		if (structFile[1].contains(d[0]) || d[0].equals("*")) {
		    structFile[1] = d[1] + "\n" + structFile[1];
		    structFiles.set(j, structFile);
		}
	    }
	}

	/* add all the imports we determine have been used */
	generatedCode = generatedCode.replace("<generatedImports>",
		generatedImports);

	/* get rid of all empty lines */
	String[] generatedCodeLines = generatedCode.split("\n");
	generatedCode = "";
	for (int i = 0; i < generatedCodeLines.length; i++) {
	    if (!generatedCodeLines[i].trim().equals(""))
		generatedCode = generatedCode + generatedCodeLines[i] + "\n";
	}

	return generatedCode;
    }

    /**
     * Searches user code for a struct, pieces it together, and returns the
     * struct code and the new user code.
     * 
     * @param code
     *            the user code
     * @return String array index 0: new user code index 1: the struct name
     *         index 2: the struct code
     */
    private static String[] getStruct(String code) {
	/* split the user code into lines and characters for parsing */
	String[] codeLines = code.split("\n");
	char[] codeChars = code.toCharArray();

	/* get the indexes of string/character literals and comments */
	ArrayList<ArrayList<Integer[]>> ignores = getIgnoreIndexes(code);
	ArrayList<Integer[]> allIgnores = new ArrayList<Integer[]>();
	allIgnores.addAll(ignores.get(0));
	allIgnores.addAll(ignores.get(1));

	/* look for a struct declaration */
	int startIndex = 0;
	for (int i = 0; i < codeLines.length; i++) {
	    /* if a line starts with struct it's a struct declaration */
	    if (removeComments(codeLines[i]).trim().startsWith("struct ")) {
		/*
		 * increment the start index until we're at the beginning of the
		 * declaration
		 */
		while (inIgnoredText(startIndex, allIgnores)
			|| codeChars[startIndex] != 's')
		    startIndex++;

		/* find the index of the last bracket in the struct */
		int endIndex = startIndex;
		String insideElements = "";
		while (endIndex < codeChars.length) {
		    if (codeChars[endIndex] == '{'
			    && !inIgnoredText(endIndex, allIgnores)) {
			break;
		    }
		    endIndex++;
		}
		endIndex++;
		while (endIndex < codeChars.length) {
		    if (codeChars[endIndex] == '}'
			    && !inIgnoredText(endIndex, allIgnores)) {
			break;
		    }
		    insideElements = insideElements
			    + String.valueOf(codeChars[endIndex]);
		    endIndex++;
		}
		endIndex++;

		/* get the user code without the struct object */
		String newCode = code.substring(0, startIndex)
			+ code.substring(endIndex, codeChars.length);

		/* get the name of the struct object to create */
		String structName = removeComments(
			code.substring(startIndex, endIndex)).split(" ")[1]
			.trim();

		/* get each individual variable within the struct */
		String[] insideElementsLines = insideElements.split("\n");
		for (int j = 0; j < insideElementsLines.length; j++) {
		    insideElementsLines[j] = removeComments(
			    insideElementsLines[j]).trim();
		}
		insideElements = "";
		for (String s : insideElementsLines)
		    insideElements = insideElements + s;
		String[] elements = insideElements.split(";");
		for (int j = 0; j < elements.length; j++)
		    elements[j] = "public " + elements[j].trim() + ";";

		/* construct java code from these elements */
		String elementCode = "";
		for (String e : elements)
		    elementCode = elementCode + "\t" + e + "\n";
		elementCode = elementCode
			.substring(0, elementCode.length() - 1);

		/* construct code for the whole struct */
		String structCode = "public class " + structName + " {\n"
			+ elementCode + "\n}";

		/* return our results */
		return new String[] { newCode, structName, structCode };
	    }
	    startIndex = startIndex + codeLines[i].length() + 1; // +1 for the
								 // \n char
	}
	/* if we don't find anything */
	return new String[] { code, "", "" };
    }

    /**
     * Get the end index of a function
     * 
     * @param userCode
     *            the text from which to extract the function
     * @param lowerBound
     *            the start index of the function
     * @return the end index of the function
     */
    private static int getFunctionUpperBound(String userCode, int lowerBound) {
	/* get the code as a char array */
	char[] codeChars = userCode.toCharArray();

	/* get the indexes of string/character literals and comments */
	ArrayList<ArrayList<Integer[]>> ignores = getIgnoreIndexes(userCode);
	ArrayList<Integer[]> allIgnores = new ArrayList<Integer[]>();
	allIgnores.addAll(ignores.get(0));
	allIgnores.addAll(ignores.get(1));

	int startIndex = lowerBound;

	/* find the index of the first bracket in the func */
	int endIndex = startIndex;
	while (endIndex < codeChars.length) {
	    if (codeChars[endIndex] == '{'
		    && !inIgnoredText(endIndex, allIgnores)) {
		break;
	    }
	    endIndex++;
	}
	endIndex++;

	/* find the value of the last bracket in the func */
	int open = 1;
	int close = 0;
	while (open > close) {
	    if (codeChars[endIndex] == '}'
		    && !inIgnoredText(endIndex, allIgnores)) {
		close++;
	    } else if (codeChars[endIndex] == '{'
		    && !inIgnoredText(endIndex, allIgnores)) {
		open++;
	    }
	    endIndex++;
	}
	return endIndex;
    }

    /**
     * Split user code into an array; one indice will contain user functions and
     * the other will contain all other user code.
     * 
     * @param userCode
     * @return String[] { userFunctions, userCode }
     */
    private static String[] splitUserFunctions(String userCode) {
	/* two variables to hold the two parts of the code */
	String userFunctions = "";
	String userNormalCode = "";

	/* keep track of the character index we're at */
	int charIndex = 0;

	/* create a list to store function start & end indexes */
	ArrayList<int[]> functionIndexes = new ArrayList<int[]>();

	/* iterate by lines looking for functions */
	for (String line : userCode.split("\n")) {
	    /* check for function declaration */
	    if (line.trim().startsWith("func ")) {
		int[] bounds = { charIndex,
			getFunctionUpperBound(userCode, charIndex) };
		functionIndexes.add(bounds);
	    }

	    /* advance the character index (+1 for the \n char) */
	    charIndex = charIndex + line.length() + 1;
	}

	/* split the original string into parts based on the function bounds */
	int nonFunctionIndex = 0;
	for (int[] bounds : functionIndexes) {
	    /*
	     * append what is outside the function bounds to the userNormalCode
	     * variable
	     */
	    userNormalCode = userNormalCode
		    + userCode.substring(nonFunctionIndex, bounds[0]) + "\n";

	    /*
	     * get the current user function & change its first line for java
	     * func decalration
	     */
	    String[] functionLines = userCode.substring(bounds[0], bounds[1])
		    .split("\n");
	    functionLines[0] = functionLines[0].trim();

	    /* remove the first five letters; the "func " & add in public */
	    functionLines[0] = "public static " + functionLines[0].substring(5);

	    /* get the function name for verbose output */
	    String[] fparts = functionLines[0].split(" ");
	    String restoff = "";
	    for (int i = 3; i < fparts.length; i++)
		restoff = restoff + fparts[i] + " ";
	    verbose(YELLOW + restoff.substring(0, restoff.indexOf("(")) + " "
		    + NORMAL, 0);
	    printedResults = true;

	    /* add each of the elements in the array together into a function */
	    String function = "";
	    for (int i = 0; i < functionLines.length; i++) {
		if (i != 0 && i < functionLines.length - 1)
		    function = function + "\t" + functionLines[i] + "\n";
		else
		    function = function + functionLines[i] + "\n";

	    }

	    /*
	     * append what is inside the function bounds to the userFunctions
	     * variable
	     */
	    userFunctions = userFunctions + function + "\n\n";

	    /*
	     * advance the lower bound of the "non function" code to the upper
	     * bound of the function code
	     */
	    nonFunctionIndex = bounds[1];
	}

	/* add any remaining code to the user "non-function" code */
	userNormalCode = userNormalCode
		+ userCode.substring(nonFunctionIndex, userCode.length());

	/* return the two parts */
	String[] r = { userFunctions, userNormalCode };
	return r;
    }

    /**
     * Search the javautoFile for imports and add them all to the generated
     * imports
     * 
     * @return imports that have been generated
     */
    private static String generateImports() {
	/* get raw file contents of the file containing our imports */
	String[] importContents = resourceRead(javautoFile).split("\n");

	/* variable to store our generated imports */
	String imports = "";

	/* add each line that starts with "import " to our imports variable */
	for (String importLine : importContents) {
	    if (importLine.startsWith("import "))
		imports = imports + importLine + "\n";
	}

	/* return our imports */
	return imports;
    }

    /**
     * Search the javautoFile for all class variables and return them
     * 
     * @return class variables that have been generated
     */
    private static String generateVariables() {
	/* get raw file contents of the file containing our variables */
	String[] variablesContents = resourceRead(javautoFile).split("\n");

	/* variable to store our generated class vars */
	String classVars = "";

	/* add each line that has a class varaible to our classVars */
	for (String line : variablesContents) {
	    if ((line.trim().startsWith("public ") || line.trim().startsWith(
		    "private "))
		    && line.trim().endsWith(";"))
		classVars = classVars + line + "\n";
	}

	/* return our class vars */
	return classVars;
    }

    /**
     * Gets a list of functions and their source code from the javautoFile
     * 
     * @return ArrayList<String[]> with each element containing {function name,
     *         function code}
     */
    private static ArrayList<String[]> getFunctions() {
	/* define the list where we'll store all the data */
	ArrayList<String[]> functionDataList = new ArrayList<String[]>();

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
		String functionCode = getFunctionCode(line, functionContents);

		/*
		 * modify the function code so that the declaration becomes
		 * static
		 */
		String[] lines = functionCode.split("\n");
		lines[0] = lines[0].trim();
		if (lines[0].startsWith("private ")) {
		    lines[0] = "\tprivate static " + lines[0].substring(8);
		} else if (lines[0].startsWith("public ")) {
		    lines[0] = "\tpublic static " + lines[0].substring(7);
		}
		functionCode = "";
		for (String l : lines)
		    functionCode = functionCode + l + "\n";

		/*
		 * add it to the list unless it's the "run" function used in a
		 * thread
		 */
		if (!functionName.toLowerCase().equals("run")) {
		    String[] function = { functionName.trim(), functionCode };
		    functionDataList.add(function);
		}
	    }
	}

	/* return our list without duplicates */
	return combineFunctionDuplicates(functionDataList);
    }

    /**
     * Take an array list with each item having a format {function name,
     * function code} and combine duplicate function names into one entry that
     * still has the source code of each so an array list containing two
     * elements like this: {doSomething, code1}, {doSomething, code2} would
     * become {doSomething, code1\n\ncode2} combineFunctionDuplicates() is
     * necessary because some of our functions are defined multiple times to
     * allow for different parameters and this allows us to add all iterations
     * of a function at once.
     *
     * @param functionListWithDuplicates
     *            the list to run through
     * @return a version of the list without duplicates but with all the code
     */
    private static ArrayList<String[]> combineFunctionDuplicates(
	    ArrayList<String[]> functionListWithDuplicates) {
	/* define our final list that wont have duplicates */
	ArrayList<String[]> functionListNoDuplicates = new ArrayList<String[]>();

	/* list to keep track of function names that have already been combined */
	ArrayList<String> alreadyCombined = new ArrayList<String>();

	for (String[] f : functionListWithDuplicates) {
	    /* the name of the function is stored in the first indice */
	    String functionName = f[0];

	    /* if we haven't already combined the function */
	    if (!alreadyCombined.contains(functionName)) {
		/* the combined code of all functions of the same name */
		String combinedCode = "";

		/* check every item in the list */
		for (String[] function : functionListWithDuplicates) {
		    /* if the names match add the code in */
		    if (functionName.equals(function[0])) {
			combinedCode = combinedCode + function[1] + "\n\n";
		    }
		}

		/* add the function to the list */
		String[] combinedFunction = { functionName, combinedCode };
		functionListNoDuplicates.add(combinedFunction);

		/* mark the name as covered */
		alreadyCombined.add(functionName);
	    }
	}

	/* return our final results */
	return functionListNoDuplicates;
    }

    /**
     * Get the code for a single function from a java file of functions based
     * off its signature
     * 
     * @param signature
     *            All or part of the function signature, eg.
     *            "public void function(int i)" If there are two declarations
     *            with the same name and you only provide
     *            "public void functionName" it will return the first one. To
     *            get a specific one include the full signature like
     *            "public void functionName(int i, int j)"
     * @param fullFunctionsText
     *            The contents of a java file that contains the function we're
     *            trying to extract
     * @return Full text of the single function we're trying to find
     */
    private static String getFunctionCode(String signature, String wholeFile) {
	/* find the index of the function */
	int funcIndex = wholeFile.indexOf(signature);

	/* trim the file so that it starts at this index */
	wholeFile = wholeFile.substring(funcIndex);

	/* file as string -> file as char array */
	char[] wholeFileChars = wholeFile.toCharArray();

	/* variables to hold position and brace counts */
	int index = 0;
	int openBrace = 0;
	int closeBrace = 0;

	/* find the opening bracket of function */
	while (openBrace == 0) {

	    /* if we find the brace we're done */
	    if (wholeFileChars[index] == '{') {
		openBrace++;
		index++;
	    }

	    /* check for different kinds of comments */
	    else if (wholeFileChars[index] == '/') {
		index++;

		/* if it's // comment until end of line */
		if (wholeFileChars[index] == '/') {
		    while (wholeFileChars[index] != '\n') {
			index++;
		    }
		    index++;
		}

		/* if it's a /* comment until * / */
		else if (wholeFileChars[index] == '*') {
		    index++;
		    boolean done = false;
		    while (done == false) {
			if (wholeFileChars[index] == '*') {
			    index++;
			    if (wholeFileChars[index] == '/') {
				index++;
				done = true;
			    }
			} else {
			    index++;
			}
		    }
		}
	    }

	    /* if it's some other character just keep going */
	    else {
		index++;
	    }
	}

	while (openBrace > closeBrace) {
	    /* if we find a brace then increment */
	    if (wholeFileChars[index] == '{') {
		openBrace++;
		index++;
	    } else if (wholeFileChars[index] == '}') {
		closeBrace++;
		index++;
	    }

	    /* check for string literals */
	    else if (wholeFileChars[index] == '"') {
		index++;
		while (wholeFileChars[index] != '"') {
		    if (wholeFileChars[index] == '\\') {
			index++;
		    }
		    index++;
		}
		index++;
	    }

	    /* check for character literals */
	    else if (wholeFileChars[index] == '\'') {
		index++;
		while (wholeFileChars[index] != '\'') {
		    if (wholeFileChars[index] == '\\') {
			index++;
		    }
		    index++;
		}
		index++;
	    }

	    /* check for different kinds of comments */
	    else if (wholeFileChars[index] == '/') {
		index++;

		/* if it's // comment until end of line */
		if (wholeFileChars[index] == '/') {
		    while (wholeFileChars[index] != '\n') {
			index++;
		    }
		    index++;
		}

		/* if it's a /* comment until * / */
		else if (wholeFileChars[index] == '*') {
		    index++;
		    boolean done = false;
		    while (done == false) {
			if (wholeFileChars[index] == '*') {
			    index++;
			    if (wholeFileChars[index] == '/') {
				index++;
				done = true;
			    }
			} else {
			    index++;
			}
		    }
		}
	    }

	    /* if it's some other character just keep going */
	    else {
		index++;
	    }

	}

	return wholeFile.substring(0, index + 1);
    }

    /**
     * Handle all python like string replacements ("%s" % something) within a
     * string.
     * 
     * @param data
     *            Data within which replacements happen.
     * @return Data with all replacements done.
     */
    private static String doPythonStringReplacements(String data) {
	/*
	 * As long as some are being replaced keep replacing them. We have to do
	 * this one at a time because as soon as one part is changed the indexes
	 * to ignore (string literals and comments) change so we have to rebuild
	 * everything
	 */
	String replaced = "some";
	while (replaced.equals("some")) {
	    String[] returned = doPythonStringReplacement(data);
	    replaced = returned[1];
	    data = returned[0];
	}
	return data;
    }

    /**
     * Handle a _single_ python style string replacement within a string. For
     * instance, the following string: System.out.println("Time: \t%s" %
     * (time)); Would become:
     * System.out.println("Time: \t%s".replaceFirst("%s",time));
     * 
     * @param data
     *            the string to search through and perform a replacement in.
     * @return a string array, the first index being the modified string data,
     *         the second index being either "some" or "none", depending on if a
     *         replacement was made or if no replacement was made.
     */
    private static String[] doPythonStringReplacement(String data) {
	/* build a regex for the data */
	String regexp = "(\".+\")(\\s*%\\s*(\\())";
	Pattern pattern = Pattern.compile(regexp);
	Matcher matcher = pattern.matcher(data);

	/* build ignore lists for the data */
	ArrayList<ArrayList<Integer[]>> ignores = getIgnoreIndexes(data);
	ArrayList<Integer[]> commentIgnores = ignores.get(0);
	ArrayList<Integer[]> literalIgnores = ignores.get(1);

	/* use these indexes to populate a list of general ignore indexes */
	ArrayList<Integer[]> allIgnores = new ArrayList<Integer[]>();
	for (Integer[] j : commentIgnores)
	    allIgnores.add(j);
	for (Integer[] j : literalIgnores)
	    allIgnores.add(j);

	while (matcher.find()) {
	    /* use the regex search to get all the relevant indexes */
	    int matchedStartIndex = matcher.start();
	    int literalEndIndex = matchedStartIndex + matcher.group(1).length();
	    int elementsStartIndex = matcher.end() - matcher.group(3).length();

	    /*
	     * this is the index of the % character -- it's the "check index"
	     * because we can check whether it's inside a comment/string to see
	     * if we should actually do the modification.
	     */
	    int checkIndex = matcher.end() - matcher.group(2).length();

	    /* only proceed if the checkIndex isn't inside our ignore list */
	    if (!inIgnoredText(checkIndex, allIgnores)) {
		/*
		 * find the upper bound of the elements we're adding into the
		 * string
		 */
		int elementsEndIndex = findEndParen(elementsStartIndex, data,
			allIgnores);

		/* get the elements that need to be subbed in */
		ArrayList<String> elements = getElements(elementsStartIndex,
			elementsEndIndex, data, allIgnores);

		/* build the .replaceFirst() line */
		String rCode = "";
		for (String e : elements) {
		    rCode = rCode + ".replaceFirst(\"%s\"," + e + ")";
		}

		/* build the whole literal */
		String modString = data.substring(matchedStartIndex,
			literalEndIndex) + rCode;

		/* build a new string with the replacement */
		String fixed = data.substring(0, matchedStartIndex) + modString
			+ data.substring(elementsEndIndex);

		return new String[] { fixed, "some" };
	    }
	}

	/* if we get to this point no replacements have been made */
	return new String[] { data, "none" };
    }

    /**
     * Get each element inside of a tuple like structure. So for a given line of
     * code: String intro = "My name is %s %s %s" % ("John",
     * getMiddleName("John"), getLastName("John")); The tuple-like structure
     * ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ The element list
     * would look like ["\"John\"", "getMiddleName(\"John\")",
     * "getLastName(\"John\")"]
     * 
     * @param startElement
     *            the index of the opening ( of the tuple-like structure
     * @param endElements
     *            the index of the closing ) of the tuple-like structure
     * @param context
     *            the greater string in which it resides
     * @param indexesToIgnore
     *            a list of index bounds we want to ignore, eg comments or
     *            string literals. Each element should have the format
     *            [startOfIgnore, endOfIgnore]
     * @return a list of individual elements
     */
    private static ArrayList<String> getElements(int startElement,
	    int endElement, String context, ArrayList<Integer[]> indexesToIgnore) {
	/* get the comma indexes */
	ArrayList<Integer> splitIndexes = getElementCommas(startElement,
		endElement, context, indexesToIgnore);

	/* add the beginning and end of the structure to the slip indexes */
	splitIndexes.add(0, startElement);
	splitIndexes.add(endElement - 1);

	/* list to hold the elements we parse */
	ArrayList<String> elements = new ArrayList<String>();

	if (splitIndexes.size() > 0) {
	    for (int i = 1; i < splitIndexes.size(); i++) {
		elements.add(context.substring(splitIndexes.get(i - 1) + 1,
			splitIndexes.get(i)).trim());
	    }
	}

	return elements;
    }

    /**
     * Very specific use. Gets the "real" commas inside of a tuple-like
     * structure. This shows which indexes it would return for an example:
     * ("parameter1", someFunction("foo", getData("foo", "bar")), "parameter3")
     * ^ ^
     * 
     * @param startElement
     *            the index of the beginning of the tuple like element
     * @param endElement
     *            the index of the end of the tuple like element
     * @param context
     *            the String within which this "tuple" is contained
     * @param indexesToIgnore
     *            a list of index bounds we want to ignore, eg comments or
     *            string literals. Each element should have the format
     *            [startOfIgnore, endOfIgnore]
     * @return a List of indexes where "true" commas are present
     */
    private static ArrayList<Integer> getElementCommas(int startElement,
	    int endElement, String context, ArrayList<Integer[]> indexesToIgnore) {
	/* to store our final values */
	ArrayList<Integer> realCommas = new ArrayList<Integer>();

	char[] contextChars = context.toCharArray();

	int index = startElement + 1; // the plus one is to skip the initial (

	/* search through every character in the string */
	while (index < endElement) {
	    /*
	     * if it's a comma and it's not inside our comments/literals add it
	     * to the list
	     */
	    if (contextChars[index] == ','
		    && !inIgnoredText(index, indexesToIgnore)) {
		realCommas.add(index);
		index++;
	    }
	    /*
	     * if it's an open paren but it isn't inside a comment/literal skip
	     * to the end paren
	     */
	    else if (contextChars[index] == '('
		    && !inIgnoredText(index, indexesToIgnore)) {
		index = findEndParen(index, context, indexesToIgnore);
	    }
	    /* otherwise it's just a normal character so skip to the next one */
	    else {
		index++;
	    }
	}

	/* now that we're all the way through return our list */
	return realCommas;
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
	    ArrayList<Integer[]> indexesToIgnore) {
	char[] contextChars = context.toCharArray();
	int index = startParen + 1;
	int open = 1;
	int close = 0;
	while (open > close) {
	    /*
	     * if the current character is a ( and it's not inside a comment or
	     * literal
	     */
	    if (contextChars[index] == '('
		    && !inIgnoredText(index, indexesToIgnore)) {
		open++;
	    }
	    /*
	     * if the current character is a ) and it's not inside a comment or
	     * literal
	     */
	    else if (contextChars[index] == ')'
		    && !inIgnoredText(index, indexesToIgnore)) {
		close++;
	    }
	    /* on to the next one */
	    index++;
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
		    endIndex = index;
		    index++;
		} else if (codeChars[index] == '*') {
		    startIndex = index - 1;
		    index++;
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
		}

		/* if we found stuff add to array */
		if (startIndex != -1)
		    commentIndexes.add(new Integer[] { startIndex, endIndex });
	    } else if (codeChars[index] == '"') {
		Integer startIndex = index;
		Integer endIndex = -1;
		index++;

		while (codeChars[index] != '"') {
		    if (codeChars[index] == '\\') {
			index++;
		    }
		    index++;
		}
		endIndex = index;
		index++;

		/* add to array */
		literalIndexes.add(new Integer[] { startIndex, endIndex });
	    } else if (codeChars[index] == '\'') {
		Integer startIndex = index;
		Integer endIndex = -1;
		index++;

		while (codeChars[index] != '\'') {
		    if (codeChars[index] == '\\') {
			index++;
		    }
		    index++;
		}
		endIndex = index;
		index++;

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

    /**
     * Removes all comments from a String and return the stripped String
     * "Th/*comment*\/is line //other stuff" becomes "This line" Necessary so
     * that line by line searching for beginning/ending with things can be
     * accurate
     * 
     * @param commented
     *            The String to remove comments from
     * @return String stripped of comments
     */
    private static String removeComments(String commented) {
	/* string -> char array, so we can analyze one char at a time */
	char[] commentedChars = commented.toCharArray();

	/* variable to hold our position */
	int index = 0;

	/* variable to hold our final String */
	String noComments = "";

	/* go through each character */
	while (index < commentedChars.length) {
	    /* check for a string literal */
	    if (commentedChars[index] == '"') {
		/*
		 * we don't want to remove something that looks like a comment
		 * if it's in here
		 */
		noComments = noComments + String.valueOf(commentedChars[index]);
		index++;
		while (commentedChars[index] != '"') {
		    noComments = noComments
			    + String.valueOf(commentedChars[index]);
		    if (commentedChars[index] == '\\') {
			index++;
			noComments = noComments
				+ String.valueOf(commentedChars[index]);
		    }
		    index++;
		}
		noComments = noComments + String.valueOf(commentedChars[index]);
		index++;
	    }

	    /* check for character literals */
	    else if (commentedChars[index] == '\'') {
		noComments = noComments + String.valueOf(commentedChars[index]);
		index++;
		while (commentedChars[index] != '\'') {
		    noComments = noComments
			    + String.valueOf(commentedChars[index]);
		    if (commentedChars[index] == '\\') {
			index++;
			noComments = noComments
				+ String.valueOf(commentedChars[index]);
		    }
		    index++;
		}
		noComments = noComments + String.valueOf(commentedChars[index]);
		index++;
	    }

	    /* check for the start of a comment */
	    else if (commentedChars[index] == '/') {
		index++;

		/* if it's this // kind of comment */
		if (commentedChars[index] == '/') {
		    index++;
		    while (commentedChars[index] != '\n') {
			index++;
			if (index >= commentedChars.length) {
			    return noComments;
			}
		    }
		    noComments = noComments + "\n";
		    index++;
		}
		/* if it's this /* kind of comment */
		else if (commentedChars[index] == '*') {
		    index++;
		    boolean done = false;
		    while (done == false) {
			/* maybe it's finishing... */
			if (commentedChars[index] == '*') {
			    index++;
			    if (commentedChars[index] == '/') {
				done = true;
			    }
			    index++;
			} else {
			    index++;
			}

		    }
		}
		/* if it's not even a comment */
		else {
		    noComments = noComments + "/";
		}
	    }

	    /* if there's no doubt just add it on */
	    else {
		noComments = noComments + String.valueOf(commentedChars[index]);
		index++;
	    }
	}
	return noComments;
    }

    /**
     * Removes all String/char literals from a String and return the stripped
     * String "System.out.println("\"Hello,\" he said." +
     * String.valueOf('\n'));" becomes "System.out.println( +
     * String.valueOf());" Doesn't remove literals from comments, so
     * "// Method "removeLiterals" removes literals" would be untouched
     * Necessary so that we can reliably check which functions are present in
     * user code
     * 
     * @param stringWithLiterals
     *            The String to remove String/char literals from
     * @return String stripped of literals
     */
    private static String removeLiterals(String stringWithLiterals) {
	/* string -> char array, so we can analyze one char at a time */
	char[] literalsChars = stringWithLiterals.toCharArray();

	/* variable to hold our position */
	int index = 0;

	/* variable to hold our final String */
	String noLiterals = "";

	/* go through each character */
	while (index < literalsChars.length) {
	    /* check for a string literal */
	    if (literalsChars[index] == '"') {
		index++;
		while (literalsChars[index] != '"') {
		    if (literalsChars[index] == '\\') {
			index++;
		    }
		    index++;
		}
		index++;
	    }

	    /* check for character literals */
	    else if (literalsChars[index] == '\'') {
		index++;
		while (literalsChars[index] != '\'') {
		    if (literalsChars[index] == '\\') {
			index++;
		    }
		    index++;
		}
		index++;
	    }

	    /* check for the start of a comment */
	    else if (literalsChars[index] == '/') {
		noLiterals = noLiterals + String.valueOf(literalsChars[index]);
		index++;

		/* if it's this // kind of comment */
		if (literalsChars[index] == '/') {
		    noLiterals = noLiterals
			    + String.valueOf(literalsChars[index]);
		    index++;
		    while (literalsChars[index] != '\n') {
			noLiterals = noLiterals
				+ String.valueOf(literalsChars[index]);
			index++;
		    }
		    noLiterals = noLiterals + "\n";
		    index++;
		}
		/* if it's this /* kind of comment */
		else if (literalsChars[index] == '*') {
		    noLiterals = noLiterals
			    + String.valueOf(literalsChars[index]);
		    index++;
		    boolean done = false;
		    while (done == false) {
			noLiterals = noLiterals
				+ String.valueOf(literalsChars[index]);
			/* maybe it's finishing... */
			if (literalsChars[index] == '*') {
			    index++;
			    noLiterals = noLiterals
				    + String.valueOf(literalsChars[index]);
			    if (literalsChars[index] == '/') {
				done = true;
			    }
			    index++;
			} else {
			    index++;
			}
		    }
		}
		/* if it's not even a comment */
		else {
		    noLiterals = noLiterals + "/";
		}
	    }

	    /* if there's no doubt just add it on */
	    else {
		noLiterals = noLiterals + String.valueOf(literalsChars[index]);
		index++;
	    }
	}
	return noLiterals;
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
     * Write data to a file
     * 
     * @param fPath
     *            the file to write to
     * @param data
     *            the data to write to file
     * @return true or false with success/failure
     */
    private static boolean fileWrite(String fPath, String data) {
	BufferedWriter bufferedWriter = null;
	boolean encounteredError = false;
	try {
	    File myFile = new File(fPath);
	    if (!myFile.exists())
		myFile.createNewFile();
	    Writer writer = new FileWriter(myFile);
	    bufferedWriter = new BufferedWriter(writer);
	    bufferedWriter.write(data);
	    return true;
	} catch (IOException e) {
	    System.out
		    .println("Compiler encountered an error writing to file \""
			    + fPath + "\"");
	    e.printStackTrace();
	    encounteredError = true;
	} finally {
	    /* if there's an error try to close the file if we can */
	    try {
		if (bufferedWriter != null)
		    bufferedWriter.close();
	    } catch (Exception ex) {

	    }
	    if (encounteredError)
		System.exit(1);
	}
	return false;
    }

    /**
     * Remove a directory and its contents.
     * 
     * @param directory
     *            File object representing directory to remove
     * @return true on success, false on failure
     */
    private static boolean deleteDirectory(File directory) {
	if (directory == null)
	    return false;
	if (!directory.exists())
	    return true;
	if (!directory.isDirectory())
	    return false;

	String[] list = directory.list();

	// Some JVMs return null for File.list() when the
	// directory is empty.
	if (list != null) {
	    for (int i = 0; i < list.length; i++) {
		File entry = new File(directory, list[i]);
		if (entry.isDirectory()) {
		    if (!deleteDirectory(entry))
			return false;
		} else {
		    if (!entry.delete())
			return false;
		}
	    }
	}
	return directory.delete();
    }

    /**
     * Check to see if a user's import statement is redundant
     * 
     * @param userImport
     *            the user's import statement
     * @param generatedImports
     *            the generated list of imports
     * @return true if the userImport is a line inside of generatedImports
     */
    private static boolean alreadyImported(String userImport,
	    String generatedImports) {
	for (String line : generatedImports.split("\n")) {
	    if (line.equals(userImport))
		return true;
	}
	return false;
    }

    /**
     * Indent each line of a string with one tab
     * 
     * @param x
     *            the string to indent
     * @return indented string
     */
    private static String indent(String x) {
	return indent(x, 1);
    }

    /**
     * Indent each line of a string with n tabs
     * 
     * @param x
     *            String to indent
     * @param n
     *            The number of tabs to indent by
     * @return indented string
     */
    private static String indent(String x, int n) {
	String indentedString = "";
	String indent = "";
	for (int i = 0; i < n; i++)
	    indent = indent + "\t";
	for (String line : x.split("\n"))
	    indentedString = indentedString + indent + line + "\n";
	return indentedString;
    }

    /**
     * Print a message to the terminal if verbose is set to true
     * 
     * @param message
     *            string to print
     * @param newLines
     *            amount of newline characters to print at end of string
     */
    private static void verbose(String message, int newLines) {
	if (verbose) {
	    System.out.print(message);
	    for (int i = 0; i < newLines; i++)
		System.out.print("\n");
	}
    }

    /**
     * Print a message to the terminal if verbose is set to true
     * 
     * @param message
     *            string to print
     */
    private static void verbose(String message) {
	verbose(message, 1);
    }

}
