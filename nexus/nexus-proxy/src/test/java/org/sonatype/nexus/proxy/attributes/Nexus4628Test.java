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
package org.sonatype.nexus.proxy.attributes;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * NEXUS-4628: Lessen the occurence of "lastRequested" attribute storing to 24h, to lessen IO in general.
 * 
 * @author cstamas
 */
public class Nexus4628Test
    extends AbstractAttributesHandlerTest
{
    protected ResourceStoreRequest fakeRemoteRequest;

    protected RepositoryItemUid uid;

    protected long beforeCallTs;

    protected long afterCallTs;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        fakeRemoteRequest = new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar" );
        fakeRemoteRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "192.168.1.1" );

        uid = getRepositoryItemUidFactory().createUid( repository, fakeRemoteRequest.getRequestPath() );

        AbstractStorageItem aitem = attributesHandler.getAttributeStorage().getAttributes( uid );
        assertThat( aitem, nullValue() );

        beforeCallTs = System.currentTimeMillis();
        repository.recreateAttributes( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ), null );
        afterCallTs = System.currentTimeMillis();
    }

    /**
     * Set up repo, and recreate attributes (this will set lastRequested too, since items does not have any attributes
     * yet). Then "touch" them, but nothing should happen (no change and no IO) since we are within the "resolution".
     * 
     * @throws IOException
     * @throws ItemNotFoundException
     */
    @Test
    public void testSimpleTouchIsDoneOnceDaily()
        throws IOException, ItemNotFoundException
    {
        AbstractStorageItem aitem;

        AttributeStorage attributeStorageSpy = Mockito.spy( repository.getAttributesHandler().getAttributeStorage() );
        repository.getAttributesHandler().setAttributeStorage( attributeStorageSpy );

        aitem = attributesHandler.getAttributeStorage().getAttributes( uid );
        checkNotNull( aitem );
        assertThat( aitem.getLastRequested(),
            allOf( greaterThanOrEqualTo( beforeCallTs ), lessThanOrEqualTo( afterCallTs ) ) );

        attributesHandler.touchItemLastRequested( System.currentTimeMillis(), repository, fakeRemoteRequest, aitem );

        aitem = attributesHandler.getAttributeStorage().getAttributes( uid );
        checkNotNull( aitem );
        assertThat( aitem.getLastRequested(),
            allOf( greaterThanOrEqualTo( beforeCallTs ), lessThanOrEqualTo( afterCallTs ) ) );

        Mockito.verify( attributeStorageSpy, Mockito.times( 2 ) ).getAttributes( Mockito.<RepositoryItemUid> any() );
        Mockito.verify( attributeStorageSpy, Mockito.times( 0 ) ).putAttribute( Mockito.<StorageItem> any() );
    }

    /**
     * The "touch" into past should always work as before: as many WRITEs as many touches happens.
     * 
     * @throws IOException
     * @throws ItemNotFoundException
     */
    @Test
    public void testSimpleTouchInPastAlwaysWork()
        throws IOException, ItemNotFoundException
    {
        AbstractStorageItem aitem;

        AttributeStorage attributeStorageSpy = Mockito.spy( repository.getAttributesHandler().getAttributeStorage() );
        repository.getAttributesHandler().setAttributeStorage( attributeStorageSpy );

        aitem = attributesHandler.getAttributeStorage().getAttributes( uid );
        checkNotNull( aitem );
        assertThat( aitem.getLastRequested(),
            allOf( greaterThanOrEqualTo( beforeCallTs ), lessThanOrEqualTo( afterCallTs ) ) );

        final long past = System.currentTimeMillis() - 10000;
        attributesHandler.touchItemLastRequested( past, repository, fakeRemoteRequest, aitem );

        aitem = attributesHandler.getAttributeStorage().getAttributes( uid );
        checkNotNull( aitem );
        assertThat( aitem.getLastRequested(), equalTo( past ) );

        final long past2 = past - 10000;
        attributesHandler.touchItemLastRequested( past2, repository, fakeRemoteRequest, aitem );

        aitem = attributesHandler.getAttributeStorage().getAttributes( uid );
        checkNotNull( aitem );
        assertThat( aitem.getLastRequested(), equalTo( past2 ) );

        Mockito.verify( attributeStorageSpy, Mockito.times( 3 ) ).getAttributes( Mockito.<RepositoryItemUid> any() );
        Mockito.verify( attributeStorageSpy, Mockito.times( 2 ) ).putAttribute( Mockito.<StorageItem> any() );
    }

}
