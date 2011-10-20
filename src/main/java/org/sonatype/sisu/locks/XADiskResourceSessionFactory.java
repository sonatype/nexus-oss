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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.guice.bean.reflect.Logs;
import org.xadisk.additional.XAFileInputStreamWrapper;
import org.xadisk.additional.XAFileOutputStreamWrapper;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.bridge.proxies.interfaces.XAFileSystemProxy;
import org.xadisk.filesystem.standalone.StandaloneFileSystemConfiguration;

@Named( "xadisk" )
@Singleton
final class XADiskResourceSessionFactory
    implements ResourceSessionFactory
{
    private final XAFileSystem fs;

    XADiskResourceSessionFactory( @Named( "xadisk.home" ) final String home, @Named( "xadisk.id" ) final String id )
    {
        final StandaloneFileSystemConfiguration config = new StandaloneFileSystemConfiguration( home, id );
        XAFileSystem fileSystem = XAFileSystemProxy.getNativeXAFileSystemReference( config.getInstanceId() );
        if ( null == fileSystem )
        {
            fileSystem = XAFileSystemProxy.bootNativeXAFileSystem( config );
        }
        this.fs = fileSystem;
    }

    public ResourceSession newSession()
    {
        return new XADiskResourceSession( fs.createSessionForLocalTransaction() );
    }

    public void shutdown()
    {
        try
        {
            fs.shutdown();
        }
        catch ( IOException e )
        {
            Logs.warn( "Problem shutting down XADisk", null, e );
        }
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

    public boolean exists( final URI resource )
    {
        try
        {
            return session.fileExists( new File( resource ) );
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