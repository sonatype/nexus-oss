package org.sonatype.nexus.integrationtests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Before;
import org.restlet.data.MediaType;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.integrationtests.nexus233.PrivilegesMessageUtil;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.RoutesMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractPrivilegeTest
    extends AbstractNexusIntegrationTest
{

    protected UserMessageUtil userUtil;

    protected RoleMessageUtil roleUtil;

    protected PrivilegesMessageUtil privUtil;

    protected TargetMessageUtil targetUtil;

    protected RoutesMessageUtil routeUtil;

    public AbstractPrivilegeTest()
    {
        this.userUtil = new UserMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
        this.roleUtil = new RoleMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
        this.privUtil =
            new PrivilegesMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
        this.targetUtil = new TargetMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        this.routeUtil = new RoutesMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
    }

    @Before
    public void resetTestUserPrivs()
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        UserResource testUser = this.userUtil.getUser( "test-user" );
        testUser.getRoles().clear();
        testUser.addRole( "anonymous" );
        this.userUtil.updateUser( testUser );
    }

    protected void printUserPrivs( String userId )
        throws IOException
    {
        UserResource user = this.userUtil.getUser( userId );
        ArrayList<String> privs = new ArrayList<String>();

        for ( Iterator iter = user.getRoles().iterator(); iter.hasNext(); )
        {
            String roleId = (String) iter.next();
            RoleResource role = this.roleUtil.getRole( roleId );

            for ( Iterator roleIter = role.getPrivileges().iterator(); roleIter.hasNext(); )
            {
                String privId = (String) roleIter.next();
                // PrivilegeBaseStatusResource priv = this.privUtil.getPrivilegeResource( privId );
                // privs.add( priv.getName() );
                CApplicationPrivilege appPriv = SecurityConfigUtil.getCApplicationPrivilege( privId );
                if ( appPriv != null )
                {
                    privs.add( appPriv.getName() );
                }
                else
                {
                    CRepoTargetPrivilege cpriv = SecurityConfigUtil.getCRepoTargetPrivilege( privId );
                    privs.add( cpriv.getName() );
                }

            }
        }

        System.out.println( "User: " + userId );
        for ( Iterator iter = privs.iterator(); iter.hasNext(); )
        {
            String privName = (String) iter.next();
            System.out.println( "\t" + privName );
        }
    }

    protected void giveUserPrivilege( String userId, String priv )
        throws IOException
    {
        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        RoleResource role = new RoleResource();
        role.setDescription( priv +" Role" );
        role.setName( priv +"Role" );
        role.setSessionTimeout( 60 );
        role.addPrivilege( priv );
        // save it
        role = this.roleUtil.createRole( role );

        // add it
        UserResource testUser = this.userUtil.getUser( userId );
        testUser.addRole( role.getId() );
        this.userUtil.updateUser( testUser );
    }

}
