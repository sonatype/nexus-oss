/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.mirror;

import java.util.Arrays;
import java.util.List;

import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.repository.Mirror;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

public class DefaultDownloadMirrorsTest
    extends AbstractNexusTestCase
{

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    public void testNoMirrors()
    {
        DefaultDownloadMirrors mirrors = newDefaultDownloadMirrors( null );

        DownloadMirrorSelector selector = mirrors.openSelector();

        assertEquals( 0, mirrors.getMirrors().size() );
        assertEquals( 0, selector.getMirrors().size() );

        selector.close();
    }

    private DefaultDownloadMirrors newDefaultDownloadMirrors( Mirror[] mirrors )
    {
        DefaultCRepository conf = new DefaultCRepository();
        conf.setRemoteStorage( new CRemoteStorage() );
        conf.getRemoteStorage().setProvider( CommonsHttpClientRemoteStorage.PROVIDER_STRING );
        conf.getRemoteStorage().setUrl( "http://repo1.maven.org/maven2/" );

        CRepositoryCoreConfiguration coreConfig = new CRepositoryCoreConfiguration( conf );

        DefaultDownloadMirrors dMirrors = new DefaultDownloadMirrors( coreConfig );

        if ( mirrors != null )
        {
            dMirrors.setMirrors( Arrays.asList( mirrors ) );
        }
        
        // to apply this
        coreConfig.applyChanges();

        return dMirrors;
    }

    public void testSimpleMirrorSelection()
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        int maxMirrors = 2;

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        dMirrors.setMaxMirrors( maxMirrors );

        assertEquals( mirrors.length, dMirrors.getMirrors().size() );

        DownloadMirrorSelector selector = dMirrors.openSelector();

        List<Mirror> _mirrors = selector.getMirrors();

        assertEquals( maxMirrors, _mirrors.size() );
        assertEquals( mirrors[0], _mirrors.get( 0 ));
        assertEquals( mirrors[1], _mirrors.get( 1 ) );

        selector.close();
    }

    public void testFailure()
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        DownloadMirrorSelector selector = dMirrors.openSelector();

        selector.feedbackFailure( mirrors[0] );

        selector.feedbackSuccess( mirrors[1] );

        // feedback has not been applied yet
        assertEquals( mirrors[0], dMirrors.openSelector().getMirrors().get( 0 ) );

        selector.close();
        assertEquals( mirrors[1], dMirrors.openSelector().getMirrors().get( 0 ) );
    }

    public void testBlacklistDecay()
        throws Exception
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        long blacklistTTL = 100L; // milliseconds

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        dMirrors.setBlacklistExpiration( blacklistTTL );

        DownloadMirrorSelector selector = dMirrors.openSelector();

        selector.feedbackFailure( mirrors[0] );

        selector.feedbackSuccess( mirrors[1] );

        selector.close();

        assertEquals( true, dMirrors.isBlacklisted( mirrors[0] ) );

        Thread.sleep( blacklistTTL * 2 );

        assertEquals( false, dMirrors.isBlacklisted( mirrors[0] ) );

    }

    public void testSetUrls()
        throws Exception
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        DownloadMirrorSelector selector = dMirrors.openSelector();

        selector.feedbackFailure( mirrors[0] );

        selector.feedbackSuccess( mirrors[1] );

        selector.close();

        // sanity check

        assertEquals( true, dMirrors.isBlacklisted( mirrors[0] ) );

        dMirrors.setMirrors( Arrays.asList( new Mirror( "3", "another-mirror" ), new Mirror( "4", "one-more-mirror" ),
                                            mirrors[0] ) );

        assertEquals( true, dMirrors.isBlacklisted( mirrors[0] ) );
    }

    public void testFailureThenSuccess()
    {
        Mirror[] mirrors = new Mirror[] { new Mirror( "1", "mirror1" ), new Mirror( "2", "mirror2" ) };

        DefaultDownloadMirrors dMirrors = newDefaultDownloadMirrors( mirrors );

        DownloadMirrorSelector selector = dMirrors.openSelector();

        selector.feedbackFailure( mirrors[0] );

        selector.feedbackSuccess( mirrors[0] );

        selector.close();

        // sanity check

        assertEquals( false, dMirrors.isBlacklisted( mirrors[0] ) );
    }
}
