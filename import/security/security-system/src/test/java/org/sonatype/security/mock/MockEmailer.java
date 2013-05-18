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
package org.sonatype.security.mock;

import java.util.List;

import org.sonatype.security.email.SecurityEmailer;

public class MockEmailer
    implements SecurityEmailer
{

    public List<String> forgotUserIds;

    public void sendForgotUsername( String email, List<String> userIds )
    {
        forgotUserIds = userIds;
    }

    public void sendNewUserCreated( String email, String userid, String password )
    {
    }

    public void sendResetPassword( String email, String password )
    {
    }

    public List<String> getForgotUserIds()
    {
        return forgotUserIds;
    }

    public void setForgotUserIds( List<String> forgotUserIds )
    {
        this.forgotUserIds = forgotUserIds;
    }

}
