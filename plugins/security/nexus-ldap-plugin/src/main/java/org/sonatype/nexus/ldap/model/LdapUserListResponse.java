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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Response wrapper that contains the list of ldap users.
 */
@XStreamAlias(value = "userList")
@XmlRootElement(name = "userList")
public class LdapUserListResponse
{

  private List<LdapUserDTO> data = new ArrayList<LdapUserDTO>();

  /**
   * Get the list of ldap user objects.
   */
  @XmlElementWrapper(name = "data")
  @XmlElement(name = "ldapUser")
  public List<LdapUserDTO> getData() {
    return data;
  }

  /**
   * Set the list of ldap user objects.
   */
  public void setData(List<LdapUserDTO> data) {
    this.data = data;
  }
}
