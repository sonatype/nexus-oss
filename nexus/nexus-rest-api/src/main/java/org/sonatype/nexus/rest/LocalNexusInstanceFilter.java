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
package org.sonatype.nexus.rest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.Nexus;

/**
 * A restlet Filter, that handles "instanceName" attribute to put the correspondent (local or remote/proxied) Nexus
 * instance into request attributes, hance making local and remote call transparent in underlying restlets.
 * 
 * @author cstamas
 */
@Component( role = Filter.class, hint = "localNexusInstance" )
public class LocalNexusInstanceFilter
    extends Filter
{
    @Requirement
    private Nexus nexus;

    /**
     * The filter constructor.
     */
    public LocalNexusInstanceFilter()
    {
        super();
    }

    /**
     * The filter constructor.
     */
    public LocalNexusInstanceFilter( Context context )
    {
        super( context );
    }

    /**
     * A beforeHandle will simply embed in request attributes the local Nexus instance.
     */
    protected int beforeHandle( Request request, Response response )
    {
        request.getAttributes().put( Nexus.class.getName(), nexus );

        return CONTINUE;
    }
}
