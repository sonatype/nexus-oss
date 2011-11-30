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
package org.sonatype.nexus.proxy;

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Assert;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

public abstract class AbstractShadowRepositoryTest
    extends AbstractProxyTestEnvironment
{
    private static final long A_DAY = 24L * 60L * 60L * 1000L;

    protected abstract void addShadowReposes()
        throws ConfigurationException, IOException, ComponentLookupException;

    protected void testProxyLastRequestedAttribute( ShadowRepository shadowRepository, String shadowPath,
        String masterPath )
        throws Exception
    {
        Repository masterRepository = shadowRepository.getMasterRepository();

        ResourceStoreRequest shadowRequest = new ResourceStoreRequest( shadowPath );
        ResourceStoreRequest masterRequest = new ResourceStoreRequest( masterPath );

        // simulate "user request" by adding IP address to requests
        shadowRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );
        masterRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );

        StorageItem shadowItem = shadowRepository.retrieveItem( shadowRequest );
        assertTrue( "Shadow MUST return a link", StorageLinkItem.class.isAssignableFrom( shadowItem.getClass() ) );
        StorageItem masterItem = masterRepository.retrieveItem( masterRequest );
        assertTrue( "Master MUST NOT return a link", !StorageLinkItem.class.isAssignableFrom( masterItem.getClass() ) );

        // produce a lastRequest timestamp to now less 10 days
        long lastRequest = System.currentTimeMillis() - 10 * A_DAY;

        // now set the lastRequest stamp programatically to both items to this "old" timestamp
        shadowRepository.getAttributesHandler().touchItemLastRequested( lastRequest, shadowItem );
        masterRepository.getAttributesHandler().touchItemLastRequested( lastRequest, masterItem );

        // now request the object, the lastRequested timestamp should be updated
        shadowItem = shadowRepository.retrieveItem( shadowRequest );
        assertTrue( "Shadow MUST return a link", StorageLinkItem.class.isAssignableFrom( shadowItem.getClass() ) );

        // verify that shadow item lastRequested is updated, but master is still untouched
        // the attribute load will give us items without UIDs!
        Attributes shadowItem1 =
            shadowRepository.getAttributesHandler().getAttributeStorage().getAttributes(
                shadowRepository.createUid( shadowPath ) );
        Assert.assertTrue( "Shadow must have updated lastRequested field!",
            shadowItem1.getLastRequested() > lastRequest );

        // verify that shadow item lastRequested is updated, but master is still untouched
        // the attribute load will give us items without UIDs!
        Attributes masterItem1 =
            masterRepository.getAttributesHandler().getAttributeStorage().getAttributes(
                masterRepository.createUid( masterPath ) );
        Assert.assertTrue( "Master must have untouched lastRequested field!",
            masterItem1.getLastRequested() == lastRequest );

        // now dereference the link
        masterItem = getRootRouter().dereferenceLink( (StorageLinkItem) shadowItem );
        assertTrue( "Dereferenced item MUST NOT return a link", !StorageLinkItem.class.isAssignableFrom( masterItem
            .getClass() ) );

        // remember the lastRequests
        long shadowLastRequested = shadowItem.getLastRequested();
        long masterLastRequested = masterItem.getLastRequested();

        // verify that lastRequested is maintained (is updated to now)
        Assert.assertTrue( "Shadow lastRequested have to be updated", shadowLastRequested > lastRequest );
        Assert.assertTrue( "Master lastRequested have to be updated", masterLastRequested > lastRequest );

        // verify that master item, requested during resolution has lastRequested greater or equal then shadow link
        Assert.assertTrue( "Master have to be updated at least at same time or later then shadow",
            masterLastRequested >= shadowLastRequested );

        // check the shadow attributes programatically
        Attributes shadowItemAttrs =
            shadowRepository.getAttributesHandler().getAttributeStorage().getAttributes(
                shadowRepository.createUid( shadowPath ) );
        Assert.assertEquals( "The attributes differ", shadowLastRequested, shadowItemAttrs.getLastRequested() );

        // check the master attributes programatically
        Attributes masterItemAttrs =
            masterRepository.getAttributesHandler().getAttributeStorage().getAttributes(
                masterRepository.createUid( masterPath ) );
        Assert.assertEquals( "The attributes differ", masterLastRequested, masterItemAttrs.getLastRequested() );
    }
}