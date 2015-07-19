#Javauto
Javauto is a programming language for automation. Derived from Java, it is a cross platform alternative to something like AutoIt.

Website: http://javauto.org/

![build](https://travis-ci.org/Javauto/javauto-core.svg)

#Installing
Detailed install & getting started instructions: http://javauto.org/docs/getting-started.html

##Linux
Install JDK, clone the repository, run the install script.
```
$ sudo apt-get install default-jdk
$ git clone https://github.com/matthewdowney/javauto
$ cd javauto
$ sudo ./install-linux.sh
```

Now that it's installed you can remove the repo if you want.
```
$ cd ..
$ rm -rf javauto/
```

##Windows
To install on Windows, you're going to need to [download the latest JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
Done? Great. Now:

1. Clone the repo or download a zip file and extract it.
2. **Name this folder javauto and put it at your desired install location.** Program Files or simply the C:\ drive is recommended. 
3. Once that's done open up the folder and double click the install-windows.bat (you might have to run this as administrator).

Now it's installed.

##Mac
An official installer is coming soon, but it's really not that hard if you have the urge to do it yourself. All that's prohibiting me from implementing this installer is my lack of a Mac.

1. Fork/download the repo
2. Put it somewhere
3. Add the `jars` directory to the system path

Now it's pretty much installed, you'll just have to call jar files explicity instead of using OS specific wrappers. For example, you'll have to say `java -jar javauto-helper.jar` instead of `javauto-helper`.
