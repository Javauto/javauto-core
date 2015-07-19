package com.automation.javauto.compiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;

/**
 * Takes a directory and adds each class file within it to a JAR file.
 * 
 * @author matthew.downey
 *
 */
public class CustomJarCompiler {
    /* define constants for different terminal colors */
    private static String GREEN = "\033[92m";
    private static String YELLOW = "\033[93m";
    private static String NORMAL = "\033[0m";

    public static boolean colors = true;

    public static boolean verbose = false;

    public static void create(String output, String mainClass, String inputDir)
	    throws IOException {
	if (!colors) {
	    GREEN = "";
	    YELLOW = "";
	    NORMAL = "";
	}
	verbose("Building " + GREEN + (new File(output)).getAbsolutePath()
		+ NORMAL);
	Manifest manifest = new Manifest();
	manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
		"1.0");
	manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
	JarOutputStream target = new JarOutputStream(new FileOutputStream(
		output), manifest);
	add(new File(inputDir), target);
	target.close();
    }

    private static void add(File source, JarOutputStream target)
	    throws IOException {
	BufferedInputStream in = null;
	try {
	    if (source.isDirectory()) {
		String name = source.getPath().replace("\\", "/");
		if (!name.isEmpty()) {
		    if (!name.endsWith("/"))
			name += "/";
		    JarEntry entry = new JarEntry(name);
		    entry.setTime(source.lastModified());
		    target.putNextEntry(entry);
		    target.closeEntry();
		}
		for (File nestedFile : source.listFiles())
		    add(nestedFile, target);
		return;
	    }

	    JarEntry entry = new JarEntry(source.getName());
	    entry.setTime(source.lastModified());
	    target.putNextEntry(entry);
	    in = new BufferedInputStream(new FileInputStream(source));

	    byte[] buffer = new byte[1024];
	    while (true) {
		int count = in.read(buffer);
		if (count == -1)
		    break;
		target.write(buffer, 0, count);
	    }
	    target.closeEntry();
	    verbose("\tadded " + YELLOW + source.getName() + NORMAL);
	} finally {
	    if (in != null)
		in.close();
	}
    }

    private static void verbose(String msg) {
	if (verbose)
	    System.out.println(msg);
    }

}
