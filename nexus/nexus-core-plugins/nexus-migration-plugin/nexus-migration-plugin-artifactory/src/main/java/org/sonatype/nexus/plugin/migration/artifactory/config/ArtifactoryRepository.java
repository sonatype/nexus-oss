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
package org.sonatype.nexus.plugin.migration.artifactory.config;

import static org.sonatype.nexus.plugin.migration.artifactory.util.DomUtil.getValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class ArtifactoryRepository
{
    private final Xpp3Dom dom;

    public ArtifactoryRepository( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    @SuppressWarnings( "deprecation" )
    public static ArtifactoryRepository read( File file )
        throws IOException, XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( file );
        try
        {
            return new ArtifactoryRepository( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            reader.close();
        }
    }

    @SuppressWarnings( "deprecation" )
    public static ArtifactoryRepository read( InputStream input )
        throws IOException, XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( input );
        try
        {
            return new ArtifactoryRepository( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            reader.close();
        }
    }

    public String getKey()
    {
        return getValue( dom, "key" );
    }

    public String getDescription()
    {
        return getValue( dom, "description" );
    }

    public boolean getHandleReleases()
    {
        return Boolean.parseBoolean( getValue( dom, "handleReleases" ) );
    }

    public boolean getHandleSnapshots()
    {
        return Boolean.parseBoolean( getValue( dom, "handleSnapshots" ) );
    }

    public String getUrl()
    {
        return getValue( dom, "url" );
    }

    public String getType()
    {
        return getValue( dom, "type" );
    }

    public String getProxy()
    {
        return getValue( dom, "proxyRef" );
    }

    public String getUsername()
    {
        return getValue( dom, "username" );
    }

    public String getPassword()
    {
        return getValue( dom, "password" );
    }

}
