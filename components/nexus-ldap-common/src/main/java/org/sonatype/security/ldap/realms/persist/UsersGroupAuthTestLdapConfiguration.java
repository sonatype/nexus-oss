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

package org.sonatype.security.ldap.realms.persist;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.sisu.goodies.eventbus.EventBus;

@Singleton
@Named("UsersGroupAuthTestLdapConfiguration")
public class UsersGroupAuthTestLdapConfiguration
    extends DefaultLdapConfiguration
{
  private LdapAuthConfiguration ldapAuthConfiguration;

  private CConnectionInfo connectionInfo;

  @Inject
  public UsersGroupAuthTestLdapConfiguration(final ApplicationConfiguration applicationConfiguration,
      final ConfigurationValidator validator, final PasswordHelper passwordHelper, final EventBus eventBus)
  {
    super(applicationConfiguration, validator, passwordHelper, eventBus);
  }

  // ==
  
  public void setLdapAuthConfiguration(LdapAuthConfiguration ldapAuthConfiguration) {
    this.ldapAuthConfiguration = ldapAuthConfiguration;
  }

  public void setConnectionInfo(CConnectionInfo connectionInfo) {
    this.connectionInfo = connectionInfo;
  }
  
  // ==

  @Override
  public LdapAuthConfiguration getLdapAuthConfiguration() {
    return this.ldapAuthConfiguration;
  }

  @Override
  public CConnectionInfo readConnectionInfo() {
    return connectionInfo;
  }

  @Override
  public void clearCache() {
  }

  @Override
  public void save() {
  }
}
