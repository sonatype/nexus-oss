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
package org.sonatype.nexus.testng;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.launcher.Launcher;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.appcontext.AppContext;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.IObjectFactory;

public class PlexusObjectFactory
    implements IObjectFactory
{
    private PlexusContainer plexusContainer;

    private static final PlexusContainer container;


    static
    {
        try
        {
            container = setupContainer();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static PlexusContainer getContainer()
    {
        return container;
    }
    
    private static synchronized PlexusContainer setupContainer()
        throws Exception
    {
        final File f = new File( "target/plexus-home" );

        if ( !f.isDirectory() )
        {
            f.mkdirs();
        }

        File bundleRoot = new File( TestProperties.getAll().get( "nexus.base.dir" ) );
        System.setProperty( "basedir", bundleRoot.getAbsolutePath() );
        System.setProperty( "bundleBasedir", bundleRoot.getAbsolutePath() );
        System.setProperty( "plexus.configuration", new File(bundleRoot, "nexus/WEB-INF/plexus.xml").getAbsolutePath() );
        
        System.setProperty( "plexus.appbooter.customizers", "org.sonatype.nexus.NexusBooterCustomizer,"
            + SeleniumAppBooterCustomizer.class.getName() );

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
            protected void customizeContext( AppContext context )
            {
                super.customizeContext( context );

                context.put( "plexus.app.booter", this );
            }
        };
        plexusAppBooter.setWorld( launcher.getWorld() );

        plexusAppBooter.startContainer();

        PlexusContainer c = plexusAppBooter.getContainer();
        return c;
    }

    private static final long serialVersionUID = -45456541236971L;

    @Override
    public Object newInstance( Constructor constructor, Object... params )
    {
        String role = constructor.getDeclaringClass().getName();
        String hint = null;
        if ( params != null && params.length == 1 && params[0] instanceof String )
        {
            hint = (String) params[0];
        }

        try
        {
            if ( hint != null )
            {
                return container.lookup( role, hint );
            }
            else
            {
                return container.lookup( role );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( e );
        }

    }

}
