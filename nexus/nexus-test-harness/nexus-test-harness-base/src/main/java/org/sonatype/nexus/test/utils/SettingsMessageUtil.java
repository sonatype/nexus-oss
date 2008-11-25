package org.sonatype.nexus.test.utils;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.test.utils.XStreamFactory;
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
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, response.getEntity().getText(), MediaType.APPLICATION_XML );

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

}
