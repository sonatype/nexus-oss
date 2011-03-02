/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.users;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;

import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.rest.users.UserResetPlexusResource;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class ForgotPasswordTest
    extends AbstractNexusTestCase
{
    private GreenMail server;

    private int emailServerPort;

    public void testRestPassword()
        throws Exception
    {

        this.copyDefaultConfigToPlace();
        this.setupEmailConfig();
        
        NexusConfiguration nexusConfig = this.lookup( NexusConfiguration.class );
        nexusConfig.loadConfiguration( true );
        
        String username = "admin";

        PlexusResource resetEmailPR = this.lookup( PlexusResource.class, "UserResetPlexusResource" );

        Request request = new Request();
        Response response = new Response( request );
        request.getAttributes().put( UserResetPlexusResource.USER_ID_KEY, username );
        resetEmailPR.delete( null, request, response );

        // Need 1 message
        server.waitForIncomingEmail( 5000, 1 );

        
        MimeMessage[] msgs = server.getReceivedMessages();
        Assert.assertTrue( "Expected email.", msgs != null && msgs.length > 0 );
        MimeMessage msg = msgs[0];

        String password = null;
        // Sample body: Your password has been reset. Your new password is: c1r6g4p8l7
        String body = GreenMailUtil.getBody( msg );

        int index = body.indexOf( "Your new password is: " );
        int passwordStartIndex = index + "Your new password is: ".length();
        if ( index != -1 )
        {
            password = body.substring( passwordStartIndex, body.indexOf( '\n', passwordStartIndex ) ).trim();
        }

        Assert.assertNotNull( password );

    }

    private void setupEmailConfig()
        throws IOException,
            XmlPullParserException
    {
        FileInputStream fis = null;
        Configuration config = null;
        try
        {
            fis = new FileInputStream( this.getNexusConfiguration() );

            NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();
            config = reader.read( fis );

            config.getSmtpConfiguration().setPort( this.emailServerPort );
            config.getSmtpConfiguration().setHostname( "localhost" );
//            config.getSmtpConfiguration().setDebugMode( true );

        }
        finally
        {
            IOUtil.close( fis );
        }
        
        // now write it back out
        FileWriter writer = null;
        
        try
        {
            writer = new FileWriter( this.getNexusConfiguration() );
            new NexusConfigurationXpp3Writer().write( writer, config );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    @Override
    public void setUp()
        throws Exception
    {
        ServerSocket socket = new ServerSocket( 0 );
        this.emailServerPort = socket.getLocalPort();
        socket.close();

        // ServerSetup smtp = new ServerSetup( 1234, null, ServerSetup.PROTOCOL_SMTP );
        ServerSetup smtp = new ServerSetup( this.emailServerPort, null, ServerSetup.PROTOCOL_SMTP );

        server = new GreenMail( smtp );
        server.setUser( "system@nexus.org", "smtp-username", "smtp-password" );
        server.start();
        
        this.lookup( SecuritySystem.class ).start();
    }

    @Override
    public void tearDown()
    {
        server.stop();
    }

}
