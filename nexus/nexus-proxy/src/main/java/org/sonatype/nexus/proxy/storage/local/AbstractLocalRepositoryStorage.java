/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
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
    private Logger logger = LoggerFactory.getLogger( getClass() );

    /**
     * The wastebasket.
     */
    private Wastebasket wastebasket;

    /**
     * The default Link persister.
     */
    private LinkPersister linkPersister;

    /**
     * The MIME support.
     */
    private MimeSupport mimeSupport;

    /**
     * Since storages are shared, we are tracking the last changes from each of them.
     */
    private Map<String, Long> repositoryContexts = new HashMap<String, Long>();


    protected AbstractLocalRepositoryStorage( Wastebasket wastebasket, LinkPersister linkPersister, MimeSupport mimeSupport,
                                              Map<String, Long> repositoryContexts )
    {
        this.wastebasket = wastebasket;
        this.linkPersister = linkPersister;
        this.mimeSupport = mimeSupport;
        this.repositoryContexts = repositoryContexts;
    }

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

    protected MimeSupport getMimeSupport()
    {
        return mimeSupport;
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
                    StorageFileItem.DIGEST_SHA1_KEY, item.getItemContext() );

            // md5 is deprecated but still calculated
            ChecksummingContentLocator md5cl =
                new ChecksummingContentLocator( sha1cl, MessageDigest.getInstance( "MD5" ),
                    StorageFileItem.DIGEST_MD5_KEY, item.getItemContext() );

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
