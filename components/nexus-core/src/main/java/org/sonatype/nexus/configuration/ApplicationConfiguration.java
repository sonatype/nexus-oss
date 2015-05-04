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
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.ApplicationDirectories;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

/**
 * ApplicationConfiguration is the main component to have and maintain configuration.
 */
public interface ApplicationConfiguration
{
  /**
   * Gets the working directory as file. The directory is created if needed and is guaranteed to exists.
   *
   * @deprecated Use {@link ApplicationDirectories}
   */
  @Deprecated
  File getWorkingDirectory();

  /**
   * Gets the working directory with some subpath. The directory is created and is guaranteed to exists.
   *
   * @deprecated Use {@link ApplicationDirectories}
   */
  @Deprecated
  File getWorkingDirectory(String key);

  /**
   * Returns the configuration directory. It defaults to $NEXUS_WORK/etc.
   */
  File getConfigurationDirectory();

  /**
   * Gets the top level local storage context.
   */
  LocalStorageContext getGlobalLocalStorageContext();

  /**
   * Gets the top level remote storage context.
   *
   * @deprecated Use {@code @Named("global") Provider<RemoteStorageContext>} instead.
   */
  @Deprecated
  RemoteStorageContext getGlobalRemoteStorageContext();

  /**
   * Saves the configuration.
   */
  void saveConfiguration() throws IOException;

  /**
   * Gets the Configuration object.
   *
   * @deprecated you should use setters/getters directly on Configurable instances, and not tampering with
   *             Configuration model directly!
   */
  @Deprecated
  Configuration getConfigurationModel();

  // FIXME: Only used by tests
  void loadConfiguration() throws IOException;

  /**
   * Explicit loading of configuration. Enables to force reloading of config.
   */
  void loadConfiguration(boolean forceReload) throws IOException;

  /**
   * Creates internals like reposes configured in nexus.xml. Called on startup.
   */
  void createInternals();

  /**
   * Cleanups the internals, like on shutdown.
   */
  void dropInternals();

  /**
   * Sets the default (applied to all that has no exceptions set with {
   * {@link #setRepositoryMaxInstanceCount(RepositoryTypeDescriptor, int)} method) maxInstanceCount. Any positive
   * integer limits the max count of live instances, any less then 0 integer removes the limitation. Note: setting
   * limitations on already booted instance will not "enforce" the limitation!
   */
  void setDefaultRepositoryMaxInstanceCount(int count);

  /**
   * Limits the maxInstanceCount for the passed in repository type. Any positive integer limits the max count of live
   * instances, any less then 0 integer removes the limitation. Note: setting limitations on already booted instance
   * will not "enforce" the limitation!
   */
  void setRepositoryMaxInstanceCount(RepositoryTypeDescriptor rtd, int count);

  /**
   * Returns the count limit for the passed in repository type.
   */
  int getRepositoryMaxInstanceCount(RepositoryTypeDescriptor rtd);

  /**
   * Creates a repository live instance out of the passed in model. It validates, registers it with repository
   * registry and puts it into configuration. And finally saves configuration.
   *
   * @return the repository instance.
   */
  Repository createRepository(CRepository settings) throws IOException;

  /**
   * Drops a user managed repository.
   *
   * @see #deleteRepository(String, boolean)
   */
  void deleteRepository(String id)
      throws NoSuchRepositoryException, IOException, AccessDeniedException;

  /**
   * Drops a repository, can only delete user managed repository unless force parameter is {@code true}.
   *
   * @throws AccessDeniedException when try to delete a non-user-managed repository and without force enabled
   */
  void deleteRepository(String id, boolean force)
      throws NoSuchRepositoryException, IOException, AccessDeniedException;
}
