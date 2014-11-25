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
package org.sonatype.security.ldap

import org.sonatype.configuration.ConfigurationException
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.security.configuration.model.SecurityConfiguration
import org.sonatype.security.configuration.source.SecurityConfigurationSource

/**
 * @since 3.0
 */
class TestSecurityConfigurationSource
implements SecurityConfigurationSource
{

  SecurityConfiguration config

  TestSecurityConfigurationSource(final SecurityConfiguration config) {
    this.config = config;
  }

  @Override
  ValidationResponse getValidationResponse() {
    return null
  }

  @Override
  void storeConfiguration() throws IOException {
    // do nothing
  }

  @Override
  SecurityConfiguration getConfiguration() {
    return config
  }

  @Override
  void setConfiguration(final SecurityConfiguration configuration) {
    this.config = configuration
  }

  @Override
  SecurityConfiguration loadConfiguration() throws ConfigurationException, IOException {
    return getConfiguration()
  }

  @Override
  InputStream getConfigurationAsStream() throws IOException {
    throw new UnsupportedOperationException()
  }

  @Override
  boolean isConfigurationUpgraded() {
    return false
  }

  @Override
  boolean isConfigurationDefaulted() {
    return false
  }
}

