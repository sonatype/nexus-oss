/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.kenai;

import java.util.Map;

import org.sonatype.nexus.capability.UniquePerCapabilityType;
import org.sonatype.nexus.kenai.internal.KenaiCapabilityDescriptor;
import org.sonatype.nexus.validation.group.Create;

import com.google.common.collect.Maps;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Kenai configuration.
 *
 * @since 3.0
 */
@UniquePerCapabilityType(value = KenaiCapabilityDescriptor.TYPE_ID, groups = Create.class)
public class KenaiConfiguration
{

  public static final String BASE_URL = "baseUrl";

  public static final String DEFAULT_ROLE = "defaultRole";

  /**
   * The remote Kenai used for authentication.
   */
  @NotBlank
  @URL
  private String baseUrl = "https://java.net/";

  /**
   * A role it assigned to all Kenai Realm users.
   */
  @NotBlank
  private String defaultRole;

  public KenaiConfiguration() {}

  public KenaiConfiguration(final Map<String, String> properties) {
    checkNotNull(properties);
    this.baseUrl = properties.get(BASE_URL);
    this.defaultRole = properties.get(DEFAULT_ROLE);
  }

  public Map<String, String> asMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put(BASE_URL, baseUrl);
    map.put(DEFAULT_ROLE, defaultRole);
    return map;
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
