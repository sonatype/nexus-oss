package org.sonatype.plexus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

public class WholeWorldClassloader
    extends ClassLoader
{
    private final ClassWorld classWorld;

    public WholeWorldClassloader( ClassWorld classWorld )
    {
        this.classWorld = classWorld;
    }

    protected ClassWorld getClassWorld()
    {
        return classWorld;
    }

    @Override
    public Class<?> loadClass( String name )
        throws ClassNotFoundException
    {
        return loadClass( name, false );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    protected Class<?> loadClass( String name, boolean resolve )
        throws ClassNotFoundException
    {
        for ( ClassRealm realm : (Collection<ClassRealm>) getClassWorld().getRealms() )
        {
            try
            {
                return realm.loadClass( name );
            }
            catch ( ClassNotFoundException e )
            {
                // ignore it
            }
        }

        throw new ClassNotFoundException( name );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public URL getResource( String name )
    {
        for ( ClassRealm realm : (Collection<ClassRealm>) getClassWorld().getRealms() )
        {
            URL result = realm.getRealmResource( name );

            if ( result != null )
            {
                return result;
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public InputStream getResourceAsStream( String name )
    {
        for ( ClassRealm realm : (Collection<ClassRealm>) getClassWorld().getRealms() )
        {
            InputStream result = realm.getRealmResourceAsStream( name );

            if ( result != null )
            {
                return result;
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Enumeration<URL> findResources( String name )
        throws IOException
    {
        ArrayList<URL> result = new ArrayList<URL>();

        for ( ClassRealm realm : (Collection<ClassRealm>) getClassWorld().getRealms() )
        {
            Enumeration<URL> realmResources = realm.findRealmResources( name );

            for ( ; realmResources.hasMoreElements(); )
            {
                result.add( realmResources.nextElement() );
            }
        }

        return Collections.enumeration( result );
    }

}
