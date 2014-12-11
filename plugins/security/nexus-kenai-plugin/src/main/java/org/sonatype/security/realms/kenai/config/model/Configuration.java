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
package org.sonatype.security.realms.kenai.config.model;

import java.util.Map;

import org.jetbrains.annotations.NonNls;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Kenai configuration.
 *
 * @since 3.0
 */
public class Configuration
{

  @NonNls
  public static final String BASE_URL = "baseUrl";

  @NonNls
  public static final String DEFAULT_ROLE = "defaultRole";

  /**
   * The remote Kenai used for authentication.
   */
  private String baseUrl = "https://java.net/";

  /**
   * A role it assigned to all Kenai Realm users.
   */
  private String defaultRole;

  public Configuration() {}

  public Configuration(final Map<String, String> properties) {
    checkNotNull(properties);
    this.baseUrl = properties.get(BASE_URL);
    this.defaultRole = properties.get(DEFAULT_ROLE);
  }

  /**
   * Get the remote Kenai used for authentication.
   */
  public String getBaseUrl() {
    return this.baseUrl;
  }

  /**
   * Get a role it assigned to all Kenai Realm users.
   */
  public String getDefaultRole() {
    return this.defaultRole;
  }

  /**
   * Set the remote Kenai used for authentication.
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Set a role it assigned to all Kenai Realm users.
   */
  public void setDefaultRole(String defaultRole) {
    this.defaultRole = defaultRole;
  }

}
