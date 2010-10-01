/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.SortControl;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.ldap.dao.password.PasswordEncoderManager;


/**
 * @author cstamas
 */
@Component( role = LdapUserDAO.class )
public class DefaultLdapUserDAO
    extends AbstractLogEnabled
    implements LdapUserDAO
{

    @Requirement
    private PasswordEncoderManager passwordEncoderManager;

    public PasswordEncoderManager getPasswordEncoderManager()
    {
        return passwordEncoderManager;
    }

    public void setPasswordEncoderManager( PasswordEncoderManager passwordEncoder )
    {
        this.passwordEncoderManager = passwordEncoder;
    }

    public void removeUser( String username, LdapContext context, LdapAuthConfiguration configuration )
        throws NoSuchLdapUserException, LdapDAOException
    {
        getLogger().info( "Searching for user: " + username );

        try
        {
            context = (LdapContext) context.lookup( StringUtils.defaultString( configuration.getUserBaseDn(), "" ) );

            context.destroySubcontext( configuration.getUserIdAttribute() + "=" + username );
        }
        catch ( NamingException e )
        {
            String message = "Failed to remove user: " + username;

            throw new LdapDAOException( message, e );
        }
    }

    public void updateUser( LdapUser user, LdapContext context, LdapAuthConfiguration configuration )
        throws NoSuchLdapUserException, LdapDAOException
    {
        LdapUser inLdap = getUser( user.getUsername(), context, configuration );

        String userIdAttribute = configuration.getUserIdAttribute();
        String userBaseDn = StringUtils.defaultString( configuration.getUserBaseDn(), "" );

        LdapContext userContext;

        try
        {
            userContext = (LdapContext) context.lookup( userBaseDn );
        }
        catch ( NamingException e )
        {
            String message = "Failed to create user for: " + user.getUsername();

            throw new LdapDAOException( message, e );
        }

        Attributes addAttrs = new BasicAttributes();

        Attributes modAttrs = new BasicAttributes();

        if ( !StringUtils.isEmpty( user.getRealName() ) )
        {
            if ( inLdap.getRealName() == null )
            {
                addAttrs.put( configuration.getUserRealNameAttribute(), user.getRealName() );
            }
            else if ( !user.getRealName().equals( inLdap.getRealName() ) )
            {
                modAttrs.put( configuration.getUserRealNameAttribute(), user.getRealName() );
            }
        }

        if ( !StringUtils.isEmpty( user.getEmail() ) )
        {
            if ( inLdap.getEmail() == null )
            {
                addAttrs.put( configuration.getEmailAddressAttribute(), user.getEmail() );
            }
            else if ( !user.getEmail().equals( inLdap.getEmail() ) )
            {
                modAttrs.put( configuration.getEmailAddressAttribute(), user.getEmail() );
            }
        }

        if ( !StringUtils.isEmpty( user.getWebsite() ) )
        {
            if ( inLdap.getWebsite() == null )
            {
                if ( configuration.isWebsiteAttributeLabelUri() )
                {
                    addAttrs.put( configuration.getWebsiteAttribute(), user.getWebsite() + " "
                        + configuration.getWebsiteUriLabel() );
                }
                else
                {
                    addAttrs.put( configuration.getWebsiteAttribute(), user.getWebsite() );
                }
            }
            else if ( !user.getWebsite().equals( inLdap.getWebsite() ) )
            {
                if ( configuration.isWebsiteAttributeLabelUri() )
                {
                    modAttrs.put( configuration.getWebsiteAttribute(), user.getWebsite() + " "
                        + configuration.getWebsiteUriLabel() );
                }
                else
                {
                    modAttrs.put( configuration.getWebsiteAttribute(), user.getWebsite() );
                }
            }
        }

        if ( addAttrs.size() > 0 )
        {
            try
            {
                userContext.modifyAttributes( userIdAttribute + "=" + user.getUsername(), LdapContext.ADD_ATTRIBUTE,
                                              addAttrs );
            }
            catch ( NamingException e )
            {
                String message = "Failed to update user: " + user.getUsername();

                throw new LdapDAOException( message, e );
            }
        }

        if ( modAttrs.size() > 0 )
        {
            try
            {
                userContext.modifyAttributes( userIdAttribute + "=" + user.getUsername(),
                                              LdapContext.REPLACE_ATTRIBUTE, modAttrs );
            }
            catch ( NamingException e )
            {
                String message = "Failed to update user: " + user.getUsername();

                throw new LdapDAOException( message, e );
            }
        }

    }

    public void changePassword( String username, String password, LdapContext context,
                                LdapAuthConfiguration configuration )
        throws NoSuchLdapUserException, LdapDAOException
    {
        String userIdAttribute = configuration.getUserIdAttribute();
        String userBaseDn = StringUtils.defaultString( configuration.getUserBaseDn(), "" );
        String passwordAttribute = configuration.getPasswordAttribute();

        try
        {
            NamingEnumeration<SearchResult> existing =
                searchUsers( username, context, new String[] { userIdAttribute }, configuration, 1 );

            if ( !existing.hasMoreElements() )
            {
                throw new NoSuchLdapUserException( username );
            }
        }
        catch ( NamingException e )
        {
            String message = "Error while checking for existing user: " + username;

            throw new LdapDAOException( message, e );
        }

        LdapContext userContext;

        try
        {
            userContext = (LdapContext) context.lookup( userBaseDn );
        }
        catch ( NamingException e )
        {
            String message = "Failed to change password for: " + username;

            throw new LdapDAOException( message, e );
        }

        Attributes userAttrs = new BasicAttributes();
        userAttrs.put( passwordAttribute, passwordEncoderManager.encodePassword( password, null ) );

        try
        {
            userContext.modifyAttributes( userIdAttribute + "=" + username, LdapContext.REPLACE_ATTRIBUTE, userAttrs );
        }
        catch ( NamingException e )
        {
            String message = "Failed to update user for: " + username;

            throw new LdapDAOException( message );
        }
    }

    public NamingEnumeration<SearchResult> searchUsers( String username, LdapContext context,
                                                        LdapAuthConfiguration configuration, long limitCount )
        throws NamingException
    {
        return searchUsers( username, context, null, configuration, limitCount );
    }

    public NamingEnumeration<SearchResult> searchUsers( LdapContext context, LdapAuthConfiguration configuration,
                                                        long limitCount )
        throws NamingException
    {
        return searchUsers( null, context, null, configuration, limitCount );
    }

    public NamingEnumeration<SearchResult> searchUsers( LdapContext context, String[] returnAttributes,
                                                        LdapAuthConfiguration configuration, long limitCount )
        throws NamingException
    {
        return searchUsers( null, context, returnAttributes, configuration, limitCount );
    }

    public NamingEnumeration<SearchResult> searchUsers( String username, LdapContext context,
                                                        String[] returnAttributes, LdapAuthConfiguration configuration,
                                                        long limitCount )
        throws NamingException
    {
        String[] userAttributes = returnAttributes;
        if ( userAttributes == null )
        {
            userAttributes = configuration.getUserAttributes();
        }

        SearchControls ctls = new SearchControls();

        ctls.setDerefLinkFlag( true );
        ctls.setSearchScope( configuration.isUserSubtree() ? SearchControls.SUBTREE_SCOPE
                        : SearchControls.ONELEVEL_SCOPE );
        ctls.setReturningAttributes( userAttributes );

        if ( limitCount > 0 )
        {
            ctls.setCountLimit( limitCount );
        }

        String filter =
            "(&(objectClass=" + configuration.getUserObjectClass() + ")(" + configuration.getUserIdAttribute() + "="
                + ( username != null ? username : "*" ) + "))";

        getLogger().debug( "Searching for users with filter: \'" + filter + "\'" );
        
        String baseDN = StringUtils.defaultString( configuration.getUserBaseDn(), "" );

        return context.search( baseDN, filter, ctls );
    }

    public SortedSet<LdapUser> getUsers( LdapContext context, LdapAuthConfiguration configuration, long limitCount )
        throws LdapDAOException
    {
        return this.getUsers( null, context, configuration, limitCount );
    }

    public SortedSet<LdapUser> getUsers( String username, LdapContext context, LdapAuthConfiguration configuration,
                                         long limitCount )
        throws LdapDAOException
    {
        try
        {
            NamingEnumeration<SearchResult> results = searchUsers( username, context, configuration, limitCount );
            SortedSet<LdapUser> users = new TreeSet<LdapUser>();

            while ( results.hasMoreElements() )
            {
                SearchResult result = results.nextElement();
                users.add( createUser( result, configuration ) );
            }

            return users;
        }
        catch ( NamingException e )
        {
            String message = "Failed to retrieve ldap information for users.";

            throw new LdapDAOException( message, e );
        }
    }

    public void createUser( LdapUser user, LdapContext context, LdapAuthConfiguration configuration )
        throws LdapDAOException
    {
        String userIdAttribute = configuration.getUserIdAttribute();
        String userBaseDn = StringUtils.defaultString( configuration.getUserBaseDn(), "" );

        try
        {
            NamingEnumeration<SearchResult> existing =
                searchUsers( user.getUsername(), context, new String[] { userIdAttribute }, configuration, 1 );

            if ( existing.hasMoreElements() )
            {
                throw new LdapDAOException( "User: " + user.getUsername() + " already exists!" );
            }
        }
        catch ( NamingException e )
        {
            String message = "Error while checking for existing user: " + user.getUsername();

            throw new LdapDAOException( message, e );
        }

        LdapContext userContext;

        try
        {
            userContext = (LdapContext) context.lookup( userBaseDn );
        }
        catch ( NamingException e )
        {
            String message = "Failed to create user for: " + user.getUsername();

            throw new LdapDAOException( message, e );
        }

        Attributes userAttrs = new BasicAttributes();

        if ( !StringUtils.isEmpty( user.getPassword() ) )
        {
            userAttrs.put( configuration.getPasswordAttribute(),
                           passwordEncoderManager.encodePassword( user.getPassword(), null ) );
        }

        if ( !StringUtils.isEmpty( user.getRealName() ) )
        {
            userAttrs.put( configuration.getUserRealNameAttribute(), user.getRealName() );
        }

        if ( !StringUtils.isEmpty( user.getEmail() ) )
        {
            userAttrs.put( configuration.getEmailAddressAttribute(), user.getEmail() );
        }

        if ( !StringUtils.isEmpty( user.getWebsite() ) )
        {
            if ( configuration.isWebsiteAttributeLabelUri() )
            {
                userAttrs.put( configuration.getWebsiteAttribute(), user.getWebsite() + " "
                    + configuration.getWebsiteUriLabel() );
            }
            else
            {
                userAttrs.put( configuration.getWebsiteAttribute(), user.getWebsite() );
            }
        }

        try
        {
            userContext.createSubcontext( userIdAttribute + "=" + user.getUsername(), userAttrs );
        }
        catch ( NamingException e )
        {
            String message = "Failed to create user for: " + user.getUsername();

            throw new LdapDAOException( message, e );
        }
    }

    public LdapUser getUser( String username, LdapContext context, LdapAuthConfiguration configuration )
        throws NoSuchLdapUserException, LdapDAOException
    {
        getLogger().debug( "Searching for user: " + username );

        try
        {
            NamingEnumeration<SearchResult> result = searchUsers( username, context, null, configuration, 1 );

            if ( result.hasMoreElements() )
            {
                return createUser( result.nextElement(), configuration );
            }
            else
            {
                throw new NoSuchLdapUserException( "A user with username '" + username + "' does not exist" );
            }
        }
        catch ( NamingException e )
        {
            String message = "Failed to retrieve information for user: " + username;

            throw new LdapDAOException( message, e );
        }
    }

    private LdapUser createUser( SearchResult result, LdapAuthConfiguration configuration )
        throws LdapDAOException
    {
        Attributes attributes = result.getAttributes();

        LdapUser user = new LdapUser();

        String userIdAttribute = configuration.getUserIdAttribute();
        String emailAddressAttribute = configuration.getEmailAddressAttribute();
        String nameAttribute = configuration.getUserRealNameAttribute();
        String websiteAttribute = configuration.getWebsiteAttribute();
        String websiteUriLabel = configuration.getWebsiteUriLabel();
        String passwordAttribute = configuration.getPasswordAttribute();
        String memberOfAttribute = configuration.getUserMemberOfAttribute();

        user.setUsername( LdapUtils.getAttributeValue( attributes, userIdAttribute, "username" ) );
        user.setDn( result.getNameInNamespace() );
        user.setEmail( LdapUtils.getAttributeValue( attributes, emailAddressAttribute, "email address" ) );
        user.setRealName( LdapUtils.getAttributeValue( attributes, nameAttribute, "name" ) );
        user.setPassword( LdapUtils.getAttributeValueFromByteArray( attributes, passwordAttribute, "password" ) );

        if ( configuration.isWebsiteAttributeLabelUri() )
        {
            user.setWebsite( LdapUtils.getLabeledUriValue( attributes, websiteAttribute, websiteUriLabel, "website" ) );
        }
        else
        {
            user.setWebsite( LdapUtils.getAttributeValue( attributes, websiteAttribute, "website" ) );
        }

        // The user might contain the groups that he is in
        if ( StringUtils.isNotEmpty( configuration.getUserMemberOfAttribute() ) )
        {
            Set<String> groups = LdapUtils.getAttributeValues( attributes, memberOfAttribute, "Member Of" );
            Set<String> resolvedGroups = new HashSet<String>();
            // now these groups are fully qualified dn's, if not we will return the full entry
            for ( String dnString : groups )
            {
                resolvedGroups.add( this.getGroupFromString( dnString ) );
            }

            user.setMembership( resolvedGroups );
        }

        return user;
    }

    private String getGroupFromString( String dnString )
    {
        String result = dnString;
        try
        {
            LdapName dn = new LdapName( dnString );
            result = String.valueOf( dn.getRdn( dn.size() - 1 ).getValue() );
        }
        catch ( InvalidNameException e )
        {
            this.getLogger().debug( "Expected a Group DN but found: " + dnString );
        }
        return result;
    }

}
