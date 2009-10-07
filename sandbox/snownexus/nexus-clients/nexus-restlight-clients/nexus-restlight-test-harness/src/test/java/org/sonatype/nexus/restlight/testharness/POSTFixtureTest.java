package org.sonatype.nexus.restlight.testharness;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;


public class POSTFixtureTest
    extends AbstractRESTTest
{
    
    private POSTFixture fixture = new POSTFixture();

    @Test
    public void testPost()
        throws HttpException, IOException, JDOMException
    {
        Document doc = new Document().setRootElement( new Element( "root" ) );
        
        fixture.setRequestDocument( doc );
        
        String url = "http://localhost:" + fixture.getPort();
        HttpClient client = new HttpClient();
        
        PostMethod post = new PostMethod( url );
        
        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );
        
        post.setRequestEntity( new StringRequestEntity( outputter.outputString( doc ), "application/xml", "UTF-8" ) );
        
        client.executeMethod( post );
        
        String statusText = post.getStatusText();
        
        System.out.println( statusText );
        
        post.releaseConnection();
        
        assertEquals( HttpServletResponse.SC_OK, post.getStatusCode() );
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

}
