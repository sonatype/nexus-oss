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

import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.apache.shiro.codec.Base64;
import org.junit.Assert;

public class LdapRestTestUtils
{

  public static void compare(LdapServerConfigurationDTO dto, CLdapServerConfiguration ldapServer) {
    Assert.assertEquals(dto.getId(), ldapServer.getId());
    Assert.assertEquals(dto.getName(), ldapServer.getName());

    Assert.assertEquals(dto.getConnectionInfo().getAuthScheme(), ldapServer.getConnectionInfo().getAuthScheme());
    Assert.assertEquals(dto.getConnectionInfo().getBackupMirrorHost(),
        ldapServer.getConnectionInfo().getBackupMirrorHost());
    Assert.assertEquals(dto.getConnectionInfo().getBackupMirrorPort(),
        ldapServer.getConnectionInfo().getBackupMirrorPort());
    Assert.assertEquals(dto.getConnectionInfo().getBackupMirrorProtocol(),
        ldapServer.getConnectionInfo().getBackupMirrorProtocol());
    Assert.assertEquals(dto.getConnectionInfo().getCacheTimeout(), ldapServer.getConnectionInfo().getCacheTimeout());
    Assert.assertEquals(dto.getConnectionInfo().getConnectionRetryDelay(),
        ldapServer.getConnectionInfo().getConnectionRetryDelay());
    Assert.assertEquals(dto.getConnectionInfo().getConnectionTimeout(),
        ldapServer.getConnectionInfo().getConnectionTimeout());
    Assert.assertEquals(dto.getConnectionInfo().getHost(), ldapServer.getConnectionInfo().getHost());
    Assert.assertEquals(dto.getConnectionInfo().getPort(), ldapServer.getConnectionInfo().getPort());
    Assert.assertEquals(dto.getConnectionInfo().getProtocol(), ldapServer.getConnectionInfo().getProtocol());
    Assert.assertEquals(dto.getConnectionInfo().getRealm(), ldapServer.getConnectionInfo().getRealm());
    Assert.assertEquals(dto.getConnectionInfo().getSearchBase(), ldapServer.getConnectionInfo().getSearchBase());
    Assert.assertEquals(
        ldapServer.getConnectionInfo().getSystemUsername(),
        Base64.decodeToString(dto.getConnectionInfo().getSystemUsername())
    );
    Assert.assertEquals(
        AbstractLdapPlexusResource.FAKE_PASSWORD,
        Base64.decodeToString(dto.getConnectionInfo().getSystemPassword())
    );

    Assert.assertEquals(dto.getUserAndGroupConfig().getEmailAddressAttribute(),
        ldapServer.getUserAndGroupConfig().getEmailAddressAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupBaseDn(),
        ldapServer.getUserAndGroupConfig().getGroupBaseDn());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupIdAttribute(),
        ldapServer.getUserAndGroupConfig().getGroupIdAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupMemberAttribute(),
        ldapServer.getUserAndGroupConfig().getGroupMemberAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupMemberFormat(),
        ldapServer.getUserAndGroupConfig().getGroupMemberFormat());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupObjectClass(),
        ldapServer.getUserAndGroupConfig().getGroupObjectClass());
    Assert
        .assertEquals(dto.getUserAndGroupConfig().getUserBaseDn(), ldapServer.getUserAndGroupConfig().getUserBaseDn());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserIdAttribute(),
        ldapServer.getUserAndGroupConfig().getUserIdAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserMemberOfAttribute(),
        ldapServer.getUserAndGroupConfig().getUserMemberOfAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserObjectClass(),
        ldapServer.getUserAndGroupConfig().getUserObjectClass());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserPasswordAttribute(),
        ldapServer.getUserAndGroupConfig().getUserPasswordAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserRealNameAttribute(),
        ldapServer.getUserAndGroupConfig().getUserRealNameAttribute());
  }

}
