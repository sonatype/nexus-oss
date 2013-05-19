/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.mirror;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.NexusProxyTestSupport;
import org.sonatype.nexus.proxy.repository.Mirror;
import org.sonatype.nexus.proxy.storage.remote.RemoteProviderHintFactory;

public class DefaultDownloadMirrorsTest
    extends NexusProxyTestSupport
{
    protected ApplicationConfiguration applicationConfiguration;

    protected RemoteProviderHintFactory remoteProviderHintFactory;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        applicationConfiguration = lookup( ApplicationConfiguration.class );

        remoteProviderHintFactory = lookup( RemoteProviderHintFactory.class );
    }

    @Test
    public void testNoMirrors()
    {
        DefaultDownloadMirrors mirrors = newDefaultDownloadMirrors( null );

        DownloadMirrorSelector selector = mirrors.openSelector( null );

        assertEquals( 0, mirrors.getMirrors().size() );
        assertEquals( 0, selector.getMirrors().size() );

        selector.close();
    }

    private DefaultDownloadMirrors newDefaultDownloadMirrors( Mirror[] mirrors )
    {
        DefaultCRepository conf = new DefaultCRepository();
        conf.setId( "kuku" );
        conf.setRemoteStorage( new CRemoteStorage() );
        conf.getRemoteStorage().setProvider( remoteProviderHintFactory.getDefaultHttpRoleHint() );
        conf.getRemoteStorage().setUrl( "http://repo1.maven.org/maven2/" );
        conf.setIndexable( false );

        CRepositoryCoreConfiguration coreConfig = new CRepositoryCoreConfiguration( applicationConfiguration, conf, null );

        DefaultDownloadMirrors dMirrors = new DefaultDownloadMirrors( coreConfig );

        if ( mirrors != null )
        {
            dMirrors.setMirrors( Arrays.asList( mirrors ) );
        }

        // to apply this
        try
        {
            coreConfig.commitChanges();
        }
        catch ( ConfigurationException e )
        {
            fail( e.getMessage() );
        }

        return dMirrors;
    }

    @Test
    public void testSimpleMirrorSelection()
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        int maxMirrors = 2;

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        dMirrors.setMaxMirrors( maxMirrors );

        assertEquals( mirrors.length, dMirrors.getMirrors().size() );

        DownloadMirrorSelector selector = dMirrors.openSelector( null );

        List<Mirror> _mirrors = selector.getMirrors();

        assertEquals( maxMirrors, _mirrors.size() );
        assertEquals( mirrors[0], _mirrors.get( 0 ) );
        assertEquals( mirrors[1], _mirrors.get( 1 ) );

        selector.close();
    }

    @Test
    public void testFailure()
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        DownloadMirrorSelector selector = dMirrors.openSelector( null );

        selector.feedbackFailure( mirrors[0] );

        selector.feedbackSuccess( mirrors[1] );

        // feedback has not been applied yet
        assertEquals( mirrors[0], dMirrors.openSelector( null ).getMirrors().get( 0 ) );

        selector.close();
        assertEquals( mirrors[1], dMirrors.openSelector( null ).getMirrors().get( 0 ) );
    }

    @Test
    public void testBlacklistDecay()
        throws Exception
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        long blacklistTTL = 100L; // milliseconds

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        dMirrors.setBlacklistExpiration( blacklistTTL );

        DownloadMirrorSelector selector = dMirrors.openSelector( null );

        selector.feedbackFailure( mirrors[0] );

        selector.feedbackSuccess( mirrors[1] );

        selector.close();

        assertEquals( true, dMirrors.isBlacklisted( mirrors[0] ) );

        Thread.sleep( blacklistTTL * 2 );

        assertEquals( false, dMirrors.isBlacklisted( mirrors[0] ) );

    }

    @Test
    public void testSetUrls()
        throws Exception
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        DownloadMirrorSelector selector = dMirrors.openSelector( null );

        selector.feedbackFailure( mirrors[0] );

        selector.feedbackSuccess( mirrors[1] );

        selector.close();

        // sanity check

        assertEquals( true, dMirrors.isBlacklisted( mirrors[0] ) );

        dMirrors.setMirrors( Arrays.asList( new Mirror( "3", "another-mirror" ), new Mirror( "4", "one-more-mirror" ),
                                            mirrors[0] ) );

        assertEquals( true, dMirrors.isBlacklisted( mirrors[0] ) );
    }

    @Test
    public void testFailureThenSuccess()
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        DownloadMirrorSelector selector = dMirrors.openSelector( null );

        selector.feedbackFailure( mirrors[0] );

        selector.feedbackSuccess( mirrors[0] );

        selector.close();

        // sanity check

        assertEquals( false, dMirrors.isBlacklisted( mirrors[0] ) );
    }
}
