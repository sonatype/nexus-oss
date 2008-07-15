package org.sonatype.nexus.rest;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * A simple restlet that is returned as root, while allowing to recreate roots in applications per application request.
 * 
 * @author cstamas
 */
public class RetargetableRestlet
    extends Restlet
{
    private Restlet root;

    public RetargetableRestlet( Context context )
    {
        super( context );
    }

    @Override
    public void handle( Request request, Response response )
    {
        super.handle( request, response );

        Restlet next = getRoot();

        if ( next != null )
        {
            next.handle( request, response );
        }
        else
        {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
        }
    }

    public Restlet getRoot()
    {
        return root;
    }

    public void setRoot( Restlet root )
    {
        this.root = root;
    }
}
