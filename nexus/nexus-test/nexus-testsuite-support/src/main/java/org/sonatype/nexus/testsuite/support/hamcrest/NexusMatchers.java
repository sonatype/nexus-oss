/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.support.hamcrest;

import java.io.File;
import java.util.regex.Pattern;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.sonatype.sisu.litmus.testsupport.hamcrest.InversionMatcher;
import org.sonatype.sisu.litmus.testsupport.hamcrest.LogFileMatcher;

/**
 * Nexus related hamcrest matchers.
 *
 * @since 2.2
 */
public abstract class NexusMatchers
{

    /**
     * Log file should not contain any of the common unwanted exceptions: NullPointerException, ClassNotFoundException,
     * ClassCastException.
     *
     * @return matcher. Never null.
     */
    @Factory
    public static Matcher<File> doesNotHaveCommonExceptions()
    {
        return Matchers.allOf(
            InversionMatcher.not( LogFileMatcher.hasExceptionOfType( NullPointerException.class ) ),
            InversionMatcher.not( LogFileMatcher.hasExceptionOfType( ClassNotFoundException.class ) ),
            InversionMatcher.not( LogFileMatcher.hasExceptionOfType( ClassCastException.class ) )
        );
    }

    /**
     * Log file has no reference to plugins that failed to activate (all plugins were activated successfully).
     *
     * @return matcher. Never null.
     */
    @Factory
    public static Matcher<File> doesNotHaveFailingPlugins()
    {
        return InversionMatcher.not( hasFailingPlugins() );
    }

    /**
     * Log file has a reference to an arbitrary plugin that failed to activate.
     *
     * @return matcher. Never null.
     */
    @Factory
    public static LogFileMatcher hasFailingPlugins()
    {
        return LogFileMatcher.hasText( Pattern.compile(
            ".*Plugin manager request \"ACTIVATE\" on plugin \".*\" FAILED!"
        ) );
    }

    /**
     * Log file has a reference that a specified plugin was successfully activated.
     *
     * @param pluginId id of plugin that is supposed to fail to activate in format {@code <groupId>:<artifactId>}
     * @return matcher. Never null.
     */
    @Factory
    public static LogFileMatcher hasPluginActivatedSuccessfully( final String pluginId )
    {
        final String escapedPluginId = pluginId.replace( ".", "\\." );
        return LogFileMatcher.hasText( Pattern.compile(
            ".*Plugin manager request \"ACTIVATE\" on plugin \"" + escapedPluginId + ".*\" was successful."
        ) );
    }

    /**
     * Log file has a reference to a specified plugin that failed to activate.
     *
     * @param pluginId id of plugin that is supposed to fail to activate in format {@code <groupId>:<artifactId>}
     * @return matcher. Never null.
     */
    @Factory
    public static LogFileMatcher hasFailingPlugin( final String pluginId )
    {
        final String escapedPluginId = pluginId.replace( ".", "\\." );
        return LogFileMatcher.hasText( Pattern.compile(
            ".*Plugin manager request \"ACTIVATE\" on plugin \"" + escapedPluginId + ".*\" FAILED!"
        ) );
    }

}
