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
package org.sonatype.security.realms.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.security.locators.SecurityXmlPlexusUserLocator;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.tools.dao.SecurityPrivilege;
import org.sonatype.security.realms.tools.dao.SecurityRole;
import org.sonatype.security.realms.tools.dao.SecurityUser;
import org.sonatype.security.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.security.realms.validator.ConfigurationValidator;
import org.sonatype.security.realms.validator.ValidationContext;
import org.sonatype.security.realms.validator.ValidationMessage;
import org.sonatype.security.realms.validator.ValidationResponse;

@Component( role = ConfigurationManager.class, hint = "default" )
public class DefaultConfigurationManager
    extends AbstractLogEnabled
    implements ConfigurationManager
{
    @org.codehaus.plexus.component.annotations.Configuration( value = "${security-xml-file}" )
    private File securityConfiguration;

    @Requirement
    private ConfigurationValidator validator;
    
    @Requirement( role = PrivilegeDescriptor.class )
    private List<PrivilegeDescriptor> privilegeDescriptors;
    
    @Requirement( role = SecurityConfigurationCleaner.class )
    private SecurityConfigurationCleaner configCleaner;

    /**
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private Configuration configuration = null;

    private ReentrantLock lock = new ReentrantLock();

    @SuppressWarnings( "unchecked" )
    public List<SecurityPrivilege> listPrivileges()
    {
        List<SecurityPrivilege> list = new ArrayList<SecurityPrivilege>();

        for ( CPrivilege item : (List<CPrivilege>) getConfiguration().getPrivileges() )
        {
            list.add( new SecurityPrivilege( item ) );
        }

        return list;
    }

    @SuppressWarnings( "unchecked" )
    public List<SecurityRole> listRoles()
    {
        List<SecurityRole> list = new ArrayList<SecurityRole>();

        for ( CRole item : (List<CRole>) getConfiguration().getRoles() )
        {
            list.add( new SecurityRole( item ) );
        }

        return list;
    }

    @SuppressWarnings( "unchecked" )
    public List<SecurityUser> listUsers()
    {
        List<SecurityUser> list = new ArrayList<SecurityUser>();

        for ( CUser user : (List<CUser>) getConfiguration().getUsers() )
        {
            // see if we have a userRole mapping
            List<String> roles = null;
            try
            {
                SecurityUserRoleMapping roleMapping = this.readUserRoleMapping( user.getId(), null );
                if ( roleMapping != null )
                {
                    roles = roleMapping.getRoles();
                }
            }
            catch ( NoSuchRoleMappingException e )
            {
                // this really should never happen, but validation could change
                // but its not going to hurt anything to keep going
                this.getLogger().debug( e.getMessage() );
            }

            list.add( new SecurityUser( user, false, roles ) );
        }

        return list;
    }

    public void createPrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException
    {
        createPrivilege( privilege, initializeContext() );
    }

    public void createPrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validatePrivilege( context, privilege, false );

        if ( vr.isValid() )
        {
            getConfiguration().addPrivilege( privilege );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void createRole( SecurityRole role )
        throws InvalidConfigurationException
    {
        createRole( role, initializeContext() );
    }

    public void createRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validateRole( context, role, false );

        if ( vr.isValid() )
        {
            getConfiguration().addRole( role );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void createUser( SecurityUser user )
        throws InvalidConfigurationException
    {
        createUser( user, null, initializeContext() );
    }

    public void createUser( SecurityUser user, String password )
        throws InvalidConfigurationException
    {
        createUser( user, password, initializeContext() );
    }

    public void createUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException
    {
        createUser( user, null, context );
    }

    public void createUser( SecurityUser user, String password, ValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        // set the password if its not null
        if ( password != null && password.trim().length() > 0 )
        {
            user.setPassword( StringDigester.getSha1Digest( password ) );
        }

        ValidationResponse vr = validator.validateUser( context, user, user.getRoles(), false );

        if ( vr.isValid() )
        {
            getConfiguration().addUser( user );
            this.createOrUpdateUserRoleMapping( this.getRoleMappingFromUser( user ) );

        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    private void createOrUpdateUserRoleMapping( SecurityUserRoleMapping roleMapping )
    {
        // delete first, ask questions later
        // we are always updating, its possible that this object could have already existed, because we cannot fully
        // sync with external realms.
        try
        {
            this.deleteUserRoleMapping( roleMapping.getUserId(), roleMapping.getSource() );
        }
        catch ( NoSuchRoleMappingException e )
        {
            // it didn't exist, thats ok.
        }

        // now add it
        this.getConfiguration().addUserRoleMapping( roleMapping );

    }

    private SecurityUserRoleMapping getRoleMappingFromUser( SecurityUser user )
    {
        SecurityUserRoleMapping roleMapping = new SecurityUserRoleMapping();

        roleMapping.setUserId( user.getId() );
        roleMapping.setSource( SecurityXmlPlexusUserLocator.SOURCE );
        roleMapping.setRoles( new ArrayList<String>( user.getRoles() ) );

        return roleMapping;

    }

    @SuppressWarnings( "unchecked" )
    public void deletePrivilege( String id )
        throws NoSuchPrivilegeException
    {
        deletePrivilege( id, true );
    }
    
    public void deletePrivilege( String id, boolean clean )
        throws NoSuchPrivilegeException
    {
        boolean found = false;
    
        for ( Iterator<CPrivilege> iter = getConfiguration().getPrivileges().iterator(); iter.hasNext(); )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }
    
        if ( !found )
        {
            throw new NoSuchPrivilegeException( id );
        }
     
        if ( clean )
        {
            cleanRemovedPrivilege( id );
        }
    }

    @SuppressWarnings( "unchecked" )
    public void deleteRole( String id )
        throws NoSuchRoleException
    {
        deleteRole( id, true );
    }
    
    protected void deleteRole( String id, boolean clean )
        throws NoSuchRoleException
    {
        boolean found = false;

        for ( Iterator<CRole> iter = getConfiguration().getRoles().iterator(); iter.hasNext(); )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }

        if ( !found )
        {
            throw new NoSuchRoleException( id );
        }
        
        if ( clean )
        {
            cleanRemovedRole( id );
        }
    }

    @SuppressWarnings( "unchecked" )
    public void deleteUser( String id )
        throws NoSuchUserException
    {
        boolean found = false;

        for ( Iterator<CUser> iter = getConfiguration().getUsers().iterator(); iter.hasNext(); )
        {
            if ( iter.next().getId().equals( id ) )
            {
                found = true;
                iter.remove();
                break;
            }
        }

        if ( !found )
        {
            throw new NoSuchUserException( id );
        }
    }

    @SuppressWarnings( "unchecked" )
    public SecurityPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        for ( CPrivilege privilege : (List<CPrivilege>) getConfiguration().getPrivileges() )
        {
            if ( privilege.getId().equals( id ) )
            {
                return new SecurityPrivilege( privilege );
            }
        }

        throw new NoSuchPrivilegeException( id );
    }

    @SuppressWarnings( "unchecked" )
    public SecurityRole readRole( String id )
        throws NoSuchRoleException
    {
        for ( CRole role : (List<CRole>) getConfiguration().getRoles() )
        {
            if ( role.getId().equals( id ) )
            {
                return new SecurityRole( role );
            }
        }

        throw new NoSuchRoleException( id );
    }

    @SuppressWarnings( "unchecked" )
    public SecurityUser readUser( String id )
        throws NoSuchUserException
    {
        for ( CUser user : (List<CUser>) getConfiguration().getUsers() )
        {
            if ( user.getId().equals( id ) )
            {
                // see if we have a userRole mapping
                List<String> roles = null;
                try
                {
                    SecurityUserRoleMapping roleMapping = this.readUserRoleMapping( id, SecurityXmlPlexusUserLocator.SOURCE );
                    if ( roleMapping != null )
                    {
                        roles = roleMapping.getRoles();
                    }
                }
                catch ( NoSuchRoleMappingException e )
                {
                    // this really should never happen, but validation could change
                    // but its not going to hurt anything to keep going
                    this.getLogger().debug( e.getMessage() );
                }

                return new SecurityUser( user, false, roles );
            }
        }

        throw new NoSuchUserException( id );
    }

    public void updatePrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        updatePrivilege( privilege, initializeContext() );
    }

    public void updatePrivilege( SecurityPrivilege privilege, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchPrivilegeException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validatePrivilege( context, privilege, true );

        if ( vr.isValid() )
        {
            deletePrivilege( privilege.getId(), false );
            getConfiguration().addPrivilege( privilege );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void updateRole( SecurityRole role )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        updateRole( role, initializeContext() );
    }

    public void updateRole( SecurityRole role, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validateRole( context, role, true );

        if ( vr.isValid() )
        {
            deleteRole( role.getId(), false );
            getConfiguration().addRole( role );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public void updateUser( SecurityUser user )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        updateUser( user, initializeContext() );
    }

    public void updateUser( SecurityUser user, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchUserException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        ValidationResponse vr = validator.validateUser( context, user, user.getRoles(), true );

        if ( vr.isValid() )
        {
            deleteUser( user.getId() );
            getConfiguration().addUser( user );
            this.createOrUpdateUserRoleMapping( this.getRoleMappingFromUser( user ) );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    @SuppressWarnings( "unchecked" )
    public String getPrivilegeProperty( SecurityPrivilege privilege, String key )
    {
        if ( privilege != null && privilege.getProperties() != null )
        {
            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
            {
                if ( prop.getKey().equals( key ) )
                {
                    return prop.getValue();
                }
            }
        }

        return null;
    }

    // TODO:
    public void createUserRoleMapping( SecurityUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException
    {
        this.createUserRoleMapping( userRoleMapping, this.initializeContext() );
    }

    public void createUserRoleMapping( SecurityUserRoleMapping userRoleMapping, ValidationContext context )
        throws InvalidConfigurationException
    {

        if ( context == null )
        {
            context = this.initializeContext();
        }

        try
        {
            // this will throw a NoSuchRoleMappingException, if there isn't one
            this.readUserRoleMapping( userRoleMapping.getUserId(), userRoleMapping.getSource() );

            ValidationResponse vr = new ValidationResponse();
            vr.addValidationError( new ValidationMessage( "*", "User Role Mapping for user '"
                + userRoleMapping.getUserId() + "' already exists." ) );

            throw new InvalidConfigurationException( vr );
        }
        catch ( NoSuchRoleMappingException e )
        {
            // expected
        }

        ValidationResponse vr = validator.validateUserRoleMapping( context, userRoleMapping, false );

        if ( vr.getValidationErrors().size() > 0 )
        {
            throw new InvalidConfigurationException( vr );
        }

        getConfiguration().addUserRoleMapping( userRoleMapping );
    }

    private CUserRoleMapping readCUserRoleMapping( String userId, String source ) throws NoSuchRoleMappingException
    {
        for ( CUserRoleMapping userRoleMapping : (List<CUserRoleMapping>) getConfiguration().getUserRoleMappings() )
        {   
            if (  StringUtils.equals( userRoleMapping.getUserId(), userId ) && StringUtils.equals( userRoleMapping.getSource(), source ))
            {
                return userRoleMapping;
            }
        }

        throw new NoSuchRoleMappingException( "No User Role Mapping for user: " + userId );
    }
    
    @SuppressWarnings( "unchecked" )
    public SecurityUserRoleMapping readUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        return new SecurityUserRoleMapping( this.readCUserRoleMapping( userId, source ) );
    }

    public void updateUserRoleMapping( SecurityUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException,
            NoSuchRoleMappingException
    {
        this.updateUserRoleMapping( userRoleMapping, this.initializeContext() );
    }

    public void updateUserRoleMapping( SecurityUserRoleMapping userRoleMapping, ValidationContext context )
        throws InvalidConfigurationException,
            NoSuchRoleMappingException
    {
        if ( context == null )
        {
            context = this.initializeContext();
        }

        if ( this.readUserRoleMapping( userRoleMapping.getUserId(), userRoleMapping.getSource() ) == null )
        {
            ValidationResponse vr = new ValidationResponse();
            vr.addValidationError( new ValidationMessage( "*", "No User Role Mapping found for user '" + userRoleMapping.getUserId()
                + "'." ) );

            throw new InvalidConfigurationException( vr );
        }

        ValidationResponse vr = validator.validateUserRoleMapping( context, userRoleMapping, true );

        if ( vr.getValidationErrors().size() > 0 )
        {
            throw new InvalidConfigurationException( vr );
        }

        this.deleteUserRoleMapping( userRoleMapping.getUserId(), userRoleMapping.getSource() );
        getConfiguration().addUserRoleMapping( userRoleMapping );
    }

    @SuppressWarnings( "unchecked" )
    public List<SecurityUserRoleMapping> listUserRoleMappings()
    {
        List<SecurityUserRoleMapping> list = new ArrayList<SecurityUserRoleMapping>();

        for ( CUserRoleMapping item : (List<CUserRoleMapping>) getConfiguration().getUserRoleMappings() )
        {
            list.add( new SecurityUserRoleMapping( item ) );
        }

        return list;
    }

    @SuppressWarnings( "unchecked" )
    public void deleteUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        boolean found = false;

        for ( Iterator<CUserRoleMapping> iter = getConfiguration().getUserRoleMappings().iterator(); iter.hasNext(); )
        {
            CUserRoleMapping userRoleMapping = iter.next();
            if ( userRoleMapping.getUserId().equals( userId )
                && ( StringUtils.equals( userRoleMapping.getSource(), source )) )
            {
                found = true;
                iter.remove();
                break;
            }
        }

        if ( !found )
        {
            throw new NoSuchRoleMappingException( "No User Role Mapping for user: " + userId );
        }
    }

    public String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException
    {
        return getPrivilegeProperty( readPrivilege( id ), key );
    }

    public void clearCache()
    {
        // Just to make sure we aren't fiddling w/ save/loading process
        lock.lock();
        configuration = null;
        lock.unlock();
    }

    public void save()
    {
        lock.lock();

        securityConfiguration.getParentFile().mkdirs();

        Writer fw = null;

        try
        {
            fw = new OutputStreamWriter( new FileOutputStream( securityConfiguration ) );

            SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException while storing configuration file", e );
        }
        finally
        {
            if ( fw != null )
            {
                try
                {
                    fw.flush();

                    fw.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            lock.unlock();
        }
    }

    private Configuration getConfiguration()
    {
        if ( configuration != null )
        {
            return configuration;
        }

        lock.lock();

        Reader fr = null;
        FileInputStream is = null;

        try
        {
            is = new FileInputStream( securityConfiguration );

            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );
        }
        catch ( FileNotFoundException e )
        {
            // This is ok, may not exist first time around
            configuration = new Configuration();
        }
        catch ( IOException e )
        {
            getLogger().error( "IOException while retrieving configuration file", e );
        }
        catch ( XmlPullParserException e )
        {
            getLogger().error( "Invalid XML Configuration", e );
        }
        finally
        {
            if ( fr != null )
            {
                try
                {
                    fr.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            if ( is != null )
            {
                try
                {
                    is.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            lock.unlock();
        }

        return configuration;
    }

    public ValidationContext initializeContext()
    {
        ValidationContext context = new ValidationContext();

        context.addExistingUserIds();
        context.addExistingRoleIds();
        context.addExistingPrivilegeIds();

        for ( CUser user : listUsers() )
        {
            context.getExistingUserIds().add( user.getId() );

            context.getExistingEmailMap().put( user.getId(), user.getEmail() );
        }

        for ( CRole role : listRoles() )
        {
            context.getExistingRoleIds().add( role.getId() );

            ArrayList<String> containedRoles = new ArrayList<String>();

            containedRoles.addAll( role.getRoles() );

            context.getRoleContainmentMap().put( role.getId(), containedRoles );

            context.getExistingRoleNameMap().put( role.getId(), role.getName() );
        }

        for ( CPrivilege priv : listPrivileges() )
        {
            context.getExistingPrivilegeIds().add( priv.getId() );
        }

        for ( CUserRoleMapping roleMappings : listUserRoleMappings() )
        {
            context.getExistingUserRoleMap().put( roleMappings.getUserId(), roleMappings.getRoles() );
        }

        return context;
    }
    
    public List<PrivilegeDescriptor> listPrivilegeDescriptors()
    {
        return Collections.unmodifiableList( privilegeDescriptors );
    }
    
    public void cleanRemovedPrivilege( String privilegeId )
    {
        configCleaner.privilegeRemoved( getConfiguration(), privilegeId );
    }
    
    public void cleanRemovedRole( String roleId )
    {
        configCleaner.roleRemoved( getConfiguration(), roleId );
    }
}
