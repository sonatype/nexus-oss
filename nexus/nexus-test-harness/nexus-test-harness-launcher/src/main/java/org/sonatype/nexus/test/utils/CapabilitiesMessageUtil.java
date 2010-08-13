package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilitiesPlexusResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class CapabilitiesMessageUtil
{
    private static final Logger LOG = Logger.getLogger( TaskScheduleUtil.class );

    private static XStream xstream;

    static
    {
        xstream = XStreamFactory.getXmlXStream();
        new CapabilitiesPlexusResource( null, null ).configureXStream( xstream );
    }

    public static List<CapabilityListItemResource> list()
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/capabilities" );

        if ( response.getStatus().isError() )
        {
            LOG.error( response.getStatus().toString() );
            return Collections.emptyList();
        }

        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, response.getEntity().getText(), MediaType.APPLICATION_XML );

        CapabilitiesListResponseResource scheduleResponse =
            (CapabilitiesListResponseResource) representation.getPayload( new CapabilitiesListResponseResource() );

        return scheduleResponse.getData();
    }

}
