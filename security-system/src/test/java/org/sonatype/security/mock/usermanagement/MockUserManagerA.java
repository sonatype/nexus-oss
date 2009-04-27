package org.sonatype.security.mock.usermanagement;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.DefaultUser;
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
        a.addRole( new Role( "RoleA", "", this.getSource() ) );
        a.addRole( new Role( "RoleB", "", this.getSource() ) );
        a.addRole( new Role( "RoleC", "", this.getSource() ) );

        DefaultUser b = new DefaultUser();
        b.setName( "Christine H. Dugas" );
        b.setEmailAddress( "cdugas@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "cdugas" );
        b.addRole( new Role( "RoleA", "", this.getSource() ) );
        b.addRole( new Role( "RoleB", "", this.getSource() ) );
        b.addRole( new Role( "Role1", "", this.getSource() ) );

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

        this.addUser( a );
        this.addUser( b );
        this.addUser( c );
        this.addUser( d );
    }

    public String getSource()
    {
        return "MockUserManagerA";
    }

}
