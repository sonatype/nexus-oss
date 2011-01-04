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
package org.sonatype.nexus.plugins.lvo;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;

import com.thoughtworks.xstream.XStream;

public class DiscoveryResponse
{
    public static final String IS_SUCCESSFUL_KEY = "isSuccessful";

    public static final String VERSION_KEY = "version";

    public static final String URL_KEY = "url";

    private final transient DiscoveryRequest request;

    private Map<String, Object> response;

    public boolean isSuccessful()
    {
        if ( getResponse().containsKey( IS_SUCCESSFUL_KEY ) )
        {
            return (Boolean) getResponse().get( IS_SUCCESSFUL_KEY );
        }
        else
        {
            return false;
        }
    }

    public void setSuccessful( boolean succesful )
    {
        getResponse().put( IS_SUCCESSFUL_KEY, succesful );
    }

    public DiscoveryResponse( DiscoveryRequest request )
    {
        this.request = request;
    }

    public DiscoveryRequest getRequest()
    {
        return request;
    }

    public String getVersion()
    {
        return (String) getResponse().get( VERSION_KEY );
    }

    public void setVersion( String version )
    {
        getResponse().put( VERSION_KEY, version );
    }

    public String getUrl()
    {
        return (String) getResponse().get( URL_KEY );
    }

    public void setUrl( String url )
    {
        getResponse().put( URL_KEY, url );
    }

    public Map<String, Object> getResponse()
    {
        if ( response == null )
        {
            response = new HashMap<String, Object>();
        }

        return response;
    }

    // ugly but workable solution
    public static void configureXStream( XStream x )
    {
        x.alias( "lvoResponse", DiscoveryResponse.class );

        x.registerLocalConverter( DiscoveryResponse.class, "response", new PrimitiveKeyedMapConverter( x.getMapper() ) );
    }
}
