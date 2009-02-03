/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.NexusAnalyzer;
import org.sonatype.nexus.index.context.NexusIndexWriter;
import org.sonatype.nexus.index.creator.IndexCreator;
import org.sonatype.nexus.index.updater.IndexDataReader.IndexDataReadResult;

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

    public Date fetchAndUpdateIndex( IndexUpdateRequest updateRequest )
        throws IOException
    {
        ResourceFetcher fetcher = updateRequest.getResourceFetcher();

        IndexingContext context = updateRequest.getIndexingContext();

        fetcher.connect( context.getId(), context.getIndexUpdateUrl() );

        try
        {
            Date contextTimestamp = context.getTimestamp();

            if ( contextTimestamp != null )
            {
                Properties properties = downloadIndexProperties( fetcher );

                Date updateTimestamp = getTimestamp( properties, IndexingContext.INDEX_TIMESTAMP );

                if ( updateTimestamp != null && !updateTimestamp.after( contextTimestamp ) )
                {
                    return null; // index is up to date
                }

                String chunkName = getUpdateChunkName( contextTimestamp, properties );

                if ( chunkName != null )
                {
                    loadIndexDirectory( updateRequest, true, chunkName );

                    return updateTimestamp;
                }
            }

            try
            {
                return loadIndexDirectory( updateRequest, false, IndexingContext.INDEX_FILE + ".gz" );
            }
            catch ( FileNotFoundException ex )
            {
                // try to look for legacy index transfer format
                return loadIndexDirectory( updateRequest, false, IndexingContext.INDEX_FILE + ".zip" );
            }
        }
        finally
        {
            fetcher.disconnect();
        }
    }

    /**
     * @deprecated use {@link #fetchAndUpdateIndex(IndexingContext, ResourceFetcher)}
     */
    public Date fetchAndUpdateIndex( IndexingContext context, TransferListener listener )
        throws IOException
    {
        return fetchAndUpdateIndex( context, listener, null );
    }

    /**
     * @deprecated use {@link #fetchAndUpdateIndex(IndexingContext, ResourceFetcher)}
     */
    public Date fetchAndUpdateIndex( final IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException
    {
        IndexUpdateRequest updateRequest = new IndexUpdateRequest( context );

        updateRequest.setResourceFetcher( new WagonFetcher( wagonManager, listener, null ) );

        return fetchAndUpdateIndex( updateRequest );
    }

    public Properties fetchIndexProperties( IndexingContext context, ResourceFetcher fetcher )
        throws IOException
    {
        fetcher.connect( context.getId(), context.getIndexUpdateUrl() );

        try
        {
            return downloadIndexProperties( fetcher );
        }
        finally
        {
            fetcher.disconnect();
        }
    }

    /**
     * @deprecated use {@link #fetchIndexProperties(IndexingContext, ResourceFetcher)}
     */
    public Properties fetchIndexProperties( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException
    {
        return fetchIndexProperties( context, new WagonFetcher( wagonManager, listener, proxyInfo ) );
    }

    private Date loadIndexDirectory( IndexUpdateRequest updateRequest, boolean merge, String remoteIndexFile )
        throws IOException
    {
        File indexArchive = File.createTempFile( remoteIndexFile, "" );

        File indexDir = new File( indexArchive.getAbsoluteFile().getParentFile(), indexArchive.getName() + ".dir" );

        indexDir.mkdirs();

        FSDirectory directory = FSDirectory.getDirectory( indexDir );

        BufferedInputStream is = null;

        try
        {
            updateRequest.getResourceFetcher().retrieve( remoteIndexFile, indexArchive );

            Date timestamp = null;

            if ( indexArchive.length() > 0 )
            {
                is = new BufferedInputStream( new FileInputStream( indexArchive ) );

                if ( remoteIndexFile.endsWith( ".gz" ) )
                {
                    timestamp = DefaultIndexUpdater.unpackIndexData( is, directory, //
                        updateRequest.getIndexingContext().getIndexCreators() );
                }
                else
                {
                    // legacy transfer format
                    timestamp = IndexUtils.unpackIndexArchive( is, directory, //
                        updateRequest.getIndexingContext().getIndexCreators() );
                }
            }

            if ( updateRequest.getDocumentFilter() != null )
            {
                IndexUtils.filterDirectory( directory, updateRequest.getDocumentFilter() );
            }

            if ( merge )
            {
                updateRequest.getIndexingContext().merge( directory );
            }
            else
            {
                updateRequest.getIndexingContext().replace( directory );
            }

            return timestamp;
        }
        finally
        {
            IOUtil.close( is );

            indexArchive.delete();

            if ( directory != null )
            {
                directory.close();
            }

            try
            {
                FileUtils.deleteDirectory( indexDir );
            }
            catch ( IOException ex )
            {
                // ignore
            }
        }
    }

    private Properties downloadIndexProperties( ResourceFetcher fetcher )
        throws IOException
    {
        String remoteIndexProperties = IndexingContext.INDEX_FILE + ".properties";

        File indexProperties = File.createTempFile( remoteIndexProperties, "" );

        FileInputStream fis = null;

        try
        {
            fetcher.retrieve( remoteIndexProperties, indexProperties );

            Properties properties = new Properties();

            fis = new FileInputStream( indexProperties );

            properties.load( fis );

            return properties;
        }
        finally
        {
            IOUtil.close( fis );
            indexProperties.delete();
        }
    }

    /**
     * Returns chunk name for downloading that contain all required updates since given <code>contextTimestamp</code> or
     * null.
     */
    public String getUpdateChunkName( Date contextTimestamp, Properties properties )
    {
        Date updateTimestamp = getTimestamp( properties, IndexingContext.INDEX_TIMESTAMP );

        if ( updateTimestamp == null || updateTimestamp.before( contextTimestamp ) )
        {
            return null; // no updates
        }

        int n = 0;

        while ( true )
        {
            Date chunkTimestamp = getTimestamp( properties, IndexingContext.INDEX_CHUNK_PREFIX + n );

            if ( chunkTimestamp == null )
            {
                break;
            }

            if ( contextTimestamp.after( chunkTimestamp ) )
            {
                SimpleDateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_DAY_FORMAT );
                df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
                return IndexingContext.INDEX_FILE + "." + df.format( chunkTimestamp ) + ".gz";
            }

            n++;
        }

        return null; // no update chunk available
    }

    public Date getTimestamp( Properties properties, String key )
    {
        String indexTimestamp = properties.getProperty( key );

        if ( indexTimestamp != null )
        {
            try
            {
                SimpleDateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT );
                df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
                return df.parse( indexTimestamp );
            }
            catch ( ParseException ex )
            {
            }
        }
        return null;
    }

    /**
     * Unpack index data using specified Lucene Index writer
     * 
     * @param is an input stream to unpack index data from
     * @param w a writer to save index data
     * @param ics a collection of index creators for updating unpacked documents.
     */
    public static Date unpackIndexData( InputStream is, Directory d, Collection<? extends IndexCreator> ics )
        throws IOException
    {
        NexusIndexWriter w = new NexusIndexWriter( d, new NexusAnalyzer(), true );
    
        try
        {
            IndexDataReader dr = new IndexDataReader( is );
            
            IndexDataReadResult result = dr.readIndex( w, ics );
            
            return result.getTimestamp();
        }
        finally
        {
            IndexUtils.close( w );
        }
    }

    /**
     * A ResourceFetcher implementation based on Wagon
     */
    public static class WagonFetcher
        implements ResourceFetcher
    {
        private final WagonManager wagonManager;

        private final TransferListener listener;

        private final ProxyInfo proxyInfo;

        private Wagon wagon = null;

        public WagonFetcher( WagonManager wagonManager, TransferListener listener, ProxyInfo proxyInfo )
        {
            this.wagonManager = wagonManager;
            this.listener = listener;
            this.proxyInfo = proxyInfo;
        }

        public void connect( String id, String url )
            throws IOException
        {
            Repository repository = new Repository( id, url );

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
            }
            catch ( AuthenticationException ex )
            {
                String msg = "Authentication exception connecting to " + repository;
                logError( msg, ex );
                throw new IOException( msg );
            }
            catch ( WagonException ex )
            {
                String msg = "Wagon exception connecting to " + repository;
                logError( msg, ex );
                throw new IOException( msg );
            }
        }

        public void disconnect()
        {
            if ( wagon != null )
            {
                try
                {
                    wagon.disconnect();
                }
                catch ( ConnectionException ex )
                {
                    logError( "Failed to close connection", ex );
                }
            }
        }

        public void retrieve( String name, File targetFile )
            throws IOException,
                FileNotFoundException
        {
            try
            {
                wagon.get( name, targetFile );
            }
            catch ( AuthorizationException e )
            {
                String msg = "Authorization exception retrieving " + name;
                logError( msg, e );
                throw new IOException( msg );
            }
            catch ( ResourceDoesNotExistException e )
            {
                String msg = "Resource " + name + " does not exist";
                logError( msg, e );
                throw new FileNotFoundException( msg );
            }
            catch ( WagonException e )
            {
                String msg = "Transfer for " + name + " failed";
                logError( msg, e );
                throw new IOException( msg + "; " + e.getMessage() );
            }
        }

        private void logError( String msg, Exception ex )
        {
            if ( listener != null )
            {
                listener.debug( msg + "; " + ex.getMessage() );
            }
        }

    }

}
