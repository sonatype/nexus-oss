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

    @Factory
    public static Matcher<File> logHasNoCommonExceptions()
    {
        return Matchers.allOf(
            InversionMatcher.not( LogFileMatcher.hasExceptionOfType( NullPointerException.class ) ),
            InversionMatcher.not( LogFileMatcher.hasExceptionOfType( ClassNotFoundException.class ) ),
            InversionMatcher.not( LogFileMatcher.hasExceptionOfType( ClassCastException.class ) )
        );
    }

    @Factory
    public static Matcher<File> logHasNoFailingPlugins()
    {
        return InversionMatcher.not( logHasFailingPlugins() );
    }

    @Factory
    public static LogFileMatcher logHasFailingPlugins()
    {
        return LogFileMatcher.hasText( Pattern.compile(
            ".*Plugin manager request \"ACTIVATE\" on plugin \".*\" FAILED!"
        ) );
    }

    @Factory
    public static LogFileMatcher logHasFailingPlugin( final String pluginId )
    {
        final String escapedPluginId = pluginId.replace( ".", "\\." );
        return LogFileMatcher.hasText( Pattern.compile(
            ".*Plugin manager request \"ACTIVATE\" on plugin \"" + escapedPluginId + ".*\" FAILED!"
        ) );
    }

}
