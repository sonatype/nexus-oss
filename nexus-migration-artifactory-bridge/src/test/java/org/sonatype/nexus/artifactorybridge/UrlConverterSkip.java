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
package org.sonatype.nexus.artifactorybridge;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.plugin.migration.artifactory.persist.MappingConfiguration;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;

public class UrlConverterSkip
    extends PlexusTestCase
{

    private UrlConverter urlConverter;

    @Override
    protected void setUp()
        throws Exception
    {
        urlConverter = (UrlConverter) lookup( UrlConverter.class );
        MappingConfiguration cfg = (MappingConfiguration) lookup( MappingConfiguration.class );
        cfg.addMapping( new CMapping( "repo1", "central" ) );
        cfg.addMapping( new CMapping( "libs-local", "libs-local", "libs-local-releases", "libs-local-snapshots" ) );
    }

    public void testDownload()
        throws Exception
    {
        String url;

        url = urlConverter.convertDownload( "/repo1/org/apache/maven/2.0.9/maven-2.0.9.zip" );
        assertEquals( "/content/repositories/central/org/apache/maven/2.0.9/maven-2.0.9.zip", url );

        url = urlConverter.convertDownload( "/libs-local/local/lib/1.0-SNAPSHOT/lib-1.0-SNAPSHOT.jar" );
        assertEquals( "/content/groups/libs-local/local/lib/1.0-SNAPSHOT/lib-1.0-SNAPSHOT.jar", url );

        assertNull( urlConverter.convertDownload( "/" ) );
        assertNull( urlConverter.convertDownload( null ) );
        assertNull( urlConverter.convertDownload( "dummy" ) );
    }

}
