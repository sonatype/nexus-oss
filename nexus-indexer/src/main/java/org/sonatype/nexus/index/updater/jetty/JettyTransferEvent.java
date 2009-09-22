package org.sonatype.nexus.index.updater.jetty;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;

import java.io.File;
import java.util.List;

public class JettyTransferEvent
    extends TransferEvent
{

    private static final long serialVersionUID = 1L;

    public JettyTransferEvent( final String url, final Exception exception, final int requestType )
    {
        super( new DummyWagon(), resourceFor( url ), exception, requestType );
    }

    public JettyTransferEvent( final String url, final int eventType, final int requestType )
    {
        super( new DummyWagon(), resourceFor( url ), eventType, requestType );
    }

    private static Resource resourceFor( final String url )
    {
        Resource res = new Resource();
        res.setName( url );

        return res;
    }

    public static final class DummyWagon
        implements Wagon
    {

        public void addSessionListener( final SessionListener listener )
        {
            // TODO Auto-generated method stub

        }

        public void addTransferListener( final TransferListener listener )
        {
            // TODO Auto-generated method stub

        }

        public void connect( final Repository source )
            throws ConnectionException, AuthenticationException
        {
            // TODO Auto-generated method stub

        }

        public void connect( final Repository source, final ProxyInfo proxyInfo )
            throws ConnectionException, AuthenticationException
        {
            // TODO Auto-generated method stub

        }

        public void connect( final Repository source, final ProxyInfoProvider proxyInfoProvider )
            throws ConnectionException, AuthenticationException
        {
            // TODO Auto-generated method stub

        }

        public void connect( final Repository source, final AuthenticationInfo authenticationInfo )
            throws ConnectionException, AuthenticationException
        {
            // TODO Auto-generated method stub

        }

        public void connect( final Repository source, final AuthenticationInfo authenticationInfo,
                             final ProxyInfo proxyInfo )
            throws ConnectionException, AuthenticationException
        {
            // TODO Auto-generated method stub

        }

        public void connect( final Repository source, final AuthenticationInfo authenticationInfo,
                             final ProxyInfoProvider proxyInfoProvider )
            throws ConnectionException, AuthenticationException
        {
            // TODO Auto-generated method stub

        }

        public void disconnect()
            throws ConnectionException
        {
            // TODO Auto-generated method stub

        }

        public void get( final String resourceName, final File destination )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
            // TODO Auto-generated method stub

        }

        public List<String> getFileList( final String destinationDirectory )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean getIfNewer( final String resourceName, final File destination, final long timestamp )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
            // TODO Auto-generated method stub
            return false;
        }

        public Repository getRepository()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getTimeout()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public boolean hasSessionListener( final SessionListener listener )
        {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean hasTransferListener( final TransferListener listener )
        {
            // TODO Auto-generated method stub
            return false;
        }

        public boolean isInteractive()
        {
            // TODO Auto-generated method stub
            return false;
        }

        public void openConnection()
            throws ConnectionException, AuthenticationException
        {
            // TODO Auto-generated method stub

        }

        public void put( final File source, final String destination )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
            // TODO Auto-generated method stub

        }

        public void putDirectory( final File sourceDirectory, final String destinationDirectory )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
            // TODO Auto-generated method stub

        }

        public void removeSessionListener( final SessionListener listener )
        {
            // TODO Auto-generated method stub

        }

        public void removeTransferListener( final TransferListener listener )
        {
            // TODO Auto-generated method stub

        }

        public boolean resourceExists( final String resourceName )
            throws TransferFailedException, AuthorizationException
        {
            // TODO Auto-generated method stub
            return false;
        }

        public void setInteractive( final boolean interactive )
        {
            // TODO Auto-generated method stub

        }

        public void setTimeout( final int timeoutValue )
        {
            // TODO Auto-generated method stub

        }

        public boolean supportsDirectoryCopy()
        {
            // TODO Auto-generated method stub
            return false;
        }

    }

}
