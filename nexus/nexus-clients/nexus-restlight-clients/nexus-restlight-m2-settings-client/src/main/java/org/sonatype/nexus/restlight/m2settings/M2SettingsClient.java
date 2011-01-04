/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
