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
 * Response wrapper object that contains the list of ldap servers.
 */
@XStreamAlias(value = "serverList")
@XmlRootElement(name = "serverList")
public class LdapServerListResponse
{
  private List<LdapServerListEntryDTO> data = new ArrayList<LdapServerListEntryDTO>();

  /**
   * Get the list of ldap servers.
   */
  @XmlElementWrapper(name = "data")
  @XmlElement(name = "ldapServerListEntry")
  public List<LdapServerListEntryDTO> getData() {
    return data;
  }

  /**
   * Set the list of ldap servers.
   */
  public void setData(List<LdapServerListEntryDTO> data) {
    this.data = data;
  }
}
