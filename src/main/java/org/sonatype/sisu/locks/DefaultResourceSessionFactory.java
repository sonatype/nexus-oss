/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.sisu.locks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
final class DefaultResourceSessionFactory
    implements ResourceSessionFactory
{
    ResourceSession session = new DefaultResourceSession();

    public ResourceSession newSession()
    {
        return session;
    }

    public void shutdown()
    {
        // no cleanup required
    }
}

final class DefaultResourceSession
    implements ResourceSession
{
    public boolean exists( final URI resource )
    {
        return new File( resource ).exists();
    }

    public boolean isFolder( final URI resource )
    {
        return new File( resource ).isDirectory();
    }

    public InputStream read( final URI resource )
    {
        try
        {
            return new FileInputStream( new File( resource ) );
        }
        catch ( final FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }

    public String[] list( final URI resource )
    {
        return new File( resource ).list();
    }

    public OutputStream write( final URI resource )
    {
        try
        {
            return new FileOutputStream( new File( resource ) );
        }
        catch ( final FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }

    public void createFile( final URI resource )
    {
        try
        {
            new File( resource ).createNewFile();
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public void createFolder( final URI resource )
    {
        new File( resource ).mkdirs();
    }

    public void copy( final URI from, final URI to )
    {
        final InputStream is = read( from );
        final OutputStream os = write( to );

        try
        {
            final byte[] buf = new byte[1024];
            for ( int len; ( len = is.read( buf ) ) > 0; )
            {
                os.write( buf, 0, len );
            }
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            try
            {
                os.close();
            }
            catch ( final IOException e )
            {
                // ignore
            }
            try
            {
                os.close();
            }
            catch ( final IOException e )
            {
                // ignore
            }
        }
    }

    public void move( final URI from, final URI to )
    {
        new File( from ).renameTo( new File( to ) );
    }

    public void delete( final URI resource )
    {
        new File( resource ).delete();
    }

    public void commit()
    {
    }
}