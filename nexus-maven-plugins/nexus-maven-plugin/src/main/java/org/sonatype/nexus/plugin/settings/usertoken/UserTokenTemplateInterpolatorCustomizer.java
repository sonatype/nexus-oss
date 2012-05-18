/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugin.settings.usertoken;

import com.google.common.base.Throwables;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.codehaus.plexus.interpolation.Interpolator;
import org.sonatype.nexus.plugin.settings.DownloadSettingsTemplateMojo;
import org.sonatype.nexus.plugin.settings.TemplateInterpolatorCustomizer;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkState;

/**
 * User-token {@link TemplateInterpolatorCustomizer}.
 *
 * @since 2.1
 */
@Component(role=TemplateInterpolatorCustomizer.class, hint="usertoken", instantiationStrategy="per-lookup")
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

    //@NonNls
    private static final String ENCRYPTED_SUFFIX = ".encrypted";

    @Requirement
    private ClientFactory clientFactory;

    @Requirement
    private MasterPasswordEncryption encryption;

    private DownloadSettingsTemplateMojo owner;

    @Override
    public void customize(final DownloadSettingsTemplateMojo owner, final Interpolator interpolator) {
        this.owner = owner;

        interpolator.addValueSource(new AbstractValueSource(false)
        {
            @Override
            public Object getValue(String expression) {
                // Check for encryption flag
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
                        result = encryption.encrypt(result);
                    }
                    catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                }

                return result;
            }
        });
    }

    private URI serviceUri() {
        checkState(owner != null);
        try {
            String tmp = owner.getNexusUrl();
            if (!tmp.endsWith("/")) {
                tmp = tmp + "/";
            }
            return new URI(tmp + "service/local/usertoken/current");
        }
        catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Cached user-token details, as more than one interpolation key may need to use this data.
     *
     * Component using instantiationStrategy="per-lookup" to try and avoid holding on to this for too long.
     */
    private UserTokenDTO userToken;

    private UserTokenDTO getUserToken() {
        if (userToken == null) {
            Client client = clientFactory.create(owner);

            ClientResponse response = client.resource(serviceUri())
                .accept(MediaType.APPLICATION_JSON) // for now force use of JSON so we'll expect the jackson provider to be used
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
