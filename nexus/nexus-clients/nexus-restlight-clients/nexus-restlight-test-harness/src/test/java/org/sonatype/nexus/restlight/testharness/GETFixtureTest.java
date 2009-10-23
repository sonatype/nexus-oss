package org.sonatype.nexus.restlight.testharness;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

import java.io.IOException;


public class GETFixtureTest
    extends AbstractRESTTest
{
    
    private final GETFixture fixture = new GETFixture( getExpectedUser(), getExpectedPassword() );
    
    @Test
    public void testGet()
        throws HttpException, IOException, JDOMException
    {
        Document doc = new Document().setRootElement( new Element( "root" ) );
        
        fixture.setResponseDocument( doc );
        
        String url = "http://localhost:" + fixture.getPort();
        HttpClient client = new HttpClient();
        setupAuthentication( client );
        
        GetMethod get = new GetMethod( url );
        
        client.executeMethod( get );
        
        SAXBuilder builder = new SAXBuilder();
        Document resp = builder.build( get.getResponseBodyAsStream() );
        
        get.releaseConnection();
        
        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );
        assertEquals( outputter.outputString( doc ), outputter.outputString( resp ) );
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

}
