package org.sonatype.nexus.integrationtests.nexus393;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;

public class ResetPasswordUtils
{

    public static Status resetPassword( String username )
        throws Exception
    {
        String serviceURI = "service/local/users_reset/" + username;
        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        return response.getStatus();
    }

}
