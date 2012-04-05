/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.email.SecurityEmailer;

@Singleton
@Typed( value = SecurityEmailer.class )
@Named( value = "default" )
public class MockEmailer
    implements SecurityEmailer
{

    public void sendForgotUsername( String arg0, List<String> arg1 )
    {
        // TODO Auto-generated method stub

    }

    public void sendNewUserCreated( String arg0, String arg1, String arg2 )
    {
        // TODO Auto-generated method stub

    }

    public void sendResetPassword( String arg0, String arg1 )
    {
        // TODO Auto-generated method stub

    }

}
