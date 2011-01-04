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
package org.sonatype.nexus.test.utils;

import java.io.IOException;

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
import org.testng.Assert;

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

        Assert.assertTrue( response.getStatus().isSuccess(), "Error getting Settings: " + response.getStatus() + "\n"
            + responseText );

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
        Response response = validateSmtpResponse( smtpSettings );

        return response.getStatus();
    }

    public static Response validateSmtpResponse( SmtpSettingsResource smtpSettings )
        throws IOException
    {
        String serviceURI = "service/local/check_smtp_settings/";

        SmtpSettingsResourceRequest configResponse = new SmtpSettingsResourceRequest();
        configResponse.setData( smtpSettings );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( configResponse );

        Response response = RequestFacade.sendMessage( serviceURI, Method.PUT, representation );
        return response;
    }

}
