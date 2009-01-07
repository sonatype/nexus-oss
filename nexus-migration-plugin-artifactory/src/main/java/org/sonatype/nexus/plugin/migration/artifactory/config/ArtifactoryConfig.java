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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class ArtifactoryConfig
{

    private final Xpp3Dom dom;

    public ArtifactoryConfig( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    @SuppressWarnings( "deprecation" )
    public static ArtifactoryConfig read( File file )
        throws IOException, XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( file );
        try
        {
            return new ArtifactoryConfig( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            reader.close();
        }
    }

    @SuppressWarnings( "deprecation" )
    public static ArtifactoryConfig read( InputStream input )
        throws IOException, XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( input );
        try
        {
            return new ArtifactoryConfig( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            reader.close();
        }
    }

    public Map<String, ArtifactoryRepository> getLocalRepositories()
    {
        Xpp3Dom repositoriesDom = dom.getChild( "localRepositories" );
        if ( repositoriesDom == null )
        {
            return Collections.emptyMap();
        }

        Map<String, ArtifactoryRepository> localRepositories = new HashMap<String, ArtifactoryRepository>();
        for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "localRepository" ) )
        {
            ArtifactoryRepository repo = new ArtifactoryRepository( repoDom );
            localRepositories.put( repo.getKey(), repo );
        }
        localRepositories = Collections.unmodifiableMap( localRepositories );
        return localRepositories;
    }

    public Map<String, ArtifactoryRepository> getRemoteRepositories()
    {
        Xpp3Dom repositoriesDom = dom.getChild( "remoteRepositories" );
        if ( repositoriesDom == null )
        {
            return Collections.emptyMap();
        }

        Map<String, ArtifactoryRepository> remoteRepositories = new HashMap<String, ArtifactoryRepository>();
        for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "remoteRepository" ) )
        {
            ArtifactoryRepository repo = new ArtifactoryRepository( repoDom );
            remoteRepositories.put( repo.getKey(), repo );
        }
        remoteRepositories = Collections.unmodifiableMap( remoteRepositories );
        return remoteRepositories;
    }

    public Map<String, ArtifactoryVirtualRepository> getVirtualRepositories()
    {
        Xpp3Dom repositoriesDom = dom.getChild( "virtualRepositories" );
        if ( repositoriesDom == null )
        {
            return Collections.emptyMap();
        }

        Map<String, ArtifactoryVirtualRepository> virtualRepositories =
            new LinkedHashMap<String, ArtifactoryVirtualRepository>();
        for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "virtualRepository" ) )
        {
            ArtifactoryVirtualRepository virtualRepo = new ArtifactoryVirtualRepository( repoDom );
            virtualRepositories.put( virtualRepo.getKey(), virtualRepo );
        }
        virtualRepositories = Collections.unmodifiableMap( virtualRepositories );
        return virtualRepositories;
    }

    public Map<String, ArtifactoryProxy> getProxies()
    {
        Xpp3Dom proxiesDom = dom.getChild( "proxies" );
        if ( proxiesDom == null )
        {
            return Collections.emptyMap();
        }

        Map<String, ArtifactoryProxy> proxies = new LinkedHashMap<String, ArtifactoryProxy>();
        for ( Xpp3Dom proxyDom : proxiesDom.getChildren( "proxy" ) )
        {
            proxies.put( getValue( proxyDom, "key" ), new ArtifactoryProxy( proxyDom ) );
        }
        proxies = Collections.unmodifiableMap( proxies );
        return proxies;
    }

    public Map<String, ArtifactoryRepository> getRepositories()
    {
        Map<String, ArtifactoryRepository> repositories = new HashMap<String, ArtifactoryRepository>();
        repositories.putAll( getLocalRepositories() );
        repositories.putAll( getRemoteRepositories() );
        repositories = Collections.unmodifiableMap( repositories );
        return repositories;
    }

}
