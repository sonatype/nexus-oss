package org.sonatype.nexus.rest.users;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.security.NoSuchUserException;

public class UserResetResourceHandler
    extends AbstractUserResourceHandler
{    
    private String userId;
    
    public UserResetResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
        
        this.userId = getRequest().getAttributes().get( USER_ID_KEY ).toString();
    }
    
    protected String getUserId()
    {
        return this.userId;
    }
    
    @Override
    public boolean allowDelete()
    {
        return true;
    }
    
    @Override
    public void delete()
    {
        try
        {
            getNexusSecurityConfiguration().resetPassword( getUserId() );
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Got IO Exception!", e );
        }
        catch ( NoSuchUserException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "User ID not found!" );
            
            getLogger().log( Level.FINE, "Invalid userid: " + getUserId(), e );
        }
    }
}