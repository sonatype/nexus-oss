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

import org.jdom.Document;
import org.sonatype.nexus.restlight.common.AbstractRESTLightClient;
import org.sonatype.nexus.restlight.common.ProxyConfig;
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

    private static final String M2SETTINGS_VOCAB_BASE_PATH = "m2settings";
    
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
        super( baseUrl, user, password, M2SETTINGS_VOCAB_BASE_PATH );
    }

    public M2SettingsClient( final String baseUrl, final String user, final String password, final ProxyConfig proxyConfig)
    throws RESTLightClientException
    {
        super( baseUrl, user, password, M2SETTINGS_VOCAB_BASE_PATH, proxyConfig );
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
