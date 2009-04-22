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
package org.sonatype.jsecurity.locators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.UserManager;

@Component(role=UserManager.class, hint="MockUserLocatorB")
public class MockUserLocatorB
    extends AbstractTestUserLocator
{

    public String getSource()
    {
        return "MockUserLocatorB";
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();
        
        PlexusUser a = new PlexusUser();
        a.setName( "Brenda D. Burton" );
        a.setEmailAddress( "bburton@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "bburton" );
        a.addRole( this.createFakeRole( "RoleA" ) );
        a.addRole( this.createFakeRole( "RoleB" ) );
        a.addRole( this.createFakeRole( "RoleC" ) );
        
        PlexusUser b = new PlexusUser();
        b.setName( "Julian R. Blevins" );
        b.setEmailAddress( "jblevins@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "jblevins" );
        b.addRole( this.createFakeRole( "RoleA" ) );
        b.addRole( this.createFakeRole( "RoleB" ) );
        
        PlexusUser c = new PlexusUser();
        c.setName( "Kathryn J. Simmons" );
        c.setEmailAddress( "ksimmons@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "ksimmons" );
        c.addRole( this.createFakeRole( "RoleA" ) );
        c.addRole( this.createFakeRole( "RoleB" ) );

        PlexusUser d = new PlexusUser();
        d.setName( "Florence T. Dahmen" );
        d.setEmailAddress( "fdahmen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "fdahmen" );
        d.addRole( this.createFakeRole( "RoleA" ) );
        d.addRole( this.createFakeRole( "RoleB" ) );
        
        PlexusUser e = new PlexusUser();
        e.setName( "Jill  Codar" );
        e.setEmailAddress( "jcodar@sonatype.org" );
        e.setSource( this.getSource() );
        e.setUserId( "jcodar" );
        
//        PlexusUser f = new PlexusUser();
//        f.setName( "Joe Coder" );
//        f.setEmailAddress( "jcoder@sonatype.org" );
//        f.setSource( this.getSource() );
//        f.setUserId( "jcoder" );
//        f.addRole( this.createFakeRole( "Role1" ) );
//        f.addRole( this.createFakeRole( "Role2" ) );
//        f.addRole( this.createFakeRole( "Role3" ) );
        
        users.add( a );
        users.add( b );
        users.add( c );
        users.add( d );
        users.add( e );
//        users.add( f );
        
        return users;
    }

    public Set<PlexusRole> getUsersAdditinalRoles( String userId )
    {
        
        Map<String, Set<PlexusRole>> userToRoleMap = new HashMap<String, Set<PlexusRole>>();
        
        Set<PlexusRole> roles1 = new HashSet<PlexusRole>();
        
        roles1.add( new PlexusRole("ExtraRole1", "ExtraRole1", this.getSource()) );
        roles1.add( new PlexusRole("ExtraRole2", "ExtraRole2", this.getSource()) );
        userToRoleMap.put( "jcoder", roles1 );
        
        
        
        return userToRoleMap.get( userId );
    }

}
