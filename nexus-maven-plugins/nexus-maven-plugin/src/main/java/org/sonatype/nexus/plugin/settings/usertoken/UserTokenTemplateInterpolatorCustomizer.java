/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugin.settings.usertoken;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.codehaus.plexus.interpolation.Interpolator;
import org.sonatype.nexus.plugin.settings.DownloadSettingsTemplateMojo;
import org.sonatype.nexus.plugin.settings.TemplateInterpolatorCustomizer;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * User-token {@link TemplateInterpolatorCustomizer}.
 *
 * @since 2.1
 */
@Component(role=TemplateInterpolatorCustomizer.class, hint="usertoken", instantiationStrategy ="per-lookup")
public class UserTokenTemplateInterpolatorCustomizer
    implements TemplateInterpolatorCustomizer
{
    public static final char SEPARATOR = ':';

    //@NonNls
    public static final String USER_TOKEN = "userToken";

    //@NonNls
    public static final String USER_TOKEN_NAME_CODE = USER_TOKEN + ".nameCode";

    //@NonNls
    public static final String USER_TOKEN_PASS_CODE = USER_TOKEN + ".passCode";

    private static final String ENCRYPTED_SUFFIX = ".encrypted";

    @Requirement
    private MasterPasswordEncryption encryption;

    private DownloadSettingsTemplateMojo owner;

    @Override
    public void customize(final DownloadSettingsTemplateMojo owner, final Interpolator interpolator) {
        this.owner = owner;

        interpolator.addValueSource(new AbstractValueSource(false)
        {
            // FIXME: Sort out how to hook up encryption using master-password muck
            // FIXME: Maybe something like $[encrypt.userToken.passCode] to wrap $[userToken.passCode] in encrypted envelope?
            // FIXME: Or perhaps just $[userToken.encryptedPassCode] ?

            @Override
            public Object getValue(String expression) {
                boolean encrypt = false;
                if (expression.toLowerCase().endsWith(ENCRYPTED_SUFFIX)) {
                    encrypt = true;

                    // Strip off suffix and continue
                    expression = expression.substring(0, expression.length() - ENCRYPTED_SUFFIX.length());
                }

                String result = null;

                if (expression.equalsIgnoreCase(USER_TOKEN)) {
                    result = renderUserToken();
                }
                else if (expression.equalsIgnoreCase(USER_TOKEN_NAME_CODE)) {
                    result = getNameCode();
                }
                else if (expression.equalsIgnoreCase(USER_TOKEN_PASS_CODE)) {
                    result = getPassCode();
                }

                // Attempt to encrypt
                if (encrypt && result != null) {
                    try {
                        return encryption.encrypt(result);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                return result;
            }
        });
    }

    private Client createClient() {
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

    private URI serviceUri() {
        try {
            return new URI(owner.getNexusUrl() + "/service/local/usertoken/current");
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private UserTokenDTO userToken;

    private UserTokenDTO getUserToken() {
        if (userToken == null) {
            Client client = createClient();

            ClientResponse response = client.resource(serviceUri())
                .accept(MediaType.APPLICATION_JSON) // for now force use of JSON so we'll expect our jackson provider to be used
                .get(ClientResponse.class);

            Status status = response.getClientResponseStatus();
            if (status != Status.OK) {
                throw new RuntimeException("Failed to fetch user-token, status: " + status);
            }

            userToken = response.getEntity(UserTokenDTO.class);
        }
        return userToken;
    }

    public String renderUserToken() {
        //noinspection StringBufferReplaceableByString
        return new StringBuilder()
            .append(getNameCode())
            .append(SEPARATOR)
            .append(getPassCode())
            .toString();
    }

    public String getNameCode() {
        return getUserToken().getNameCode();
    }

    public String getPassCode() {
        return getUserToken().getPassCode();
    }
}
