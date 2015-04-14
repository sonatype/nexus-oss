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
import com.orientechnologies.orient.core.id.ORecordId
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.common.entity.EntityId
import org.sonatype.nexus.common.validation.Validate
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.manager.RepositoryManager
import org.sonatype.nexus.repository.storage.Asset
import org.sonatype.nexus.repository.storage.Component
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.StorageTx
import org.sonatype.nexus.repository.view.ViewFacet

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.constraints.NotNull

import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME
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

  @Inject
  RepositoryManager repositoryManager

  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  List<AssetXO> readAssets(final StoreLoadParameters parameters) {
    return readAssets(parameters.getFilter('repositoryName'), parameters.getFilter('componentId'))
  }

  @Validate
  List<AssetXO> readAssets(final @NotNull(message = '[repositoryName] may not be null') String repositoryName,
                           final @NotNull(message = '[componentId] may not be null') String componentId)
  {
    Repository repository = repositoryManager.get(repositoryName)
    if (!repository.facet(ViewFacet).online) {
      return null
    }
    StorageTx storageTx = repository.facet(StorageFacet).openTx()
    try {
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
    finally {
      storageTx.close()
    }
  }

}
