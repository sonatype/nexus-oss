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
package org.sonatype.nexus.configuration.security.upgrade;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.upgrade.AbstractDataUpgrader;
import org.sonatype.security.model.upgrade.SecurityDataUpgrader;

@Singleton
@Typed( value = SecurityDataUpgrader.class )
@Named( value = "2.0.4" )
public class SecurityData204Upgrade
    extends AbstractDataUpgrader<Configuration>
    implements SecurityDataUpgrader
{

    @Override
    public void doUpgrade( Configuration cfg )
        throws ConfigurationIsCorruptedException
    {

        CRole admin = new CRole();
        admin.setDescription( "Deprecated admin role, use nexus-admin instead" );
        admin.setId( "admin" );
        admin.setName( "Nexus Administrator Role" );
        admin.setReadOnly( false );
        admin.addRole( "nexus-admin" );
        cfg.addRole( admin );
        CRole developer = new CRole();
        developer.setDescription( "Deprecated developer role, use nexus-developer instead" );
        developer.setId( "developer" );
        developer.setName( "Developer" );
        developer.setReadOnly( false );
        developer.addRole( "nexus-developer" );
        cfg.addRole( developer );
        CRole deployer = new CRole();
        deployer.setDescription( "Deprecated deployment role, use nexus-deployment instead" );
        deployer.setId( "deployment" );
        deployer.setName( "Deployment" );
        deployer.setReadOnly( false );
        deployer.addRole( "nexus-deployment" );
        cfg.addRole( deployer );
    }

}
