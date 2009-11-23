package org.sonatype.sample.wrapper;

import java.util.List;

/**
 * "High level" wrapper.conf editor, that uses WrapperConfWrapper and provides high-level editing capabilities but
 * keeping user made changed to the file.
 * 
 * @author cstamas
 */
public interface WrapperEditor
{
    /**
     * Returns the wrapper startup timeout in seconds.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-startup-timeout.html.
     * @return
     */
    int getWrapperStartupTimeout();

    /**
     * Sets the wrapper startup timeout in seconds.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-startup-timeout.html.
     * @param seconds the timeout in seconds
     */
    void setWrapperStartupTimeout( int seconds );

    /**
     * Returns the wrapper shutdown timeout in seconds.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-shutdown-timeout.html.
     * @return
     */
    int getWrapperShutdownTimeout();

    /**
     * Sets the wrapper shutdown timeout in seconds.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-shutdown-timeout.html.
     * @param seconds the timeout in seconds
     */
    void setWrapperShutdownTimeout( int seconds );

    /**
     * Returns the wrapper java main class.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-mainclass.html.
     * @return
     */
    String getWrapperJavaMainclass();

    /**
     * Sets the wrapper java main class.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-mainclass.html.
     * @param class to be executed by JSW brought up JVM
     */
    void setWrapperJavaMainclass( String cls );

    /**
     * Returns the unchangeable list of wrapper.java.classpath settings. The order in list follows wrapper.conf
     * "notation":
     * 
     * <pre>
     * wrapper.java.classpath.1=../../../lib/*.jar
     * wrapper.java.classpath.2=../../../conf/
     * </pre>
     * 
     * Note: First index is 1, not 0, but in Java List implementation, we follow the Java-way, hence
     * wrapper.java.classpath.1 becomes 1st element of the returned list on index 0. Changes to this list are <b>not</b>
     * persisted!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @return
     */
    List<String> getWrapperJavaClasspath();

    /**
     * Adds a new element to wrapper.conf wrapper.java.classpath configuration, to the last position. First index is 1,
     * not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @param classpathElem
     */
    void addWrapperJavaClasspath( String classpathElem );

    /**
     * Removes 1st occurence of the provided parameter from wrapper.conf wrapper.java.classpath list, potentionally
     * shiting up all elements after it from pos to pos-1. First index is 1, not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @param classpathElem
     */
    void removeWrapperJavaClasspath( String classpathElem );

    /**
     * Adds new element to wrapper.conf wrapper.java.classpath configuration, potentionally shifting down all elements
     * after pos to pos+1. First index is 1, not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @param pos
     * @param classpathElem
     */
    void addWrapperJavaClasspath( int pos, String classpathElem );
}
