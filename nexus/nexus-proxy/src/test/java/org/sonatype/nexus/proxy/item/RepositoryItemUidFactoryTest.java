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
package org.sonatype.nexus.proxy.item;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryItemUidFactoryTest
    extends AbstractProxyTestEnvironment
{
    protected RepositoryItemUidFactory factory;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        return new M2TestsuiteEnvironmentBuilder( ss );
    }

    public void setUp()
        throws Exception
    {
        super.setUp();

        factory = lookup( RepositoryItemUidFactory.class );
    }

    @Test
    public void testSameLockResourceInstance()
        throws Exception
    {
        Repository repository = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );

        RepositoryItemUid uid1 = factory.createUid( repository, "/some/blammo/poth" );

        RepositoryItemUid uid2 = factory.createUid( repository, "/some/blammo/poth" );

        DefaultRepositoryItemUidLock uidLock1 = (DefaultRepositoryItemUidLock) uid1.getLock();

        DefaultRepositoryItemUidLock uidLock2 = (DefaultRepositoryItemUidLock) uid2.getLock();

        uidLock1.lock( Action.read );

        // They share SAME lock
        Assert.assertNotSame( "UIDLock instances should be different", uidLock1, uidLock2 );
        Assert.assertSame( "UIDLock lock instances should be same", uidLock1.getContentLock(),
            uidLock2.getContentLock() );
        Assert.assertEquals( "Since invoked from same UT thread, both should say we have lock held",
            uidLock1.getContentLock().hasLocksHeld(), uidLock2.getContentLock().hasLocksHeld() );
    }

    @Test
    public void testRelease()
        throws Exception
    {
        Repository repository = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );

        RepositoryItemUid uid = factory.createUid( repository, "/some/blammo/poth" );

        DefaultRepositoryItemUidLock uidLock1 = (DefaultRepositoryItemUidLock) uid.getLock();

        uidLock1.lock( Action.read );

        Assert.assertTrue( "Since locked it should say we have lock held", uidLock1.getContentLock().hasLocksHeld() );

        uidLock1.unlock();

        Assert.assertFalse( "Since unlocked it should say we have no lock held",
            uidLock1.getContentLock().hasLocksHeld() );

        // unlock above released it too
        // uidLock1.release();

        try
        {
            uidLock1.unlock();

            Assert.fail( "Reusing a released instance of UIDLock is a no-go" );
        }
        catch ( IllegalStateException e )
        {
            // good
        }
    }

}
