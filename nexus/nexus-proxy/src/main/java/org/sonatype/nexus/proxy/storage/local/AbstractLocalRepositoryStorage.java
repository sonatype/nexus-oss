/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.storage.local;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreIteratorRequest;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ChecksummingContentLocator;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

/**
 * Abstract Storage class. It have ID and defines logger. Predefines all write methods to be able to "decorate"
 * StorageItems with attributes if supported.
 * 
 * @author cstamas
 */
public abstract class AbstractLocalRepositoryStorage
    implements LocalRepositoryStorage
{
    @Requirement
    private Logger logger;

    /**
     * The wastebasket.
     */
    @Requirement
    private Wastebasket wastebasket;

    /**
     * The default Link persister.
     */
    @Requirement
    private LinkPersister linkPersister;

    /**
     * The MIME util.
     */
    @Requirement
    private MimeUtil mimeUtil;

    /**
     * Since storages are shared, we are tracking the last changes from each of them.
     */
    private Map<String, Long> repositoryContexts = new HashMap<String, Long>();

    protected Logger getLogger()
    {
        return logger;
    }

    protected Wastebasket getWastebasket()
    {
        return wastebasket;
    }

    protected LinkPersister getLinkPersister()
    {
        return linkPersister;
    }

    protected MimeUtil getMimeUtil()
    {
        return mimeUtil;
    }

    // ==

    /**
     * Remote storage specific, when the remote connection settings are actually applied.
     * 
     * @param context
     */
    protected void updateContext( Repository repository, LocalStorageContext context )
        throws LocalStorageException
    {
        // empty, override if needed
    }

    protected synchronized LocalStorageContext getLocalStorageContext( Repository repository )
        throws LocalStorageException
    {
        if ( repository.getLocalStorageContext() != null )
        {
            // we have repo specific settings
            // if contextContains key and is newer, or does not contain yet
            if ( ( repositoryContexts.containsKey( repository.getId() ) && repository.getLocalStorageContext().getLastChanged() > ( repositoryContexts.get( repository.getId() ).longValue() ) )
                || !repositoryContexts.containsKey( repository.getId() ) )
            {
                updateContext( repository, repository.getLocalStorageContext() );

                repositoryContexts.put( repository.getId(),
                    Long.valueOf( repository.getLocalStorageContext().getLastChanged() ) );
            }
        }

        return repository.getLocalStorageContext();
    }

    // ==

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    @Deprecated
    public URL getAbsoluteUrlFromBase( Repository repository, ResourceStoreRequest request )
        throws LocalStorageException
    {
        StringBuffer urlStr = new StringBuffer( repository.getLocalUrl() );

        if ( request.getRequestPath().startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            urlStr.append( request.getRequestPath() );
        }
        else
        {
            urlStr.append( RepositoryItemUid.PATH_SEPARATOR ).append( request.getRequestPath() );
        }
        try
        {
            return new URL( urlStr.toString() );
        }
        catch ( MalformedURLException e )
        {
            try
            {
                return new File( urlStr.toString() ).toURI().toURL();
            }
            catch ( MalformedURLException e1 )
            {
                throw new LocalStorageException( "The local storage has a malformed URL as baseUrl!", e );
            }
        }
    }

    public final void deleteItem( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, LocalStorageException
    {
        getWastebasket().delete( this, repository, request );
    }

    // ==

    public Iterator<StorageItem> iterateItems( Repository repository, ResourceStoreIteratorRequest request )
        throws ItemNotFoundException, LocalStorageException
    {
        throw new UnsupportedOperationException( "Iteration not supported!" );
    }

    // ==

    protected void prepareStorageFileItemForStore( final StorageFileItem item )
        throws LocalStorageException
    {
        try
        {
            // replace content locator
            ChecksummingContentLocator sha1cl =
                new ChecksummingContentLocator( item.getContentLocator(), MessageDigest.getInstance( "SHA1" ),
                    RequestContext.CTX_DIGEST_SHA1_KEY, item.getItemContext() );

            // md5 is deprecated but still calculated
            ChecksummingContentLocator md5cl =
                new ChecksummingContentLocator( sha1cl, MessageDigest.getInstance( "MD5" ),
                    RequestContext.CTX_DIGEST_MD5_KEY, item.getItemContext() );

            item.setContentLocator( md5cl );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new LocalStorageException(
                "The JVM does not support SHA1 MessageDigest or MD5 MessageDigest, that is essential for Nexus. We cannot write to local storage! Please run Nexus on JVM that does provide these MessageDigests.",
                e );
        }
    }
}
