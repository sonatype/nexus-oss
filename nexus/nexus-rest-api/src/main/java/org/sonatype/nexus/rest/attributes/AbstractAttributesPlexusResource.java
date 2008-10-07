package org.sonatype.nexus.rest.attributes;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.rest.restore.AbstractRestorePlexusResource;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;

public abstract class AbstractAttributesPlexusResource
    extends AbstractRestorePlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        RebuildAttributesTask task = (RebuildAttributesTask) getNexusInstance( request ).createTaskInstance(
            RebuildAttributesTaskDescriptor.ID );

        task.setRepositoryId( getRepositoryId( request ) );

        task.setRepositoryGroupId( getRepositoryGroupId( request ) );

        task.setResourceStorePath( getResourceStorePath( request ) );

        this.handleDelete( task, request );
    }

}
