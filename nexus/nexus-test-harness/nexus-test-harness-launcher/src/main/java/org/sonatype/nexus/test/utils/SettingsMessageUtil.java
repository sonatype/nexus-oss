/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import java.io.IOException;

import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.SmtpSettingsResource;
import org.sonatype.nexus.rest.model.SmtpSettingsResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class SettingsMessageUtil
{

    private static XStream xstream;

    static
    {
        xstream = XStreamFactory.getXmlXStream();
    }

    public static GlobalConfigurationResource getCurrentSettings()
        throws IOException
    {
        String serviceURI = "service/local/global_settings/current";
        Response response = RequestFacade.doGetRequest( serviceURI );
        
        String responseText = response.getEntity().getText(); 
        
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, responseText, MediaType.APPLICATION_XML );

        Assert.assertTrue( "Error getting Settings: "+ response.getStatus() +"\n"+ responseText, response.getStatus().isSuccess() );
        
        GlobalConfigurationResourceResponse configResponse =
            (GlobalConfigurationResourceResponse) representation.getPayload( new GlobalConfigurationResourceResponse() );

        return configResponse.getData();
    }

    public static Status save( GlobalConfigurationResource globalConfig )
        throws IOException
    {
        String serviceURI = "service/local/global_settings/current";

        GlobalConfigurationResourceResponse configResponse = new GlobalConfigurationResourceResponse();
        configResponse.setData( globalConfig );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( configResponse );

        Response response = RequestFacade.sendMessage( serviceURI, Method.PUT, representation );

        return response.getStatus();
    }

    public static Status validateSmtp( SmtpSettingsResource smtpSettings )
        throws IOException
    {
        String serviceURI = "service/local/check_smtp_settings/";

        SmtpSettingsResourceRequest configResponse = new SmtpSettingsResourceRequest();
        configResponse.setData( smtpSettings );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( configResponse );

        Response response = RequestFacade.sendMessage( serviceURI, Method.PUT, representation );

        return response.getStatus();
    }

}
