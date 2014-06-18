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
package com.sonatype.nexus.ldap;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugin.PluginIdentity;

import org.eclipse.sisu.EagerSingleton;

/**
 * LDAP plugin.
 *
 * @since 2.4
 */
@Named
@EagerSingleton
public class LdapPlugin
    extends PluginIdentity
{

  /**
   * Prefix for ID-like things.
   */
  public static final String ID_PREFIX = "ldap";

  /**
   * Expected groupId for plugin artifact.
   */
  public static final String GROUP_ID = "com.sonatype.nexus.plugins";

  /**
   * Expected artifactId for plugin artifact.
   */
  public static final String ARTIFACT_ID = "nexus-ldap-plugin";

  /**
   * Prefix for @Named configuration.
   */
  public static final String CONFIG_PREFIX = "${" + ID_PREFIX;

  /**
   * Prefix for REST resources
   */
  public static final String REST_PREFIX = "/" + ID_PREFIX;

  /**
   * Prefix for permissions.
   */
  public static final String PERMISSION_PREFIX = "nexus:" + ID_PREFIX + ":";

  /**
   * LDAP realm name.
   */
  public static final String REALM_NAME = "NexusLdapAuthenticationRealm";

  /**
   * LDAP user source name.
   */
  public static final String USER_SOURCE = "LDAP";

  @Inject
  public LdapPlugin()
      throws Exception
  {
    super(GROUP_ID, ARTIFACT_ID);
  }

}