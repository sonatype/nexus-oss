package org.sonatype.nexus.configuration.security.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.jsecurity.locators.SecurityXmlPlexusUserLocator;
import org.sonatype.jsecurity.model.v2_0_1.CPrivilege;
import org.sonatype.jsecurity.model.v2_0_1.CProperty;
import org.sonatype.jsecurity.model.v2_0_1.CRole;
import org.sonatype.jsecurity.model.v2_0_1.CUser;
import org.sonatype.jsecurity.model.v2_0_1.Configuration;
import org.sonatype.jsecurity.model.v2_0_1.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;

@Component( role = SecurityUpgrader.class, hint = "2.0.1" )
public class Upgrade201to202
    implements SecurityUpgrader
{
    public Object loadConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException
    {
        FileReader fr = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            fr = new FileReader( file );

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

    public void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException
    {
        Configuration oldc = (Configuration) message.getConfiguration();

        org.sonatype.jsecurity.model.Configuration newc = new org.sonatype.jsecurity.model.Configuration();

        newc.setVersion( org.sonatype.jsecurity.model.Configuration.MODEL_VERSION );

        for ( CUser oldu : (List<CUser>) oldc.getUsers() )
        {
            org.sonatype.jsecurity.model.CUser newu = new org.sonatype.jsecurity.model.CUser();

            newu.setEmail( oldu.getEmail() );
            newu.setId( oldu.getId() );
            newu.setName( oldu.getName() );
            newu.setPassword( oldu.getPassword() );
            newu.setStatus( oldu.getStatus() );
            
            // convert the old roles mapping to the new one
            this.migrateOldRolesToUserRoleMapping( oldu.getId(), SecurityXmlPlexusUserLocator.SOURCE, oldu.getRoles(), newc );

            newc.addUser( newu );
        }

        Map<String, String> roleIdMap = new HashMap<String, String>();

        for ( CRole oldr : (List<CRole>) oldc.getRoles() )
        {
                org.sonatype.jsecurity.model.CRole newr = new org.sonatype.jsecurity.model.CRole();

                newr.setDescription( oldr.getDescription() );
                newr.setId( oldr.getId() );
                newr.setName( oldr.getName() );
                newr.setPrivileges( oldr.getPrivileges() );
                newr.setRoles( oldr.getRoles() );
                newr.setSessionTimeout( oldr.getSessionTimeout() );

                newc.addRole( newr );

        }

        for ( CPrivilege oldp : (List<CPrivilege>) oldc.getPrivileges() )
        {
                org.sonatype.jsecurity.model.CPrivilege newp = new org.sonatype.jsecurity.model.CPrivilege();

                newp.setDescription( oldp.getDescription() );
                newp.setId( oldp.getId() );
                newp.setName( oldp.getName() );
                newp.setType( oldp.getType() );

                for ( CProperty oldprop : (List<CProperty>) oldp.getProperties() )
                {
                    org.sonatype.jsecurity.model.CProperty newprop = new org.sonatype.jsecurity.model.CProperty();
                    newprop.setKey( oldprop.getKey() );
                    newprop.setValue( oldprop.getValue() );
                    newp.addProperty( newprop );
                }
                newc.addPrivilege( newp );
        }

        message.setModelVersion( org.sonatype.jsecurity.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }
    
    private void migrateOldRolesToUserRoleMapping( String userId, String source, List<String> roles, org.sonatype.jsecurity.model.Configuration config)
    {
        org.sonatype.jsecurity.model.CUserRoleMapping roleMapping = new org.sonatype.jsecurity.model.CUserRoleMapping();
        roleMapping.setRoles( roles );
        roleMapping.setSource( source );
        roleMapping.setUserId( userId );
        
        config.addUserRoleMapping( roleMapping );
    }

   
}
