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
package org.sonatype.security;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.jsecurity.realm.Realm;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Reader;

public abstract class AbstractSecurityTestCase
    extends PlexusTestCase
{

    protected File PLEXUS_HOME = new File( "./target/plexus_home" );

    protected File CONFIG_DIR = new File( PLEXUS_HOME, "conf" );

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        context.put( "application-conf", CONFIG_DIR.getAbsolutePath() );
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

        Map<String, Realm> realmMap = this.getContainer().lookupMap( Realm.class );

        config.setRealms( new ArrayList<String>( realmMap.keySet() ) );
        source.storeConfiguration();
    }

    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

        Reader fr = new InputStreamReader( is );

        return reader.read( fr );
    }
}
