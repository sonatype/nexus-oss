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

import org.jdom.Document;
import org.sonatype.nexus.restlight.common.AbstractRESTLightClient;
import org.sonatype.nexus.restlight.common.RESTLightClientException;

/**
 * <p>
 * REST client for interacting with the nexus-m2-settings-template plugin, found in Nexus Professional 1.3.2+
 * </p>
 * <p>
 * Currently, this client is capable of retrieving the settings.xml template in two ways:
 * </p>
 * <ul>
 * <li>From an absolute URL pointing to a template location within a running Nexus Professional instance</li>
 * <li>Given a template ID, at which point the client will construct the proper URL to access that template using the
 * Nexus Professional base URL with which the client instance was constructed.</li>
 * </ul>
 */
public class M2SettingsClient
extends AbstractRESTLightClient
{

    /**
     * Base URL used to construct URLs for retrieving a settings template based on a template ID.
     */
    public static final String SETTINGS_TEMPLATE_BASE = SVC_BASE + "/templates/settings/";

    /**
     * Action URL suffix used to construct URLs for retrieving a settings template based on a template ID.
     */
    public static final String GET_CONTENT_ACTION = "/content";

    public M2SettingsClient( final String baseUrl, final String user, final String password )
    throws RESTLightClientException
    {
        super( baseUrl, user, password, "m2settings" );
    }

    /**
     * Retrieve a Maven 2.x settings.xml template using an absolute URL referencing a running Nexus Professional instance.
     * 
     * @param url
     *            The absolute URL to the settings template (normally copied from the Nexus UI).
     */
    public Document getSettingsTemplateAbsolute( final String url )
    throws RESTLightClientException
    {
        return getAbsolute( url );
    }

    /**
     * Retrieve a Maven 2.x settings.xml template using the base URL used to construct this client instance, along with
     * the supplied template ID.
     * 
     * @param templateName
     *            The template ID to retrieve.
     */
    public Document getSettingsTemplate( final String templateName )
    throws RESTLightClientException
    {
        return get( SETTINGS_TEMPLATE_BASE + templateName + GET_CONTENT_ACTION );
    }

}
