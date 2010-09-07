package org.sonatype.security.ldap.dao;

import java.util.SortedSet;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.realm.ldap.LdapContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component( role = LdapConnectionTester.class )
public class DefaultLdapConnectionTester
    implements LdapConnectionTester
{

    @Requirement
    private LdapUserDAO ldapUserDao;

    @Requirement
    private LdapGroupDAO ldapGroupDAO;

    private Logger logger = LoggerFactory.getLogger( getClass() );

    public void testConnection( LdapContextFactory ldapContextFactory )
        throws NamingException
    {
        // get the connection and close it, if this throws an exception, then the config is wrong.
        LdapContext ctx = null;
        try
        {
            ctx = ldapContextFactory.getSystemLdapContext();
            ctx.getAttributes( "" );
        }
        finally
        {
            if ( ctx != null )
            {
                try
                {
                    ctx.close();
                }
                catch ( NamingException e )
                {
                    // ignore, it might not even be open
                }
            }
        }

    }

    public SortedSet<LdapUser> testUserAndGroupMapping( LdapContextFactory ldapContextFactory,
        LdapAuthConfiguration ldapAuthConfiguration, int numberOfResults )
        throws LdapDAOException,
            NamingException
    {
        LdapContext ctx = ldapContextFactory.getSystemLdapContext();
        try
        {
            SortedSet<LdapUser> users = this.ldapUserDao.getUsers(
                ldapContextFactory.getSystemLdapContext(),
                ldapAuthConfiguration,
                numberOfResults );

            if ( ldapAuthConfiguration.isLdapGroupsAsRoles()
                && StringUtils.isEmpty( ldapAuthConfiguration.getUserMemberOfAttribute() ) )
                for ( LdapUser ldapUser : users )
                {
                    try
                    {
                        ldapUser.setMembership( this.ldapGroupDAO.getGroupMembership( ldapUser.getUsername(), ctx, ldapAuthConfiguration ) );
                    }
                    catch ( NoLdapUserRolesFoundException e )
                    {
                        // this is ok, the users has no roles, not a problem
                        if ( logger.isDebugEnabled() )
                        {
                            this.logger.debug( "While testing for user mapping user: " + ldapUser.getUsername()
                                + " had no roles." );
                        }
                    }
                }
            return users;
        }
        finally
        {
            try
            {
                ctx.close();
            }
            catch ( NamingException e )
            {
                // ignore, it might not even be open
            }
        }
    }

}
