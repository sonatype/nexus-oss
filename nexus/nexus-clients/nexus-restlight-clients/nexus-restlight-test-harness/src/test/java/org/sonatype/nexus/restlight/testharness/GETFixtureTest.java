/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
