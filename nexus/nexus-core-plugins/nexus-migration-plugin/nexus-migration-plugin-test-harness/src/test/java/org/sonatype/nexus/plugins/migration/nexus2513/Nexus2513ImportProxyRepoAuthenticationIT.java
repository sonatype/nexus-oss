package org.sonatype.nexus.plugins.migration.nexus2513;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.AuthenticationSettings;
import org.sonatype.nexus.rest.model.RepositoryResource;

public class Nexus2513ImportProxyRepoAuthenticationIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void proxyRepoAuthentication()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "20090819.113329.zip" ) );
        commitMigration( migrationSummary );

        RepositoryResource sss = (RepositoryResource) repositoryUtil.getRepository( "sss-releases" );
        validateAuthentication( sss );

        sss = (RepositoryResource) repositoryUtil.getRepository( "sss-snapshots" );
        validateAuthentication( sss );
    }

    private void validateAuthentication( RepositoryResource sss )
    {
        Assert.assertNotNull( sss.getRemoteStorage() );
        AuthenticationSettings auth = sss.getRemoteStorage().getAuthentication();
        Assert.assertNotNull( auth );

        Assert.assertEquals( "rseddon", auth.getUsername() );
        // TODO is it possible to validate the PW? "12Hola.."
        Assert.assertEquals( "|$|N|E|X|U|S|$|", auth.getPassword() );
    }
}
