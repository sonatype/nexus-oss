package org.sonatype.nexus.proxy;

/**
 * IllegalOperationException is thrown when an illegal action is tried against a ResourceStore (ie. write to a read only,
 * unavailable, etc.). Previously it was (wrongly) AccessDeniedException used to mark these problems, and it caused
 * problems on REST API to distinct an "authz problem = accessDenied = HTTP 401" and "bad request = HTTP 400".
 * 
 * @author cstamas
 */
public abstract class IllegalOperationException
    extends Exception
{
    private static final long serialVersionUID = -1075426559861827023L;

    public IllegalOperationException( String message )
    {
        super( message );
    }

    public IllegalOperationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public IllegalOperationException( Throwable cause )
    {
        super( cause );
    }
}
