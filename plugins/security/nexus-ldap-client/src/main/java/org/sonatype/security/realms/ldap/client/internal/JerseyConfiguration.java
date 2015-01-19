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
package org.sonatype.security.realms.ldap.client.internal;

import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.client.rest.support.EntitySupport;
import org.sonatype.security.realms.ldap.api.dto.LdapConnectionInfoDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapServerConfigurationDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapServerRequest;
import org.sonatype.security.realms.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;
import org.sonatype.security.realms.ldap.client.Configuration;
import org.sonatype.security.realms.ldap.client.Connection;
import org.sonatype.security.realms.ldap.client.Connection.Host;
import org.sonatype.security.realms.ldap.client.Connection.Protocol;
import org.sonatype.security.realms.ldap.client.Mapping;
import org.sonatype.security.realms.ldap.client.internal.JerseyConfiguration.ConfigurationXO;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.commons.codec.binary.Base64;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An LDAP configuration entity.
 *
 * @since 3.0
 */
public class JerseyConfiguration
    extends EntitySupport<Configuration, ConfigurationXO>
    implements Configuration
{
  /**
   * A hack to not expose Restlet ancient DTOs but keep state. The restlet DTOs are hidden and used only
   * for transport, basically keeping client users "safe" from changing them.
   */
  // TODO: currently XO has getters/setters to enable BeanUtils simple copy, see EntitySupport
  public static class ConfigurationXO
  {
    private String name;

    private Connection connection;

    private Mapping mapping;

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public Connection getConnection() {
      return connection;
    }

    public void setConnection(final Connection connection) {
      this.connection = connection;
    }

    public Mapping getMapping() {
      return mapping;
    }

    public void setMapping(final Mapping mapping) {
      this.mapping = mapping;
    }
  }

  private final JerseyNexusClient client;

  public JerseyConfiguration(final JerseyNexusClient client) {
    super(null);
    this.client = client;
  }

  public JerseyConfiguration(final JerseyNexusClient client, final LdapServerConfigurationDTO settings) {
    super(settings.getId(), toXO(settings));
    this.client = client;
  }

  @Override
  public String getName() {
    return settings().name;
  }

  @Override
  public Configuration setName(final String name) {
    checkNotNull(name);
    settings().name = name;
    return this;
  }

  @Override
  public Connection getConnection() {
    return settings().connection;
  }

  @Override
  public Mapping getMapping() {
    return settings().mapping;
  }

  @Override
  protected ConfigurationXO createSettings(final String id) {
    final ConfigurationXO data = new ConfigurationXO();
    data.connection = new Connection();
    data.mapping = new Mapping();
    return data;
  }

  @Override
  protected ConfigurationXO doGet() {
    try {
      return toXO(client.serviceResource("ldap/servers/" + id()).get(LdapServerRequest.class).getData());
    }
    catch (UniformInterfaceException e) {
      throw client.convert(e);
    }
    catch (ClientHandlerException e) {
      throw client.convert(e);
    }
  }

  @Override
  protected ConfigurationXO doCreate() {
    try {
      final LdapServerRequest entity = new LdapServerRequest();
      entity.setData(toDto(settings()));
      return toXO(client.serviceResource("ldap/servers").post(LdapServerRequest.class, entity).getData());
    }
    catch (UniformInterfaceException e) {
      throw client.convert(e);
    }
    catch (ClientHandlerException e) {
      throw client.convert(e);
    }
  }

  @Override
  protected ConfigurationXO doUpdate() {
    try {
      final LdapServerRequest entity = new LdapServerRequest();
      entity.setData(toDto(settings()));
      return toXO(client.serviceResource("ldap/servers/" + id()).put(LdapServerRequest.class, entity).getData());
    }
    catch (UniformInterfaceException e) {
      throw client.convert(e);
    }
    catch (ClientHandlerException e) {
      throw client.convert(e);
    }
  }

  @Override
  protected void doRemove() {
    try {
      client.serviceResource("ldap/servers/" + id()).delete();
    }
    catch (UniformInterfaceException e) {
      throw client.convert(e);
    }
    catch (ClientHandlerException e) {
      throw client.convert(e);
    }
  }

  // ==

  private static ConfigurationXO toXO(final LdapServerConfigurationDTO dto)
  {
    final ConfigurationXO result = new ConfigurationXO();
    result.name = checkNotNull(dto.getName());
    final Connection connection = new Connection();
    final LdapConnectionInfoDTO dtoConnection = dto.getConnectionInfo();
    connection.setSearchBase(dtoConnection.getSearchBase());
    if (!Strings.isNullOrEmpty(dtoConnection.getSystemUsername())) {
      connection.setSystemUsername(new String(Base64.decodeBase64(
          dtoConnection.getSystemUsername().getBytes(Charsets.UTF_8)), Charsets.UTF_8));
    }
    if (!Strings.isNullOrEmpty(dtoConnection.getSystemPassword())) {
      connection.setSystemPassword(new String(Base64.decodeBase64(
          dtoConnection.getSystemPassword().getBytes(Charsets.UTF_8)), Charsets.UTF_8));
    }
    connection.setAuthScheme(dtoConnection.getAuthScheme());
    final Host host = new Host(
        Protocol.valueOf(dtoConnection.getProtocol()),
        dtoConnection.getHost(),
        dtoConnection.getPort()
    );
    connection.setHost(host);
    if (!Strings.isNullOrEmpty(dtoConnection.getBackupMirrorHost())) {
      final Host backupHost = new Host(
          Protocol.valueOf(dtoConnection.getBackupMirrorProtocol()),
          dtoConnection.getBackupMirrorHost(),
          dtoConnection.getBackupMirrorPort()
      );
      connection.setBackupHost(backupHost);
    }
    connection.setSaslRealm(dtoConnection.getRealm());
    connection.setConnectionTimeout(dtoConnection.getConnectionTimeout());
    connection.setConnectionRetryDelay(dtoConnection.getConnectionRetryDelay());
    connection.setMaxIncidentsCount(dtoConnection.getMaxIncidentsCount());
    result.connection = connection;

    final Mapping mapping = new Mapping();
    final LdapUserAndGroupAuthConfigurationDTO dtoMapping = dto.getUserAndGroupConfig();
    mapping.setEmailAddressAttribute(dtoMapping.getEmailAddressAttribute());
    mapping.setLdapGroupsAsRoles(dtoMapping.isLdapGroupsAsRoles());
    mapping.setGroupBaseDn(dtoMapping.getGroupBaseDn());
    mapping.setGroupIdAttribute(dtoMapping.getGroupIdAttribute());
    mapping.setGroupMemberAttribute(dtoMapping.getGroupMemberAttribute());
    mapping.setGroupMemberFormat(dtoMapping.getGroupMemberFormat());
    mapping.setGroupObjectClass(dtoMapping.getGroupObjectClass());
    mapping.setUserPasswordAttribute(dtoMapping.getUserPasswordAttribute());
    mapping.setUserIdAttribute(dtoMapping.getUserIdAttribute());
    mapping.setUserObjectClass(dtoMapping.getUserObjectClass());
    mapping.setLdapFilter(dtoMapping.getLdapFilter());
    mapping.setUserBaseDn(dtoMapping.getUserBaseDn());
    mapping.setUserRealNameAttribute(dtoMapping.getUserRealNameAttribute());
    mapping.setUserSubtree(dtoMapping.isUserSubtree());
    mapping.setGroupSubtree(dtoMapping.isGroupSubtree());
    mapping.setUserMemberOfAttribute(dtoMapping.getUserMemberOfAttribute());
    result.mapping = mapping;
    return result;
  }

  private LdapServerConfigurationDTO toDto(ConfigurationXO xo) {
    final LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    dto.setId(id());
    dto.setName(xo.name);
    final Connection connInfo = xo.connection;
    final LdapConnectionInfoDTO infoDto = new LdapConnectionInfoDTO();
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
    if (!Strings.isNullOrEmpty(connInfo.getSystemUsername())) {
      infoDto.setSystemUsername(Base64.encodeBase64String(connInfo.getSystemUsername().getBytes(Charsets.UTF_8)));
    }
    if (!Strings.isNullOrEmpty(connInfo.getSystemPassword())) {
      infoDto.setSystemPassword(Base64.encodeBase64String(connInfo.getSystemPassword().getBytes(Charsets.UTF_8)));
    }
    dto.setConnectionInfo(infoDto);

    final LdapUserAndGroupAuthConfigurationDTO mappingDto = new LdapUserAndGroupAuthConfigurationDTO();
    final Mapping mapping = xo.mapping;
    mappingDto.setEmailAddressAttribute(mapping.getEmailAddressAttribute());
    mappingDto.setLdapGroupsAsRoles(mapping.isLdapGroupsAsRoles());
    mappingDto.setGroupBaseDn(mapping.getGroupBaseDn());
    mappingDto.setGroupIdAttribute(mapping.getGroupIdAttribute());
    mappingDto.setGroupMemberAttribute(mapping.getGroupMemberAttribute());
    mappingDto.setGroupMemberFormat(mapping.getGroupMemberFormat());
    mappingDto.setGroupObjectClass(mapping.getGroupObjectClass());
    mappingDto.setUserPasswordAttribute(mapping.getUserPasswordAttribute());
    mappingDto.setUserIdAttribute(mapping.getUserIdAttribute());
    mappingDto.setUserObjectClass(mapping.getUserObjectClass());
    mappingDto.setLdapFilter(mapping.getLdapFilter());
    mappingDto.setUserBaseDn(mapping.getUserBaseDn());
    mappingDto.setUserRealNameAttribute(mapping.getUserRealNameAttribute());
    mappingDto.setUserSubtree(mapping.isUserSubtree());
    mappingDto.setGroupSubtree(mapping.isGroupSubtree());
    mappingDto.setUserMemberOfAttribute(mapping.getUserMemberOfAttribute());
    dto.setUserAndGroupConfig(mappingDto);
    return dto;
  }
}