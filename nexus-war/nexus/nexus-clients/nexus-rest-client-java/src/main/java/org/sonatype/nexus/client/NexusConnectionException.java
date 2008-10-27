package org.sonatype.nexus.client;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.rest.model.NexusError;

/**
 * Thrown when a NexusClient cannot connect to a Nexus instance, or the Nexus instance returns a non success response.
 */
public class NexusConnectionException
    extends Exception
{

    /**
     * Errors returned from a Nexus server.
     */
    private List<NexusError> errors = new ArrayList<NexusError>();
    
    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -5163493126499979929L;

    public NexusConnectionException()
    {
        super();
    }
    
    public NexusConnectionException( List<NexusError> errors )
    {
        super();
        this.errors = errors;
    }

    public NexusConnectionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NexusConnectionException( String message, Throwable cause, List<NexusError> errors  )
    {
        super( message, cause );
        this.errors = errors;
        
    }
    
    public NexusConnectionException( String message )
    {
        super( message );
    }
    

    public NexusConnectionException( String message, List<NexusError> errors  )
    {
        super( message );
        this.errors = errors;
    }

    public NexusConnectionException( Throwable cause )
    {
        super( cause );
    }
    
    public NexusConnectionException( Throwable cause, List<NexusError> errors  )
    {
        super( cause );
        this.errors = errors;
    }
    
    /**
     * A list of errors returned from the server, if any.  Could be empty or null.
     * 
     * @return A List of errors returned from the server.
     */
    public List<NexusError> getErrors()
    {
        return errors;
    }

    @Override
    public String getMessage()
    {
        StringBuffer message = new StringBuffer(super.getMessage());
        
        if(this.getErrors() != null)
        {
            for ( NexusError error : this.getErrors() )
            {
                message.append( "\n" ).append( error.getMsg() );
            }
        }
        
        return message.toString();
    }
    
    

}
