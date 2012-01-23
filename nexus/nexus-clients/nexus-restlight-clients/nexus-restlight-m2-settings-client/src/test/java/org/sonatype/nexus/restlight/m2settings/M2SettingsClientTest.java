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
package org.sonatype.nexus.restlight.m2settings;

import static org.junit.Assert.assertEquals;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class M2SettingsClientTest
    extends AbstractRESTTest
{
    
    private final ConversationalFixture fixture = new ConversationalFixture( getExpectedUser(), getExpectedPassword() );
    
    @Test
    public void getSettingsTemplateUsingToken()
        throws RESTLightClientException, JDOMException, IOException
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        
        conversation.add( getVersionCheckFixture() );
        
        String token = "testToken";
        
        GETFixture settingsGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        settingsGet.setExactURI( M2SettingsClient.SETTINGS_TEMPLATE_BASE + token + M2SettingsClient.GET_CONTENT_ACTION );
        
        Document testDoc = readTestDocumentResource( "settings-template-" + token + ".xml" );
        
        settingsGet.setResponseDocument( testDoc );
        
        conversation.add( settingsGet );
        
        fixture.setConversation( conversation );
        
        M2SettingsClient client = new M2SettingsClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );
        
        Document doc = client.getSettingsTemplate( token );
        
        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );
        assertEquals( outputter.outputString( testDoc ), outputter.outputString( doc ) );
    }

    @Test
    public void getSettingsTemplateUsingAbsoluteURL()
        throws RESTLightClientException, JDOMException, IOException
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        
        conversation.add( getVersionCheckFixture() );
        
        String token = "testToken";
        
        GETFixture settingsGet = new GETFixture( getExpectedUser(), getExpectedPassword() );
        settingsGet.setExactURI( M2SettingsClient.SETTINGS_TEMPLATE_BASE + token + M2SettingsClient.GET_CONTENT_ACTION );
        
        Document testDoc = readTestDocumentResource( "settings-template-" + token + ".xml" );
        
        settingsGet.setResponseDocument( testDoc );
        
        conversation.add( settingsGet );
        
        fixture.setConversation( conversation );
        
        M2SettingsClient client = new M2SettingsClient( getBaseUrl(), getExpectedUser(), getExpectedPassword() );
        
        String url = getBaseUrl() + M2SettingsClient.SETTINGS_TEMPLATE_BASE + token + M2SettingsClient.GET_CONTENT_ACTION;
        Document doc = client.getSettingsTemplateAbsolute( url );
        
        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );
        assertEquals( outputter.outputString( testDoc ), outputter.outputString( doc ) );
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

}
