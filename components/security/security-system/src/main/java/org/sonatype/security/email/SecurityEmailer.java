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

/**
 * A Component use to notify a user when his/her password is changed or reset.
 */
public interface SecurityEmailer
{
    /**
     * Send an email to the user telling them they have a new account.
     * 
     * @param email
     * @param userid
     * @param password
     */
    void sendNewUserCreated( String email, String userid, String password );

    /**
     * Send an email to the user telling them their password has changed.
     * 
     * @param email
     * @param password
     */
    void sendResetPassword( String email, String password );

    /**
     * Send an email to the user reminding them of their username.
     * 
     * @param email
     * @param userIds
     */
    void sendForgotUsername( String email, List<String> userIds );
}
