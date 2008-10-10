package org.sonatype.nexus.configuration.security.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.jsecurity.model.v2_0_0.CProperty;
import org.sonatype.jsecurity.model.v2_0_0.CPrivilege;
import org.sonatype.jsecurity.model.v2_0_0.CRole;
import org.sonatype.jsecurity.model.v2_0_0.CUser;
import org.sonatype.jsecurity.model.v2_0_0.Configuration;
import org.sonatype.jsecurity.model.v2_0_0.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;

@Component( role = SecurityUpgrader.class, hint = "2.0.0" )
public class Upgrade200to201
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
            newu.setRoles( oldu.getRoles() );

            newc.addUser( newu );
        }

        for ( CRole oldr : (List<CRole>) oldc.getRoles() )
        {
            if ( !getRolesToRemove().contains( oldr.getId() ) )
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
        }

        for ( CPrivilege oldp : (List<CPrivilege>) oldc.getPrivileges() )
        {
            if ( !getPrivsToRemove().contains( oldp.getId() ) )
            {
                org.sonatype.jsecurity.model.CPrivilege newp = new org.sonatype.jsecurity.model.CPrivilege();
    
                newp.setDescription( oldp.getDescription() );
                newp.setId( oldp.getId() );
                newp.setName( oldp.getName() );
                newp.setType( oldp.getType() );
                
                for ( CProperty oldprop : ( List<CProperty> ) oldp.getProperties() )
                {
                    org.sonatype.jsecurity.model.CProperty newprop = new org.sonatype.jsecurity.model.CProperty();
                    newprop.setKey( oldprop.getKey() );
                    newprop.setValue( oldprop.getValue() );
                    newp.addProperty( newprop );
                }    
                newc.addPrivilege( newp );
            }
        }

        message.setModelVersion( org.sonatype.jsecurity.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }
    
    private Set<String> getRolesToRemove()
    {
        Set<String> set = new HashSet<String>();
        
        set.add( "admin" );
        set.add( "deployment" );
        set.add( "anonymous" );
        set.add( "developer" );
        set.add( "ui-search" );
        set.add( "ui-repo-browser" );
        set.add( "ui-system-feeds" );
        set.add( "ui-logs-config-files" );
        set.add( "ui-server-admin" );
        set.add( "ui-repository-admin" );
        set.add( "ui-group-admin" );
        set.add( "ui-routing-admin" );
        set.add( "ui-scheduled-tasks-admin" );
        set.add( "ui-repository-targets-admin" );
        set.add( "ui-users-admin" );
        set.add( "ui-roles-admin" );
        set.add( "ui-privileges-admin" );
        set.add( "ui-basic" );
        
        return set;
    }
    
    private Set<String> getPrivsToRemove()
    {
        Set<String> set = new HashSet<String>();
        
        set.add( "T1" );
        set.add( "T2" );
        set.add( "T3" );
        set.add( "T4" );
        set.add( "T5" );
        set.add( "T6" );
        set.add( "T7" );
        set.add( "T8" );
        set.add( "1000" );
        set.add( "1" );
        set.add( "2" );
        set.add( "3" );
        set.add( "4" );
        set.add( "5" );
        set.add( "6" );
        set.add( "7" );
        set.add( "8" );
        set.add( "9" );
        set.add( "10" );
        set.add( "11" );
        set.add( "12" );
        set.add( "13" );
        set.add( "14" );
        set.add( "15" );
        set.add( "16" );
        set.add( "17" );
        set.add( "18" );
        set.add( "19" );
        set.add( "20" );
        set.add( "21" );
        set.add( "22" );
        set.add( "23" );
        set.add( "24" );
        set.add( "25" );
        set.add( "26" );
        set.add( "27" );
        set.add( "28" );
        set.add( "29" );
        set.add( "30" );
        set.add( "31" );
        set.add( "32" );
        set.add( "33" );
        set.add( "34" );
        set.add( "35" );
        set.add( "36" );
        set.add( "37" );
        set.add( "38" );
        set.add( "39" );
        set.add( "40" );
        set.add( "41" );
        set.add( "42" );
        set.add( "43" );
        set.add( "44" );
        set.add( "45" );
        set.add( "46" );
        set.add( "47" );
        set.add( "48" );
        set.add( "49" );
        set.add( "50" );
        set.add( "51" );
        set.add( "54" );
        set.add( "55" );
        set.add( "56" );
        set.add( "57" );
        set.add( "58" );
        set.add( "59" );
        set.add( "64" );
        set.add( "65" );
        set.add( "66" );
        set.add( "67" );
        set.add( "68" );
        set.add( "69" );
        
        return set;
    }
}
