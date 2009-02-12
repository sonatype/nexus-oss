package org.sonatype.nexus.configuration.security.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;

@Component( role = SecurityContentUpgrader.class, hint = "2.0.3" )
public class UpgradeContent202to203
    implements SecurityContentUpgrader
{

    @Requirement
    private Logger logger;

    public Object loadConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException
    {
        FileReader fr = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            fr = new FileReader( file );

            // this will need to be the 2.0.2 reader
            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            return reader.read( fr );
        }
        catch ( XmlPullParserException e )
        {
            throw new ConfigurationIsCorruptedException( file.getAbsolutePath(), e );
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    public void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException
    {
        // this will need to be the 2.0.2 reader
        Configuration configuration = (Configuration) message.getConfiguration();
        // the view permissions are created on the fly
        // so we just need to add them where needed
        // the format is repository-<repositoryId>

        // put the privileges in a map so we can access them easily
        // only the target privileges

        // we need the static privileges too, and we need to directly parse that file from the classpath.
        Map<String, CPrivilege> privilegeMap = this.getStaticPrivilages();
        for ( CPrivilege privilege : (List<CPrivilege>) configuration.getPrivileges() )
        {
            if ( "target".equals( privilege.getType() ) )
            {
                privilegeMap.put( privilege.getId(), privilege );
            }
        }

        for ( CRole role : (List<CRole>) configuration.getRoles() )
        {

            // we need to add a 'view' permission for each target we find.
            Set<String> repositories = new HashSet<String>();

            List<String> privilegeIds = (List<String>) role.getPrivileges();

            for ( String privilegeId : privilegeIds )
            {
                CPrivilege privilege = privilegeMap.get( privilegeId );
                if ( privilege != null )
                {
                    repositories.addAll( this.getRepositoriesFromTargetPrivilege( privilege ) );
                }
                else
                {
                    this.logger.warn( "Failed to find privilege '" + privilegeId + "', but it was under the role '"
                        + role.getId() + "'." );
                }
            }

            // if repository-all is in the list, that is all we should add, 'all' and thats it
            if ( repositories.contains( "all" ) )
            {
                this.addViewPermissionToRole( role, privilegeIds, "all" );
            }
            else
            {
                // now we need to update the role with any new 'view' permissions
                for ( String repoId : repositories )
                {
                    this.addViewPermissionToRole( role, privilegeIds, repoId );
                }
            }
        }
    }

    private void addViewPermissionToRole( CRole role, List<String> existingPrivilegeIds, String repositoryId )
    {
        String permission = "repository-" + repositoryId;
        // make sure this privilege doesn't exists already
        if ( !existingPrivilegeIds.contains( permission ) )
        {
            role.addPrivilege( permission );
        }
    }

    @SuppressWarnings( { "unchecked" } )
    private Set<String> getRepositoriesFromTargetPrivilege( CPrivilege privilege )
    {
        // the properties we are looking for are:
        // repositoryId or repositoryGroupId
        // if not we need to use 'all'
        Set<String> repositories = new HashSet<String>();

        for ( CProperty property : (List<CProperty>) privilege.getProperties() )
        {
            if ( "repositoryId".equals( property.getKey() ) || "repositoryGroupId".equals( property.getKey() ) )
            {
                if ( StringUtils.isNotEmpty( property.getValue() ) )
                {
                    repositories.add( property.getValue() );
                }
            }
        }

        // if we didn't find a repository or group, add 'all' to the set
        if ( repositories.isEmpty() )
        {
            repositories.add( "all" );
        }

        return repositories;
    }

    private Map<String, CPrivilege> getStaticPrivilages()
    {
        String staticSecurityPath = "/META-INF/nexus/static-security.xml";
        Map<String, CPrivilege> privilegeMap = new HashMap<String, CPrivilege>();

        Reader fr = null;
        InputStream is = null;

        try
        {
            is = getClass().getResourceAsStream( staticSecurityPath );
            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            Configuration staticConfig = reader.read( fr );

            for ( CPrivilege privilege : (List<CPrivilege>) staticConfig.getPrivileges() )
            {
                if ( "target".equals( privilege.getType() ) )
                {
                    privilegeMap.put( privilege.getId(), privilege );
                }
            }

        }
        catch ( IOException e )
        {
            this.logger.error( "IOException while retrieving configuration file", e );
        }
        catch ( XmlPullParserException e )
        {
            this.logger.error( "Invalid XML Configuration", e );
        }
        finally
        {
            IOUtil.close( fr );
            IOUtil.close( is );
        }

        return privilegeMap;
    }

}
