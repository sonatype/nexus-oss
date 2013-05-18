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
package org.sonatype.security.realms.url;

import java.io.File;
import java.io.FileReader;

import junit.framework.Assert;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.realms.url.config.UrlRealmConfiguration;

import com.sonatype.security.realms.url.config.model.Configuration;
import com.sonatype.security.realms.url.config.model.io.xpp3.UrlRealmConfigurationXpp3Reader;

public class URLRealmConfigurationTest
    extends AbstractSecurityTestCase
{

    public void testWriteThenRead()
        throws Exception
    {

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.getAuthorizationManager( "default" ).addRole( new Role( "defaultRole", "Default Test Role",
                                                                               "Default Test Role Description",
                                                                               "default", true, null, null ) );

        UrlRealmConfiguration realmConfiguration = this.lookup( UrlRealmConfiguration.class );

        File dest = new File( CONFIG_DIR, "url-realm.xml" );
        dest.getParentFile().mkdirs();

        Configuration config = new Configuration();
        config.setDefaultRole( "defaultRole" );
        config.setEmailDomain( "emailDomain.com" );
        config.setUrl( "http://foobar.com/your/path" );

        // FileWriter fileWriter = null;
        FileReader fileReader = null;

        try
        {
            realmConfiguration.updateConfiguration( config );
            // fileWriter = new FileWriter( dest );
            // UrlRealmConfigurationXpp3Writer writer = new UrlRealmConfigurationXpp3Writer();
            // writer.write( fileWriter, config );
            // fileWriter.close();

            // now read
            fileReader = new FileReader( dest );
            UrlRealmConfigurationXpp3Reader reader = new UrlRealmConfigurationXpp3Reader();
            Configuration actualConfig = reader.read( fileReader );

            // compare
            Assert.assertEquals( config.getDefaultRole(), actualConfig.getDefaultRole() );
            Assert.assertEquals( config.getEmailDomain(), actualConfig.getEmailDomain() );
            Assert.assertEquals( config.getUrl(), actualConfig.getUrl() );
            // the version should already be set
            Assert.assertEquals( "Version Does not match.", "1.0.0", actualConfig.getVersion() );

            // clear the config, then read using the model
            realmConfiguration.clearCache();
            actualConfig = realmConfiguration.getConfiguration();

            Assert.assertEquals( config.getDefaultRole(), actualConfig.getDefaultRole() );
            Assert.assertEquals( config.getEmailDomain(), actualConfig.getEmailDomain() );
            Assert.assertEquals( config.getUrl(), actualConfig.getUrl() );
            // the version should already be set
            Assert.assertEquals( "Version Does not match.", "1.0.0", actualConfig.getVersion() );

        }
        finally
        {
            // IOUtil.close( fileWriter );
            IOUtil.close( fileReader );
        }
    }

}
