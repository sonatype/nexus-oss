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
 * List entry of an ldap server.
 */
@XmlType(name = "ldapServerListEntry")
public class LdapServerListEntryDTO
{
  private String id;

  private String name;

  private String url;

  private String ldapUrl;

  /**
   * Get the id of the ldap server.
   */
  public String getId() {
    return id;
  }

  /**
   * Set the id of the ldap server.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the name of the ldap server.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the ldap server.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the url used to manage the ldap server in nexus.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set the url used to manage the ldap server in nexus.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Get the url used to access the remote ldap server.
   */
  public String getLdapUrl() {
    return ldapUrl;
  }

  /**
   * Set the url used to access the remote ldap server.
   */
  public void setLdapUrl(String ldapUrl) {
    this.ldapUrl = ldapUrl;
  }

}
