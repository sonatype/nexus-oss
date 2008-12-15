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
