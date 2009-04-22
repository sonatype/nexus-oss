/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security;

import org.sonatype.security.email.NoSuchEmailException;
import org.sonatype.security.events.SecurityEventHandler;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.NoSuchUserException;

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
