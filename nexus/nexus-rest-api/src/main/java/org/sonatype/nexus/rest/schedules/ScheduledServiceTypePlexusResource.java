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
package org.sonatype.nexus.rest.schedules;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.FormFieldResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResourceResponse;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "ScheduledServiceTypePlexusResource" )
@Path( ScheduledServiceTypePlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class ScheduledServiceTypePlexusResource
    extends AbstractScheduledServicePlexusResource
{
    public static final String RESOURCE_URI = "/schedule_types";

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:tasktypes]" );
    }

    /**
     * Get the list of scheduled service types available in nexus. And all of the configuration parameters available for
     * each type.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = ScheduledServiceTypeResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        ScheduledServiceTypeResourceResponse result = new ScheduledServiceTypeResourceResponse();

        List<ScheduledTaskDescriptor> taskDescriptors = getNexusConfiguration().listScheduledTaskDescriptors();

        for ( ScheduledTaskDescriptor taskDescriptor : taskDescriptors )
        {
            if ( taskDescriptor.isExposed() )
            {
                ScheduledServiceTypeResource type = new ScheduledServiceTypeResource();
                type.setId( taskDescriptor.getId() );
                type.setName( taskDescriptor.getName() );

                type.setFormFields( (List<FormFieldResource>) formFieldToDTO( taskDescriptor.formFields(),
                                                                              FormFieldResource.class ) );

                result.addData( type );
            }
        }

        sortTaskType( result.getData() );

        return result;
    }

    private void sortTaskType( List<ScheduledServiceTypeResource> types )
    {
        Collections.sort( types, new Comparator<ScheduledServiceTypeResource>()
        {
            public int compare( ScheduledServiceTypeResource t1, ScheduledServiceTypeResource t2 )
            {
                return ( t1.getName() ).compareTo( t2.getName() );
            }
        } );
    }

}
