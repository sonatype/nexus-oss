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
package org.sonatype.nexus;

import java.util.Arrays;
import java.util.List;

import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.templates.repository.maven.Maven1GroupRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1ProxyRepositoryTemplate;

public class GroupUpdateTest
    extends AbstractNexusTestCase
{
    Nexus nexus;
    RepositoryRegistry repoRegistry;
    RemoteRepositoryStorage remoteRepositoryStorage;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        nexus = lookup( Nexus.class );
        repoRegistry = lookup( RepositoryRegistry.class );
        remoteRepositoryStorage = lookup( RemoteRepositoryStorage.class, "apacheHttpClient3x" );
    }
    
    public void testUpdateGroup()
        throws Exception
    {
        createM1HostedRepo( "m1h" );
        createM1ProxyRepo( "m1p" );
        MavenGroupRepository group = createM1Group( "m1g", Arrays.asList( "central-m1", "m1h", "m1p" ) );
        
        assertTrue( group.getMemberRepositoryIds().contains( "m1h" ) );
        assertTrue( group.getMemberRepositoryIds().contains( "m1p" ) );
        assertTrue( group.getMemberRepositoryIds().contains( "central-m1" ) );
        assertTrue( group.getMemberRepositoryIds().size() == 3 );
        
        // now delete the proxy
        nexus.deleteRepository( "m1p" );
        
        assertTrue( group.getMemberRepositoryIds().contains( "m1h" ) );
        assertTrue( group.getMemberRepositoryIds().contains( "central-m1" ) );
        assertTrue( group.getMemberRepositoryIds().size() == 2 );
    }
    
    private MavenRepository createM1HostedRepo( String id )
        throws Exception
    {        
        Maven1HostedRepositoryTemplate template = 
            ( Maven1HostedRepositoryTemplate ) nexus.getRepositoryTemplates()
                .getTemplates( Maven1HostedRepositoryTemplate.class, RepositoryPolicy.RELEASE ).pick();

        template.getConfigurableRepository().setIndexable( false );
        template.getConfigurableRepository().setId( id );
        template.getConfigurableRepository().setName( id );

        return template.create();
    }
    
    private MavenRepository createM1ProxyRepo( String id )
        throws Exception
    {
        Maven1ProxyRepositoryTemplate template = 
            ( Maven1ProxyRepositoryTemplate ) nexus.getRepositoryTemplates()
                .getTemplates( Maven1ProxyRepositoryTemplate.class, RepositoryPolicy.RELEASE ).pick();

        template.getConfigurableRepository().setIndexable( false );
        template.getConfigurableRepository().setId( id );
        template.getConfigurableRepository().setName( id ); 

        return template.create();
    }
    
    private MavenGroupRepository createM1Group( String id, List<String> members )
        throws Exception
    {
        Maven1GroupRepositoryTemplate template = 
            ( Maven1GroupRepositoryTemplate ) nexus.getRepositoryTemplates()
                .getTemplates( Maven1GroupRepositoryTemplate.class ).pick();

        template.getConfigurableRepository().setId( id );
        template.getConfigurableRepository().setName( id );
        template.getConfigurableRepository().setIndexable( false );        
        
        for ( String member : members )
        {
            template.getExternalConfiguration( true ).addMemberRepositoryId( member );
        }
        
        return ( MavenGroupRepository ) template.create();
    }
}
