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

package org.sonatype.nexus.configuration.application;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

public interface MutableConfiguration
{
  // ----------------------------------------------------------------------------------------------------------
  // Security (TODO: this should be removed, security has to be completely "paralell" and not interleaved!)
  // ----------------------------------------------------------------------------------------------------------

  boolean isSecurityEnabled();

  void setSecurityEnabled(boolean enabled)
      throws IOException;

  boolean isAnonymousAccessEnabled();

  /**
   * Configures anonymous access in atomic way.
   *
   * @param enabled  {@code true} to enable and {@code false} to disable it.
   * @param username the username of the user to be used as "anonymous" user. If {@code enabled} parameter is
   *                 {@code true}, this value must be non-null.
   * @param password the password of the user to be used as "anonymous" user. If {@code enabled} parameter is
   *                 {@code true}, this value must be non-null.
   * @throws InvalidConfigurationException if {@code enabled} parameter is {@code true}, but passed in username or
   *                                       password parameters are empty ({@code null} or empty string).
   */
  void setAnonymousAccess(boolean enabled, String username, String password)
      throws InvalidConfigurationException;

  String getAnonymousUsername();

  String getAnonymousPassword();

  /**
   * Set anonymous access.
   *
   * @deprecated Use {@link #setAnonymousAccess(boolean, String, String)} instead.
   */
  @Deprecated
  void setAnonymousAccessEnabled(boolean enabled)
      throws IOException;

  /**
   * Set anonymous username.
   *
   * @deprecated Use {@link #setAnonymousAccess(boolean, String, String)} instead.
   */
  @Deprecated
  void setAnonymousUsername(String val)
      throws InvalidConfigurationException;

  /**
   * Set anonymous password.
   *
   * @deprecated Use {@link #setAnonymousAccess(boolean, String, String)} instead.
   */
  @Deprecated
  void setAnonymousPassword(String val)
      throws InvalidConfigurationException;

  List<String> getRealms();

  void setRealms(List<String> realms)
      throws InvalidConfigurationException;

  // ----------------------------------------------------------------------------
  // Scheduled Tasks
  // ----------------------------------------------------------------------------

  List<ScheduledTaskDescriptor> listScheduledTaskDescriptors();

  ScheduledTaskDescriptor getScheduledTaskDescriptor(String id);

  // ----------------------------------------------------------------------------------------------------------
  // Repositories
  // ----------------------------------------------------------------------------------------------------------

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

  // CRepository: CRUD

  /**
   * Creates a repository live instance out of the passed in model. It validates, registers it with repository
   * registry and puts it into configuration. And finally saves configuration.
   *
   * @return the repository instance.
   */
  Repository createRepository(CRepository settings)
      throws ConfigurationException, IOException;

  /**
   * Removes repository from configuration, checks it's dependants too (ie shadows), updates groups and path mappings
   * (Routes on UI) if needed.
   */
  void deleteRepository(String id)
      throws NoSuchRepositoryException, IOException, ConfigurationException;

  // FIXME: This will be removed: NEXUS-2363 vvvvv
  // CRemoteNexusInstance

  Collection<CRemoteNexusInstance> listRemoteNexusInstances();

  CRemoteNexusInstance readRemoteNexusInstance(String alias)
      throws IOException;

  void createRemoteNexusInstance(CRemoteNexusInstance settings)
      throws IOException;

  void deleteRemoteNexusInstance(String alias)
      throws IOException;
  // FIXME: This will be removed: NEXUS-2363 ^^^^^

}
