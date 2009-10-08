package org.sonatype.nexus.integrationtests.nexus1730;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.security.rest.model.PrivilegeResource;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

public class Nexus1730DeleteRepoIT
    extends AbstractNexusIntegrationTest
{
    protected PrivilegesMessageUtil privUtil;
    protected RepositoryMessageUtil repoUtil;
    protected GroupMessageUtil groupUtil;
    
    public Nexus1730DeleteRepoIT()
        throws Exception
    {
        privUtil = new PrivilegesMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
        repoUtil = new RepositoryMessageUtil( getJsonXStream(), MediaType.APPLICATION_JSON, getRepositoryTypeRegistry() );
        groupUtil = new GroupMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
    }
    
    @Test
    public void testDeleteRepo()
        throws Exception
    {
        createRepository();
        List<String> privilegeIds = createPrivileges();
        
        for ( String privilegeId : privilegeIds )
        {
            checkForPrivilege( privilegeId, true );
        }
        
        deleteRepository();
        
        for ( String privilegeId : privilegeIds )
        {
            checkForPrivilege( privilegeId, false );
        }
    }
    
    @Test
    public void testDeleteGroup()
        throws Exception
    {
        createGroup();
        List<String> privilegeIds = createGroupPrivileges();
        
        for ( String privilegeId : privilegeIds )
        {
            checkForPrivilege( privilegeId, true );
        }
        
        deleteGroup();
        
        for ( String privilegeId : privilegeIds )
        {
            checkForPrivilege( privilegeId, false );
        }
    }
    
    private void createRepository()
        throws Exception
    {
        RepositoryResource repo = new RepositoryResource();
        repo.setId( "nexus1730-repo" );
        repo.setRepoType( "hosted" );
        repo.setName( "nexus1730-repo" );
        repo.setProvider( "maven2" );
        repo.setFormat( "maven2" );
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repoUtil.createRepository( repo );   
    }
    
    private void deleteRepository() 
        throws IOException
    {
        repoUtil.sendMessage( Method.DELETE, null, "nexus1730-repo" );
    }
    
    private void createGroup() 
        throws IOException
    {
        RepositoryGroupResource group = new RepositoryGroupResource();
        group.setId( "nexus1730-group" );
        group.setFormat( "maven2" );
        group.setProvider( "maven2" );
        group.setName( "nexus1730-group" );
        
        RepositoryGroupMemberRepository repo = new RepositoryGroupMemberRepository();
        repo.setId( testRepositoryId );
        group.setRepositories( Arrays.asList( repo ) );
        
        groupUtil.createGroup( group );
    }
    
    private void deleteGroup() 
        throws IOException
    {
        RepositoryGroupResource group = new RepositoryGroupResource();
        group.setId( "nexus1730-group" );
        
        groupUtil.sendMessage( Method.DELETE, group );
    }
    
    private List<String> createPrivileges()
        throws Exception
    {
        PrivilegeResource priv = new PrivilegeResource();
        priv.setDescription( "nexus1730-priv" );
        priv.setMethod( Arrays.asList( "read","delete","create","update" ) );
        priv.setRepositoryId( "nexus1730-repo" );
        priv.setRepositoryTargetId( "1" );
        priv.setType( TargetPrivilegeDescriptor.TYPE );
        priv.setName( "nexus1730-priv" );
        List<PrivilegeStatusResource> privs = privUtil.createPrivileges( priv );
        
        List<String> privIds = new ArrayList<String>();
        
        for ( PrivilegeStatusResource privilege : privs )
        {
            privIds.add( privilege.getId() );
        }
        
        return privIds;
    }
    
    private List<String> createGroupPrivileges() 
        throws IOException
    {
        PrivilegeResource priv = new PrivilegeResource();
        priv.setDescription( "nexus1730-priv" );
        priv.setMethod( Arrays.asList( "read","delete","create","update" ) );
        priv.setRepositoryGroupId( "nexus1730-group" );
        priv.setRepositoryTargetId( "1" );
        priv.setType( TargetPrivilegeDescriptor.TYPE );
        priv.setName( "nexus1730-priv" );
        List<PrivilegeStatusResource> privs = privUtil.createPrivileges( priv );
        
        List<String> privIds = new ArrayList<String>();
        
        for ( PrivilegeStatusResource privilege : privs )
        {
            privIds.add( privilege.getId() );
        }
        
        return privIds;
    }
    
    private boolean checkForPrivilege( String id, boolean shouldFind )
        throws Exception
    {
        Response response = privUtil.sendMessage( Method.GET, null, id );
        if ( response.getStatus().isSuccess() != shouldFind )
        {
            Assert.fail( "Privilege " + id + " should " + ( shouldFind ? "" : " not " ) + "have been found" );
        }
        
        return true;
    }
}
