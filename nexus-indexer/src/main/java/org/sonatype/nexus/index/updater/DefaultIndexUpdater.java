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
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.LockObtainFailedException;
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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.context.DocumentFilter;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.NexusAnalyzer;
import org.sonatype.nexus.index.context.NexusIndexWriter;
import org.sonatype.nexus.index.incremental.IncrementalHandler;
import org.sonatype.nexus.index.updater.IndexDataReader.IndexDataReadResult;

/**
 * A default index updater implementation
 * 
 * @author Jason van Zyl
 * @author Eugene Kuleshov
 */
@Component(role = IndexUpdater.class)
public class DefaultIndexUpdater
    extends AbstractLogEnabled
    implements IndexUpdater
{
    @Requirement( role = IncrementalHandler.class )
    IncrementalHandler incrementalHandler;
    
    @Requirement
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
                Properties localProperties = loadLocallyStoredRemoteProperties( context );
                Properties properties = downloadIndexProperties( context, fetcher );

                Date updateTimestamp = getTimestamp( properties, IndexingContext.INDEX_TIMESTAMP );

                if ( updateTimestamp != null && !updateTimestamp.after( contextTimestamp ) )
                {
                    return null; // index is up to date
                }
                
                List<String> filenames = incrementalHandler.loadRemoteIncrementalUpdates( updateRequest, localProperties, properties );
                
                // if we have some incremental files, merge them in
                if ( filenames != null )
                {
                    for ( String filename : filenames )
                    {
                        loadIndexDirectory( updateRequest, true, filename );
                    }
                    
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
            return downloadIndexProperties( context, fetcher );
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
                        updateRequest.getIndexingContext() );
                }
                else
                {
                    // legacy transfer format
                    timestamp = unpackIndexArchive( is, directory, //
                        updateRequest.getIndexingContext() );
                }
            }

            if ( updateRequest.getDocumentFilter() != null )
            {
                filterDirectory( directory, updateRequest.getDocumentFilter() );
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

    /**
     * Unpack legacy index archive into a specified Lucene <code>Directory</code>
     * 
     * @param is a <code>ZipInputStream</code> with index data
     * @param directory Lucene <code>Directory</code> to unpack index data to
     * @return {@link Date} of the index update or null if it can't be read
     */
    public static Date unpackIndexArchive( InputStream is, Directory directory, IndexingContext context )
        throws IOException
    {
        File indexArchive = File.createTempFile( "nexus-index", "" );

        File indexDir = new File( indexArchive.getAbsoluteFile().getParentFile(), indexArchive.getName() + ".dir" );

        indexDir.mkdirs();

        FSDirectory fdir = FSDirectory.getDirectory( indexDir );

        try
        {
            unpackDirectory( fdir, is );
            copyUpdatedDocuments( fdir, directory, context );

            Date timestamp = IndexUtils.getTimestamp( fdir );
            IndexUtils.updateTimestamp( directory, timestamp );
            return timestamp;
        }
        finally
        {
            IndexUtils.close( fdir );
            indexArchive.delete();
            IndexUtils.delete( indexDir );
        }
    }

    private static void unpackDirectory( Directory directory, InputStream is )
        throws IOException
    {
        byte[] buf = new byte[4096];

        ZipEntry entry;

        ZipInputStream zis = null;

        try
        {
            zis = new ZipInputStream( is );

            while ( ( entry = zis.getNextEntry() ) != null )
            {
                if ( entry.isDirectory() || entry.getName().indexOf( '/' ) > -1 )
                {
                    continue;
                }

                IndexOutput io = directory.createOutput( entry.getName() );
                try
                {
                    int n = 0;

                    while ( ( n = zis.read( buf ) ) != -1 )
                    {
                        io.writeBytes( buf, n );
                    }
                }
                finally
                {
                    IndexUtils.close( io );
                }
            }
        }
        finally
        {
            IndexUtils.close( zis );
        }
    }

    private static void copyUpdatedDocuments( Directory sourcedir, Directory targetdir, IndexingContext context )
        throws CorruptIndexException,
            LockObtainFailedException,
            IOException
    {
        IndexWriter w = null;
        IndexReader r = null;
        try
        {
            r = IndexReader.open( sourcedir );
            w = new IndexWriter( targetdir, false, new NexusAnalyzer(), true );

            for ( int i = 0; i < r.maxDoc(); i++ )
            {
                if ( !r.isDeleted( i ) )
                {
                    w.addDocument( IndexUtils.updateDocument( r.document( i ), context ) );
                }
            }

            w.optimize();
            w.flush();
        }
        finally
        {
            IndexUtils.close( w );
            IndexUtils.close( r );
        }
    }
    
    private static void filterDirectory( Directory directory, DocumentFilter filter )
        throws IOException
    {
        IndexReader r = null;
        try
        {
            r = IndexReader.open( directory );
    
            int numDocs = r.numDocs();
    
            for ( int i = 0; i < numDocs; i++ )
            {
                if ( r.isDeleted( i ) )
                {
                    continue;
                }
    
                Document d = r.document( i );
    
                if ( !filter.accept( d ) )
                {
                    r.deleteDocument( i );
                }
            }
        }
        finally
        {
            IndexUtils.close( r );
        }
    
        IndexWriter w = null;
        try
        {
            // analyzer is unimportant, since we are not adding/searching to/on index, only reading/deleting
            w = new IndexWriter( directory, new NexusAnalyzer() );
    
            w.optimize();
    
            w.flush();
        }
        finally
        {
            IndexUtils.close( w );
        }
    }
    
    private Properties loadLocallyStoredRemoteProperties( IndexingContext context )
    {        
        String remoteIndexProperties = IndexingContext.INDEX_FILE + ".properties";

        File indexProperties = new File( context.getIndexDirectoryFile(), remoteIndexProperties );

        FileInputStream fis = null;

        try
        {
            Properties properties = new Properties();

            fis = new FileInputStream( indexProperties );

            properties.load( fis );

            return properties;
        }
        catch ( IOException e )
        {
            getLogger().debug( "Unable to read remote properties stored locally", e );
        }
        finally
        {
            IOUtil.close( fis );
        }
        
        return null;
    }
    
    private Properties downloadIndexProperties( IndexingContext context, ResourceFetcher fetcher )
        throws IOException
    {
        String remoteIndexProperties = IndexingContext.INDEX_FILE + ".properties";

        File indexProperties = new File( context.getIndexDirectoryFile(), remoteIndexProperties );

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
        }
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
    public static Date unpackIndexData( InputStream is, Directory d, IndexingContext context )
        throws IOException
    {
        NexusIndexWriter w = new NexusIndexWriter( d, new NexusAnalyzer(), true );
    
        try
        {
            IndexDataReader dr = new IndexDataReader( is );
            
            IndexDataReadResult result = dr.readIndex( w, context );
            
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
