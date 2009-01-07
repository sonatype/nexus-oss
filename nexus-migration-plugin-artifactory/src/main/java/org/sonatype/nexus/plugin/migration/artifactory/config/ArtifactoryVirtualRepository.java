/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ArtifactoryVirtualRepository
{

    private Xpp3Dom dom;

    private List<String> resolvedRepositories;

    public ArtifactoryVirtualRepository( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getKey()
    {
        return getValue( dom, "key" );
    }

    public List<String> getRepositories()
    {
        Xpp3Dom repositoriesDom = dom.getChild( "repositories" );
        if ( repositoriesDom == null )
        {
            return Collections.emptyList();
        }

        List<String> repos = new ArrayList<String>();
        for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "repositoryRef" ) )
        {
            repos.add( repoDom.getValue() );
        }
        return Collections.unmodifiableList( repos );
    }

    public List<String> getResolvedRepositories()
    {
        return resolvedRepositories;
    }

    public void setResolvedRepositories( List<String> resolvedRepositories )
    {
        this.resolvedRepositories = resolvedRepositories;
    }

}
