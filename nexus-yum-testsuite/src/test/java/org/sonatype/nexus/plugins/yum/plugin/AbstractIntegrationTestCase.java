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
package org.sonatype.nexus.plugins.yum.plugin;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.YumClient;
import org.sonatype.nexus.testsuite.support.NexusRunningITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
public class AbstractIntegrationTestCase
    extends NexusRunningITSupport
{

    private YumClient yum;

    @Override
    protected NexusBundleConfiguration configureNexus( NexusBundleConfiguration configuration )
    {
        return configuration.addPlugins( getPluginFile() );// .enableDebugging( 8000, true );
    }

    protected YumClient yum()
    {
        if ( yum == null )
        {
            yum = client().getSubsystem( YumClient.class );
        }

        return yum;
    }

    private File getPluginFile()
    {
        URL pluginFileUrl = getClass().getResource( "/plugin.zip" );
        if ( pluginFileUrl == null )
        {
            throw new IllegalStateException( "Couldn't find /plugin.zip in classpath" );
        }
        try
        {
            return new File( pluginFileUrl.toURI() );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( "Could not determine plugin bundle URI.", e );
        }
    }

    protected File testData( String path )
    {
        return testData().resolveFile( path );
    }

}
