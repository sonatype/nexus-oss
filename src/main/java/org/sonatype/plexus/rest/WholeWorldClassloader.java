/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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
            URL result = realm.getResource( name );

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
            InputStream result = realm.getResourceAsStream( name );

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
            Enumeration<URL> realmResources = realm.findResources( name );

            for ( ; realmResources.hasMoreElements(); )
            {
                result.add( realmResources.nextElement() );
            }
        }

        return Collections.enumeration( result );
    }

}
