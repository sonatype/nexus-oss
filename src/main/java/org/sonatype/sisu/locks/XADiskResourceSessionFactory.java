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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xadisk.additional.XAFileInputStreamWrapper;
import org.xadisk.additional.XAFileOutputStreamWrapper;
import org.xadisk.bridge.proxies.interfaces.Session;

@Named( "xadisk" )
@Singleton
final class XADiskResourceSessionFactory
    implements ResourceSessionFactory
{
    XADiskResourceSessionFactory()
    {
    }

    public ResourceSession newSession()
    {
        return null;
    }

    public void shutdown()
    {
    }
}

final class XADiskResourceSession
    implements ResourceSession
{
    private final Session session;

    XADiskResourceSession( final Session session )
    {
        this.session = session;
    }

    public boolean isFile( final URI resource )
    {
        final File f = new File( resource );
        try
        {
            return session.fileExists( f ) && !session.fileExistsAndIsDirectory( f );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public boolean isFolder( final URI resource )
    {
        try
        {
            return session.fileExistsAndIsDirectory( new File( resource ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public InputStream read( final URI resource )
    {
        try
        {
            return new XAFileInputStreamWrapper( session.createXAFileInputStream( new File( resource ) ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public String[] list( final URI resource )
    {
        try
        {
            return session.listFiles( new File( resource ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public OutputStream write( final URI resource )
    {
        try
        {
            return new XAFileOutputStreamWrapper( session.createXAFileOutputStream( new File( resource ), true ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void createFile( final URI resource )
    {
        try
        {
            session.createFile( new File( resource ), false );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void createFolder( final URI resource )
    {
        try
        {
            session.createFile( new File( resource ), true );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void copy( final URI from, final URI to )
    {
        try
        {
            session.copyFile( new File( from ), new File( to ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void move( final URI from, final URI to )
    {
        try
        {
            session.moveFile( new File( from ), new File( to ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void delete( final URI resource )
    {
        try
        {
            session.deleteFile( new File( resource ) );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void commit()
    {
        try
        {
            session.commit();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}