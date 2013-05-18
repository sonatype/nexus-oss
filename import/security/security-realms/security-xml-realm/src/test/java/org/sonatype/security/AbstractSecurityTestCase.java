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
package org.sonatype.security;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.shiro.realm.Realm;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.inject.BeanScanning;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.sisu.ehcache.CacheManagerComponent;

public abstract class AbstractSecurityTestCase
    extends InjectedTestCase
{

    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";

    protected File PLEXUS_HOME = new File( "./target/plexus_home" );

    protected File CONFIG_DIR = new File( PLEXUS_HOME, "conf" );

    @Inject
    private Map<String, Realm> realmMap;

    @Override
    public void configure( Properties properties )
    {
        properties.put( "application-conf", CONFIG_DIR.getAbsolutePath() );
        properties.put( "security-xml-file", CONFIG_DIR.getAbsolutePath() + "/security.xml" );
        super.configure( properties );
    }

    @Override
    public BeanScanning scanning()
    {
        return BeanScanning.INDEX;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        FileUtils.deleteDirectory( PLEXUS_HOME );

        super.setUp();

        CONFIG_DIR.mkdirs();

        SecurityConfigurationSource source = this.lookup( SecurityConfigurationSource.class, "file" );
        SecurityConfiguration config = source.loadConfiguration();

        config.setRealms( new ArrayList<String>( realmMap.keySet() ) );
        source.storeConfiguration();

        lookup( SecuritySystem.class ).start();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        try
        {
            lookup( SecuritySystem.class ).stop();
            lookup( CacheManagerComponent.class ).shutdown();
        }
        finally
        {
            super.tearDown();
        }
    }

    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

        Reader fr = new InputStreamReader( is );

        return reader.read( fr );
    }

    protected Configuration getSecurityConfiguration()
        throws IOException, XmlPullParserException
    {
        // now lets check the XML file for the user and the role mapping
        SecurityConfigurationXpp3Reader secReader = new SecurityConfigurationXpp3Reader();
        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader( new File( CONFIG_DIR, "security.xml" ) );
            return secReader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }
    }
}
