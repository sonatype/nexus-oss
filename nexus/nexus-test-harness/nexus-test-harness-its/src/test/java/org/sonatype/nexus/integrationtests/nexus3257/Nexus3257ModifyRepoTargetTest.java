package org.sonatype.nexus.integrationtests.nexus3257;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.rest.model.PrivilegeResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

public class Nexus3257ModifyRepoTargetTest
    extends AbstractNexusIntegrationTest
{
    TargetMessageUtil targetUtil;
    PrivilegesMessageUtil privUtil;
    
    public Nexus3257ModifyRepoTargetTest()
    {
        targetUtil = new TargetMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
        privUtil = new PrivilegesMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
    }
    
    @Test
    public void testChangeTarget()
        throws Exception
    {
        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setContentClass( "maven2" );
        target.setName( "nexus3257-target" );
        target.addPattern( "/some-pattern" );        
        
        target = targetUtil.createTarget( target );
        
        // now add some privs
        PrivilegeResource privReq = new PrivilegeResource();
        privReq.setDescription( "nexus3257-target repo-target privilege" );
        privReq.setMethod( Arrays.asList( "create", "read", "update", "delete" ) );
        privReq.setName( "nexus-3257-priv" );
        privReq.setRepositoryTargetId( target.getId() );
        privReq.setType( TargetPrivilegeDescriptor.TYPE );
        
        List<PrivilegeStatusResource> privs = privUtil.createPrivileges( privReq );
        
        // now make sure the privs exist
        checkPrivs( privs );
        
        // now lets change the target and add a new path
        target.addPattern( "/other-pattern" );
        targetUtil.saveTarget( target, true );
        
        // now make sure the privs still exist
        checkPrivs( privs );
    }
    
    private void checkPrivs( List<PrivilegeStatusResource> privs )
        throws Exception
    {
        for ( PrivilegeStatusResource priv : privs )
        {
            privUtil.getPrivilegeResource( priv.getId() );
        }
    }
}
