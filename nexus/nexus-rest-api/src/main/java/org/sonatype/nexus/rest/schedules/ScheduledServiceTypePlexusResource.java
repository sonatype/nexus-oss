package org.sonatype.nexus.rest.schedules;

import java.util.List;

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

/**
 * @author tstevens
 * @plexus.component role-hint="ScheduledServiceTypePlexusResource"
 */
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

        List<ScheduledTaskDescriptor> taskDescriptors = getNexusInstance( request ).listScheduledTaskDescriptors();

        for ( ScheduledTaskDescriptor taskDescriptor : taskDescriptors )
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

        return result;
    }

}
