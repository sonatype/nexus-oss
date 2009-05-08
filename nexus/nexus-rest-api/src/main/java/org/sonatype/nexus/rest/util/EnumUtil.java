package org.sonatype.nexus.rest.util;

import org.restlet.data.Status;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

public class EnumUtil
{

    public static <E extends Enum<E>> E valueOf( String name, Class<E> enumClass )
        throws PlexusResourceException
    {
        if ( name == null )
        {
            throw validationError( name, enumClass );
        }
        try
        {
            return Enum.valueOf( enumClass, name );
        }
        catch ( IllegalArgumentException e )
        {
            throw validationError( name, enumClass );
        }
    }

    private static <E> PlexusResourceException validationError( String name, Class<E> enumClass )
    {
        ErrorMessage err = new ErrorMessage();
        err.setId( "*" );
        err.setMsg( "No enum const " + enumClass + "." + name );

        ErrorResponse ner = new ErrorResponse();
        ner.addError( err );

        return new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", ner );
    }

}
