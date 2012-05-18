/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugin.settings.usertoken;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugin.settings.DownloadSettingsTemplateMojo;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * REST client factory helper.
 *
 * @since 2.1
 */
@Component(role=ClientFactory.class)
public class ClientFactory
{
    public Client create(final DownloadSettingsTemplateMojo owner) {
        checkNotNull(owner != null);

        ApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getClasses().add(JacksonJsonProvider.class);
        ApacheHttpClient4 client = ApacheHttpClient4.create(config);

        // Configure BASIC auth
        String userName = owner.getUsername();
        String password = owner.getPassword();
        if (userName != null && password != null) {
            client.addFilter(new HTTPBasicAuthFilter(userName, password));
        }

        // Configure proxy muck
        String proxyHost = owner.getProxyHost();
        int proxyPort = owner.getProxyPort();
        String proxyUser = owner.getProxyUsername();
        String proxyPassword = owner.getProxyPassword();

        if (proxyHost != null && proxyPort != -1) {
            config.getProperties().put(DefaultApacheHttpClient4Config.PROPERTY_PROXY_URI, "http://" + proxyHost + ":" + proxyPort);
        }
        if (proxyUser != null) {
            config.getProperties().put(DefaultApacheHttpClient4Config.PROPERTY_PROXY_USERNAME, proxyUser);
        }
        if (proxyPassword != null) {
            config.getProperties().put(DefaultApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD, proxyPassword);
        }

        return client;
    }
}
