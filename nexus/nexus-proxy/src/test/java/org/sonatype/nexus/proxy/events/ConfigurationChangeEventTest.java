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
package org.sonatype.nexus.proxy.events;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.ConfigurationCommitEvent;
import org.sonatype.nexus.configuration.ConfigurationPrepareForSaveEvent;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;

public class ConfigurationChangeEventTest
    extends AbstractProxyTestEnvironment
{
    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        return new M2TestsuiteEnvironmentBuilder( ss );
    }

    public void testSimplePull()
        throws Exception
    {
        // flush all potential changes
        getApplicationEventMulticaster()
            .notifyEventListeners( new ConfigurationPrepareForSaveEvent( getApplicationConfiguration() ) );
        getApplicationEventMulticaster()
            .notifyEventListeners( new ConfigurationCommitEvent( getApplicationConfiguration() ) );

        // get hold on all registered reposes
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );
        Repository repo2 = getRepositoryRegistry().getRepository( "repo2" );
        Repository repo3 = getRepositoryRegistry().getRepository( "repo3" );
        Repository inhouse = getRepositoryRegistry().getRepository( "inhouse" );
        Repository inhouseSnapshot = getRepositoryRegistry().getRepository( "inhouse-snapshot" );
        MavenGroupRepository test = getRepositoryRegistry().getRepositoryWithFacet( "test", MavenGroupRepository.class );

        // now change some of them
        repo1.setLocalStatus( LocalStatus.OUT_OF_SERVICE );
        repo3.setName( "kuku" );
        test.setMergeMetadata( false );

        // changes are not applied yet!
        assertEquals( "Should not be applied!", LocalStatus.IN_SERVICE, repo1.getLocalStatus() );
        assertEquals( "Should not be applied!", "repo3", repo3.getName() );
        assertEquals( "Should not be applied!", true, test.isMergeMetadata() );

        // fire prepareForSave event
        ConfigurationPrepareForSaveEvent pevt = new ConfigurationPrepareForSaveEvent( getApplicationConfiguration() );
        getApplicationEventMulticaster().notifyEventListeners( pevt );
        assertFalse( pevt.isVetoed() );

        getApplicationEventMulticaster()
            .notifyEventListeners( new ConfigurationCommitEvent( getApplicationConfiguration() ) );

        // changes are now applied!
        assertEquals( "Should be applied!", LocalStatus.OUT_OF_SERVICE, repo1.getLocalStatus() );
        assertEquals( "Should be applied!", "kuku", repo3.getName() );
        assertEquals( "Should be applied!", false, test.isMergeMetadata() );

        // changed reposes should be in event
        assertTrue( "Is changed!", pevt.getChanges().contains( repo1 ) );
        assertTrue( "Is changed!", pevt.getChanges().contains( repo3 ) );
        assertTrue( "Is changed!", pevt.getChanges().contains( test ) );

        // others are not in event
        assertFalse( "Is not changed!", pevt.getChanges().contains( repo2 ) );
        assertFalse( "Is not changed!", pevt.getChanges().contains( inhouse ) );
        assertFalse( "Is not changed!", pevt.getChanges().contains( inhouseSnapshot ) );

    }
}
