/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import javax.inject.Inject;

import org.codehaus.plexus.util.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.sonatype.guice.bean.containers.InjectedTest;

import org.sonatype.nexus.test.util.StopWatch;

public class ConfigurableInjectedTest
    extends InjectedTest
{

    static
    {
        System.setProperty( "guice.disable.misplaced.annotation.check", "true" );
        // http://code.google.com/p/guava-libraries/issues/detail?id=92
        System.setProperty( "guava.executor.class", "NONE" );
        // http://code.google.com/p/google-guice/issues/detail?id=288#c30
        System.setProperty( "guice.executor.class", "NONE" );
    }

    @Rule
    public TestName testName = new TestName();

    @Inject
    private Logger log;

    private StopWatch stopWatch;

    @Override
    public void configure( final Properties properties )
    {
        loadAll( properties, "injected-test.properties" );
        // per test class properties
        load( properties, this.getClass().getSimpleName() + "/injected-test.properties" );
        super.configure( properties );
    }


    private void loadAll( final Properties properties, final String name )
    {
        try
        {
            final Enumeration<URL> resources = getClass().getClassLoader().getResources( name );
            while ( resources.hasMoreElements() )
            {
                final URL resource = resources.nextElement();
                load( properties, resource );
            }
        }
        catch ( final IOException e )
        {
            throw new IllegalStateException( "Failed to load " + name, e );
        }
    }

    private void load( final Properties properties, final String name )
    {
        load( properties, getClass().getResource( name ) );
    }

    private void load( final Properties properties, final URL url )
    {
        if ( url != null )
        {
            InputStream in = null;
            try
            {
                in = url.openStream();
                if ( in != null )
                {
                    properties.load( in );
                }
                properties.putAll( System.getProperties() );
            }
            catch ( final IOException e )
            {
                throw new IllegalStateException( "Failed to load " + url.toExternalForm(), e );
            }
            finally
            {
                IOUtil.close( in );
            }
        }
    }

    /**
     * Setup test injection and log the start of the test.
     * <p>
     * final to protect against subclasses from forgetting to call super.
     */
    @Override
    @Before
    public final void setUp()
    {
        System.out.println("setUp method");
        stopWatch = new StopWatch();
        super.setUp();
        final String info = String.format( "Running test %s", testName.getMethodName() );
        log.info( fill( info.length(), '*' ) );
        log.info( info );
        log.info( fill( info.length(), '*' ) );
    }

    /**
     * Tear down injection and log the end of the test.
     */
    @Override
    @After
    public final void tearDown()
    {
        super.tearDown();
        final String info = String.format( "Running test %s took %s", testName.getMethodName(), stopWatch );
        log.info( fill( info.length(), '*' ) );
        log.info( info );
        log.info( fill( info.length(), '*' ) );
        stopWatch = null;
    }

    private String fill( final int length, final char fillWith )
    {
        final char[] fill = new char[length];
        Arrays.fill( fill, fillWith );
        final String result = new String( fill );
        return result;
    }
}
