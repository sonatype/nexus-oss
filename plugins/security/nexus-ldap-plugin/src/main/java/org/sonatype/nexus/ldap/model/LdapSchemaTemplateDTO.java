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
package org.sonatype.nexus.ldap.model;

import javax.xml.bind.annotation.XmlType;

/**
 * LDAP schema template object, contains defaults for a schema
 */
@XmlType(name = "ldapSchemaTemplate")
public class LdapSchemaTemplateDTO
{
  private String name;

  private LdapUserAndGroupAuthConfigurationDTO userAndGroupConfig;

  /**
   * Get the name of the template.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the template.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the template configuration.
   */
  public LdapUserAndGroupAuthConfigurationDTO getUserAndGroupConfig() {
    return userAndGroupConfig;
  }

  /**
   * Set the template configuration.
   */
  public void setUserAndGroupConfig(LdapUserAndGroupAuthConfigurationDTO userAndGroupConfig) {
    this.userAndGroupConfig = userAndGroupConfig;
  }

}
