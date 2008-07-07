package org.sonatype.nexus.rest.users;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

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
        //TODO: actually reset the password
    }
}