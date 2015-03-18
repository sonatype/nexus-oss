package com.sonatype.nexus.repository.nuget.internal

import javax.inject.Named

import com.sonatype.nexus.repository.nuget.internal.proxy.NugetProxyRecipe

import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.repository.manager.InitialRepositoryConfiguration

/**
 * Provide default hosted and proxy repositories for NuGet.
 * @since 3.0
 */
@Named
class NugetInitialRepositoryConfiguration
    implements InitialRepositoryConfiguration

{
  @Override
  List<Configuration> getRepositoryConfigurations() {
    return [
        new Configuration(repositoryName: 'nuget-hosted', recipeName: NugetHostedRecipe.NAME, attributes: [:]),
        new Configuration(repositoryName: 'nuget.org-proxy', recipeName: NugetProxyRecipe.NAME, attributes:
            [
                proxy     : [
                    remoteUrl     : 'http://www.nuget.org/api/v2/',
                    artifactMaxAge: 5
                ],
                httpclient: [
                    connection: [
                        timeout: 20000,
                        retries: 2
                    ]
                ]
            ]
        )
    ]
  }
}
