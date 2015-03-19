package org.sonatype.nexus.repository.maven.internal

import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.repository.manager.DefaultRepositoriesContributor
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2HostedRecipe
import org.sonatype.nexus.repository.maven.internal.policy.ChecksumPolicy
import org.sonatype.nexus.repository.maven.internal.policy.VersionPolicy


/**
 * Provide default hosted and proxy repositories for Maven.
 * @since 3.0
 */
@Named
@Singleton
class MavenDefaultRepositoriesContributor
    implements DefaultRepositoriesContributor
{
  @Override
  List<Configuration> getRepositoryConfigurations() {
    return [
        new Configuration(repositoryName: 'releases', recipeName: Maven2HostedRecipe.NAME, attributes:
            [
                maven: [
                    versionPolicy              : VersionPolicy.RELEASE.toString(),
                    checksumPolicy             : ChecksumPolicy.STRICT.toString(),
                    strictContentTypeValidation: false
                ]

            ]
        ),
        new Configuration(repositoryName: 'snapshots', recipeName: Maven2HostedRecipe.NAME, attributes:
            [
                maven: [
                    versionPolicy              : VersionPolicy.SNAPSHOT.toString(),
                    checksumPolicy             : ChecksumPolicy.STRICT.toString(),
                    strictContentTypeValidation: false
                ]
            ]
        )
    ]
  }
}
