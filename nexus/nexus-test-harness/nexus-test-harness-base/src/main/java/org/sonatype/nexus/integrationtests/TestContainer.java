/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;

public class TestContainer
{

    private static TestContainer SELF = null;

    private TestContext testContext = new TestContext();

    private PlexusContainer container;

    private TestContainer()
    {
        this.setupContainer();
    }

    public static TestContainer getInstance()
    {
        synchronized ( TestContainer.class )
        {
            if ( SELF == null )
            {
                SELF = new TestContainer();
            }
        }
        return SELF;
    }

    private void setupContainer()
    {

        File f = new File( "target/plexus-home" );

        if ( !f.isDirectory() )
        {
            f.mkdirs();
        }

        Map<String, Object> context = new HashMap<String, Object>();
        context.put( "plexus.home", f.getAbsolutePath() );

        // ----------------------------------------------------------------------------
        // Configuration
        // ----------------------------------------------------------------------------

        ContainerConfiguration containerConfiguration = new DefaultContainerConfiguration()
            .setName( "test" ).setContext( context );

        try
        {
            this.container = new DefaultPlexusContainer( containerConfiguration );
        }
        catch ( PlexusContainerException e )
        {
            e.printStackTrace();
            fail( "Failed to create plexus container." );
        }
    }

    public Object lookup( String role )
        throws Exception
    {
        return container.lookup( role );
    }

    public Object lookup( Class role )
        throws Exception
    {
        return container.lookup( role );
    }

    public Object lookup( String role, String id )
        throws Exception
    {
        return container.lookup( role, id );
    }

    public Object lookup( Class role, String id )
        throws Exception
    {
        return container.lookup( role, id );
    }

    public TestContext getTestContext()
    {
        return testContext;
    }

    public PlexusContainer getContainer()
    {
        return container;
    }

}
