/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.model.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.security.model.v2_0_1.CPrivilege;
import org.sonatype.security.model.v2_0_1.CProperty;
import org.sonatype.security.model.v2_0_1.CRole;
import org.sonatype.security.model.v2_0_1.CUser;
import org.sonatype.security.model.v2_0_1.Configuration;
import org.sonatype.security.model.v2_0_1.io.xpp3.SecurityConfigurationXpp3Reader;

@Singleton
@Typed( SecurityUpgrader.class )
@Named( "2.0.1" )
public class Upgrade201to202
    implements SecurityUpgrader
{
    private static String DEFAULT_SOURCE = "default";

    public Object loadConfiguration( File file )
        throws IOException, ConfigurationIsCorruptedException
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

        org.sonatype.security.model.v2_0_2.Configuration newc = new org.sonatype.security.model.v2_0_2.Configuration();

        newc.setVersion( org.sonatype.security.model.Configuration.MODEL_VERSION );

        for ( CUser oldu : (List<CUser>) oldc.getUsers() )
        {
            org.sonatype.security.model.v2_0_2.CUser newu = new org.sonatype.security.model.v2_0_2.CUser();

            newu.setEmail( oldu.getEmail() );
            newu.setId( oldu.getId() );
            newu.setName( oldu.getName() );
            newu.setPassword( oldu.getPassword() );
            newu.setStatus( oldu.getStatus() );

            // convert the old roles mapping to the new one
            this.migrateOldRolesToUserRoleMapping( oldu.getId(), DEFAULT_SOURCE, oldu.getRoles(), newc );

            newc.addUser( newu );
        }

        Map<String, String> roleIdMap = new HashMap<String, String>();

        for ( CRole oldr : (List<CRole>) oldc.getRoles() )
        {
            org.sonatype.security.model.v2_0_2.CRole newr = new org.sonatype.security.model.v2_0_2.CRole();

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
            org.sonatype.security.model.v2_0_2.CPrivilege newp = new org.sonatype.security.model.v2_0_2.CPrivilege();

            newp.setDescription( oldp.getDescription() );
            newp.setId( oldp.getId() );
            newp.setName( oldp.getName() );
            newp.setType( oldp.getType() );

            for ( CProperty oldprop : (List<CProperty>) oldp.getProperties() )
            {
                org.sonatype.security.model.v2_0_2.CProperty newprop =
                    new org.sonatype.security.model.v2_0_2.CProperty();
                newprop.setKey( oldprop.getKey() );
                newprop.setValue( oldprop.getValue() );
                newp.addProperty( newprop );
            }
            newc.addPrivilege( newp );
        }

        message.setModelVersion( org.sonatype.security.model.v2_0_2.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

    private void migrateOldRolesToUserRoleMapping( String userId, String source, List<String> roles,
                                                   org.sonatype.security.model.v2_0_2.Configuration config )
    {
        org.sonatype.security.model.v2_0_2.CUserRoleMapping roleMapping =
            new org.sonatype.security.model.v2_0_2.CUserRoleMapping();
        roleMapping.setRoles( roles );
        roleMapping.setSource( source );
        roleMapping.setUserId( userId );

        config.addUserRoleMapping( roleMapping );
    }

}
