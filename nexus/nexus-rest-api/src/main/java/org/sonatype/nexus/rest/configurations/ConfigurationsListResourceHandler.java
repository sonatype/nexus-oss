/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
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
package org.sonatype.nexus.rest.configurations;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.global.GlobalConfigurationResourceHandler;
import org.sonatype.nexus.rest.model.ConfigurationsListResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResourceResponse;

/**
 * The configuration list handler.
 * 
 * @author cstamas
 */
public class ConfigurationsListResourceHandler
    extends AbstractNexusResourceHandler
{

    public ConfigurationsListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * This hanndler simply creates a response with two elems: current and default configs.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        ConfigurationsListResourceResponse response = new ConfigurationsListResourceResponse();

        ConfigurationsListResource resource = new ConfigurationsListResource();

        resource.setResourceURI( calculateSubReference( GlobalConfigurationResourceHandler.DEFAULT_CONFIG_NAME ).getPath() );

        resource.setName( GlobalConfigurationResourceHandler.DEFAULT_CONFIG_NAME );

        response.addData( resource );

        resource = new ConfigurationsListResource();

        resource.setResourceURI( calculateSubReference( GlobalConfigurationResourceHandler.CURRENT_CONFIG_NAME ).getPath() );

        resource.setName( GlobalConfigurationResourceHandler.CURRENT_CONFIG_NAME );

        response.addData( resource );

        return serialize( variant, response );
    }
}
