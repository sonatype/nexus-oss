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

package com.sonatype.security.ldap.api.dto;


import org.sonatype.nexus.rest.model.AliasingListConverter;
import org.sonatype.nexus.rest.model.HtmlUnescapeStringConverter;

import com.thoughtworks.xstream.XStream;

public class XStreamInitalizer
{

  public XStream initXStream(XStream xstream) {
    xstream.processAnnotations(LdapAuthenticationTestRequest.class);
    xstream.processAnnotations(LdapUserListResponse.class);
    xstream.processAnnotations(LdapServerOrderRequest.class);
    xstream.processAnnotations(LdapServerRequest.class);
    xstream.processAnnotations(LdapSchemaTemplateListResponse.class);
    xstream.processAnnotations(LdapServerListResponse.class);
    xstream.processAnnotations(LdapServerLoginTestRequest.class);

    xstream.registerLocalConverter(LdapSchemaTemplateListResponse.class, "data", new AliasingListConverter(
        LdapSchemaTemplateDTO.class, "template"));

    HtmlUnescapeStringConverter converter = new HtmlUnescapeStringConverter(true);
    xstream.registerLocalConverter(LdapServerLoginTestRequest.class, "username", converter);
    xstream.registerLocalConverter(LdapServerLoginTestRequest.class, "password", converter);
    xstream.registerLocalConverter(LdapConnectionInfoDTO.class, "systemUsername", converter);
    xstream.registerLocalConverter(LdapConnectionInfoDTO.class, "systemPassword", converter);
    xstream.registerLocalConverter(LdapConnectionInfoDTO.class, "searchBase", converter);
    xstream.registerLocalConverter(LdapUserAndGroupAuthConfigurationDTO.class, "groupBaseDn", converter);
    xstream.registerLocalConverter(LdapUserAndGroupAuthConfigurationDTO.class, "userBaseDn", converter);
    xstream.registerLocalConverter(LdapUserAndGroupAuthConfigurationDTO.class, "groupMemberFormat", converter);
    xstream.registerLocalConverter(LdapUserAndGroupAuthConfigurationDTO.class, "ldapFilter", converter);

    return xstream;
  }

}
