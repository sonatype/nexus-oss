/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.obr.shadow;

import java.util.Collection;

import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.obr.ObrContentClass;
import org.sonatype.nexus.obr.metadata.ObrMetadataSource;
import org.sonatype.nexus.obr.util.ObrUtils;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.IncompatibleMasterRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.osgi.service.obr.Resource;

@Component(role = ShadowRepository.class, hint = ObrShadowRepository.ROLE_HINT, instantiationStrategy = "per-lookup",
    description = "OBR")
public class ObrShadowRepository
    extends AbstractShadowRepository
    implements ShadowRepository
{
  public static final String ROLE_HINT = "obr-shadow";

  @Requirement(hint = ObrContentClass.ID)
  private ContentClass obrContentClass;

  @Requirement
  private ObrShadowRepositoryConfigurator obrShadowRepositoryConfigurator;

  private final RepositoryKind obrShadowRepositoryKind = new DefaultRepositoryKind(ShadowRepository.class, null);

  private ContentClass masterContentClass;

  @Requirement(hint = "obr-bindex")
  private ObrMetadataSource obrMetadataSource;

  public ContentClass getRepositoryContentClass() {
    return obrContentClass;
  }

  @Override
  protected Configurator getConfigurator() {
    return obrShadowRepositoryConfigurator;
  }

  public RepositoryKind getRepositoryKind() {
    return obrShadowRepositoryKind;
  }

  @Override
  protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory() {
    return new CRepositoryExternalConfigurationHolderFactory<ObrShadowRepositoryConfiguration>()
    {
      public ObrShadowRepositoryConfiguration createExternalConfigurationHolder(final CRepository config) {
        return new ObrShadowRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
      }
    };
  }

  @Override
  public void setMasterRepository(final Repository masterRepository)
      throws IncompatibleMasterRepositoryException
  {
    // OBR isn't choosy, we can accept any sort of master content
    masterContentClass = masterRepository.getRepositoryContentClass();

    super.setMasterRepository(masterRepository);
  }

  public ContentClass getMasterRepositoryContentClass() {
    return masterContentClass;
  }

  @Override
  public Collection<StorageItem> list(final boolean fromTask, final StorageCollectionItem item)
      throws IllegalOperationException, ItemNotFoundException, StorageException
  {
    return ObrUtils.augmentListedItems(item.getRepositoryItemUid(), super.list(fromTask, item));
  }

  @Override
  protected StorageLinkItem createLink(final StorageItem item)
      throws StorageException
  {
    return updateLink(item.getRepositoryItemUid(), true);
  }

  @Override
  protected void deleteLink(final StorageItem item)
      throws StorageException
  {
    updateLink(item.getRepositoryItemUid(), false);
  }

  /**
   * Update the shadow OBR to reflect whether the affected item has been created or deleted.
   *
   * @param uid    the affected item UID
   * @param adding true when adding/updating, false when removing
   */
  private StorageLinkItem updateLink(final RepositoryItemUid uid, final boolean adding)
      throws StorageException
  {
    final Resource resource = obrMetadataSource.buildResource(ObrUtils.getCachedItem(uid));
    if (resource != null) {
      ObrUtils.updateObr(obrMetadataSource, ObrUtils.createObrUid(this), resource, adding);
    }

    return null;
  }

  @Override
  public void synchronizeWithMaster() {
    try {
      ObrUtils.buildObr(obrMetadataSource, ObrUtils.createObrUid(this), getMasterRepository(), getWalker());
    }
    catch (final StorageException e) {
      getLogger().warn("Problem rebuilding OBR metadata for repository " + getId(), e);
    }
  }

  @Override
  protected StorageItem doRetrieveItem(final ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, StorageException
  {
    try {
      // treat expired items just like not found items
      final StorageItem item = super.doRetrieveItem(request);
      if (!item.isExpired()) {
        return item;
      }
    }
    catch (final ItemNotFoundException e) {
      // drop through...
    }

    // forcibly generate missing OBR metadata
    if (ObrUtils.isObrMetadataRequest(request)) {
      synchronizeWithMaster();

      return super.doRetrieveItem(request);
    }

    // re-route request to the master repository
    return doRetrieveItemFromMaster(request);
  }
}
