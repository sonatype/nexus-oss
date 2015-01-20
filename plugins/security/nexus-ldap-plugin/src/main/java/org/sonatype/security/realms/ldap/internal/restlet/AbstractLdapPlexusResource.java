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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.net.ssl.SSLContext;

import com.sonatype.nexus.ssl.model.TrustStoreKey;
import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.security.realms.ldap.api.dto.LdapConnectionInfoDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapServerConfigurationDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;
import org.sonatype.security.realms.ldap.api.dto.XStreamInitalizer;
import org.sonatype.security.realms.ldap.internal.LdapURL;
import org.sonatype.security.realms.ldap.internal.persist.LdapConfigurationManager;
import org.sonatype.security.realms.ldap.internal.persist.LdapServerNotFoundException;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection.Protocol;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Mapping;
import org.sonatype.security.realms.ldap.internal.realms.DefaultLdapContextFactory;
import org.sonatype.security.realms.ldap.internal.ssl.SSLLdapContextFactory;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.shiro.codec.CodecSupport.PREFERRED_ENCODING;
import static org.sonatype.security.realms.ldap.api.dto.LdapTrustStoreKey.ldapTrustStoreKey;

public abstract class AbstractLdapPlexusResource
    extends AbstractSecurityPlexusResource
{

  public static final String FAKE_PASSWORD = "--FAKE-PASSWORD--";

  private final TrustStore trustStore;

  protected AbstractLdapPlexusResource(final TrustStore trustStore) {
    this.trustStore = checkNotNull(trustStore);
  }

  protected void doDelete(Context context, Request request, Response response)
      throws ResourceException
  {
    super.delete(context, request, response);
  }

  protected Object doGet(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    return super.get(context, request, response, variant);
  }

  protected Object doPost(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    return super.post(context, request, response, payload);
  }

  protected Object doPut(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    return super.put(context, request, response, payload);
  }

  @Override
  public void delete(Context context, Request request, Response response)
      throws ResourceException
  {
    try {
      this.doDelete(context, request, response);
    }
    catch (IllegalArgumentException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", e);
    }
  }

  @Override
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    try {
      return this.doGet(context, request, response, variant);
    }
    catch (IllegalArgumentException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", e);
    }
  }

  @Override
  public Object post(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    try {
      return this.doPost(context, request, response, payload);
    }
    catch (IllegalArgumentException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", e);
    }
  }

  @Override
  public Object put(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    try {
      return this.doPut(context, request, response, payload);
    }
    catch (IllegalArgumentException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", e);
    }
  }

  protected LdapConfiguration toLdapModel(final LdapServerConfigurationDTO dto)
      throws ResourceException
  {
    final LdapConfiguration result = new LdapConfiguration();
    // this method is used in edit/create case, set ID only if we have one
    if (!Strings.isNullOrEmpty(dto.getId())) {
      result.setId(dto.getId());
    }
    if (!Strings.isNullOrEmpty(dto.getName())) {
      result.setName(dto.getName());
    }
    // result.setOrder();
    result.setConnection(toLdapModel(dto.getConnectionInfo()));

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
    result.setMapping(mapping);
    return result;
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
      if (connInfo.getSystemPassword() != null) {
        infoDto.setSystemPassword(FAKE_PASSWORD);
      }
      dto.setConnectionInfo(encode(infoDto));
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

  protected Connection toLdapModel(LdapConnectionInfoDTO from)
      throws ResourceException
  {
    try {
      final Connection to = new Connection();
      to.setSearchBase(from.getSearchBase());
      to.setSystemUsername(from.getSystemUsername());
      to.setSystemPassword(from.getSystemPassword());
      to.setAuthScheme(from.getAuthScheme());
      final Host host = new Host(
          Protocol.valueOf(from.getProtocol()),
          from.getHost(),
          from.getPort()
      );
      to.setHost(host);
      if (!Strings.isNullOrEmpty(from.getBackupMirrorHost())) {
        final Host backupHost = new Host(
            Protocol.valueOf(from.getBackupMirrorProtocol()),
            from.getBackupMirrorHost(),
            from.getBackupMirrorPort()
        );
        to.setBackupHost(backupHost);
      }
      to.setSaslRealm(from.getRealm());
      to.setConnectionTimeout(from.getConnectionTimeout());
      to.setConnectionRetryDelay(from.getConnectionRetryDelay());
      to.setMaxIncidentsCount(from.getMaxIncidentsCount());
      return decode(to);
    }
    catch (NullPointerException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(), e);
    }
  }

  protected LdapContextFactory buildDefaultLdapContextFactory(final String ldapServerId,
                                                              final Connection connectionDto)
      throws MalformedURLException
  {
    DefaultLdapContextFactory ldapContextFactory = new DefaultLdapContextFactory();
    ldapContextFactory.setAuthentication(connectionDto.getAuthScheme());
    ldapContextFactory.setSearchBase(connectionDto.getSearchBase());
    ldapContextFactory.setSystemPassword(connectionDto.getSystemPassword());
    ldapContextFactory.setSystemUsername(connectionDto.getSystemUsername());
    ldapContextFactory.setUrl(
        new LdapURL(connectionDto.getHost().getProtocol().name(), connectionDto.getHost().getHostName(),
            connectionDto.getHost()
                .getPort(), connectionDto.getSearchBase()).toString());
    ldapContextFactory.setAuthentication(connectionDto.getAuthScheme());

    final TrustStoreKey key = ldapTrustStoreKey(ldapServerId == null ? "<unknown>" : ldapServerId);
    if (Protocol.ldaps == connectionDto.getHost().getProtocol()) {
      final SSLContext sslContext = trustStore.getSSLContextFor(key);
      if (sslContext != null) {
        getLogger().debug(
            "{} is using a Nexus SSL Trust Store for accessing {}",
            key, connectionDto.getHost()
        );
        return new SSLLdapContextFactory(sslContext, ldapContextFactory);
      }
    }
    getLogger().debug(
        "{} is using a JVM Trust Store for accessing {}",
        key, connectionDto.getHost()
    );

    return ldapContextFactory;
  }

  @Override
  public void configureXStream(XStream xstream) {
    super.configureXStream(xstream);
    new XStreamInitalizer().initXStream(xstream);
  }

  protected String buildExceptionMessage(String userMessage, Throwable t) {
    StringBuffer buffer = new StringBuffer(userMessage);
    buffer.append(": ");
    buffer.append(t.getMessage());

    while (t != t.getCause() && t.getCause() != null) {
      t = t.getCause();
      buffer.append(" [Caused by ").append(t.getClass().getName());
      buffer.append(": ").append(t.getMessage());
      buffer.append("]");
    }
    return buffer.toString();
  }

  protected Connection replaceFakePassword(final Connection connectionInfo,
                                           final String ldapServerId,
                                           final LdapConfigurationManager ldapConfigurationManager)
      throws ResourceException
  {
    if (connectionInfo == null) {
      return null;
    }
    String systemPassword = connectionInfo.getSystemPassword();
    if (FAKE_PASSWORD.equals(systemPassword) && ldapServerId != null) {
      try {
        LdapConfiguration config = ldapConfigurationManager.getLdapServerConfiguration(ldapServerId);
        connectionInfo.setSystemPassword(config.getConnection().getSystemPassword());
      }
      catch (LdapServerNotFoundException e) {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
      }
    }
    return connectionInfo;
  }

  private LdapConnectionInfoDTO encode(final LdapConnectionInfoDTO connectionInfo) {
    if (connectionInfo == null) {
      return null;
    }
    try {
      if (connectionInfo.getSystemUsername() != null) {
        connectionInfo.setSystemUsername(
            Base64.encodeToString(connectionInfo.getSystemUsername().getBytes(PREFERRED_ENCODING))
        );
      }
      if (connectionInfo.getSystemPassword() != null) {
        connectionInfo.setSystemPassword(
            Base64.encodeToString(connectionInfo.getSystemPassword().getBytes(PREFERRED_ENCODING))
        );
      }
      return connectionInfo;
    }
    catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  private Connection decode(final Connection connectionInfo) {
    if (connectionInfo == null) {
      return null;
    }
    if (connectionInfo.getSystemUsername() != null) {
      connectionInfo.setSystemUsername(Base64.decodeToString(connectionInfo.getSystemUsername()));
    }
    if (connectionInfo.getSystemPassword() != null) {
      connectionInfo.setSystemPassword(Base64.decodeToString(connectionInfo.getSystemPassword()));
    }
    return connectionInfo;
  }

}
