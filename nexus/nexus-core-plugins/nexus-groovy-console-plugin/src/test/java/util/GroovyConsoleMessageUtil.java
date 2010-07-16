package util;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptDTO;
import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptResponseDTO;
import com.thoughtworks.xstream.XStream;

public class GroovyConsoleMessageUtil
{

    private static final XStream xs;
    static
    {
        xs = XStreamFactory.getXmlXStream();
        xs.processAnnotations( GroovyScriptDTO.class );
        xs.processAnnotations( GroovyScriptResponseDTO.class );
    }

    public static List<GroovyScriptDTO> getScripts()
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/groovy_console" );

        String responeText = response.getEntity().getText();
        Assert.assertTrue( "Expected sucess: Status was: " + response.getStatus() + "\nResponse:\n" + responeText,
                           response.getStatus().isSuccess() );

        XStreamRepresentation representation = new XStreamRepresentation( xs, responeText, MediaType.APPLICATION_XML );
        GroovyScriptResponseDTO listRepsonse =
            (GroovyScriptResponseDTO) representation.getPayload( new GroovyScriptResponseDTO() );

        return listRepsonse.getData();

    }

    public static void addScript( GroovyScriptDTO groovyScriptDTO )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xs, "", MediaType.APPLICATION_XML );
        representation.setPayload( groovyScriptDTO );
        System.out.println( xs.toXML( groovyScriptDTO ) );

        Response response = RequestFacade.sendMessage( "service/local/groovy_console", Method.POST, representation );

        String responeText = response.getEntity().getText();
        Assert.assertTrue( "Expected sucess: Status was: " + response.getStatus() + "\nResponse:\n" + responeText,
                           response.getStatus().isSuccess() );

    }
}
