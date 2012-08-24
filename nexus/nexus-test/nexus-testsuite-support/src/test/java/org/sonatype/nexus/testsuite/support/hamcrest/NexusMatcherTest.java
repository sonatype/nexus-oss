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

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.litmus.testsupport.junit.TestInfoRule;

/**
 * {@link NexusMatchers} UTs.
 *
 * @since 2.2
 */
public class NexusMatcherTest
    extends TestSupport
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TestInfoRule testInfo = new TestInfoRule();

    @Test
    public void inexistentLogFile()
        throws Exception
    {
        thrown.expect( AssertionError.class );
        thrown.expectMessage( "java.io.FileNotFoundException: File 'nexus.log' does not exist" );
        assertThat(
            new File( "nexus.log" ),
            NexusMatchers.logHasNoCommonExceptions()
        );
    }

    @Test
    public void logFileHasNoCommonExceptions()
        throws Exception
    {
        assertThat(
            resolveLogFile(),
            NexusMatchers.logHasNoCommonExceptions()
        );
    }

    @Test
    public void logFileHasNoFailingPlugins()
        throws Exception
    {
        assertThat(
            resolveLogFile(),
            NexusMatchers.logHasNoFailingPlugins()
        );
    }

    @Test
    public void logFileHasFailingPlugins()
        throws Exception
    {
        assertThat(
            resolveLogFile(),
            NexusMatchers.logHasFailingPlugins()
        );
    }

    @Test
    public void logFileHasFailingPlugin()
        throws Exception
    {
        assertThat(
            resolveLogFile(),
            NexusMatchers.logHasFailingPlugin( "com.sonatype.nexus.plugin:nexus-outreach-plugin" )
        );
    }

    private File resolveLogFile()
    {
        return util.resolveFile( String.format(
            "src/test/uncopied-resources/%s/%s.log", testInfo.getTestClass().getSimpleName(), testInfo.getMethodName()
        ) );
    }

}
