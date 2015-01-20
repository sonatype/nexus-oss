/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.realms.ldap.internal.restlet;

import org.sonatype.security.realms.ldap.api.dto.LdapServerConfigurationDTO;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;

import org.apache.shiro.codec.Base64;
import org.junit.Assert;

public class LdapRestTestUtils
{

  public static void compare(LdapServerConfigurationDTO dto, LdapConfiguration ldapServer) {
    Assert.assertEquals(dto.getId(), ldapServer.getId());
    Assert.assertEquals(dto.getName(), ldapServer.getName());

    Assert.assertEquals(dto.getConnectionInfo().getAuthScheme(), ldapServer.getConnection().getAuthScheme());
    if (dto.getConnectionInfo().getBackupMirrorHost() != null) {
      Assert.assertEquals(dto.getConnectionInfo().getBackupMirrorHost(),
          ldapServer.getConnection().getBackupHost().getHostName());
      Assert.assertEquals(dto.getConnectionInfo().getBackupMirrorPort(),
          ldapServer.getConnection().getBackupHost().getPort());
      Assert.assertEquals(dto.getConnectionInfo().getBackupMirrorProtocol(),
          ldapServer.getConnection().getBackupHost().getProtocol().name());
    }
    Assert.assertEquals(dto.getConnectionInfo().getMaxIncidentsCount(), ldapServer.getConnection().getMaxIncidentsCount());
    Assert.assertEquals(dto.getConnectionInfo().getConnectionRetryDelay(),
        ldapServer.getConnection().getConnectionRetryDelay());
    Assert.assertEquals(dto.getConnectionInfo().getConnectionTimeout(),
        ldapServer.getConnection().getConnectionTimeout());
    Assert.assertEquals(dto.getConnectionInfo().getHost(), ldapServer.getConnection().getHost().getHostName());
    Assert.assertEquals(dto.getConnectionInfo().getPort(), ldapServer.getConnection().getHost().getPort());
    Assert.assertEquals(dto.getConnectionInfo().getProtocol(),
        ldapServer.getConnection().getHost().getProtocol().name());
    Assert.assertEquals(dto.getConnectionInfo().getRealm(), ldapServer.getConnection().getSaslRealm());
    Assert.assertEquals(dto.getConnectionInfo().getSearchBase(), ldapServer.getConnection().getSearchBase());
    Assert.assertEquals(
        ldapServer.getConnection().getSystemUsername(),
        Base64.decodeToString(dto.getConnectionInfo().getSystemUsername())
    );
    Assert.assertEquals(
        AbstractLdapPlexusResource.FAKE_PASSWORD,
        Base64.decodeToString(dto.getConnectionInfo().getSystemPassword())
    );

    Assert.assertEquals(dto.getUserAndGroupConfig().getEmailAddressAttribute(),
        ldapServer.getMapping().getEmailAddressAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupBaseDn(),
        ldapServer.getMapping().getGroupBaseDn());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupIdAttribute(),
        ldapServer.getMapping().getGroupIdAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupMemberAttribute(),
        ldapServer.getMapping().getGroupMemberAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupMemberFormat(),
        ldapServer.getMapping().getGroupMemberFormat());
    Assert.assertEquals(dto.getUserAndGroupConfig().getGroupObjectClass(),
        ldapServer.getMapping().getGroupObjectClass());
    Assert
        .assertEquals(dto.getUserAndGroupConfig().getUserBaseDn(), ldapServer.getMapping().getUserBaseDn());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserIdAttribute(),
        ldapServer.getMapping().getUserIdAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserMemberOfAttribute(),
        ldapServer.getMapping().getUserMemberOfAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserObjectClass(),
        ldapServer.getMapping().getUserObjectClass());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserPasswordAttribute(),
        ldapServer.getMapping().getUserPasswordAttribute());
    Assert.assertEquals(dto.getUserAndGroupConfig().getUserRealNameAttribute(),
        ldapServer.getMapping().getUserRealNameAttribute());
  }

}
