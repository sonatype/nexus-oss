/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
    private final Logger logger = LoggerFactory.getLogger( getClass() );

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
