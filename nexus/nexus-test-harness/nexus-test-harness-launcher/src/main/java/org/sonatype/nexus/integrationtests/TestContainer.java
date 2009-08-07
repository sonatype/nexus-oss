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
package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.FileInputStream;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.launcher.Launcher;
import org.codehaus.plexus.context.Context;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.nexus.test.utils.TestProperties;

public class TestContainer
{

    private static TestContainer SELF = null;

    private TestContext testContext = new TestContext();

    private PlexusContainer container;

    private PlexusAppBooter plexusAppBooter;

    private TestContainer()
    {
        try
        {
            this.setupEnvironment();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
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

    private void setupEnvironment()
        throws Exception
    {

        final File f = new File( "target/plexus-home" );

        if ( !f.isDirectory() )
        {
            f.mkdirs();
        }

        File bundleRoot = new File( TestProperties.getAll().get( "nexus.base.dir" ) );
        System.setProperty( "basedir", bundleRoot.getAbsolutePath() );

        System.setProperty( "plexus.appbooter.customizers", "org.sonatype.nexus.NexusBooterCustomizer,"
            + ITAppBooterCustomizer.class.getName() );

        File classworldsConf = new File( bundleRoot, "conf/classworlds.conf" );

        if ( !classworldsConf.isFile() )
        {
            throw new IllegalStateException( "The bundle classworlds.conf file is not found (\""
                + classworldsConf.getAbsolutePath() + "\")!" );
        }

        System.setProperty( "classworlds.conf", classworldsConf.getAbsolutePath() );

        // this is non trivial here, since we are running Nexus in _same_ JVM as tests
        // and the PlexusAppBooterJSWListener (actually theused WrapperManager in it) enforces then Nexus may be
        // started only once in same JVM!
        // So, we are _overrriding_ the in-bundle plexus app booter with the simplest one
        // since we dont need all the bells-and-whistles in Service and JSW
        // but we are still _reusing_ the whole bundle environment by tricking Classworlds Launcher

        // Launcher trick -- begin
        Launcher launcher = new Launcher();
        launcher.setSystemClassLoader( Thread.currentThread().getContextClassLoader() );
        launcher.configure( new FileInputStream( classworldsConf ) ); // launcher closes stream upon configuration
        // Launcher trick -- end

        // set the preconfigured world
        final PlexusAppBooter plexusAppBooter = new PlexusAppBooter()
        {
            @Override
            protected void customizeContext( Context context )
            {
                super.customizeContext( context );

                context.put( "plexus.app.booter", this );
            }
        };
        plexusAppBooter.setWorld( launcher.getWorld() );

        plexusAppBooter.startContainer();
        this.plexusAppBooter = plexusAppBooter;

        PlexusContainer container = plexusAppBooter.getContainer();
        this.container = container;
    }

    public Object lookup( String role )
        throws Exception
    {
        return container.lookup( role );
    }

    public <E> E lookup( Class<E> role )
        throws Exception
    {
        return container.lookup( role );
    }

    public Object lookup( String role, String id )
        throws Exception
    {
        return container.lookup( role, id );
    }

    public <E> E lookup( Class<E> role, String id )
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

    public PlexusAppBooter getPlexusAppBooter()
    {
        return plexusAppBooter;
    }

}
