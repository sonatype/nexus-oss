package org.sonatype.security.mock.usermanagement;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.UserManager;

@Component( role = UserManager.class, hint = "MockUserManagerA" )
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

        this.addUser( a, a.getUserId() );
        this.addUser( b, b.getUserId() );
        this.addUser( c, c.getUserId() );
        this.addUser( d, d.getUserId() );
    }

    public String getSource()
    {
        return "MockUserManagerA";
    }

}
