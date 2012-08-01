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
package org.sonatype.nexus.security.ldap.realms.testharness.nexus5193;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.restlet.data.MediaType.APPLICATION_JSON;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.test.utils.NexusRequestMatchers;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResourceRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus5193LdapsSupportIT
    extends AbstractLdapIntegrationIT
{

    @Override
    protected LdapServer lookupLdapServer()
        throws ComponentLookupException
    {
        return lookup( LdapServer.class, getClass().getName() );
    }

    /**
     * This only works because Nexus runs in the same VM as this test.
     *
     * For a launcher-based test, you would probably need to create a keystore with
     * the apache-ds cert, and set '-Djavax.net.ssl.trustStore=$jksPath'.
     */
    @BeforeClass
    public static void trustAllCerts()
        throws KeyManagementException, NoSuchAlgorithmException
    {
        SSLContext context = SSLContext.getInstance( "SSL" );
        TrustManager[] tm = new TrustManager[]{ new TrustingX509TrustManager() };
        context.init( null, tm, null );
        SSLContext.setDefault( context );
    }

    @Test
    public void searchWithLdaps()
        throws Exception
    {
        PlexusUserSearchCriteriaResourceRequest resourceRequest = new PlexusUserSearchCriteriaResourceRequest();
        PlexusUserSearchCriteriaResource criteria = new PlexusUserSearchCriteriaResource();
        criteria.setUserId( "" );
        criteria.setEffectiveUsers( true );
        resourceRequest.setData( criteria );

        XStreamRepresentation representation = new XStreamRepresentation( this.getJsonXStream(), "", APPLICATION_JSON );


        representation.setPayload( resourceRequest );

        final String response = RequestFacade.doPutForText( RequestFacade.SERVICE_LOCAL + "user_search/LDAP", representation, NexusRequestMatchers.isSuccessful() );

        PlexusUserListResourceResponse userList =
            (PlexusUserListResourceResponse) new XStreamRepresentation( this.getJsonXStream(), response,
                                                                        APPLICATION_JSON )
                .getPayload( new PlexusUserListResourceResponse() );

        List<PlexusUserResource> users = userList.getData();
        assertThat( users, hasSize( 2 ) );
    }

}
