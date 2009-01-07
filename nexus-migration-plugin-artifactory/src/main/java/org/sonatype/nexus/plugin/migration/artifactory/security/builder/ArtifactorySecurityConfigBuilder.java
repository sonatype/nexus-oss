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
package org.sonatype.nexus.plugin.migration.artifactory.security.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;

public class ArtifactorySecurityConfigBuilder
{
    public static final String VERSION_125 = "1.2.5";

    public static final String VERSION_130 = "1.3.0";

    public static final String VERSION_UNKNOWN = "unknown";

    public static ArtifactorySecurityConfig read( File file )
        throws IOException,
            XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( file );

        try
        {
            return build( Xpp3DomBuilder.build( reader ) );
        }

        finally
        {
            IOUtil.close( reader );
        }
    }

    public static ArtifactorySecurityConfig read( InputStream inputStream )
        throws IOException,
            XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( inputStream );

        try
        {
            return build( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    /**
     * @param dom
     * @return version of the security config dom
     */
    public static String validate( Xpp3Dom dom )
    {
        if ( dom.getChild( "users" ) == null )
        {
            return VERSION_UNKNOWN;
        }

        if ( dom.getChild( "users" ).getChildren( "org.artifactory.security.SimpleUser" ).length > 0 )
        {
            return VERSION_125;
        }

        if ( dom.getChild( "users" ).getChildren( "user" ).length > 0 )
        {
            return VERSION_130;
        }

        return VERSION_UNKNOWN;
    }

    public static ArtifactorySecurityConfig build( Xpp3Dom dom )
    {
        ArtifactorySecurityConfig securityConfig = new ArtifactorySecurityConfig();

        if ( validate( dom ).equals( VERSION_125 ) )
        {
            new SecurityConfig125Parser( dom, securityConfig ).parse();
        }
        else if ( validate( dom ).equals( VERSION_130 ) )
        {
            new SecurityConfig130Parser( dom, securityConfig ).parse();
        }

        return securityConfig;

    }

}
