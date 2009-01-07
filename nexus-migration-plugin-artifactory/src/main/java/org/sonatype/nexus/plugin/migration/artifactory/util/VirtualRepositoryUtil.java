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
package org.sonatype.nexus.plugin.migration.artifactory.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryVirtualRepository;

public class VirtualRepositoryUtil
{

    public static void resolveRepositories( Map<String, ArtifactoryVirtualRepository> virtualRepositories )
    {
        for ( String repoId : virtualRepositories.keySet() )
        {
            ArtifactoryVirtualRepository repo = virtualRepositories.get( repoId );
            List<String> resolvedChilds = getRepositories( repo, virtualRepositories );
            repo.setResolvedRepositories( resolvedChilds );
        }

    }

    private static List<String> getRepositories( ArtifactoryVirtualRepository repo,
                                                 Map<String, ArtifactoryVirtualRepository> virtualRepositories )
    {
        List<String> resolvedRepos = new ArrayList<String>();
        List<String> repositories = repo.getRepositories();
        for ( String childId : repositories )
        {
            ArtifactoryVirtualRepository virtualRepo = virtualRepositories.get( childId );
            if ( virtualRepo != null )
            {
                resolvedRepos.addAll( getRepositories( virtualRepo, virtualRepositories ) );
            }
            else
            {
                resolvedRepos.add( childId );
            }
        }

        return resolvedRepos;
    }

}
