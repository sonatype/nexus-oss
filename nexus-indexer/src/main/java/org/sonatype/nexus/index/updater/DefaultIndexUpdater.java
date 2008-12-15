/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.updater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.IndexChunker;
import org.sonatype.nexus.index.packer.IndexPacker;

/**
 * @author Jason van Zyl
 * @author Eugene Kuleshov
 * @plexus.component
 */
public class DefaultIndexUpdater
    extends AbstractLogEnabled
    implements IndexUpdater
{
    /** @plexus.requirement */
    private WagonManager wagonManager;

    /** @plexus.requirement role="org.sonatype.nexus.index.packer.IndexChunker" */
    private Map<String, IndexChunker> indexChunkers;

    public Date fetchAndUpdateIndex( IndexingContext context, TransferListener listener )
        throws IOException
    {
        return fetchAndUpdateIndex( context, listener, null );
    }

    public Date fetchAndUpdateIndex( final IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException
    {
        return run( context, listener, proxyInfo, new WagonTask<Date>()
        {
            public Date invoke( Wagon wagon )
                throws IOException
            {
                Date contextTimestamp = context.getTimestamp();

                if ( contextTimestamp != null )
                {
                    Properties properties = downloadIndexProperties( wagon );

                    Date updateTimestamp = getTimestamp( properties, IndexingContext.INDEX_TIMESTAMP );

                    if ( updateTimestamp != null && contextTimestamp.after( updateTimestamp ) )
                    {
                        return null; // index is up to date
                    }

                    Date chunkTimestamp = getNextUpdateChunkTimestamp( contextTimestamp, null, properties );

                    while ( chunkTimestamp != null )
                    {
                        String chunkName = getUpdateChunkName( chunkTimestamp, properties );

                        downloadIndexChunk( context, wagon, chunkName );

                        chunkTimestamp = getNextUpdateChunkTimestamp( contextTimestamp, chunkTimestamp, properties );
                    }

                    return updateTimestamp;
                }

                return downloadFullIndex( context, wagon );
            }
        } );
    }

    public Properties fetchIndexProperties( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException
    {
        return run( context, listener, proxyInfo, new WagonTask<Properties>()
        {
            public Properties invoke( Wagon wagon )
                throws IOException
            {
                return downloadIndexProperties( wagon );
            }
        } );
    }

    private <T> T run( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo, WagonTask<T> task )
        throws IOException
    {
        Repository repository = new Repository( context.getRepositoryId(), context.getIndexUpdateUrl() );

        Wagon wagon = null;

        try
        {
            wagon = wagonManager.getWagon( repository );

            if ( listener != null )
            {
                wagon.addTransferListener( listener );
            }

            // when working in the context of Maven, the WagonManager is already
            // populated with proxy information from the Maven environment

            if ( proxyInfo != null )
            {
                wagon.connect( repository, proxyInfo );
            }
            else
            {
                wagon.connect( repository );
            }

            return task.invoke( wagon );
        }
        catch ( AuthenticationException e )
        {
            throw new IOException( "Authentication exception connecting to " + repository );
        }
        catch ( WagonException e )
        {
            throw new IOException( "Wagon exception connecting to " + repository );
        }
        finally
        {
            if ( wagon != null )
            {
                try
                {
                    wagon.disconnect();
                }
                catch ( ConnectionException ex )
                {
                    if ( listener != null )
                    {
                        listener.debug( "Failed to close connection; " + ex.getMessage() );
                    }
                }
            }
        }
    }

    Date downloadFullIndex( IndexingContext context, Wagon wagon )
        throws IOException
    {
        RAMDirectory directory = new RAMDirectory();

        Date updateTimestamp = loadIndexDirectory( IndexingContext.INDEX_FILE + ".zip", wagon, directory );

        context.replace( directory );

        return updateTimestamp;
    }

    Date loadIndexDirectory( String remoteIndexFile, Wagon wagon, Directory directory )
        throws IOException
    {
        File indexArchive = File.createTempFile( "nexus", "index.zip" );

        BufferedInputStream is = null;

        try
        {
            downloadResource( wagon, remoteIndexFile, indexArchive );

            is = new BufferedInputStream( new FileInputStream( indexArchive ) );

            return IndexUtils.unpackIndexArchive( is, directory );
        }
        finally
        {
            IOUtil.close( is );
            indexArchive.delete();
        }
    }

    Properties downloadIndexProperties( Wagon wagon )
        throws IOException,
            FileNotFoundException
    {
        File indexProperties = File.createTempFile( "nexus", "index.properties" );

        try
        {
            String remoteIndexProperties = IndexingContext.INDEX_FILE + ".properties";

            downloadResource( wagon, remoteIndexProperties, indexProperties );

            Properties properties = new Properties();

            FileInputStream fis = new FileInputStream( indexProperties );
            try
            {
                properties.load( fis );
            }
            finally
            {
                fis.close();
            }

            return properties;
        }
        finally
        {
            indexProperties.delete();
        }
    }

    void downloadResource( Wagon wagon, String name, File targetFile )
        throws IOException
    {
        try
        {
            wagon.get( name, targetFile );
        }
        catch ( AuthorizationException e )
        {
            throw new IOException( "Authorization exception retrieving " + name );
        }
        catch ( ResourceDoesNotExistException e )
        {
            throw new IOException( "Resource " + name + " does not exist" );
        }
        catch ( WagonException e )
        {
            throw new IOException( "Transfer for " + name + " failed; " + e.getMessage() );
        }
    }

    /**
     * Returns chunk name for downloading or null
     */
    public Date getNextUpdateChunkTimestamp( Date contextTimestamp, Date lastChunkTimestamp, Properties properties )
    {
        Date updateTimestamp = getTimestamp( properties, IndexingContext.INDEX_TIMESTAMP );

        if ( updateTimestamp == null || updateTimestamp.before( contextTimestamp ) )
        {
            return null; // no updates
        }

        String chunkResolution = properties.getProperty( IndexingContext.INDEX_CHUNKS_RESOLUTION );

        if ( chunkResolution == null )
        {
            return null; // not chunked
        }

        Date lookingFor = lastChunkTimestamp == null ? contextTimestamp : lastChunkTimestamp;

        int n = 0;

        while ( true )
        {
            Date chunkTimestamp = getTimestamp( properties, IndexingContext.INDEX_PROPERTY_PREFIX + chunkResolution
                + "-" + n );

            if ( chunkTimestamp == null )
            {
                break;
            }

            if ( lookingFor.before( chunkTimestamp ) )
            {
                return chunkTimestamp;
            }

            n++;
        }

        return null; // no update chunk available
    }

    public String getUpdateChunkName( Date chunkTimestamp, Properties properties )
    {
        String chunkResolution = properties.getProperty( IndexingContext.INDEX_CHUNKS_RESOLUTION );

        if ( chunkResolution == null )
        {
            return null; // not chunked
        }

        IndexChunker chunker = indexChunkers.get( chunkResolution );

        if ( chunker == null )
        {
            throw new IllegalArgumentException( "Unknown chunk resolution: " + chunkResolution );
        }

        return IndexingContext.INDEX_FILE + "." + chunker.getChunkId( chunkTimestamp ) + ".zip";
    }

    public Date getTimestamp( Properties properties, String key )
    {
        String indexTimestamp = properties.getProperty( key );

        if ( indexTimestamp != null )
        {
            try
            {
                return new SimpleDateFormat( IndexPacker.INDEX_TIME_FORMAT ).parse( indexTimestamp );
            }
            catch ( ParseException ex )
            {
            }
        }
        return null;
    }

    private void downloadIndexChunk( IndexingContext context, Wagon wagon, String name )
        throws IOException
    {
        RAMDirectory directory = new RAMDirectory();

        loadIndexDirectory( name, wagon, directory );

        context.merge( directory );
    }

    /**
     * A task that requires a Wagon instance
     */
    interface WagonTask<T>
    {
        T invoke( Wagon wagon )
            throws IOException;
    }

}
