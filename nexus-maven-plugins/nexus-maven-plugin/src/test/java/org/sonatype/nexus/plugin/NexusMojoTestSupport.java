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
package org.sonatype.nexus.plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.settings.Proxy;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.sonatype.jettytestsuite.ProxyServer;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

public class NexusMojoTestSupport
    extends AbstractRESTTest
{

    protected final ConversationalFixture fixture =
        new ConversationalFixture( getExpectedUser(), getExpectedPassword() );

    protected static final Set<File> toDelete = new HashSet<File>();

    protected Log log;

    protected SecDispatcher secDispatcher;

    protected PlexusContainer container;

    protected ExpectPrompter prompter;

    private static Random random = new Random();

    protected ProxyServer proxyServer;

    private int port;

    @Before
    public void beforeEach()
        throws ComponentLookupException, PlexusContainerException, StartingException, InitializationException,
        IOException
    {
        log = new SystemStreamLog()
        {
            @Override
            public boolean isDebugEnabled()
            {
                return true;
            }
        };

        prompter = new ExpectPrompter();

        prompter.enableDebugging();

        container = new DefaultPlexusContainer();
        secDispatcher = (SecDispatcher) container.lookup( SecDispatcher.class.getName(), "maven" );

        this.port = 0;
        // non admin can't open port bellow 1024 on linux
        while ( port < 1024 )
        {
            ServerSocket ss = new ServerSocket( 0 );
            port = ss.getLocalPort();
            ss.close();
            assertThat( port, not( equalTo( 0 ) ) );
        }

        proxyServer = new ProxyServer();
        proxyServer.setPort( port );
        proxyServer.initialize();
    }

    @After
    public void afterEach()
        throws ComponentLifecycleException, StoppingException
    {
        if ( toDelete != null )
        {
            for ( Iterator<File> it = toDelete.iterator(); it.hasNext(); )
            {
                File f = it.next();

                try
                {
                    FileUtils.forceDelete( f );
                }
                catch ( IOException e )
                {
                    System.out.println( "Failed to delete test file/dir: " + f + ". Reason: " + e.getMessage() );
                }
                finally
                {
                    it.remove();
                }

            }
        }

        container.release( secDispatcher );

        // stop proxy server if started
        stopProxyServer();

        container.dispose();

        prompter.verifyPromptsUsed();
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

    protected void printTestName()
    {
        StackTraceElement e = new Throwable().getStackTrace()[1];
        System.out.println( "\n\nRunning: '"
            + ( getClass().getName().substring( getClass().getPackage().getName().length() + 1 ) ) + "#"
            + e.getMethodName() + "'\n\n" );
    }

    protected static File createTempDir( String prefix, String suffix )
        throws IOException
    {
        return createTmp( prefix, suffix, true );
    }

    protected static File createTempFile( String prefix, String suffix )
        throws IOException
    {
        return createTmp( prefix, suffix, false );
    }

    private static File createTmp( String prefix, String suffix, boolean isDir )
        throws IOException
    {
        File tmp = getTempFile( prefix, suffix );
        if ( isDir )
        {
            tmp.mkdirs();
        }
        else
        {
            tmp.createNewFile();
        }

        toDelete.add( tmp );

        return tmp;
    }

    protected static File getTempFile( String prefix, String suffix )
    {
        File tmp =
            new File( System.getProperty( "java.io.tmpDir", "target" ), prefix + random.nextInt( Integer.MAX_VALUE )
                + suffix );
        return tmp;
    }

    /**
     * Stop the proxy server if started.
     * 
     */
    protected final void stopProxyServer()
        throws StoppingException
    {
        proxyServer.stop();
    }

    /**
     * Starts a proxy server on a random port
     * 
     * @return
     */
    protected final void startProxyServer( boolean includeAuth )
        throws StartingException
    {
        if ( includeAuth )
        {
            proxyServer.getProxyServlet().setUseAuthentication( true );
            proxyServer.getProxyServlet().getAuthentications().put( "proxyuser", "proxypass" );
        }
        this.proxyServer.start();
    }

    /**
     * Get the port the proxy server is listening on
     * 
     * @return
     */
    protected final int getProxyServerPort()
    {
        return this.port;
    }

    /**
     * @return a maven setting proxy optionally configured with the proxy authentication credentials
     */
    protected final Proxy getMavenSettingsProxy( boolean includeAuth )
    {
        Proxy proxy = new Proxy();
        proxy.setActive( true );
        proxy.setHost( "localhost" );
        proxy.setPort( getProxyServerPort() );
        proxy.setNonProxyHosts( "localhostdisabled" );

        if ( includeAuth )
        {
            Map<String, String> auths = proxyServer.getProxyServlet().getAuthentications();
            assertThat( auths.isEmpty(), not( true ) );
            proxy.setPassword( auths.entrySet().iterator().next().getValue() );
            proxy.setUsername( auths.entrySet().iterator().next().getKey() );
        }

        return proxy;
    }

}
