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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.net.ssl.SSLContext;

import com.sonatype.nexus.ldap.internal.ssl.SSLLdapContextFactory;
import com.sonatype.nexus.ssl.model.TrustStoreKey;
import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;
import com.sonatype.security.ldap.api.dto.XStreamInitalizer;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.persist.LdapServerNotFoundException;
import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.ldap.realms.DefaultLdapContextFactory;
import org.sonatype.security.ldap.realms.tools.LdapURL;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;

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
import static com.sonatype.nexus.ldap.model.LdapTrustStoreKey.ldapTrustStoreKey;
import static org.apache.shiro.codec.CodecSupport.PREFERRED_ENCODING;

public abstract class AbstractLdapPlexusResource
    extends AbstractSecurityPlexusResource
{

  public static final String FAKE_PASSWORD = "--FAKE-PASSWORD--";

  private final TrustStore trustStore;

  protected AbstractLdapPlexusResource(final TrustStore trustStore) {
    this.trustStore = checkNotNull(trustStore);
  }

  protected void doDelete(Context context, Request request, Response response)
      throws ResourceException,
             InvalidConfigurationException
  {
    super.delete(context, request, response);
  }

  protected Object doGet(Context context, Request request, Response response, Variant variant)
      throws ResourceException,
             InvalidConfigurationException
  {
    return super.get(context, request, response, variant);
  }

  protected Object doPost(Context context, Request request, Response response, Object payload)
      throws ResourceException,
             InvalidConfigurationException
  {
    return super.post(context, request, response, payload);
  }

  protected Object doPut(Context context, Request request, Response response, Object payload)
      throws ResourceException,
             InvalidConfigurationException
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
    catch (InvalidConfigurationException e) {
      this.handleInvalidConfigurationException(e);
    }
  }

  @Override
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    try {
      return this.doGet(context, request, response, variant);
    }
    catch (InvalidConfigurationException e) {
      this.handleInvalidConfigurationException(e);
      return null;
    }
  }

  @Override
  public Object post(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    try {
      return this.doPost(context, request, response, payload);
    }
    catch (InvalidConfigurationException e) {
      this.handleInvalidConfigurationException(e);
      return null;
    }
  }

  @Override
  public Object put(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    try {
      return this.doPut(context, request, response, payload);
    }
    catch (InvalidConfigurationException e) {
      this.handleInvalidConfigurationException(e);
      return null;
    }
  }

  protected CLdapServerConfiguration toLdapModel(final LdapServerConfigurationDTO dto)
      throws ResourceException, InvalidConfigurationException
  {
    CLdapServerConfiguration config = convert(dto, new CLdapServerConfiguration());
    config.setConnectionInfo(decode(config.getConnectionInfo()));
    return config;
  }

  protected LdapServerConfigurationDTO toDto(CLdapServerConfiguration ldapServer) {
    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    dto.setId(ldapServer.getId());
    dto.setName(ldapServer.getName());

    if (ldapServer.getConnectionInfo() != null) {
      CConnectionInfo connInfo = ldapServer.getConnectionInfo();

      LdapConnectionInfoDTO infoDto = new LdapConnectionInfoDTO();
      infoDto.setAuthScheme(connInfo.getAuthScheme());
      infoDto.setBackupMirrorHost(connInfo.getBackupMirrorHost());
      infoDto.setBackupMirrorPort(connInfo.getBackupMirrorPort());
      infoDto.setBackupMirrorProtocol(connInfo.getBackupMirrorProtocol());
      infoDto.setCacheTimeout(connInfo.getCacheTimeout());
      infoDto.setConnectionRetryDelay(connInfo.getConnectionRetryDelay());
      infoDto.setConnectionTimeout(connInfo.getConnectionTimeout());
      infoDto.setHost(connInfo.getHost());
      infoDto.setPort(connInfo.getPort());
      infoDto.setProtocol(connInfo.getProtocol());
      infoDto.setRealm(connInfo.getRealm());
      infoDto.setSearchBase(connInfo.getSearchBase());
      infoDto.setSystemUsername(connInfo.getSystemUsername());
      if (connInfo.getSystemPassword() != null) {
        infoDto.setSystemPassword(FAKE_PASSWORD);
      }
      dto.setConnectionInfo(encode(infoDto));
    }

    if (ldapServer.getUserAndGroupConfig() != null) {
      CUserAndGroupAuthConfiguration userGroupConf = ldapServer.getUserAndGroupConfig();
      LdapUserAndGroupAuthConfigurationDTO userGroupDto = new LdapUserAndGroupAuthConfigurationDTO();
      dto.setUserAndGroupConfig(userGroupDto);

      userGroupDto.setEmailAddressAttribute(userGroupConf.getEmailAddressAttribute());
      userGroupDto.setGroupBaseDn(userGroupConf.getGroupBaseDn());
      userGroupDto.setGroupIdAttribute(userGroupConf.getGroupIdAttribute());
      userGroupDto.setGroupMemberAttribute(userGroupConf.getGroupMemberAttribute());
      userGroupDto.setGroupMemberFormat(userGroupConf.getGroupMemberFormat());
      userGroupDto.setGroupObjectClass(userGroupConf.getGroupObjectClass());
      userGroupDto.setGroupSubtree(userGroupConf.isGroupSubtree());
      userGroupDto.setLdapGroupsAsRoles(userGroupConf.isLdapGroupsAsRoles());
      userGroupDto.setUserBaseDn(userGroupConf.getUserBaseDn());
      userGroupDto.setUserIdAttribute(userGroupConf.getUserIdAttribute());
      userGroupDto.setUserMemberOfAttribute(userGroupConf.getUserMemberOfAttribute());
      userGroupDto.setUserObjectClass(userGroupConf.getUserObjectClass());
      userGroupDto.setUserPasswordAttribute(userGroupConf.getUserPasswordAttribute());
      userGroupDto.setUserRealNameAttribute(userGroupConf.getUserRealNameAttribute());
      userGroupDto.setUserSubtree(userGroupConf.isUserSubtree());
      userGroupDto.setLdapFilter(userGroupConf.getLdapFilter());
    }

    return dto;
  }

  protected LdapUserAndGroupAuthConfigurationDTO toDto(CUserAndGroupAuthConfiguration userAndGroupConfig) {
    return this.convert(userAndGroupConfig, new LdapUserAndGroupAuthConfigurationDTO());
  }

  protected CConnectionInfo toLdapModel(LdapConnectionInfoDTO connectionDto)
      throws ResourceException, InvalidConfigurationException
  {
    return decode(convert(connectionDto, new CConnectionInfo()));
  }

  private <T, F> T convert(F from, T to) {
    // xstream cheat ahead
    XStream xstream = new XStream();
    xstream.setClassLoader(this.getClass().getClassLoader());
    String fromXml = xstream.toXML(from);
    to = (T) xstream.fromXML(fromXml, to);
    return to;
  }

  protected LdapContextFactory buildDefaultLdapContextFactory(final String ldapServerId,
                                                              final CConnectionInfo connectionDto)
      throws MalformedURLException
  {
    DefaultLdapContextFactory ldapContextFactory = new DefaultLdapContextFactory();
    ldapContextFactory.setAuthentication(connectionDto.getAuthScheme());
    ldapContextFactory.setSearchBase(connectionDto.getSearchBase());
    ldapContextFactory.setSystemPassword(connectionDto.getSystemPassword());
    ldapContextFactory.setSystemUsername(connectionDto.getSystemUsername());
    ldapContextFactory.setUrl(new LdapURL(connectionDto.getProtocol(), connectionDto.getHost(), connectionDto
        .getPort(), connectionDto.getSearchBase()).toString());
    ldapContextFactory.setAuthentication(connectionDto.getAuthScheme());

    final TrustStoreKey key = ldapTrustStoreKey(ldapServerId == null ? "<unknown>" : ldapServerId);
    if ("ldaps".equals(connectionDto.getProtocol())) {
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

  protected CConnectionInfo replaceFakePassword(final CConnectionInfo connectionInfo,
                                                final String ldapServerId,
                                                final LdapConfigurationManager ldapConfigurationManager)
      throws InvalidConfigurationException, ResourceException
  {
    if (connectionInfo == null) {
      return null;
    }
    String systemPassword = connectionInfo.getSystemPassword();
    if (FAKE_PASSWORD.equals(systemPassword) && ldapServerId != null) {
      try {
        CLdapServerConfiguration config = ldapConfigurationManager.getLdapServerConfiguration(ldapServerId);
        connectionInfo.setSystemPassword(config.getConnectionInfo().getSystemPassword());
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

  private CConnectionInfo decode(final CConnectionInfo connectionInfo) {
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
