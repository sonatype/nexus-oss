package org.sonatype.security.users;

public class DefaultUser
    implements User
{

    private String fullName;

    private String username;

    private String email;

    private Object principal;

    private Object session;

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName( String fullName )
    {
        this.fullName = fullName;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public Object getPrincipal()
    {
        return principal;
    }

    public void setPrincipal( Object principal )
    {
        this.principal = principal;
    }

    public Object getSession()
    {
        return session;
    }

    public void setSession( Object session )
    {
        this.session = session;
    }

  
}
