/*
 * Nexus: RESTLight Client
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
        
        M2SettingsClient client = new M2SettingsClient( getBaseUrl(), "unused", "unused" );
        
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
        
        M2SettingsClient client = new M2SettingsClient( getBaseUrl(), "unused", "unused" );
        
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
