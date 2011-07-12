package org.sonatype.nexus.plugins.migration.nexus2501;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.security.rest.model.UserResource;

public class Nexus2501UseRolesAssignIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void checkUsersRoles()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "20090818.120005.zip" ) );
        commitMigration( migrationSummary );

        Assert.assertNotNull( roleUtil.getRole( "exclusive-group" ) );
        Assert.assertNotNull( roleUtil.getRole( "inclusive-group" ) );

        UserResource foobar = userUtil.getUser( "foobar" );
        Assert.assertTrue( foobar.getRoles().contains( "exclusive-group" ) );

        UserResource barfoo = userUtil.getUser( "barfoo" );
        Assert.assertTrue( barfoo.getRoles().contains( "inclusive-group" ) );

    }
}
