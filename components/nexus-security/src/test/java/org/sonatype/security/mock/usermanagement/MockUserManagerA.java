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
package org.sonatype.security.mock.usermanagement;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.UserManager;

@Singleton
@Typed( UserManager.class )
@Named( "MockUserManagerA" )
public class MockUserManagerA
    extends AbstractMockUserManager

{
    public MockUserManagerA()
    {

        DefaultUser a = new DefaultUser();
        a.setName( "Joe Coder" );
        a.setEmailAddress( "jcoder@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "jcoder" );
        a.addRole( new RoleIdentifier( this.getSource(), "RoleA" ) );
        a.addRole( new RoleIdentifier( this.getSource(), "RoleB" ) );
        a.addRole( new RoleIdentifier( this.getSource(), "RoleC" ) );

        DefaultUser b = new DefaultUser();
        b.setName( "Christine H. Dugas" );
        b.setEmailAddress( "cdugas@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "cdugas" );
        b.addRole( new RoleIdentifier( this.getSource(), "RoleA" ) );
        b.addRole( new RoleIdentifier( this.getSource(), "RoleB" ) );
        b.addRole( new RoleIdentifier( this.getSource(), "Role1" ) );

        DefaultUser c = new DefaultUser();
        c.setName( "Patricia P. Peralez" );
        c.setEmailAddress( "pperalez@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "pperalez" );

        DefaultUser d = new DefaultUser();
        d.setName( "Danille S. Knudsen" );
        d.setEmailAddress( "dknudsen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "dknudsen" );

        DefaultUser e = new DefaultUser();
        e.setName( "Anon e Mous" );
        e.setEmailAddress( "anonymous@sonatype.org" );
        e.setSource( this.getSource() );
        e.setUserId( "anonymous-user" );

        this.addUser( a, a.getUserId() );
        this.addUser( b, b.getUserId() );
        this.addUser( c, c.getUserId() );
        this.addUser( d, d.getUserId() );
        this.addUser( e, e.getUserId() );
    }

    public String getSource()
    {
        return "MockUserManagerA";
    }

    public String getAuthenticationRealmName()
    {
        return "MockRealmA";
    }

}
