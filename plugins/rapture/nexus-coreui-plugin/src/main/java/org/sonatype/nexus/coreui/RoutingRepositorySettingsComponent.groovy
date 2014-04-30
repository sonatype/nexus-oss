/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.coreui

import com.google.common.primitives.Ints
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.bval.guice.Validate
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.proxy.maven.MavenProxyRepository
import org.sonatype.nexus.proxy.maven.MavenRepository
import org.sonatype.nexus.proxy.maven.routing.*
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.repository.Repository
import org.sonatype.nexus.rest.RepositoryURLBuilder

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull
import java.util.concurrent.TimeUnit

/**
 * Routing Repository Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_RoutingRepositorySettings')
class RoutingRepositorySettingsComponent
extends DirectComponentSupport
{

  @Inject
  Manager manager

  @Inject
  @Named('protected')
  RepositoryRegistry repositoryRegistry

  @Inject
  RepositoryURLBuilder repositoryURLBuilder

  /**
   * Retrieve routing repository settings.
   * @return routing repository settings
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  @Validate
  RoutingRepositorySettingsXO read(final @NotNull(message = '[repositoryId] may not be null') String repositoryId) {
    MavenRepository mavenRepository = getMavenRepository(repositoryId, MavenRepository.class)
    RoutingStatus status = manager.getStatusFor(mavenRepository)
    PublishingStatus pstatus = status.publishingStatus
    DiscoveryStatus dstatus = status.discoveryStatus

    RoutingRepositorySettingsXO repositorySettingsXO = new RoutingRepositorySettingsXO(
        repositoryId: mavenRepository.id
    )

    if (PublishingStatus.PStatus.PUBLISHED == pstatus.status) {
      repositorySettingsXO.publishStatus = 'Published.'
    }
    else if (PublishingStatus.PStatus.NOT_PUBLISHED == pstatus.status) {
      repositorySettingsXO.publishStatus = 'Not published.'
    }
    else {
      repositorySettingsXO.publishStatus = 'Unknown.'
    }

    repositorySettingsXO.publishMessage = pstatus.lastPublishedMessage
    if (PublishingStatus.PStatus.PUBLISHED == pstatus.status) {
      repositorySettingsXO.publishTimestamp = pstatus.lastPublishedTimestamp
      if (mavenRepository.exposed && pstatus.lastPublishedFilePath) {
        String repositoryUrl = repositoryURLBuilder.getExposedRepositoryContentUrl(mavenRepository)
        if (repositoryUrl) {
          repositorySettingsXO.publishUrl = repositoryUrl + pstatus.lastPublishedFilePath
        }
      }
    }

    if (dstatus.status.enabled) {
      MavenProxyRepository mavenProxyRepository = getMavenRepository(repositoryId, MavenProxyRepository.class)
      DiscoveryConfig config = manager.getRemoteDiscoveryConfig(mavenProxyRepository)
      repositorySettingsXO.discoveryEnabled = config.enabled
      repositorySettingsXO.discoveryInterval = Ints.saturatedCast(TimeUnit.MILLISECONDS.toHours(config.discoveryInterval))

      if (DiscoveryStatus.DStatus.ENABLED_IN_PROGRESS == dstatus.status) {
        repositorySettingsXO.discoveryStatus = 'In progress.'
      }
      else {
        repositorySettingsXO.discoveryStatus = DiscoveryStatus.DStatus.SUCCESSFUL == dstatus.status ? 'Successful.' : 'Unsuccessful.'
        repositorySettingsXO.discoveryMessage = dstatus.lastDiscoveryMessage
        repositorySettingsXO.discoveryTimestamp = dstatus.lastDiscoveryTimestamp
      }
    }

    return repositorySettingsXO
  }

  /**
   * Updates routing repository settings.
   * @return updated routing repository settings
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  @Validate
  RoutingRepositorySettingsXO update(final @NotNull(message = '[repositorySettingsXO] may not be null') @Valid RoutingRepositorySettingsXO repositorySettingsXO) {
    MavenProxyRepository mavenProxyRepository = getMavenRepository(repositorySettingsXO.repositoryId, MavenProxyRepository.class)
    manager.setRemoteDiscoveryConfig(mavenProxyRepository, new DiscoveryConfig(
        repositorySettingsXO.discoveryEnabled,
        TimeUnit.HOURS.toMillis(Math.max(repositorySettingsXO.discoveryInterval ?: 24, 0))
    ))
    return read(repositorySettingsXO.repositoryId)
  }

  /**
   * Force updates prefix list for given repository.
   * @return updated routing repository settings
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:repositories:update')
  @Validate
  RoutingRepositorySettingsXO updatePrefixFile(final @NotNull(message = '[repositoryId] may not be null') String repositoryId) {
    MavenRepository mavenRepository = getMavenRepository(repositoryId, MavenRepository.class)
    manager.updatePrefixFile(mavenRepository)
    return read(repositoryId)
  }

  private <T extends MavenRepository> T getMavenRepository(final String repositoryId, final Class<T> clazz) {
    Repository repository = repositoryRegistry.getRepository(repositoryId)
    T mavenRepository = repository.adaptToFacet(clazz)
    if (mavenRepository != null) {
      if (!manager.isMavenRepositorySupported(mavenRepository)) {
        throw new Exception("Repository with ID='${repositoryId}' unsupported!")
      }
      return mavenRepository
    }
    else {
      throw new Exception("Repository with ID='${repositoryId}' is not a required type of ${clazz.simpleName}.")
    }
  }

}
