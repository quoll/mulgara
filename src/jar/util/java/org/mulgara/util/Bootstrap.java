/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): Duraspace.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.util;

// Java 2 standard packages
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * Bootstraps the execution of the executable JAR by including embedded JAR
 * files in the classpath. <P/>
 *
 * This class is only meant to be used from within the executable JAR. It will
 * throw an error if you try to execute it from outside a JAR file. <P/>
 *
 * To use this class, add the following to the attributes to the manifest of
 * your enclosing JAR: <PRE>
 * Main-Class: org.mulgara.Bootstrap
 * Embedded-Jar: jar1filename.jar, jar2filename.jar,jar3filename.jar
 * Embedded-Main-Class: com.foo.Bar
 * </PRE> The <CODE>Main-Class</CODE> attribute tells the JVM which class to
 * execute (this class) when run as <KBD>java -jar &lt;jar-file&gt;</KBD> . The
 * <CODE>Embedded-Jar</CODE> attribute specifies the names of the embedded JAR
 * files to be included in the classpath. The <CODE>Embedded-Main-Class</CODE>
 * attribute tells the bootstrap loader which class to execute once the JARs
 * have beed added to the classpath (ie. this is the actual class you want to
 * run). <P/>
 *
 * Consult the JAR <A href="http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html">
 * JAR file specification</A> for more details on manifest attributes. <P/>
 *
 * At the moment, this class extracts embedded JAR files to the temp directory
 * (defined by the system property <CODE>java.io.tmpdir</CODE>) using
     * java.io.File.createTempFile() and adds them to the classpath. A better way to
 * implement this would be to write a real classloader.
 *
 * @created 2000-08-09
 *
 * @author Tom Adams
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:29 $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Bootstrap extends URLClassLoader {

  //
  // Constants
  //

  /**
   * the key to retrieve an embedded jar file name
   *
   */
  private final static String EMBEDDED_JAR_KEY = "Embedded-Jar";

  /**
   * the key to retrieve the executable class
   *
   */
  private final static String EMBEDDED_MAIN_CLASS_KEY = "Embedded-Main-Class";

  /**
   * Used by the addToSystemClasspath hack
   */
  private static final Class<?>[] parameters = new Class[]{URL.class};

  /**
   * create a 100K temp buffer to store the JAR bytes in
   *
   */

  //static private byte [] buf = new byte [10240000];
  private static byte[] buf = new byte[102400];

  //
  // Members
  //

  /** the array of JAR files to load */
  @SuppressWarnings("unused")
  private URL[] jarURLs = null;

  //
  // Constructors
  //

  /**
   * Creates a new bootstrap class loader, and loads the <code>jarURLs</code>.
   *
   * @param jarURLs an array of URLs pointing to the jar files to be loaded
   */
  public Bootstrap(URL[] jarURLs) {

    // call the super class constructor
    super(jarURLs, ClassLoader.getSystemClassLoader());

    //try to expose The Server Jars to the System classpath
    try {
      addToSystemClasspath(jarURLs);
    }
    catch (Exception e) {
      new Exception("Failed to add Embedded Jars to the System Classpath", e).printStackTrace();
    }

    // set the list of URLs
    this.jarURLs = jarURLs;
  }


  /**
   * Loads a comma separated list of embedded JAR files specified by an <CODE>Embedded-Jar</CODE>
   * manifest attribute, then executes a class specified by an <CODE>Embedded-Main-Class</CODE>
   * manifest attribute.
   *
   * @param args PARAMETER TO DO
   */
  public static void main(String[] args) {

    // Here's how this works... We need to get at the manifest of the JAR this
    // class is contained within. To do this we need to know the name of a
    // resource within the JAR.
    //
    // The only resource that we *know* is in the JAR is this class. So we
    // get the resource associated with this class and get the JAR's manifest.
    //
    // From the manifest we read the attributes we care about, write the
    // embedded JARs to a temporary directory, load them, then execute the main
    // class.
    try {

      // retrieve the manifest attributes
      Attributes manifestAttr = retrieveManifestAttributes();

      // throw and error if we couldn't get any manifest attributes
      if (manifestAttr == null) throw new Exception("No manifest attributes found for JAR");

      // get the name of the embedded main class
      String embeddedMainClass = manifestAttr.getValue(EMBEDDED_MAIN_CLASS_KEY);

      // it's pointless to continue without a main class
      if (embeddedMainClass == null) {
        throw new Exception("No Embedded-Main-Class attribute in manifest");
      }

      // Set the path of the jar as a System property - mulgara.jar.path
      URL bootURL = ClassLoader.getSystemResource("org/mulgara/util/Bootstrap.class");

      String bootURLString = bootURL.toString();
      String preString = "jar:file:";
      int startIndex = preString.length();
      int bangIndex = bootURLString.indexOf('!');
      String jarPath = bootURLString.substring(startIndex, bangIndex);
      System.setProperty("mulgara.jar.path", jarPath);

      // ************************************************************************
      // HACK: This is a hack to get the ARP parser working inside the bootstrapper.
      //       It should be removed once we work out a real solution to the
      //       dodgy error message problem.
      // set the xerces system property if we're executing the Mulgara server
      if (embeddedMainClass.equals("org.mulgara.server.EmbeddedMulgaraServer")) {
        System.setProperty("org.mulgara.xml.ResourceDocumentBuilderFactory",
            "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
      }

      // ************************************************************************
      // get a list of URLs to the embedded JARs
      LinkedList<URL> jarURLs = getEmbeddedJarURLs(manifestAttr);

      // create a new bootstrap classloader
      Bootstrap loader = new Bootstrap((URL[])jarURLs.toArray(new URL[jarURLs.size()]));

      // set the context class loader to the bootstrap
      Thread.currentThread().setContextClassLoader(loader);

      // invokes the 'real' main class
      loader.invokeClass(embeddedMainClass, args);
    } catch (Exception e) {

      // print the contents of the exception
      System.err.println("Unable to bootstrap embedded main class: " + e.toString());
      System.err.println(">> Stack trace:");
      e.printStackTrace();

      // get the underlying cause
      Throwable rootCause = null;
      Throwable cause = e.getCause();

      while (cause != null) {
        rootCause = cause;
        cause = cause.getCause();
      }

      // print a stack trace on it
      if (rootCause != null) {
        System.err.println(">> Root cause stack trace:");
        System.err.flush();
        rootCause.printStackTrace();
      }

    }

  }


  /**
   * Shutdown the current application by forcing the runtime shutdown hooks to be executed.
   *
   * @param args command line arguments
   */
  public static void shutdown(String[] args) {
    System.exit(0);
  }

  /**
   * Hack used to add the Embedded Jars to the system classpath.
   *
   * @param urls URLs of JARs to embed
   * @throws IOException Caused by any problem updating the system class path
   */
  public static void addToSystemClasspath(URL[] urls) throws IOException {

    if (urls == null) throw new IllegalArgumentException("null 'urls' parameter.");

    //add each
    for (int i = 0; i < urls.length; i++) {
      addToSystemClasspath(urls[i]);
    }
  }

  /**
   * Adds the jar (url) to the system classpath.
   *
   * @param url URL to add to the system classpath.
   * @throws IOException Caused by any problem updating the system class path
   */
  public static void addToSystemClasspath(URL url) throws IOException {

    URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> sysclass = URLClassLoader.class;

    try {
      Method method = sysclass.getDeclaredMethod("addURL", parameters);
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] {url});
    } catch (Throwable t) {
      t.printStackTrace();
      throw new IOException("Error, could not add URL to system classloader");
    }
  }


  /**
   * Returns a list of URLs to the embedded jar files.
   *
   * @param manifestAttr the manifest attributes of the jar to retrieve the
   *      embedded jar names from
   * @return a list of URLs to the embedded jar files, null if no embedded jars found
   */
  private static LinkedList<URL> getEmbeddedJarURLs(Attributes manifestAttr) {

    // check the parameters
    if (manifestAttr == null) throw new IllegalArgumentException("Null manifest attribute");

    // create a list to hold the JAR resources
    LinkedList<URL> jarURLs = new LinkedList<URL>();

    // get the list of embedded jars
    String embeddedJarList = manifestAttr.getValue(EMBEDDED_JAR_KEY);

    if (embeddedJarList != null ) {
    
      // tokense the list of jar files
      StringTokenizer jarTokenizer = new StringTokenizer(embeddedJarList, " ,\t\f");

      // add a URL for each embedded jar to the array
      while (jarTokenizer.hasMoreTokens()) {
        // write this JAR to a temp file and get its URL
        URL jarURL = writeTempJARFile(jarTokenizer.nextToken());
        if (jarURL != null) jarURLs.add(jarURL);
      }
    }

    // we don't want the buffer hanging around
    buf = null;

    // return the URLs
    return jarURLs;
  }

  /**
   * Retrieves the manifest attributes of the JAR from which this file is running.
   *
   * @return the manifest attributes of the JAR from which this class is running
   * @throws Exception if unable to retrieve the JAR resource for a class
   */
  private static Attributes retrieveManifestAttributes() throws Exception {

    // get the name of this class with all the "."s replaced with "/"s
    String className = Bootstrap.class.getName().replace('.', '/');

    // create a new URL pointing to the resource representing this class
    URL classURL = ClassLoader.getSystemResource(className + ".class");

    // throw an error if we could not get it
    if (classURL == null) throw new Exception("Unable to retrieve JAR resource for " + className);

    // end if
    // open a connection to the class resource
    URLConnection urlConn = classURL.openConnection();

    // make sure that we're executing from within a JAR
    if ( (urlConn == null) || ! (urlConn instanceof JarURLConnection)) {
      throw new Exception("Bootstrap class must be executed from within a JAR");
    }

    // return its manifest attributes
    return ((JarURLConnection)urlConn).getMainAttributes();
  }


  /**
   * Writes an embedded JAR file to a temporary file, and returns a URL to the
   * temporary file.
   *
   * @param embeddedJARFilename the name of the embedded JAR file to retrieve
   * @return the URL of the temporary JAR file, null if we were unable to
   *      retrieve a the embedded JAR or something went wrong writing a temp file
   */
  private static URL writeTempJARFile(String embeddedJARFilename) {

    URL embeddedJarURL = null;

    try {

      // get the embedded jar as a stream
      InputStream jarIn = ClassLoader.getSystemResourceAsStream(embeddedJARFilename);

      // check that the embedded filename is valid
      if (jarIn == null) {
        throw new IOException("Embedded JAR: " + embeddedJARFilename + " does not exist in enclosing JAR.");
      }

      try {

        // create a temporary file to write the jar to (we may need to keep the
        // class on disk for windows weenies...)
        File tmpJarFile = File.createTempFile("mulgara", ".jar");
        tmpJarFile.deleteOnExit();
  
        // set the embedded JAR's URL
        embeddedJarURL = tmpJarFile.toURI().toURL();

        // get a stream so we can write to it
        FileOutputStream out = new FileOutputStream(tmpJarFile);
  
        try {
          // write the embedded jar to disk
          int n;
          while ((n = jarIn.read(buf)) != -1) out.write(buf, 0, n);

        } finally {
          out.close();
        }
  
      } finally {
        jarIn.close();
      }

    } catch (IOException ioe) {
      System.err.println(ioe);
    }

    // return the temp file's URL
    return embeddedJarURL;
  }


  /**
   * Invokes the <CODE>main()</CODE> or or <CODE>shutdown()</CODE> method of the
   * class <CODE>className</CODE> with the given array of arguments. The class
   * must define a <CODE>public static void xxx()</CODE> that takes an array of
   * String.
   *
   * @param className the name of the class to execute
   * @param args the arguments for the class' main method
   * @throws ClassNotFoundException if the specified class could not be found
   * @throws NoSuchMethodException if the specified class does not contain a
   *      "main" method
   * @throws InvocationTargetException if the application raised an exception
   * @throws IllegalAccessException if we do not have access to invoke the
   *      method
   */
  private void invokeClass(String className,
      String[] args) throws ClassNotFoundException,
      NoSuchMethodException,
      InvocationTargetException,
      IllegalAccessException {

    // by default the main method is requested
    String methodRequest = "main";

    // check for a shutdown request
    for (int i = 0; i < args.length; i++) {

      if (args[i].equalsIgnoreCase("--shutdown") || args[i].equalsIgnoreCase("-x")) {
        // change the method to a shutdown
        methodRequest = "shutdown";
      }
    }

    // load the class
    Class<?> c = this.loadClass(className);

    // get its main method
    Method m = c.getMethod(methodRequest, new Class[] {args.getClass()});
    m.setAccessible(true);

    // retrieve its modifiers
    int mods = m.getModifiers();

    // make sure that it is a public static void method
    if ((m.getReturnType() != void.class) || !Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
      throw new NoSuchMethodException(methodRequest);
    }

    // invoke it!
    m.invoke(null, new Object[] {args});
  }

}
