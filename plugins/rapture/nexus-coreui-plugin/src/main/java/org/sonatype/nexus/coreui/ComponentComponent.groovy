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
package org.sonatype.nexus.coreui

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.constraints.NotNull

import org.sonatype.nexus.common.entity.EntityId
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.PagedResponse
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.repository.MissingFacetException
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.group.GroupFacet
import org.sonatype.nexus.repository.manager.RepositoryManager
import org.sonatype.nexus.repository.security.RepositoryViewPermission
import org.sonatype.nexus.repository.storage.Component
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.StorageTx
import org.sonatype.nexus.security.SecurityHelper
import org.sonatype.nexus.validation.Validate

import com.google.common.collect.ImmutableList
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod

import static org.sonatype.nexus.repository.security.BreadActions.READ

/**
 * Component {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Component')
class ComponentComponent
    extends DirectComponentSupport
{
  private static String UNATTACHED = "unattached";

  @Inject
  SecurityHelper securityHelper

  @Inject
  RepositoryManager repositoryManager

  @DirectMethod
  PagedResponse<ComponentXO> read(final StoreLoadParameters parameters) {
    Repository repository = repositoryManager.get(parameters.getFilter('repositoryName'))
    securityHelper.ensurePermitted(new RepositoryViewPermission(repository, READ))

    if (!repository.configuration.online) {
      return null
    }

    def sort = parameters.sort?.get(0)
    def querySuffix = ''
    if (sort) {
      querySuffix += " ORDER BY ${sort.property} ${sort.direction}"
      if (sort.property == StorageFacet.P_GROUP) {
        querySuffix += ", ${StorageFacet.P_NAME} ASC,${StorageFacet.P_VERSION} ASC"
      }
      else if (sort.property == StorageFacet.P_NAME) {
        querySuffix += ", ${StorageFacet.P_VERSION} ASC,${StorageFacet.P_GROUP} ASC"
      }
    }
    if (parameters.start) {
      querySuffix += " SKIP ${parameters.start}"
    }
    if (parameters.limit) {
      querySuffix += " LIMIT ${parameters.limit}"
    }

    StorageTx storageTx = repository.facet(StorageFacet).openTx()
    try {
      def repositories
      try {
        repositories = repository.facet(GroupFacet).leafMembers()
        querySuffix = " GROUP BY ${StorageFacet.P_GROUP},${StorageFacet.P_NAME},${StorageFacet.P_VERSION}" + querySuffix
      }
      catch (MissingFacetException e) {
        repositories = ImmutableList.of(repository)
      }

      List<ComponentXO> results = storageTx.findComponents(null, null, repositories, querySuffix).collect { component ->
        new ComponentXO(
            id: component.entityMetadata.id,
            repositoryName: repository.name,
            group: component.group(),
            name: component.name(),
            version: component.version(),
            format: component.format()
        )
      }
      if (parameters.start == 0 && readUnattachedAssets(storageTx, repositories).size()) {
        results = new ArrayList<>(results)
        results.add(0, new ComponentXO(
            id: UNATTACHED,
            repositoryName: repository.name
        ))
      }
      return new PagedResponse<ComponentXO>(
          (results.size() < parameters.limit ? 0 : parameters.limit) + results.size() + parameters.start,
          results
      )
    }
    finally {
      storageTx.close()
    }
  }

  @DirectMethod
  List<AssetXO> readAssets(final StoreLoadParameters parameters) {
    return readAssets(parameters.getFilter('repositoryName'), parameters.getFilter('componentId'))
  }

  @Validate
  List<AssetXO> readAssets(final @NotNull String repositoryName, final @NotNull String componentId) {
    Repository repository = repositoryManager.get(repositoryName)
    securityHelper.ensurePermitted(new RepositoryViewPermission(repository, READ))

    if (!repository.configuration.online) {
      return null
    }
    StorageTx storageTx = repository.facet(StorageFacet).openTx()
    try {
      if (componentId == UNATTACHED) {
        return readUnattachedAssets(storageTx, ImmutableList.of(repository))
      }
      else {
        Component component = storageTx.findComponent(new EntityId(componentId), storageTx.getBucket())
        if (component == null) {
          return null
        }

        return storageTx.browseAssets(component).collect { asset ->
          new AssetXO(
              id: asset.entityMetadata.id,
              name: asset.name() ?: component.name(),
              contentType: asset.contentType()
          )
        }
      }
    }
    finally {
      storageTx.close()
    }
  }

  private List<AssetXO> readUnattachedAssets(final StorageTx storageTx, final Iterable<Repository> repositories) {
    return storageTx.findAssets("${StorageFacet.P_COMPONENT} IS NULL", null, repositories, null).collect { asset ->
      new AssetXO(
          id: asset.entityMetadata.id,
          name: asset.name(),
          contentType: asset.contentType()
      )
    }
  }
}
