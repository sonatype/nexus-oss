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
package com.sonatype.nexus.ssl.plugin.internal.repository;

import java.util.Map;

import org.sonatype.nexus.capability.support.CapabilityConfigurationSupport;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configuration adapter for {@link RepositoryCapability}.
 *
 * @since ssl 1.0
 */
public class RepositoryCapabilityConfiguration
    extends CapabilityConfigurationSupport
{

  public static final String REPOSITORY_ID = "repository";

  private String repositoryId;

  public RepositoryCapabilityConfiguration() {
    super();
  }

  public RepositoryCapabilityConfiguration(final Map<String, String> properties) {
    checkNotNull(properties);
    this.repositoryId = properties.get(REPOSITORY_ID);
  }

  public String getRepositoryId() {
    return repositoryId;
  }

  public RepositoryCapabilityConfiguration withRepositoryId(final String repositoryId) {
    this.repositoryId = repositoryId;
    return this;
  }

  public Map<String, String> asMap() {
    final Map<String, String> props = Maps.newHashMap();
    props.put(REPOSITORY_ID, repositoryId);
    return props;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "repositoryId='" + repositoryId +
        '}';
  }
}
