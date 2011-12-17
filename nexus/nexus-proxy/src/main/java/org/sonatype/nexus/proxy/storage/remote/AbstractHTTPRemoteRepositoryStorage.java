/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.storage.remote;

import java.net.MalformedURLException;
import java.net.URL;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.RemoteAccessDeniedException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

/**
 * This class is a base abstract class for HTTP remote storage.
 *
 * @author cstamas
 */
public abstract class AbstractHTTPRemoteRepositoryStorage
    extends AbstractRemoteRepositoryStorage
    implements RemoteRepositoryStorage
{

    protected AbstractHTTPRemoteRepositoryStorage( final UserAgentBuilder userAgentBuilder,
                                                   final ApplicationStatusSource applicationStatusSource,
                                                   final MimeSupport mimeSupport )
    {
        super( userAgentBuilder, applicationStatusSource, mimeSupport );
    }

    @Override
    public boolean isReachable( final ProxyRepository repository,
                                final ResourceStoreRequest request )
        throws RemoteStorageException
    {
        boolean result = false;

        try
        {
            request.pushRequestPath( RepositoryItemUid.PATH_ROOT );

            try
            {
                result = checkRemoteAvailability( 0, repository, request, false );
            }
            catch ( RemoteAccessDeniedException e )
            {
                return true;
            }
        }
        finally
        {
            request.popRequestPath();
        }

        return result;
    }

    @Override
    public void validateStorageUrl( String url )
        throws RemoteStorageException
    {
        try
        {
            URL u = new URL( url );

            if ( !"http".equals( u.getProtocol().toLowerCase() ) && !"https".equals( u.getProtocol().toLowerCase() ) )
            {
                throw new RemoteStorageException( "Unsupported protocol, only HTTP/HTTPS protocols are supported: "
                                                      + u.getProtocol().toLowerCase() );
            }
        }
        catch ( MalformedURLException e )
        {
            throw new RemoteStorageException( "Malformed URL", e );
        }
    }

    @Override
    public boolean containsItem( long newerThen, ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteStorageException
    {
        return checkRemoteAvailability( newerThen, repository, request, true );
    }

    /**
     * Returns {@code true} if only and only if we are positive that remote peer (remote URL of passed in
     * ProxyRepository) points to a remote repository that is hosted by Amazon S3 Storage. This method will return false
     * as long as we don't make very 1st HTTP request to remote peer. After that 1st request, we retain the status until
     * ProxyRepository configuration changes. See NEXUS-3338 for more.
     *
     * @param repository that needs to be checked.
     * @return true only if we know that ProxyRepository in question points to Amazon S3 storage.
     * @throws RemoteStorageException in case of some error.
     */
    public boolean isRemotePeerAmazonS3Storage( final ProxyRepository repository )
        throws RemoteStorageException
    {
        RemoteStorageContext ctx = getRemoteStorageContext( repository );

        // it is S3 if we have CTX_KEY_S3_FLAG set, the flag value is not null, and flag value is true
        // if flag is False, we know it is not S3
        // if flag is null, we still did not contact remote, so we were not able to tell yet
        return ctx.hasContextObject( getS3FlagKey() )
            && ( (DefaultRemoteStorageContext.BooleanFlagHolder) ctx.getContextObject( getS3FlagKey() ) ).isFlag();
    }

    /**
     * Checks is remote a S3 server and puts a Boolean into remote storage context, thus preventing any further checks
     * (we check only once).
     *
     * @param repository            to check for
     * @param httpServerHeaderValue value of "server" http response header
     * @throws RemoteStorageException re-thrown from {@link #getRemoteStorageContext(ProxyRepository)}
     */
    protected void checkForRemotePeerAmazonS3Storage( final ProxyRepository repository,
                                                      final String httpServerHeaderValue )
        throws RemoteStorageException
    {
        RemoteStorageContext ctx = getRemoteStorageContext( repository );

        // we already know the result, do nothing
        if ( ctx.hasContextObject( getS3FlagKey() )
            && !( (DefaultRemoteStorageContext.BooleanFlagHolder) ctx.getContextObject( getS3FlagKey() ) ).isNull() )
        {
            return;
        }

        // for now, we check the HTTP response header "Server: AmazonS3"

        boolean isAmazonS3 = ( httpServerHeaderValue != null )
            && ( httpServerHeaderValue.toLowerCase().contains( "amazons3" ) );

        if ( ctx.hasContextObject( getS3FlagKey() ) )
        {
            ( (DefaultRemoteStorageContext.BooleanFlagHolder) ctx.getContextObject( getS3FlagKey() ) ).setFlag(
                isAmazonS3 );
        }

        if ( isAmazonS3 )
        {
            getLogger().warn(
                "The proxy repository \""
                    + repository.getName()
                    + "\" (ID="
                    + repository.getId()
                    + ") is backed by Amazon S3 service. This means that Nexus can't reliably detect the validity of "
                    + "your setup (baseUrl of proxy repository)!"
            );
        }
    }

    /**
     * Initially, this method is here only to share the code for "availability check" and for "contains" check.
     * Unfortunately, the "availability" check cannot be done at RemoteStorage level, since it is completely repository
     * layout unaware and is able to tell only about the existence of remote server and that the URI on it exists. This
     * "availability" check will have to be moved upper into repository, since it is aware of "what it holds".
     * Ultimately, this method will check is the remote server "present" and is responding or not. But nothing more.
     */
    protected abstract boolean checkRemoteAvailability( long newerThen,
                                                        ProxyRepository repository,
                                                        ResourceStoreRequest request,
                                                        boolean isStrict )
        throws RemoteStorageException;

    /**
     * Returns the context key for S3 flag.
     *
     * @return the context key for S3 flag. If {@code null}, we do not handle S3.
     */
    protected abstract String getS3FlagKey();

}
