/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal

import com.sonatype.nexus.repository.nuget.internal.proxy.NugetGroupRecipe
import com.sonatype.nexus.repository.nuget.internal.proxy.NugetProxyRecipe
import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.repository.manager.DefaultRepositoriesContributor
import org.sonatype.nexus.repository.storage.WritePolicy

import javax.inject.Named
import javax.inject.Singleton

/**
 * Provide default hosted and proxy repositories for NuGet.
 * @since 3.0
 */
@Named
@Singleton
class NugetDefaultRepositoriesContributor
    implements DefaultRepositoriesContributor

{

  static final String DEFAULT_HOSTED_NAME = 'nuget-hosted'

  static final String DEFAULT_PROXIED_NAME = 'nuget.org-proxy'

  static final String DEFAULT_GROUP_NAME = 'nuget-group'


  @Override
  List<Configuration> getRepositoryConfigurations() {
    return [
        new Configuration(repositoryName: DEFAULT_HOSTED_NAME, recipeName: NugetHostedRecipe.NAME, online: true,
            attributes:
                [
                    storage: [
                        writePolicy: WritePolicy.ALLOW.toString()
                    ]
                ]
        ),
        new Configuration(repositoryName: DEFAULT_PROXIED_NAME, recipeName: NugetProxyRecipe.NAME, online: true,
            attributes:
                [
                    proxy: [
                        remoteUrl     : 'http://www.nuget.org/api/v2/',
                        artifactMaxAge: 5
                    ]
                ]
        ),
        new Configuration(repositoryName: DEFAULT_GROUP_NAME, recipeName: NugetGroupRecipe.NAME, online: true,
            attributes:
                [
                    group: [
                        memberNames: [DEFAULT_HOSTED_NAME, DEFAULT_PROXIED_NAME]
                    ]
                ]
        )
    ]
  }
}
