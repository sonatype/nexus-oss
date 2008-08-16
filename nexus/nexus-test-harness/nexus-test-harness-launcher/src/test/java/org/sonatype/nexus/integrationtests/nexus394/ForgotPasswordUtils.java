package org.sonatype.nexus.integrationtests.nexus394;

import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.UserForgotPasswordRequest;
import org.sonatype.nexus.rest.model.UserForgotPasswordResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class ForgotPasswordUtils
{
    private static XStream xstream;

    static
    {
        xstream = new XStream();
        XStreamInitializer.initialize( xstream );
    }

    public static Response recoverUserPassword( String username, String email )
        throws Exception
    {
        String serviceURI = "service/local/users_forgotpw";
        UserForgotPasswordResource resource = new UserForgotPasswordResource();
        resource.setUserId( username );
        resource.setEmail( email );

        UserForgotPasswordRequest request = new UserForgotPasswordRequest();
        request.setData( resource );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        return RequestFacade.sendMessage( serviceURI, Method.POST, representation );
    }

}
