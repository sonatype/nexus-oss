package org.sonatype.nexus.email;

import java.util.List;

public interface NexusEmailer
{
    String ROLE = NexusEmailer.class.getName();
    
    void sendNewUserCreated( String email, String userid, String password );
    
    void sendResetPassword( String email, String password );
    
    void sendForgotUsername( String email, List<String> userIds );
}
