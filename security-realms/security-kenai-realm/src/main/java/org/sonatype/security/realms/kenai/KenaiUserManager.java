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
package org.sonatype.security.realms.kenai;

import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.inject.Description;
import org.sonatype.security.usermanagement.AbstractReadOnlyUserManager;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserSearchCriteria;

/**
 * A User Manager that uses the Kenai API from java.net.  NOTE: to test your login go to java.net/people.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( value = UserManager.class )
@Named( value = "kenai" )
@Description( value = "Kenai Realm Users" )
public class KenaiUserManager
    extends AbstractReadOnlyUserManager
{
    public static final String SOURCE = "kenai";

    public String getSource()
    {
        return SOURCE;
    }   

    public String getAuthenticationRealmName()
    {
        return SOURCE;
    }

    public Set<User> listUsers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<String> listUserIds()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public User getUser( String userId )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
