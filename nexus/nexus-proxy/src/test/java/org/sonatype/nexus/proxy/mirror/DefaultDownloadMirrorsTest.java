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

import org.sonatype.nexus.proxy.AbstractNexusTestCase;

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

        assertEquals( 0, mirrors.getUrls().size() );
        assertEquals( 0, selector.getUrls().size() );

        selector.close();
    }

    private DefaultDownloadMirrors newDefaultDownloadMirrors( String[] urls )
    {
        DefaultDownloadMirrors mirrors = new DefaultDownloadMirrors();

        if ( urls != null )
        {
            mirrors.setUrls( Arrays.asList( urls ) );
        }

        return mirrors;
    }

    public void testSimpleMirrorSelection()
    {
        String[] urls = new String[] { "mirror1", "mirror2" };

        int maxMirrors = 2;

        DefaultDownloadMirrors mirrors = newDefaultDownloadMirrors( urls );
        
        mirrors.setMaxMirrors( maxMirrors );

        assertEquals( urls.length, mirrors.getUrls().size() );

        DownloadMirrorSelector selector = mirrors.openSelector();

        List<String> _urls = selector.getUrls();

        assertEquals( maxMirrors, _urls.size() );
        assertEquals( urls[0], _urls.get( 0 ) );
        assertEquals( urls[1], _urls.get( 1 ) );

        selector.close();
    }

    public void testFailure()
    {
        String[] urls = new String[] { "mirror1", "mirror2" };

        DefaultDownloadMirrors mirrors = newDefaultDownloadMirrors( urls );

        DownloadMirrorSelector selector = mirrors.openSelector();

        selector.feedbackFailure( urls[0] );

        selector.feedbackSuccess( urls[1] );

        // feedback has not been applied yet
        assertEquals( urls[0], mirrors.openSelector().getUrls().get( 0 ) );

        selector.close();
        assertEquals( urls[1], mirrors.openSelector().getUrls().get( 0 ) );
    }

    public void testBlacklistDecay()
        throws Exception
    {
        String[] urls = new String[] { "mirror1", "mirror2" };

        long blacklistTTL = 100L; // milliseconds

        DefaultDownloadMirrors mirrors = newDefaultDownloadMirrors( urls );

        mirrors.setBlacklistExpiration( blacklistTTL );

        DownloadMirrorSelector selector = mirrors.openSelector();

        selector.feedbackFailure( urls[0] );

        selector.feedbackSuccess( urls[1] );

        selector.close();

        assertEquals( true, mirrors.isBlacklisted( urls[0] ) );

        Thread.sleep( blacklistTTL * 2 );

        assertEquals( false, mirrors.isBlacklisted( urls[0] ) );

    }

    public void testSetUrls()
        throws Exception
    {
        String[] urls = new String[] { "mirror1", "mirror2" };

        DefaultDownloadMirrors mirrors = newDefaultDownloadMirrors( urls );

        DownloadMirrorSelector selector = mirrors.openSelector();

        selector.feedbackFailure( urls[0] );

        selector.feedbackSuccess( urls[1] );

        selector.close();

        // sanity check

        assertEquals( true, mirrors.isBlacklisted( urls[0] ) );

        mirrors.setUrls( Arrays.asList( "another-mirror", "one-more-mirror", "mirror1" ) );

        assertEquals( true, mirrors.isBlacklisted( urls[0] ) );
    }

    public void testFailureThenSuccess()
    {
        String[] urls = new String[] { "mirror1", "mirror2" };

        DefaultDownloadMirrors mirrors = newDefaultDownloadMirrors( urls );

        DownloadMirrorSelector selector = mirrors.openSelector();

        selector.feedbackFailure( urls[0] );

        selector.feedbackSuccess( urls[0] );

        selector.close();

        // sanity check

        assertEquals( false, mirrors.isBlacklisted( urls[0] ) );
    }
}
