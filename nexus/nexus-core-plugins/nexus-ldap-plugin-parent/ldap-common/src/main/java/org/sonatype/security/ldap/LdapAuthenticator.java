package org.sonatype.security.ldap;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.realm.ldap.LdapContextFactory;
import org.jsecurity.realm.ldap.LdapUtils;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.password.PasswordEncoderManager;

@Component( role = LdapAuthenticator.class )
public class LdapAuthenticator
{
    @Requirement
    private PasswordEncoderManager passwordManager;
    
    public void authenticateUserWithPassword( LdapUser ldapUser, String password ) throws AuthenticationException
    {
        // use the passwordmanager
        if ( !this.passwordManager.isPasswordValid( ldapUser.getPassword(), password, null ) )
        {
            throw new AuthenticationException( "User '" + ldapUser.getUsername() + "' cannot be authenticated." );
        }
    }

    public void authenticateUserWithBind( LdapUser ldapUser, String password,
        LdapContextFactory ldapContextFactory, String authScheme ) throws AuthenticationException
    {
        String userId = ldapUser.getUsername();

        // Binds using the username and password provided by the user.

        String bindUsername = ldapUser.getDn();

        // if we are authorizing against DIGEST-MD5 or CRAM-MD5 then username is not the DN
        if ( "DIGEST-MD5".equals( authScheme ) || "CRAM-MD5".equals( authScheme ) )
        {
            bindUsername = userId;
        }

        // check using bind
        this.checkPasswordUsingBind( ldapContextFactory, bindUsername, password );

    }

    private void checkPasswordUsingBind( LdapContextFactory ldapContextFactory, String user, String pass )
        throws AuthenticationException
    {
        LdapContext ctx = null;
        try
        {
            ctx = ldapContextFactory.getLdapContext( user, pass );
            ctx.getAttributes( "" );
        }
        catch ( javax.naming.AuthenticationException e )
        {
            throw new AuthenticationException( "User '" + user + "' cannot be authenticated.", e );
        }
        catch ( NamingException e )
        {
            throw new AuthenticationException( "User '" + user + "' cannot be authenticated.", e );
        }
        finally
        {
            LdapUtils.closeContext( ctx );
        }
    }

}
