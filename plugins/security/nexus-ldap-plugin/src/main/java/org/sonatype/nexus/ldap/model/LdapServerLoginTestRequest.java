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

import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Request wrapper object that contains the ldap login test details.
 */
@XStreamAlias(value = "loginTestRequest")
@XmlRootElement(name = "loginTestRequest")
public class LdapServerLoginTestRequest
{
  LdapServerLoginTestDTO data;

  /**
   * Get the ldap login test object.
   */
  public LdapServerLoginTestDTO getData() {
    return data;
  }

  /**
   * Set the ldap login test object.
   */
  public void setData(LdapServerLoginTestDTO data) {
    this.data = data;
  }
}
