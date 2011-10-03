/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.sample.wrapper;

import java.util.List;

/**
 * "High level" wrapper.conf editor, that uses WrapperConfWrapper and provides high-level editing capabilities but
 * keeping user made changed to the file.
 * 
 * @author cstamas
 */
public interface WrapperEditor
    extends PersistedConfiguration
{
    /**
     * Returns the WrapperConfWrapper instance that this Editor uses under the hud.
     * 
     * @return
     */
    WrapperConfWrapper getWrapperConfWrapper();

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
     * Returns a changeable list of wrapper.java.classpath settings. The order in list follows wrapper.conf "notation":
     * 
     * <pre>
     * wrapper.java.classpath.1=../../../lib/*.jar
     * wrapper.java.classpath.2=../../../conf/
     * </pre>
     * 
     * Note: First index is 1, not 0, but in Java List implementation, we follow the Java-way, hence
     * wrapper.java.classpath.1 becomes 1st element of the returned list on index 0. Changes to this list are <b>not</b>
     * persisted! Use setWrapperJavaClasspath() to persist it!
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
     * @return true
     */
    boolean addWrapperJavaClasspath( String classpathElem );

    /**
     * Removes 1st occurence of the provided parameter from wrapper.conf wrapper.java.classpath list, potentionally
     * shiting up all elements after it from pos to pos-1. First index is 1, not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @param classpathElem
     * @return true if elem found and removed
     */
    boolean removeWrapperJavaClasspath( String classpathElem );

    /**
     * Replaces all elements in wrapper.conf wrapper.java.classpath configuration.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-classpath-n.html.
     * @param pos
     * @param classpathElem
     */
    void setWrapperJavaClasspath( List<String> classpathElems );

    /**
     * Returns a changeable list of wrapper.java.additional settings. The order in list follows wrapper.conf "notation":
     * 
     * <pre>
     * wrapper.java.additional.1=-d32
     * wrapper.java.additional.2=-Xmx512
     * </pre>
     * 
     * Note: First index is 1, not 0, but in Java List implementation, we follow the Java-way, hence
     * wrapper.java.additional.1 becomes 1st element of the returned list on index 0. Changes to this list are
     * <b>not</b> persisted! Use setWrapperJavaAdditional() to persist it!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-additional-n.html.
     * @return
     */
    List<String> getWrapperJavaAdditional();

    /**
     * Adds a new element to wrapper.conf wrapper.java.classpath configuration, to the last position. First index is 1,
     * not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-additional-n.html.
     * @param additionalElem
     * @return true
     */
    boolean addWrapperJavaAdditional( String additionalElem );

    /**
     * Removes 1st occurence of the provided parameter from wrapper.conf wrapper.java.classpath list, potentionally
     * shiting up all elements after it from pos to pos-1. First index is 1, not 0!
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-additional-n.html.
     * @param additionalElem
     * @return true if elem found and removed
     */
    boolean removeWrapperJavaAdditional( String additionalElem );

    /**
     * Replaces all elements in wrapper.conf wrapper.java.classpath configuration.
     * 
     * @see http://wrapper.tanukisoftware.org/doc/english/prop-java-additional-n.html.
     * @param pos
     * @param additionalElems
     */
    void setWrapperJavaAdditional( List<String> additionalElems );
}
