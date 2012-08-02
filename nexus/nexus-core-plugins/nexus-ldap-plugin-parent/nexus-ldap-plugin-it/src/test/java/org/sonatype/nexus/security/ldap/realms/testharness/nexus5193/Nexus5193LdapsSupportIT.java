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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.isSuccessful;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapConnMessageUtil;
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
    public void testAuth()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        LdapConnMessageUtil connUtil = new LdapConnMessageUtil( getJsonXStream(), MediaType.APPLICATION_JSON );
        LdapConnectionInfoDTO request = getConnectionInfo();
        LdapConnectionInfoDTO responseConnInfo = connUtil.updateConnectionInfo( request );
        responseConnInfo.setSystemPassword( "" );
        request.setSystemPassword( "" );

        assertThat( responseConnInfo, equalTo( request ) );

        // test for correct ldap config
        File ldapConfigFile = new File( WORK_CONF_DIR, "/ldap.xml" );
        assertThat( "cannot find ldap config", ldapConfigFile.exists() );
        assertThat( FileUtils.readFileToString( ldapConfigFile ), containsString( "more&amp;more" ) );

        connUtil.validateLdapConfig( getConnectionInfo() );

        connUtil.testAuth( getConnectionInfo(), isSuccessful() );
    }

    private LdapConnectionInfoDTO getConnectionInfo()
    {
        LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();

        connInfo.setAuthScheme( "simple" );
        connInfo.setHost( "localhost" );
        connInfo.setPort( this.getLdapPort() );
        connInfo.setProtocol( "ldaps" );
        // connInfo.setRealm( "" );
        connInfo.setSearchBase( "o=sonatype" );
        connInfo.setSystemPassword( "secret" );
        connInfo.setSystemUsername( "uid=admin,ou=system" );
        return connInfo;
    }
}
