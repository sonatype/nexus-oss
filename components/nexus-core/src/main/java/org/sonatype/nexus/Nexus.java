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

package org.sonatype.nexus;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.templates.NoSuchTemplateIdException;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

/**
 * The main Nexus application interface.
 *
 * @author Jason van Zyl
 * @author cstamas
 */
public interface Nexus
    extends ApplicationStatusSource
{
  // ------------------------------------------------------------------
  // Configuration

  NexusConfiguration getNexusConfiguration();

  // ----------------------------------------------------------------------------
  // Reposes
  // ----------------------------------------------------------------------------

  StorageItem dereferenceLinkItem(StorageLinkItem item)
      throws NoSuchResourceStoreException, ItemNotFoundException, AccessDeniedException, IllegalOperationException,
             StorageException;

  RepositoryRouter getRootRouter();

  // ----------------------------------------------------------------------------
  // Repo maintenance
  // ----------------------------------------------------------------------------

  /**
   * Delete a user managed repository
   *
   * @see #deleteRepository(String, boolean)
   */
  public void deleteRepository(String id)
      throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException;

  /**
   * Delete a repository, can only delete user managed repository unless force == true
   *
   * @throws AccessDeniedException when try to delete a non-user-managed repository and without force enabled
   */
  public void deleteRepository(String id, boolean force)
      throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException;

  // ----------------------------------------------------------------------------
  // Maintenance
  // ----------------------------------------------------------------------------

  NexusStreamResponse getConfigurationAsStream()
      throws IOException;

  @Deprecated
  void expireAllCaches(ResourceStoreRequest request);

  @Deprecated
  void reindexAllRepositories(String path, boolean fullReindex)
      throws IOException;

  @Deprecated
  void rebuildAttributesAllRepositories(ResourceStoreRequest request)
      throws IOException;

  @Deprecated
  void rebuildMavenMetadataAllRepositories(ResourceStoreRequest request)
      throws IOException;

  @Deprecated
  Collection<String> evictAllUnusedProxiedItems(ResourceStoreRequest request, long timestamp)
      throws IOException;

  @Deprecated
  SnapshotRemovalResult removeSnapshots(SnapshotRemovalRequest request)
      throws NoSuchRepositoryException, IllegalArgumentException;

  /**
   * List the names of files in nexus-work/conf
   */
  Map<String, String> getConfigurationFiles();

  /**
   * Get the content of configuration file based on the key
   *
   * @param key index in configuration file name list
   */
  NexusStreamResponse getConfigurationAsStreamByKey(String key)
      throws IOException;

  // ----------------------------------------------------------------------------
  // Repo templates
  // ----------------------------------------------------------------------------

  TemplateSet getRepositoryTemplates();

  RepositoryTemplate getRepositoryTemplateById(String id)
      throws NoSuchTemplateIdException;

  ;
}
