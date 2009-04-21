package org.sonatype.security;

import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchUserException;
import org.sonatype.security.email.NoSuchEmailException;
import org.sonatype.security.events.SecurityEventHandler;

public interface PlexusSecurity extends ConfigurationManager
{

    void forgotPassword( String userId, String email )
    throws NoSuchUserException,
        NoSuchEmailException;

    void forgotUsername( String email, String... ignoredUserIds )
        throws NoSuchEmailException;
    
    void resetPassword( String userId )
        throws NoSuchUserException;
    
    void changePassword( String userId, String oldPassword, String newPassword )
        throws NoSuchUserException,
            InvalidCredentialsException;
    
    void changePassword( String userId, String newPassword )
        throws NoSuchUserException;
    
    boolean isAnonymousAccessEnabled();
    
    String getAnonymousUsername();

    boolean isSecurityEnabled();
        
    void addSecurityEventHandler( SecurityEventHandler eventHandler );
    
    boolean removeSecurityEventHandler( SecurityEventHandler eventHandler );
    
}
