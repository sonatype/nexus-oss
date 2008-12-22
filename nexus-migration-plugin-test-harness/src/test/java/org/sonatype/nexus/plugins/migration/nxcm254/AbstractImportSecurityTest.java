package org.sonatype.nexus.plugins.migration.nxcm254;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.plugins.migration.util.PlexusUserMessageUtil;
import org.sonatype.nexus.rest.model.PlexusRoleResource;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;

public abstract class AbstractImportSecurityTest
    extends AbstractMigrationIntegrationTest
{

    protected PlexusUserMessageUtil userUtil;

    protected TargetMessageUtil repoTargetUtil;

    protected RoleMessageUtil roleUtil;

    protected PrivilegesMessageUtil privilegeUtil;

    /**
     * System user, role, privilege, repoTarget, before importing artifactory
     */
    protected List<PrivilegeBaseStatusResource> prePrivilegeList;

    protected List<RoleResource> preRoleList;

    protected List<PlexusUserResource> preUserList;

    protected List<RepositoryTargetListResource> preTargetList;

    abstract protected void importSecurity()
        throws Exception;

    abstract protected void verifySecurity()
        throws Exception;

    @Test
    public void testImportSecurity()
        throws Exception
    {
        loadPreResources();

        importSecurity();

        verifySecurity();
    }

    @SuppressWarnings( "static-access" )
    protected void loadPreResources()
        throws Exception
    {
        // load PREs
        preUserList = userUtil.getList();
        prePrivilegeList = privilegeUtil
            .getResourceListFromResponse( privilegeUtil.sendMessage( Method.GET, null, "" ) );
        preRoleList = roleUtil.getList();
        preTargetList = repoTargetUtil.getList();
    }

    public AbstractImportSecurityTest()
        throws Exception
    {
        // initialize the utils
        userUtil = new PlexusUserMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
        repoTargetUtil = new TargetMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
        privilegeUtil = new PrivilegesMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
        roleUtil = new RoleMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );

    }

    @SuppressWarnings( "static-access" )
    protected List<RepositoryTargetListResource> getImportedRepoTargetList()
        throws Exception
    {
        List<RepositoryTargetListResource> targetList = repoTargetUtil.getList();

        List<RepositoryTargetListResource> addedList = new ArrayList<RepositoryTargetListResource>();

        for ( RepositoryTargetListResource target : targetList )
        {
            if ( !containRepoTarget( preTargetList, target.getId() ) )
            {
                addedList.add( target );
            }
        }
        return addedList;
    }

    protected List<RoleResource> getImportedRoleList()
        throws Exception
    {
        List<RoleResource> roleList = roleUtil.getList();

        List<RoleResource> addedList = new ArrayList<RoleResource>();

        for ( RoleResource role : roleList )
        {
            if ( !containRole( preRoleList, role.getId() ) )
            {
                addedList.add( role );
            }
        }
        return addedList;
    }

    protected List<PrivilegeBaseStatusResource> getImportedPrivilegeList()
        throws Exception
    {
        List<PrivilegeBaseStatusResource> privilegeList = privilegeUtil.getResourceListFromResponse( privilegeUtil
            .sendMessage( Method.GET, null, "" ) );

        List<PrivilegeBaseStatusResource> addedList = new ArrayList<PrivilegeBaseStatusResource>();

        for ( PrivilegeBaseStatusResource privilege : privilegeList )
        {
            if ( !containPrivilege( prePrivilegeList, privilege.getId() ) )
            {
                addedList.add( privilege );
            }
        }
        return addedList;
    }

    protected List<PlexusUserResource> getImportedUserList()
        throws Exception
    {
        List<PlexusUserResource> userList = userUtil.getList();

        List<PlexusUserResource> addedList = new ArrayList<PlexusUserResource>();

        for ( PlexusUserResource user : userList )
        {
            if ( !containUser( preUserList, user.getUserId() ) )
            {
                addedList.add( user );
            }
        }
        return addedList;
    }

    private void checkUsers()
        throws Exception
    {
        List<PlexusUserResource> userList = userUtil.getList();

        // artifactory admin is conflicted with nexus admin, so a suffix should be added
        checkUser( userList, "admin-artifactory", "admin-artifactory", DEFAULT_EMAIL, "admin" );
        // others keep unchanged
        checkUser( userList, "admin1", "admin1", DEFAULT_EMAIL, "admin" );
        checkUser( userList, "user", "user", DEFAULT_EMAIL );
        checkUser( userList, "user1", "user1", DEFAULT_EMAIL );
    }

    protected boolean containRepoTarget( List<RepositoryTargetListResource> repoTargetList, String repoTargetId )
    {
        for ( RepositoryTargetListResource target : repoTargetList )
        {
            if ( target.getId().equals( repoTargetId ) )
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings( "unchecked" )
    private void checkUser( List<PlexusUserResource> userList, String id, String name, String email, String... roleIds )
    {
        PlexusUserResource user = getUserById( userList, id );

        Assert.assertNotNull( "User with id '" + id + "' does not exist", user );
        Assert.assertEquals( name, user.getName() );
        Assert.assertEquals( email, user.getEmail() );

        for ( String roleId : roleIds )
        {
            Assert.assertTrue(
                "User with id '" + id + "' does not contain the role '" + roleId + "'",
                containPlexusRole( user.getRoles(), roleId ) );
        }
    }

    protected boolean containRole( List<RoleResource> roleList, String roleId )
    {
        for ( RoleResource role : roleList )
        {
            if ( role.getId().equals( roleId ) )
            {
                return true;
            }
        }
        return false;
    }
    
/*    protected boolean containRole( List<String> roleList, String roleId )
    {
        for ( String role : roleList )
        {
            if ( role.equals( roleId ) )
            {
                return true;
            }
        }

        return false;
    }*/

    protected boolean containPlexusRole( List<PlexusRoleResource> roleList, String roleId )
    {
        for ( PlexusRoleResource role : roleList )
        {
            if ( role.getRoleId().equals( roleId ) )
            {
                return true;
            }
        }
        return false;
    }

    protected boolean containUser( List<PlexusUserResource> userList, String userId )
    {
        for ( PlexusUserResource user : userList )
        {
            if ( user.getUserId().equals( userId ) )
            {
                return true;
            }
        }
        return false;
    }

    protected boolean containPrivilege( List<PrivilegeBaseStatusResource> privList, String privId )
    {
        for ( PrivilegeBaseStatusResource priv : privList )
        {
            if ( priv.getId().equals( privId ) )
            {
                return true;
            }
        }
        return false;
    }

    protected boolean containPrivilegeName( List<PrivilegeBaseStatusResource> privList, String privName )
    {
        for ( PrivilegeBaseStatusResource priv : privList )
        {
            if ( priv.getName().equals( privName ) )
            {
                return true;
            }
        }
        return false;
    }

    protected PlexusUserResource getUserById( List<PlexusUserResource> userList, String id )
    {
        for ( PlexusUserResource user : userList )
        {
            if ( user.getUserId().equals( id ) )
            {
                return user;
            }
        }
        return null;
    }

}
