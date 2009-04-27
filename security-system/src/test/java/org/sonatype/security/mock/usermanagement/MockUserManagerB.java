package org.sonatype.security.mock.usermanagement;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.UserManager;

@Component( role = UserManager.class, hint = "MockUserManagerB" )
public class MockUserManagerB
    extends AbstractMockUserManager

{
    public MockUserManagerB()
    {

        DefaultUser a = new DefaultUser();
        a.setName( "Brenda D. Burton" );
        a.setEmailAddress( "bburton@sonatype.org" );
        a.setSource( this.getSource() );
        a.setUserId( "bburton" );
        a.addRole( new Role( "RoleA", "", this.getSource() ) );
        a.addRole( new Role( "RoleB", "", this.getSource() ) );
        a.addRole( new Role( "RoleC", "", this.getSource() ) );

        DefaultUser b = new DefaultUser();
        b.setName( "Julian R. Blevins" );
        b.setEmailAddress( "jblevins@sonatype.org" );
        b.setSource( this.getSource() );
        b.setUserId( "jblevins" );
        b.addRole( new Role( "RoleA", "", this.getSource() ) );
        b.addRole( new Role( "RoleB", "", this.getSource() ) );

        DefaultUser c = new DefaultUser();
        c.setName( "Kathryn J. Simmons" );
        c.setEmailAddress( "ksimmons@sonatype.org" );
        c.setSource( this.getSource() );
        c.setUserId( "ksimmons" );
        c.addRole( new Role( "RoleA", "", this.getSource() ) );
        c.addRole( new Role( "RoleB", "", this.getSource() ) );

        DefaultUser d = new DefaultUser();
        d.setName( "Florence T. Dahmen" );
        d.setEmailAddress( "fdahmen@sonatype.org" );
        d.setSource( this.getSource() );
        d.setUserId( "fdahmen" );
        d.addRole( new Role( "RoleA", "", this.getSource() ) );
        d.addRole( new Role( "RoleB", "", this.getSource() ) );

        DefaultUser e = new DefaultUser();
        e.setName( "Jill  Codar" );
        e.setEmailAddress( "jcodar@sonatype.org" );
        e.setSource( this.getSource() );
        e.setUserId( "jcodar" );

        DefaultUser f = new DefaultUser();
        f.setName( "Joe Coder" );
        f.setEmailAddress( "jcoder@sonatype.org" );
        f.setSource( this.getSource() );
        f.setUserId( "jcoder" );
        f.addRole( new Role( "Role1", "", this.getSource() ) );
        f.addRole( new Role( "Role2", "", this.getSource() ) );
        f.addRole( new Role( "Role3", "", this.getSource() ) );

        this.addUser( a );
        this.addUser( b );
        this.addUser( c );
        this.addUser( d );
        this.addUser( e );
        this.addUser( f );
    }

    public String getSource()
    {
        return "MockUserManagerB";
    }

}
