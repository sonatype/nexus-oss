package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * This is a helper for obtaining Wagon based ResourceFetchers. Some Indexer integrations does have access to Wagon
 * already, so this is here just to help them. Since Wagon (et al) is just optional dependency, looking up this
 * component in integrations where Wagon is not present, should be avoided. This helper is rather limited, as it offers
 * only "HTTP" wagons! This is not made a Plexus component since SISU would crack in CLI, while trying to load up this
 * class, because of lacking Wagon classes from classpath!
 * 
 * @author cstamas
 */
public class WagonHelper
{
    private final PlexusContainer plexusContainer;

    public WagonHelper( final PlexusContainer plexusContainer )
    {
        this.plexusContainer = plexusContainer;
    }

    public WagonFetcher getWagonResourceFetcher( final TransferListener listener )
        throws ComponentLookupException
    {
        return getWagonResourceFetcher( listener, null, null );
    }

    public WagonFetcher getWagonResourceFetcher( final TransferListener listener,
                                                 final AuthenticationInfo authenticationInfo, final ProxyInfo proxyInfo )
        throws ComponentLookupException
    {
        // we limit ourselves to HTTP only
        return new WagonFetcher( plexusContainer.lookup( Wagon.class, "http" ), listener, authenticationInfo, proxyInfo );
    }

    public static class WagonFetcher
        extends AbstractResourceFetcher
        implements ResourceFetcher
    {
        private final TransferListener listener;

        private final AuthenticationInfo authenticationInfo;

        private final ProxyInfo proxyInfo;

        private final Wagon wagon;

        public WagonFetcher( final Wagon wagon, final TransferListener listener,
                             final AuthenticationInfo authenticationInfo, final ProxyInfo proxyInfo )
        {
            this.wagon = wagon;
            this.listener = listener;
            this.authenticationInfo = authenticationInfo;
            this.proxyInfo = proxyInfo;
        }

        public void connect( final String id, final String url )
            throws IOException
        {
            Repository repository = new Repository( id, url );

            try
            {
                // wagon = wagonManager.getWagon( repository );

                if ( listener != null )
                {
                    wagon.addTransferListener( listener );
                }

                // when working in the context of Maven, the WagonManager is already
                // populated with proxy information from the Maven environment

                if ( authenticationInfo != null )
                {
                    if ( proxyInfo != null )
                    {
                        wagon.connect( repository, authenticationInfo, proxyInfo );
                    }
                    else
                    {
                        wagon.connect( repository, authenticationInfo );
                    }
                }
                else
                {
                    if ( proxyInfo != null )
                    {
                        wagon.connect( repository, proxyInfo );
                    }
                    else
                    {
                        wagon.connect( repository );
                    }
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

        public void retrieve( final String name, final File targetFile )
            throws IOException, FileNotFoundException
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

        private void logError( final String msg, final Exception ex )
        {
            if ( listener != null )
            {
                listener.debug( msg + "; " + ex.getMessage() );
            }
        }
    }
}
