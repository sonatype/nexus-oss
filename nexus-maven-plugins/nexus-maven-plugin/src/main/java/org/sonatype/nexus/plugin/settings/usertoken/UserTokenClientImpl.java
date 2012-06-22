/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.settings.usertoken;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.jsoup.Jsoup;
import org.sonatype.nexus.plugin.settings.ClientConfiguration;
import org.sonatype.nexus.plugin.settings.ClientFactory;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link UserTokenClient} implementation.
 *
 * @since 2.1
 */
@Component(role=UserTokenClient.class)
public class UserTokenClientImpl
    implements UserTokenClient
{
    //@NonNls
    private static final String SERVICE_PATH = "service/local/usertoken/current";

    @Requirement
    private ClientFactory clientFactory;

    // Constructor for Plexus
    public UserTokenClientImpl() {
        super();
    }

    @VisibleForTesting
    public UserTokenClientImpl(final ClientFactory clientFactory) {
        this.clientFactory = checkNotNull(clientFactory);
    }

    public UserTokenDTO getCurrent(final ClientConfiguration config) {
        checkNotNull(config);
        Client client = clientFactory.create(config);

        ClientResponse response = client.resource(serviceUri(config))
            .accept(MediaType.APPLICATION_XML)
            .get(ClientResponse.class);

        Status status = response.getClientResponseStatus();
        if (status != Status.OK) {
            String message = "Failed to fetch user-token";
            System.out.println(response.hasEntity());
            if (response.hasEntity()) {
                try {
                    InputStream input = response.getEntityInputStream();
                    String detail = IOUtil.toString(input);
                    IOUtil.close(input);
                    detail = Jsoup.parse(detail).text();
                    message += "; " + detail;
                }
                catch (Exception e) {
                    //ignore
                    e.printStackTrace();
                }
            }
            throw new RuntimeException(message);
        }

        return response.getEntity(UserTokenDTO.class);
    }

    private URI serviceUri(final ClientConfiguration config) {
        try {
            String tmp = config.getNexusUrl();
            if (!tmp.endsWith("/")) {
                tmp = tmp + "/";
            }
            return new URI(tmp + SERVICE_PATH);
        }
        catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
}