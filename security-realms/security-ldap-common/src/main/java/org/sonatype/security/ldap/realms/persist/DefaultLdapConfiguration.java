/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.password.PasswordEncoderManager;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;
import org.sonatype.security.ldap.realms.persist.model.Configuration;
import org.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Reader;
import org.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Writer;
import org.sonatype.security.ldap.upgrade.cipher.PlexusCipherException;

@Component( role = LdapConfiguration.class, hint = "default", instantiationStrategy = "singleton" )
public class DefaultLdapConfiguration
    extends AbstractLogEnabled
    implements LdapConfiguration
{
    @org.codehaus.plexus.component.annotations.Configuration( value = "${application-conf}/ldap.xml" )
    private File configurationFile;

    private Configuration configuration;

    @Requirement( role = ConfigurationValidator.class )
    private ConfigurationValidator validator;

    @Requirement
    private PasswordHelper passwordHelper;
    
    @Requirement
    private PasswordEncoderManager passwordEncoderManager;
    
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    private ReentrantLock lock = new ReentrantLock();

    public CConnectionInfo readConnectionInfo()
    {
        CConnectionInfo connInfo = getConfiguration().getConnectionInfo();

        return connInfo;
    }

    public CUserAndGroupAuthConfiguration readUserAndGroupConfiguration()
    {
        return getConfiguration().getUserAndGroupConfig();
    }

    public void updateUserAndGroupConfiguration( CUserAndGroupAuthConfiguration userAndGroupConfig )
        throws InvalidConfigurationException
    {
        lock.lock();

        try
        {
            ValidationResponse vr = validator.validateUserAndGroupAuthConfiguration( null, userAndGroupConfig );

            if ( vr.getValidationErrors().size() > 0 )
            {
                throw new InvalidConfigurationException( vr );
            }

            getConfiguration().setUserAndGroupConfig( userAndGroupConfig );
        }
        finally
        {
            lock.unlock();
        }
    }

    public void updateConnectionInfo( CConnectionInfo connectionInfo )
        throws InvalidConfigurationException
    {
        lock.lock();

        try
        {
            ValidationResponse vr = validator.validateConnectionInfo( null, connectionInfo );

            if ( vr.getValidationErrors().size() > 0 )
            {
                throw new InvalidConfigurationException( vr );
            }

            getConfiguration().setConnectionInfo( connectionInfo );
        }
        finally
        {
            lock.unlock();
        }

    }

    private Configuration getConfiguration()
    {
        Reader fr = null;
        FileInputStream is = null;
       
        try
        {
            
            lock.lock();
            
            if ( configuration != null )
            {
                return configuration;
            }
            
            is = new FileInputStream( configurationFile );

            LdapConfigurationXpp3Reader reader = new LdapConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );

            ValidationResponse vr = validator.validateModel( new ValidationRequest( configuration ) );

            if ( vr.getValidationErrors().size() > 0 )
            {
                // TODO need to code the handling of invalid config
                configuration = new Configuration();
            }                 
            
         // decrypt the password, if it fails assume the password is clear text.
            // If the password is wrong the the LDAP Realm will not work, which is no different. If the user typed in the
            // password wrong.
            if ( configuration.getConnectionInfo() != null && StringUtils.isNotEmpty( configuration.getConnectionInfo().getSystemPassword() ) )
            {
                try
                {
                    configuration.getConnectionInfo().setSystemPassword( passwordHelper.decrypt( configuration.getConnectionInfo().getSystemPassword() ) );
                }
                catch ( PlexusCipherException e )
                {
                    this.getLogger().error(
                        "Failed to decrypt password, assuming the password in file: '"
                            + configurationFile.getAbsolutePath() + "' is clear text.",
                        e );
                }
            }
            
        }
        catch ( FileNotFoundException e )
        {
            // This is ok, may not exist first time around
            configuration = this.getDefaultConfiguration();
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

    public void save()
    {
        lock.lock();

        configurationFile.getParentFile().mkdirs();

        Writer fw = null;

        try
        {
            // this is sort of dirty...
            String clearPass = null;
            
            // change the password to be encrypted
            if ( configuration.getConnectionInfo() != null && StringUtils.isNotEmpty( configuration.getConnectionInfo().getSystemPassword() ) )
            {
                try
                {
                    clearPass = configuration.getConnectionInfo().getSystemPassword();
                    configuration.getConnectionInfo().setSystemPassword( passwordHelper.encrypt( clearPass ) );
                }
                catch ( PlexusCipherException e )
                {
                    getLogger().error( "Failed to encrypt password while storing configuration file", e );
                }
            }
            
            fw = new OutputStreamWriter( new FileOutputStream( configurationFile ) );

            LdapConfigurationXpp3Writer writer = new LdapConfigurationXpp3Writer();

            writer.write( fw, configuration );
            
            // now reset the password
            if ( configuration.getConnectionInfo() != null )
            {
                configuration.getConnectionInfo().setSystemPassword( clearPass );
            }
            
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
        
        // fire clear cache event
        this.applicationEventMulticaster.notifyEventListeners( new LdapClearCacheEvent( null ) );
    }

    public void clearCache()
    {
        configuration = null;
        
        // fire clear cache event
        this.applicationEventMulticaster.notifyEventListeners( new LdapClearCacheEvent( null ) );
    }

    private Configuration getDefaultConfiguration()
    {

        Configuration defaultConfig = null;

        Reader fr = null;
        InputStream is = null;
        try
        {
            is = getClass().getResourceAsStream( "/META-INF/realms/ldap.xml" );
            LdapConfigurationXpp3Reader reader = new LdapConfigurationXpp3Reader();
            fr = new InputStreamReader( is );
            defaultConfig = reader.read( fr );
        }
        catch ( IOException e )
        {
            this
                .getLogger()
                .error(
                    "Failed to read default LDAP Realm configuration.  This may be corrected while the application is running.",
                    e );
            defaultConfig = new Configuration();
        }
        catch ( XmlPullParserException e )
        {
            this
                .getLogger()
                .error(
                    "Failed to read default LDAP Realm configuration.  This may be corrected while the application is running.",
                    e );
            defaultConfig = new Configuration();
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
        }
        return defaultConfig;
    }

    public LdapAuthConfiguration getLdapAuthConfiguration()
    {
        CUserAndGroupAuthConfiguration userAndGroupsConf = readUserAndGroupConfiguration();
        LdapAuthConfiguration authConfig = new LdapAuthConfiguration();

        authConfig.setEmailAddressAttribute( userAndGroupsConf.getEmailAddressAttribute() );
//        authConfig.setPasswordEncoding( userAndGroupsConf.getPreferredPasswordEncoding() );
        authConfig.setUserBaseDn( StringUtils.defaultString( userAndGroupsConf.getUserBaseDn(), "" ) );
        authConfig.setUserIdAttribute( userAndGroupsConf.getUserIdAttribute() );
        authConfig.setUserObjectClass( userAndGroupsConf.getUserObjectClass() );
        authConfig.setPasswordAttribute( userAndGroupsConf.getUserPasswordAttribute() );
        authConfig.setUserRealNameAttribute( userAndGroupsConf.getUserRealNameAttribute() );

        authConfig.setGroupBaseDn( StringUtils.defaultString( userAndGroupsConf.getGroupBaseDn(), "" ) );
        authConfig.setGroupIdAttribute( userAndGroupsConf.getGroupIdAttribute() );
        // authConfig.setGroupMappings( groupMappings )
        authConfig.setGroupMemberAttribute( userAndGroupsConf.getGroupMemberAttribute() );
        authConfig.setGroupMemberFormat( userAndGroupsConf.getGroupMemberFormat() );
        authConfig.setGroupObjectClass( userAndGroupsConf.getGroupObjectClass() );
        authConfig.setUserSubtree( userAndGroupsConf.isUserSubtree() );
        authConfig.setGroupSubtree( userAndGroupsConf.isGroupSubtree() );
        authConfig.setUserMemberOfAttribute( userAndGroupsConf.getUserMemberOfAttribute() );
        authConfig.setLdapGroupsAsRoles( userAndGroupsConf.isLdapGroupsAsRoles() );
        return authConfig;
    }
}
