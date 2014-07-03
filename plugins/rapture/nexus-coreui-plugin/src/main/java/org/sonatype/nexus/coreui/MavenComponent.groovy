/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.guice.Validate
import org.sonatype.nexus.maven.tasks.RebuildMavenMetadataTask
import org.sonatype.nexus.proxy.maven.MavenRepository
import org.sonatype.nexus.proxy.maven.gav.Gav
import org.sonatype.nexus.proxy.maven.gav.GavCalculator
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.scheduling.NexusScheduler

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Maven {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Maven')
class MavenComponent
extends DirectComponentSupport
{

  @Inject
  @Named("protected") RepositoryRegistry protectedRepositoryRegistry

  @Inject
  NexusScheduler nexusScheduler

  /**
   * Retrieves Maven related information.
   *
   * @param repositoryId containing the artifact
   * @param path of artifact
   * @return Maven information or null if repository / path does not point to a Maven artifact.
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  @Validate
  RepositoryStorageItemMavenInfoXO readInfo(final @NotEmpty(message = '[repositoryId] may not be empty') String repositoryId,
                                            final @NotEmpty(message = '[path] may not be empty') String path)
  {
    def repository = protectedRepositoryRegistry.getRepository(repositoryId)
    if (!repository.repositoryKind.isFacetAvailable(MavenRepository)) {
      return null
    }
    GavCalculator gavCalculator = repository.adaptToFacet(MavenRepository).gavCalculator
    Gav gav = gavCalculator.pathToGav(path)
    if (!gav || gav.signature || gav.hash) {
      return null
    }
    return new RepositoryStorageItemMavenInfoXO(
        repositoryId: repositoryId,
        groupId: gav.groupId,
        artifactId: gav.artifactId,
        baseVersion: gav.baseVersion,
        version: gav.version,
        extension: gav.extension,
        classifier: gav.classifier,
        xml: gav.with {
          def xml = '<dependency>\n' +
              "  <groupId>${gav.groupId}</groupId>\n" +
              "  <artifactId>${gav.artifactId}</artifactId>\n" +
              "  <version>${gav.baseVersion}</version>\n"
          if (gav.classifier) {
            xml += "  <classifier>${gav.classifier}</classifier>\n"
          }
          if (gav.extension && gav.extension != 'jar') {
            xml += "  <type>${gav.extension}</type>\n"
          }
          xml += '</dependency>'
          return xml
        }
    )
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:metadata:delete')
  @Validate
  void rebuildMetadata(final @NotEmpty(message = '[id] may not be empty') String id,
                       final String path)
  {
    // validate repository id
    protectedRepositoryRegistry.getRepository(id)
    RebuildMavenMetadataTask task = nexusScheduler.createTaskInstance(RebuildMavenMetadataTask)
    task.setRepositoryId(id)
    task.setResourceStorePath(path)
    nexusScheduler.submit("Rebuild metadata ${id}:${path}", task)
  }

}
