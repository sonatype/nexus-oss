/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.schedules;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceTypePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResourceResponse;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "ScheduledServiceTypePlexusResource" )
public class ScheduledServiceTypePlexusResource
    extends AbstractScheduledServicePlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/schedule_types";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:tasktypes]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        ScheduledServiceTypeResourceResponse result = new ScheduledServiceTypeResourceResponse();

        List<ScheduledTaskDescriptor> taskDescriptors = getNexus().listScheduledTaskDescriptors();

        for ( ScheduledTaskDescriptor taskDescriptor : taskDescriptors )
        {
            if ( taskDescriptor.isExposed() )
            {
                ScheduledServiceTypeResource type = new ScheduledServiceTypeResource();
                type.setId( taskDescriptor.getId() );
                type.setName( taskDescriptor.getName() );

                for ( ScheduledTaskPropertyDescriptor propertyDescriptor : taskDescriptor.getPropertyDescriptors() )
                {
                    ScheduledServiceTypePropertyResource property = new ScheduledServiceTypePropertyResource();
                    property.setHelpText( propertyDescriptor.getHelpText() );
                    property.setId( propertyDescriptor.getId() );
                    property.setName( propertyDescriptor.getName() );
                    property.setRequired( propertyDescriptor.isRequired() );
                    property.setType( propertyDescriptor.getType() );

                    type.addProperty( property );
                }

                result.addData( type );
            }
        }

        return result;
    }

}
