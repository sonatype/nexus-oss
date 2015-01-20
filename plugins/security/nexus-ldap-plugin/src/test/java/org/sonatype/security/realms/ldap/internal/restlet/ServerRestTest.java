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

import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.realms.ldap.api.dto.LdapConnectionInfoDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapServerConfigurationDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapServerRequest;
import org.sonatype.security.realms.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;
import org.sonatype.security.realms.ldap.internal.persist.LdapConfigurationManager;
import org.sonatype.security.realms.ldap.internal.persist.LdapServerNotFoundException;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Mapping;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;

public class ServerRestTest
    extends AbstractLdapRestTest
{

  @Test
  public void testModifiable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    Assert.assertTrue(pr.isModifiable());
  }

  @Test
  public void testGet()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);

    // add 2 ldapServers
    LdapConfiguration ldapServer1 = new LdapConfiguration();
    ldapServer1.setName("testGet1");
    ldapServer1.setConnection(this.buildConnectionInfo());
    ldapServer1.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    LdapConfiguration ldapServer2 = new LdapConfiguration();
    ldapServer2.setName("testGet2");
    ldapServer2.setConnection(this.buildConnectionInfo());
    ldapServer2.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer2);

    // now get the second one
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    LdapServerRequest ldapResponse = (LdapServerRequest) pr.get(
        null,
        this.buildRequest(ldapServer1.getId()),
        null,
        null);

    this.compare(ldapResponse.getData(), ldapServer1);
  }

  @Test
  public void testPut()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);

    LdapConfiguration ldapServer1 = new LdapConfiguration();
    ldapServer1.setName("testPut");
    ldapServer1.setConnection(this.buildConnectionInfo());
    ldapServer1.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    LdapServerRequest ldapRequest = new LdapServerRequest();
    ldapRequest.setData(toDto(ldapServer1));

    ldapRequest.getData().setName("testPut-new");
    ldapRequest.getData().getConnectionInfo().setHost("newhost");
    ldapRequest.getData().getUserAndGroupConfig().setEmailAddressAttribute("newEmailAddressAttribute");

    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    Request request = this.buildRequest(ldapServer1.getId());
    LdapServerRequest ldapResponse = (LdapServerRequest) pr.put(
        null,
        request,
        null,
        ldapRequest);

    // update the request with the expected URL, so we can compare
    ldapRequest.getData().setUrl(request.getResourceRef().toString());

    ldapRequest.getData().getConnectionInfo().setSystemPassword(
        encodeBase64((AbstractLdapPlexusResource.FAKE_PASSWORD))
    );
    this.compare(ldapRequest.getData(), ldapResponse.getData());
  }

  @Test
  public void testDelete()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);

    // add 2 ldapServers
    LdapConfiguration ldapServer1 = new LdapConfiguration();
    ldapServer1.setName("testDelete1");
    ldapServer1.setConnection(this.buildConnectionInfo());
    ldapServer1.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    LdapConfiguration ldapServer2 = new LdapConfiguration();
    ldapServer2.setName("testDelete2");
    ldapServer2.setConnection(this.buildConnectionInfo());
    ldapServer2.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer2);

    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    pr.delete(null, this.buildRequest(ldapServer1.getId()), null);
    try {
      ldapConfigurationManager.getLdapServerConfiguration(ldapServer1.getId());
      Assert.fail("epected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }

    pr.delete(null, this.buildRequest(ldapServer2.getId()), null);
    try {
      ldapConfigurationManager.getLdapServerConfiguration(ldapServer1.getId());
      Assert.fail("epected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }

    try {
      pr.delete(null, this.buildRequest(ldapServer2.getId()), null);
    }
    catch (ResourceException e) {
      Assert.assertEquals(404, e.getStatus().getCode());
    }
  }

  protected LdapServerConfigurationDTO toDto(LdapConfiguration ldapServer) {
    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    dto.setId(ldapServer.getId());
    dto.setName(ldapServer.getName());

    if (ldapServer.getConnection() != null) {
      Connection connInfo = ldapServer.getConnection();

      LdapConnectionInfoDTO infoDto = new LdapConnectionInfoDTO();
      infoDto.setAuthScheme(connInfo.getAuthScheme());
      if (connInfo.getBackupHost() != null) {
        infoDto.setBackupMirrorHost(connInfo.getBackupHost().getHostName());
        infoDto.setBackupMirrorPort(connInfo.getBackupHost().getPort());
        infoDto.setBackupMirrorProtocol(connInfo.getBackupHost().getProtocol().name());
      }
      infoDto.setMaxIncidentsCount(connInfo.getMaxIncidentsCount());
      infoDto.setConnectionRetryDelay(connInfo.getConnectionRetryDelay());
      infoDto.setConnectionTimeout(connInfo.getConnectionTimeout());
      infoDto.setHost(connInfo.getHost().getHostName());
      infoDto.setPort(connInfo.getHost().getPort());
      infoDto.setProtocol(connInfo.getHost().getProtocol().name());
      infoDto.setRealm(connInfo.getSaslRealm());
      infoDto.setSearchBase(connInfo.getSearchBase());
      infoDto.setSystemUsername(connInfo.getSystemUsername());
      infoDto.setSystemPassword(connInfo.getSystemPassword());
      dto.setConnectionInfo(infoDto);
    }

    if (ldapServer.getMapping() != null) {
      dto.setUserAndGroupConfig(toDto(ldapServer.getMapping()));
    }

    return dto;
  }

  protected LdapUserAndGroupAuthConfigurationDTO toDto(Mapping mapping) {
    final LdapUserAndGroupAuthConfigurationDTO result = new LdapUserAndGroupAuthConfigurationDTO();
    result.setEmailAddressAttribute(mapping.getEmailAddressAttribute());
    result.setLdapGroupsAsRoles(mapping.isLdapGroupsAsRoles());
    result.setGroupBaseDn(mapping.getGroupBaseDn());
    result.setGroupIdAttribute(mapping.getGroupIdAttribute());
    result.setGroupMemberAttribute(mapping.getGroupMemberAttribute());
    result.setGroupMemberFormat(mapping.getGroupMemberFormat());
    result.setGroupObjectClass(mapping.getGroupObjectClass());
    result.setUserPasswordAttribute(mapping.getUserPasswordAttribute());
    result.setUserIdAttribute(mapping.getUserIdAttribute());
    result.setUserObjectClass(mapping.getUserObjectClass());
    result.setLdapFilter(mapping.getLdapFilter());
    result.setUserBaseDn(mapping.getUserBaseDn());
    result.setUserRealNameAttribute(mapping.getUserRealNameAttribute());
    result.setUserSubtree(mapping.isUserSubtree());
    result.setGroupSubtree(mapping.isGroupSubtree());
    result.setUserMemberOfAttribute(mapping.getUserMemberOfAttribute());
    return result;
  }

}
