package org.sonatype.nexus.plugins.migration.nxcm254;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.plugins.migration.util.PlexusUserMessageUtil;
import org.sonatype.nexus.rest.model.PlexusRoleResource;
import org.sonatype.nexus.rest.model.PlexusUserResource;

public abstract class AbstractImportSecurityTest
    extends AbstractMigrationIntegrationTest
{

    protected PlexusUserMessageUtil userUtil;

    public AbstractImportSecurityTest()
    {
        userUtil = new PlexusUserMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
    }

    protected abstract File getBackupFile();

    @Test
    public void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getBackupFile() );
        commitMigration( migrationSummary );

        checkUsers();
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

    @SuppressWarnings("unchecked")
    private void checkUser( List<PlexusUserResource> userList, String id, String name, String email, String... roleIds )
    {
        PlexusUserResource user = getUserResourceById( userList, id );

        Assert.assertNotNull( "User with id '" + id + "' does not exist", user );
        Assert.assertEquals( name, user.getName() );
        Assert.assertEquals( email, user.getEmail() );

        for ( String roleId : roleIds )
        {
            Assert.assertTrue( "User with id '" + id + "' does not contain the role '" + roleId + "'", containRole(
                user.getRoles(),
                roleId ) );
        }
    }

    private boolean containRole( List<PlexusRoleResource> roleList, String roleId )
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

    private PlexusUserResource getUserResourceById( List<PlexusUserResource> userList, String id )
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
