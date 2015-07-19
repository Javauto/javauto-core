package com.automation.javauto.compiler;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.StandardLocation;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;

/**
 * Takes Java code and generates a class file. Errors are sent to
 * {@link DealWithCompilerErrors}.
 * 
 * @author matthew.downey
 *
 */
public class CustomJavaCompiler {
    public static DiagnosticCollector<JavaFileObject> compile(String name,
	    String code, String outDir, String classpath) throws IOException {
	/* get JavaCompiler object to create the .class file */
	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

	/* throw an exception if there's an issue with the compiler */
	if (compiler == null) {
	    throw new NullPointerException(
		    "Couldn't find java system compiler.");
	}

	/* get a file manager object that lets us control the output location */
	StandardJavaFileManager fileManager = compiler.getStandardFileManager(
		null, null, null);

	/* tell the file manager where to put the generated class files */
	fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
		Arrays.asList(new File(outDir)));

	/*
	 * tell the file manager where to look for related class files that may
	 * be referenced
	 */
	fileManager.setLocation(StandardLocation.CLASS_PATH,
		Arrays.asList(new File(classpath)));

	/*
	 * get a DiagnosticCollector<JavaFileObject> object to hold a list of
	 * potential errors
	 */
	DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

	/* create a JavaFileObject from our source code and class name */
	JavaFileObject file = new JavaSourceFromString(name, code);

	/* place our file as the only item in a list of files to be compiled */
	Iterable<JavaFileObject> compilationUnits = Arrays.asList(file);

	/* supply the compiler with all the information required to compile */
	CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
		null, null, compilationUnits);

	/*
	 * compile it -- note that errors will be inserted into our diagnostics
	 * variable
	 */
	boolean win = task.call();

	/* win == true if there weren't compilation errors */
	if (win)
	    return null;
	else
	    /* return errors */
	    return diagnostics;
    }
}

class JavaSourceFromString extends SimpleJavaFileObject {
    final String code;

    JavaSourceFromString(String name, String code) {
	super(URI.create("string:///" + name.replace('.', '/')
		+ Kind.SOURCE.extension), Kind.SOURCE);
	this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
	return code;
    }
}
