/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.index.updater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

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
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;

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

    public Date fetchAndUpdateIndex( IndexingContext context, TransferListener listener )
        throws IOException
    {
        return fetchAndUpdateIndex( context, listener, null );
    }

    public Date fetchAndUpdateIndex( final IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException
    {
        return (Date) run( context, listener, proxyInfo, new WagonTask()
        {
            public Object invoke( Wagon wagon )
                throws FileNotFoundException,
                    IOException
            {
                Date contextTimestamp = context.getTimestamp();

                if ( contextTimestamp != null )
                {
                    Date updateTimestamp = getIndexTimestamp( context, wagon );

                    if ( updateTimestamp.before( contextTimestamp ) || updateTimestamp.equals( contextTimestamp ) )
                    {
                        return null;
                    }
                }

                File indexArchive = File.createTempFile( "nexus", "index.zip" );

                try
                {
                    String remoteIndexFile = IndexingContext.INDEX_FILE + ".zip";

                    downloadResource( wagon, remoteIndexFile, indexArchive );

                    RAMDirectory directory = new RAMDirectory();

                    BufferedInputStream is = new BufferedInputStream( new FileInputStream( indexArchive ), 4096 );

                    Date updateTimestamp = IndexUtils.unpackIndexArchive( is, directory );

                    context.replace( directory );

                    return updateTimestamp;
                }
                finally
                {
                    indexArchive.delete();
                }
            }

            private Date getIndexTimestamp( IndexingContext context, Wagon wagon )
                throws IOException
            {
                try
                {
                    Properties properties = downloadIndexProperties( wagon );

                    String indexFileTimestamp = properties.getProperty( IndexingContext.INDEX_TIMESTAMP );

                    if ( indexFileTimestamp != null )
                    {
                        return new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT ).parse( indexFileTimestamp );
                    }
                }
                catch ( ParseException ex )
                {
                }
                return null;
            }
        } );
    }

    public Properties fetchIndexProperties( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException
    {
        return (Properties) run( context, listener, proxyInfo, new WagonTask()
        {
            public Object invoke( Wagon wagon )
                throws FileNotFoundException,
                    IOException
            {
                return downloadIndexProperties( wagon );
            }
        } );
    }

    private Object run( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo, WagonTask task )
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

            properties.load( new FileInputStream( indexProperties ) );

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
     * A task that requires a Wagon instance
     */
    public interface WagonTask
    {
        Object invoke( Wagon wagon )
            throws FileNotFoundException,
                IOException;
    }

}
