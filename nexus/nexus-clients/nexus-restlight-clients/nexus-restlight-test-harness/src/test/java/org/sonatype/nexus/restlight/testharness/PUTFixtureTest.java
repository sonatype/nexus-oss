package org.sonatype.nexus.restlight.testharness;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

public class PUTFixtureTest
    extends AbstractRESTTest
{

    private final PUTFixture fixture = new PUTFixture( getExpectedUser(), getExpectedPassword() );

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

    @Test
    public void testPUT()
        throws Exception
    {
        Document doc = new Document().setRootElement( new Element( "root" ) );

        fixture.setRequestDocument( doc );

        String url = "http://localhost:" + fixture.getPort();

        HttpClient client = new HttpClient();
        setupAuthentication( client );

        PutMethod put = new PutMethod( url );

        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );

        put.setRequestEntity( new StringRequestEntity( outputter.outputString( doc ), "application/xml", "UTF-8" ) );

        client.executeMethod( put );

        String statusText = put.getStatusText();

        System.out.println( statusText );

        put.releaseConnection();

        assertEquals( HttpServletResponse.SC_OK, put.getStatusCode() );
    }

}
