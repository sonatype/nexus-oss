package org.sonatype.nexus.configuration.security.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.security.model.io.xpp3.NexusSecurityConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;

@Component( role = SecurityUpgrader.class, hint = "1.0.0" )
public class Upgrade100to200
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

            NexusSecurityConfigurationXpp3Reader reader = new NexusSecurityConfigurationXpp3Reader();

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

        org.sonatype.jsecurity.model.v2_0_0.Configuration newc = new org.sonatype.jsecurity.model.v2_0_0.Configuration();

        newc.setVersion( org.sonatype.jsecurity.model.v2_0_0.Configuration.MODEL_VERSION );

        for ( CUser oldu : (List<CUser>) oldc.getUsers() )
        {
            org.sonatype.jsecurity.model.v2_0_0.CUser newu = new org.sonatype.jsecurity.model.v2_0_0.CUser();

            newu.setEmail( oldu.getEmail() );
            newu.setId( oldu.getUserId() );
            newu.setName( oldu.getName() );
            newu.setPassword( oldu.getPassword() );
            newu.setStatus( oldu.getStatus() );
            newu.setRoles( oldu.getRoles() );

            newc.addUser( newu );
        }

        for ( CRole oldr : (List<CRole>) oldc.getRoles() )
        {
            org.sonatype.jsecurity.model.v2_0_0.CRole newr = new org.sonatype.jsecurity.model.v2_0_0.CRole();

            newr.setDescription( oldr.getDescription() );
            newr.setId( oldr.getId() );
            newr.setName( oldr.getName() );
            newr.setPrivileges( oldr.getPrivileges() );
            newr.setRoles( oldr.getRoles() );
            newr.setSessionTimeout( oldr.getSessionTimeout() );

            newc.addRole( newr );
        }

        for ( CRepoTargetPrivilege oldp : (List<CRepoTargetPrivilege>) oldc.getRepositoryTargetPrivileges() )
        {
            org.sonatype.jsecurity.model.v2_0_0.CPrivilege newp = new org.sonatype.jsecurity.model.v2_0_0.CPrivilege();

            newp.setDescription( oldp.getDescription() );
            newp.setId( oldp.getId() );
            newp.setName( oldp.getName() );
            newp.setType( "target" );

            org.sonatype.jsecurity.model.v2_0_0.CProperty prop = new org.sonatype.jsecurity.model.v2_0_0.CProperty();
            prop.setKey( "method" );
            prop.setValue( oldp.getMethod() );
            newp.addProperty( prop );

            if ( !StringUtils.isEmpty( oldp.getRepositoryId() ) )
            {
                prop = new org.sonatype.jsecurity.model.v2_0_0.CProperty();
                prop.setKey( "repositoryGroupId" );
                prop.setValue( oldp.getGroupId() );
                newp.addProperty( prop );
            }

            if ( !StringUtils.isEmpty( oldp.getRepositoryId() ) )
            {
                prop = new org.sonatype.jsecurity.model.v2_0_0.CProperty();
                prop.setKey( "repositoryId" );
                prop.setValue( oldp.getRepositoryId() );
                newp.addProperty( prop );
            }

            prop = new org.sonatype.jsecurity.model.v2_0_0.CProperty();
            prop.setKey( "repositoryTargetId" );
            prop.setValue( oldp.getRepositoryTargetId() );
            newp.addProperty( prop );

            newc.addPrivilege( newp );
        }

        for ( CApplicationPrivilege oldp : (List<CApplicationPrivilege>) oldc.getApplicationPrivileges() )
        {
            org.sonatype.jsecurity.model.v2_0_0.CPrivilege newp = new org.sonatype.jsecurity.model.v2_0_0.CPrivilege();

            newp.setDescription( oldp.getDescription() );
            newp.setId( oldp.getId() );
            newp.setName( oldp.getName() );
            newp.setType( "method" );

            org.sonatype.jsecurity.model.v2_0_0.CProperty prop = new org.sonatype.jsecurity.model.v2_0_0.CProperty();
            prop.setKey( "method" );
            prop.setValue( oldp.getMethod() );
            newp.addProperty( prop );

            prop = new org.sonatype.jsecurity.model.v2_0_0.CProperty();
            prop.setKey( "permission" );
            prop.setValue( oldp.getPermission() );
            newp.addProperty( prop );

            newc.addPrivilege( newp );
        }

        message.setModelVersion( org.sonatype.jsecurity.model.v2_0_0.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }
}
