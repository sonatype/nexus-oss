/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.leveldb.attributes;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.Options;
import org.sonatype.nexus.leveldb.LevelDbManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.AbstractAttributeStorage;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.attributes.JacksonJSONMarshaller;
import org.sonatype.nexus.proxy.attributes.Marshaller;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;

/**
 * {@link AttributeStorage} implementation that uses LevelDB to store attributes.
 * 
 * @author cstamas
 * @since 2.7.0
 */
@Typed( AttributeStorage.class )
@Named( "leveldb" )
@Singleton
public class LevelDBAttributeStorage
    extends AbstractAttributeStorage
    implements AttributeStorage
{
    private final Marshaller marshaller;

    private final DB levelDb;

    /**
     * Instantiates a new LevelDB attribute storage.
     */
    @Inject
    public LevelDBAttributeStorage( final LevelDbManager levelDbManager )
        throws IOException
    {
        this( levelDbManager, new JacksonJSONMarshaller() );
    }

    /**
     * Instantiates a new LevelDB attribute storage.
     */
    public LevelDBAttributeStorage( final LevelDbManager levelDbManager, final Marshaller marshaller )
        throws IOException
    {
        checkNotNull( levelDbManager );
        this.marshaller = checkNotNull( marshaller );
        // we obtain a managed DB instance, as we need it throughout whole Nx lifecycle, and manager will close it out for us
        levelDb = levelDbManager.openManaged( "nx-attributes", new Options().createIfMissing( true ) );
        getLogger().info( "LevelDB AttributeStorage in place, using {} marshaller.", marshaller );
    }

    @Override
    public boolean deleteAttributes( final RepositoryItemUid uid )
        throws IOException
    {
        final RepositoryItemUidLock uidLock = uid.getLock();
        uidLock.lock( Action.delete );
        try
        {
            getLogger().debug( "Deleting attribute on UID={}", uid );
            try
            {
                levelDb.delete( bytes( uid.getKey() ) );
                return true;
            }
            catch ( DBException e )
            {
                getLogger().error( "Got DBException during deletion of UID=" + uid.toString(), e );
            }
            return false;
        }
        finally
        {
            uidLock.unlock();
        }
    }

    @Override
    public Attributes getAttributes( final RepositoryItemUid uid )
        throws IOException
    {
        final RepositoryItemUidLock uidLock = uid.getLock();
        uidLock.lock( Action.read );
        try
        {
            getLogger().debug( "Loading attribute on UID={}", uid );
            final byte[] attributeContent = levelDb.get( bytes( uid.getKey() ) );
            if ( attributeContent != null )
            {
                final Attributes result = marshaller.unmarshal( new ByteArrayInputStream( attributeContent ) );
                result.setRepositoryId( uid.getRepository().getId() );
                result.setPath( uid.getPath() );
                // fixing remoteChecked
                if ( result.getCheckedRemotely() == 0 || result.getCheckedRemotely() == 1 )
                {
                    result.setCheckedRemotely( System.currentTimeMillis() );
                    result.setExpired( true );
                }
                // fixing lastRequested
                if ( result.getLastRequested() == 0 )
                {
                    result.setLastRequested( System.currentTimeMillis() );
                }
                return result;
            }
        }
        catch ( DBException e )
        {
            getLogger().error( "Got DBException during retrieval of UID=" + uid.toString(), e );
        }
        finally
        {
            uidLock.unlock();
        }
        return null;
    }

    @Override
    public void putAttributes( final RepositoryItemUid uid, Attributes attributes )
        throws IOException
    {
        checkNotNull( attributes );
        final RepositoryItemUidLock uidLock = uid.getLock();
        uidLock.lock( Action.create );
        try
        {
            getLogger().debug( "Storing attribute on UID={}", uid );
            try
            {
                final Attributes onDisk = getAttributes( uid );
                if ( onDisk != null && ( onDisk.getGeneration() > attributes.getGeneration() ) )
                {
                    // change detected, overlay the to be saved onto the newer one and swap
                    onDisk.overlayAttributes( attributes );
                    // and overlay other things too
                    onDisk.setRepositoryId( uid.getRepository().getId() );
                    onDisk.setPath( uid.getPath() );
                    onDisk.setReadable( attributes.isReadable() );
                    onDisk.setWritable( attributes.isWritable() );
                    attributes = onDisk;
                }
                attributes.incrementGeneration();
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                marshaller.marshal( attributes, bos );
                levelDb.put( bytes( uid.getKey() ), bos.toByteArray() );
            }
            catch ( DBException e )
            {
                getLogger().error( "Got DBException during store of UID=" + uid.toString(), e );
            }
        }
        finally
        {
            uidLock.unlock();
        }
    }
}
