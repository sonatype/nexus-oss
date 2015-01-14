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
package org.sonatype.security.realms.kenai;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.security.realms.kenai.config.KenaiRealmConfiguration;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Realm that connects to a java.net kenai API.
 *
 * @author Brian Demers
 */
@Singleton
@Typed(Realm.class)
@Named(KenaiRealm.ROLE)
@Description("Kenai Realm")
public class KenaiRealm
        extends AuthorizingRealm
{
    public static final String ROLE = "kenai";

    private static final Logger logger = LoggerFactory.getLogger(KenaiRealm.class);

    private final KenaiRealmConfiguration kenaiRealmConfiguration;

    private final Hc4Provider hc4Provider;

    @Inject
    public KenaiRealm(final KenaiRealmConfiguration kenaiRealmConfiguration,
                      final Hc4Provider hc4Provider)
    {
        this.kenaiRealmConfiguration = checkNotNull(kenaiRealmConfiguration);
        this.hc4Provider = checkNotNull(hc4Provider);
        setName(ROLE);

        // TODO: write another test before enabling this
        // this.setAuthenticationCachingEnabled( true );
    }

    // ------------ AUTHENTICATION ------------

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
            throw new AccountException("User \"" + upToken.getUsername()
                    + "\" cannot be authenticated via Kenai Realm.");
        }
    }

    private AuthenticationInfo buildAuthenticationInfo(final UsernamePasswordToken token) {
        return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
    }

    private boolean authenticateViaUrl(final UsernamePasswordToken usernamePasswordToken) {
        final HttpClient client = hc4Provider.createHttpClient();

        try {
            final String url = kenaiRealmConfiguration.getConfiguration().getBaseUrl() + "api/login/authenticate.json";
            final List<NameValuePair> nameValuePairs = Lists.newArrayListWithCapacity(2);
            nameValuePairs.add(new BasicNameValuePair("username", usernamePasswordToken.getUsername()));
            nameValuePairs.add(new BasicNameValuePair("password", new String(usernamePasswordToken.getPassword())));
            final HttpPost post = new HttpPost(url);
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));
            final HttpResponse response = client.execute(post);

            try {
                logger.debug("Kenai Realm user \"{}\" validated against URL={} as {}", usernamePasswordToken.getUsername(), url,
                        response.getStatusLine());
                return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299;
            }
            finally {
                HttpClientUtils.closeQuietly(response);
            }
        }
        catch (IOException e) {
            logger.info("Kenai Realm was unable to perform authentication", e);
            return false;
        }
    }

    // ------------ AUTHORIZATION ------------

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
        // only if authenticated with this realm too
        if (!principals.getRealmNames().contains(getName())) {
            return null;
        }
        // add the default role
        final SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.addRole(kenaiRealmConfiguration.getConfiguration().getDefaultRole());

        final String username = principals.getPrimaryPrincipal().toString();
        CloseableHttpClient httpclient = HttpClients.createDefault();

        final String remoteUrl = kenaiRealmConfiguration.getConfiguration().getBaseUrl() + "api/projects.json?size=200&username=" + username + "&roles=" + "admin%2Cdeveloper";

        HttpGet httpget = new HttpGet(remoteUrl);

        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            public String handleResponse(
                    final HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };

        try {
            String responseBody = httpclient.execute(httpget, responseHandler);

            // Process non-null JSON response
            if (responseBody != null) {
                appendAuthorizationInfo(authorizationInfo, responseBody);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return authorizationInfo;
    }

    private JSONObject buildJsonObject(String responseText)
            throws JSONException, IOException
    {
        return new JSONObject(responseText);
    }

    private Set<String> buildRoleSetFromJsonObject(JSONObject jsonObject)
            throws JSONException
    {
        Set<String> roles = new HashSet<>();
        JSONArray projectArray = jsonObject.getJSONArray("projects");

        for (int ii = 0; ii < projectArray.length(); ii++)
        {
            JSONObject projectObject = projectArray.getJSONObject(ii);
            if (projectObject.has("name"))
            {
                String projectName = projectObject.getString("name");
                if (StringUtils.isNotEmpty(projectName))
                {
                    logger.trace("Found project {} in request", projectName);
                    roles.add(projectName);
                }
                else
                {
                    logger.debug("Found empty string in json object projects[{}].name", Integer.valueOf(ii));
                }
            }
        }

        return roles;
    }

    private void appendAuthorizationInfo(SimpleAuthorizationInfo authorizationInfo, String responseText)
            throws JSONException, IOException {
        JSONObject jsonObject = buildJsonObject(responseText);
        Set<String> roles = buildRoleSetFromJsonObject(jsonObject);
        authorizationInfo.addRoles(roles);
    }
}
