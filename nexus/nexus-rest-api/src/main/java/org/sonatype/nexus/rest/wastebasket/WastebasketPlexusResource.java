package org.sonatype.nexus.rest.wastebasket;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.WastebasketResource;
import org.sonatype.nexus.rest.model.WastebasketResourceResponse;
import org.sonatype.nexus.tasks.EmptyTrashTask;

/**
 *
 * The Wastebasket resource handler. It returns the status of the wastebasket, and purges it.
 *
 * @author cstamas
 * @author tstevens
 * @plexus.component role-hint="wastebasket"
 *
 */
public class WastebasketPlexusResource
    extends AbstractNexusPlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/wastebasket";
    }

    @Override
    public Object get( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            WastebasketResourceResponse result = new WastebasketResourceResponse();

            WastebasketResource resource = new WastebasketResource();

            resource.setItemCount( getNexusInstance( request ).getWastebasketItemCount() );

            resource.setSize( getNexusInstance( request ).getWastebasketSize() );

            result.setData( resource );

            return result;

        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "IOException during configuration retrieval!", e );
        }
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        EmptyTrashTask task = (EmptyTrashTask) getNexusInstance( request ).createTaskInstance( EmptyTrashTask.HINT );

        getNexusInstance( request ).submit( "Internal", task );

        response.setStatus( Status.SUCCESS_NO_CONTENT );
    }

    @Override
    public boolean isModifiable()
    {
       return true;
    }
    
    
    
    

}
