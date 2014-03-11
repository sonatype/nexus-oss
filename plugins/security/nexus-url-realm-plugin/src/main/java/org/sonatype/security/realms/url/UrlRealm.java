/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.security.realms.url;

import java.io.IOException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.security.realms.url.config.UrlRealmConfiguration;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Realm that performs HTTP GET authenticated with user entered credentials against a remote URL to perform authc.
 *
 * @since 2.8.0
 */
@Singleton
@Typed(Realm.class)
@Named(UrlRealm.NAME)
@Description("URL Realm")
public class UrlRealm
    extends AuthorizingRealm
{
  public static final String NAME = "URLRealm";

  private static final Logger logger = LoggerFactory.getLogger(UrlRealm.class);

  private final ApplicationConfiguration applicationConfiguration;

  private final UrlRealmConfiguration urlRealmConfiguration;

  private final Hc4Provider hc4Provider;

  @Inject
  public UrlRealm(final ApplicationConfiguration applicationConfiguration,
                  final UrlRealmConfiguration urlRealmConfiguration,
                  final Hc4Provider hc4Provider)
  {
    this.applicationConfiguration = checkNotNull(applicationConfiguration);
    this.urlRealmConfiguration = checkNotNull(urlRealmConfiguration);
    this.hc4Provider = checkNotNull(hc4Provider);
    setName(NAME);
    setAuthenticationCachingEnabled(true);
    setAuthorizationCachingEnabled(true);
  }

  @Override
  public boolean supports(final AuthenticationToken token) {
    return (token instanceof UsernamePasswordToken);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
      throws AuthenticationException
  {
    final UsernamePasswordToken upToken = (UsernamePasswordToken) token;

    // if the user can authenticate we are good to go
    if (authenticateViaUrl(upToken)) {
      return buildAuthenticationInfo(upToken);
    }
    else {
      throw new UnknownAccountException("User \"" + upToken.getUsername()
          + "\" cannot be authenticated against URL Realm.");
    }
  }

  private AuthenticationInfo buildAuthenticationInfo(final UsernamePasswordToken token) {
    return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
  }

  private boolean authenticateViaUrl(final UsernamePasswordToken usernamePasswordToken) {
    final DefaultRemoteStorageContext ctx = new DefaultRemoteStorageContext(
        applicationConfiguration.getGlobalRemoteStorageContext());
    ctx.setRemoteAuthenticationSettings(new UsernamePasswordRemoteAuthenticationSettings(
        usernamePasswordToken.getUsername(), new String(usernamePasswordToken.getPassword())));
    final HttpClient client = hc4Provider.createHttpClient(ctx);
    try {
      final String url = urlRealmConfiguration.getConfiguration().getBaseUrl();
      final HttpResponse response = client.execute(new HttpGet(url));

      try {
        logger.debug("URL Realm user \"{}\" validated against URL={} as {}", usernamePasswordToken.getUsername(), url,
            response.getStatusLine());
        final boolean success =
            response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299;
        return success;
      }
      finally {
        HttpClientUtils.closeQuietly(response);
      }
    }
    catch (IOException e) {
      logger.info("URL Realm was unable to perform authentication", e);
      return false;
    }
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
    // only if authenticated with this realm too
    if (!principals.getRealmNames().contains(getName())) {
      return null;
    }
    // add the default role
    final SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
    authorizationInfo.addRole(urlRealmConfiguration.getConfiguration().getDefaultRole());
    return authorizationInfo;
  }
}
