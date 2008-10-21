package org.sonatype.nexus.integrationtests.nexus450;

import java.io.IOException;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.XStreamFactory;

import com.thoughtworks.xstream.XStream;

public class UserCreationUtil
{
    private static XStream xstream;

    static
    {
        xstream = XStreamFactory.getXmlXStream();
    }

    public static Status login()
        throws IOException
    {
        String serviceURI = "service/local/authentication/login";

        return RequestFacade.doGetRequest( serviceURI ).getStatus();
    }

}
