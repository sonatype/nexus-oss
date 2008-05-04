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
package org.sonatype.nexus.rest.global;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResourceResponse;

/**
 * The GlobalConfigurationList resource handler. This is a read only resource that simply returns a list of known
 * configuration resources.
 * 
 * @author cstamas
 */
public class GlobalConfigurationListResourceHandler
    extends AbstractGlobalConfigurationResourceHandler
{

    /**
     * The default Resource constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public GlobalConfigurationListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * We allow HTTP GETs.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * The representation returned on incoming GETs. We currently support two configurations: "default" and "current".
     * We use here simple DTO creation to construct the response.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        GlobalConfigurationListResourceResponse response = new GlobalConfigurationListResourceResponse();

        GlobalConfigurationListResource data = new GlobalConfigurationListResource();

        data.setName( GlobalConfigurationResourceHandler.DEFAULT_CONFIG_NAME );

        data.setResourceURI( calculateSubReference( data.getName() ).getPath() );

        response.addData( data );

        data = new GlobalConfigurationListResource();

        data.setName( GlobalConfigurationResourceHandler.CURRENT_CONFIG_NAME );

        data.setResourceURI( calculateSubReference( data.getName() ).getPath() );

        response.addData( data );

        return serialize( variant, response );
    }

}
