package org.sonatype.nexus.plugins.migration.nexus1447;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;

public class Nexus1447ImportWebProxyIT
    extends AbstractMigrationIntegrationTest
{
    @Override
    protected void runOnce()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
    }

    @Test
    public void importProxy()
        throws Exception
    {
        RepositoryProxyResource repo = (RepositoryProxyResource) this.repositoryUtil.getRepository( "nexus1447-repo" );
        RemoteHttpProxySettings proxy = repo.getRemoteStorage().getHttpProxySettings();

        Assert.assertNotNull( "Proxy repository not defined", proxy );
        Assert.assertEquals( "Proxy configuration do no match", "10.10.10.10", proxy.getProxyHostname() );
        Assert.assertEquals( "Proxy configuration do no match", 8080, proxy.getProxyPort() );
        Assert.assertNotNull( "Proxy configuration do no match", proxy.getAuthentication() );
        Assert.assertEquals( "Proxy configuration do no match", "un", proxy.getAuthentication().getUsername() );
        Assert.assertEquals( "Proxy configuration do no match", "|$|N|E|X|U|S|$|", proxy.getAuthentication().getPassword() );
    }

}
