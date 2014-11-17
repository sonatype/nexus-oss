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
package org.sonatype.security.configuration.source

import org.sonatype.security.configuration.model.SecurityConfiguration

import javax.inject.Named
import javax.inject.Singleton

/**
 * Security configuration defaults.
 *
 * @since 3.0
 */
@Named('static')
@Singleton
class StaticSecurityConfigurationSource
implements SecurityConfigurationSource
{

  SecurityConfiguration config

  StaticSecurityConfigurationSource() {
    config = new SecurityConfiguration(
        version: SecurityConfiguration.MODEL_VERSION,
        anonymousAccessEnabled: true,
        anonymousUsername: 'anonymous',
        anonymousPassword: 'anonymous',
        realms: ['XmlAuthenticatingRealm', 'XmlAuthorizingRealm']
    )
  }

  @Override
  void storeConfiguration() throws IOException {
    throw new UnsupportedOperationException("The ${this.class.simpleName} is static source!")
  }

  @Override
  SecurityConfiguration getConfiguration() {
    return config
  }

  @Override
  void setConfiguration(final SecurityConfiguration configuration) {
    throw new UnsupportedOperationException("The ${this.class.simpleName} is static source!")
  }

  @Override
  SecurityConfiguration loadConfiguration() {
    return getConfiguration()
  }

}

