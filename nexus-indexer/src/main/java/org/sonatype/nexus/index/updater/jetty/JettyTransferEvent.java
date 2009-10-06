/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
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
        }

        public void addTransferListener( final TransferListener listener )
        {
        }

        public void connect( final Repository source )
            throws ConnectionException, AuthenticationException
        {
        }

        public void connect( final Repository source, final ProxyInfo proxyInfo )
            throws ConnectionException, AuthenticationException
        {
        }

        public void connect( final Repository source, final ProxyInfoProvider proxyInfoProvider )
            throws ConnectionException, AuthenticationException
        {
        }

        public void connect( final Repository source, final AuthenticationInfo authenticationInfo )
            throws ConnectionException, AuthenticationException
        {
        }

        public void connect( final Repository source, final AuthenticationInfo authenticationInfo,
                             final ProxyInfo proxyInfo )
            throws ConnectionException, AuthenticationException
        {
        }

        public void connect( final Repository source, final AuthenticationInfo authenticationInfo,
                             final ProxyInfoProvider proxyInfoProvider )
            throws ConnectionException, AuthenticationException
        {
        }

        public void disconnect()
            throws ConnectionException
        {
        }

        public void get( final String resourceName, final File destination )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
        }

        public List<String> getFileList( final String destinationDirectory )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
            return null;
        }

        public boolean getIfNewer( final String resourceName, final File destination, final long timestamp )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
            return false;
        }

        public Repository getRepository()
        {
            return null;
        }

        public int getTimeout()
        {
            return 0;
        }

        public boolean hasSessionListener( final SessionListener listener )
        {
            return false;
        }

        public boolean hasTransferListener( final TransferListener listener )
        {
            return false;
        }

        public boolean isInteractive()
        {
            return false;
        }

        public void openConnection()
            throws ConnectionException, AuthenticationException
        {
        }

        public void put( final File source, final String destination )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
        }

        public void putDirectory( final File sourceDirectory, final String destinationDirectory )
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
        {
        }

        public void removeSessionListener( final SessionListener listener )
        {
        }

        public void removeTransferListener( final TransferListener listener )
        {
        }

        public boolean resourceExists( final String resourceName )
            throws TransferFailedException, AuthorizationException
        {
            return false;
        }

        public void setInteractive( final boolean interactive )
        {
        }

        public void setTimeout( final int timeoutValue )
        {
        }

        public boolean supportsDirectoryCopy()
        {
            return false;
        }

    }

}
