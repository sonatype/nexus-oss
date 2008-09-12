package org.sonatype.nexus.integrationtests.nexus395;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class ForgotUsernameUtils
{

    private static XStream xstream;

    static
    {
        xstream = XStreamInitializer.initialize( new XStream() );
        XStreamInitializer.initialize( xstream );
    }

    public static Status recoverUsername( String email )
        throws Exception
    {
        String serviceURI = "service/local/users_forgotid/" + email;
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( null );

        return RequestFacade.sendMessage( serviceURI, Method.POST, representation ).getStatus();
    }

}
