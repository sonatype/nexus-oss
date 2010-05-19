package org.sonatype.nexus.integrationtests.nexus2062;


import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;

public class Nexus2062EmptyGroupIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void createEmptyGroup()
        throws Exception
    {
        GroupMessageUtil groupUtil = new GroupMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        
        RepositoryGroupResource resource = new RepositoryGroupResource();
        resource.setExposed( true );
        resource.setFormat( "maven2" );
        resource.setId( "emptygroup" );
        resource.setName( "emptygroup" );
        resource.setProvider( "maven2" );
        
        resource = groupUtil.createGroup( resource );
        
        Assert.assertEquals( 0, resource.getRepositories().size() );
    }
    
    @Test
    public void createGroupWithRepoAndDelete()
        throws Exception
    {
        GroupMessageUtil groupUtil = new GroupMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        
        RepositoryGroupResource resource = new RepositoryGroupResource();
        resource.setExposed( true );
        resource.setFormat( "maven2" );
        resource.setId( "nonemptygroup" );
        resource.setName( "nonemptygroup" );
        resource.setProvider( "maven2" );
        
        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( REPO_TEST_HARNESS_REPO );
        resource.addRepository( member );
        
        resource = groupUtil.createGroup( resource );
        
        resource.getRepositories().clear();
        
        resource = groupUtil.updateGroup( resource );
        
        Assert.assertEquals( 0, resource.getRepositories().size() );
    }
}
