package org.sonatype.nexus.plugin.migration.artifactory.config.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryVirtualRepository;
import org.sonatype.nexus.plugin.migration.artifactory.util.VirtualRepositoryUtil;

public class ResolveVirtualRepositoriesTest
{

    @Test
    public void resolveVirtualRepos()
        throws Exception
    {
        InputStream input = getClass().getResourceAsStream( "/config-virtual-repos.xml" );
        ArtifactoryConfig config = ArtifactoryConfig.read( input );

        Map<String, ArtifactoryVirtualRepository> virtualRepos = config.getVirtualRepositories();
        VirtualRepositoryUtil.resolveRepositories( virtualRepos );

        List<String> resolvedRemoteRepos = virtualRepos.get( "remote-repos" ).getResolvedRepositories();
        assertNotNull( resolvedRemoteRepos );
        assertEquals( 3, resolvedRemoteRepos.size() );
        assertTrue( resolvedRemoteRepos.contains( "java.net.m2" ) );
        assertTrue( resolvedRemoteRepos.contains( "java.net.m1" ) );
        assertTrue( resolvedRemoteRepos.contains( "repo1" ) );

        List<String> resolvedSnapshots = virtualRepos.get( "plugins-snapshots" ).getResolvedRepositories();
        assertNotNull( resolvedSnapshots );
        assertEquals( 5, resolvedSnapshots.size() );
        assertTrue( resolvedSnapshots.contains( "java.net.m2" ) );
        assertTrue( resolvedSnapshots.contains( "java.net.m1" ) );
        assertTrue( resolvedSnapshots.contains( "repo1" ) );
        assertTrue( resolvedSnapshots.contains( "plugins-snapshots-local" ) );
        assertTrue( resolvedSnapshots.contains( "ext-snapshots-local" ) );

    }
}
