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
import java.io.InputStream;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.global.GlobalConfigurationResourceHandler;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;

/**
 * The configuration resource.
 * 
 * @author cstamas
 */
public class ConfigurationsResourceHandler
    extends AbstractNexusResourceHandler
{
    /**
     * Standard Resource constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public ConfigurationsResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        getVariants().clear();

        getVariants().add( new Variant( MediaType.APPLICATION_XML ) );
    }

    /**
     * The GET handler. It chooses the config to serialize by request attribute (actually a mapped URL) and sends it.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        String configurationName = getRequest()
            .getAttributes().get( GlobalConfigurationResourceHandler.CONFIG_NAME_KEY ).toString();

        if ( !GlobalConfigurationResourceHandler.DEFAULT_CONFIG_NAME.equals( configurationName )
            && !GlobalConfigurationResourceHandler.CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND );

            return null;
        }
        else
        {
            InputStream is = null;

            if ( GlobalConfigurationResourceHandler.DEFAULT_CONFIG_NAME.equals( configurationName ) )
            {
                is = getNexus().getDefaultConfigurationAsStream();
            }
            else
            {
                is = getNexus().getConfigurationAsStream();
            }

            if ( is != null )
            {
                return new InputStreamRepresentation( MediaType.TEXT_XML, is );
            }
            else
            {
                return null;
            }
        }
    }
}
