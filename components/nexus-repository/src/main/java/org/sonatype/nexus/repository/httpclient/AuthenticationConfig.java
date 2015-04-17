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
package org.sonatype.nexus.repository.httpclient;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.http.auth.Credentials;
import org.hibernate.validator.constraints.NotEmpty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * HTTP client authentication configuration.
 *
 * @since 3.0
 */
public abstract class AuthenticationConfig
{
  @NotEmpty
  private final String type;

  @NotEmpty
  private final List<String> preferredAuthSchemes;

  public AuthenticationConfig(final String type, final List<String> preferredAuthSchemes) {
    this.type = checkNotNull(type);
    this.preferredAuthSchemes = checkNotNull(preferredAuthSchemes);
  }

  public String getType() {
    return type;
  }

  public List<String> getPreferredAuthSchemes() {
    return preferredAuthSchemes;
  }

  @JsonIgnore
  public abstract Credentials getCredentials();
}
