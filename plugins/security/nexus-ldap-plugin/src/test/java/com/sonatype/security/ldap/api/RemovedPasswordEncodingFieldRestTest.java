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
package com.sonatype.security.ldap.api;

import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;

public class RemovedPasswordEncodingFieldRestTest
    extends AbstractLdapRestTest
{

  @Test
  public void testGet()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);

    CLdapServerConfiguration ldapServer1 = new CLdapServerConfiguration();
    ldapServer1.setName("testGet1");
    ldapServer1.setConnectionInfo(this.buildConnectionInfo());
    ldapServer1.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());

    // HERE IS THE MAGIC FOR THIS TEST
    ldapServer1.getUserAndGroupConfig().setPreferredPasswordEncoding("Clear"); // doesn't matter the actual value

    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    // now get the second one
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    LdapServerRequest ldapResponse = (LdapServerRequest) pr.get(
        null,
        this.buildRequest(ldapServer1.getId()),
        null,
        null);

    this.compare(ldapResponse.getData(), ldapServer1);
  }


  private <T, F> T convert(F from, T to) {
    // xstream cheat ahead
    XStream xstream = new XStream();
    xstream.setClassLoader(Thread.currentThread().getContextClassLoader());
    String fromXml = xstream.toXML(from);
    to = (T) xstream.fromXML(fromXml, to);
    return to;
  }
}
