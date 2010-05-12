package org.sonatype.security.email;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A null implementation of a SecurityEmailer.
 */
public class NullSecurityEmailer
    implements SecurityEmailer
{

    private Logger logger = LoggerFactory.getLogger( NullSecurityEmailer.class );

    public void sendForgotUsername( String email, List<String> userIds )
    {
        this.logger.error( "No SecurityEmailer, user will not be notified." );
    }

    public void sendNewUserCreated( String email, String userid, String password )
    {
        this.logger.error( "No SecurityEmailer, user will not be notified." );
    }

    public void sendResetPassword( String email, String password )
    {
        this.logger.error( "No SecurityEmailer, user will not be notified." );
    }

}
