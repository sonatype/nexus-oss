package org.sonatype.nexus.plugin.migration.artifactory.config;

import java.util.Collections;
import java.util.List;

public class ArtifactoryDefaultVirtualRepository
    extends ArtifactoryVirtualRepository
{

    private List<String> allRepositories;

    public ArtifactoryDefaultVirtualRepository( List<String> allRepositories )
    {
        super( null );
        this.allRepositories = Collections.unmodifiableList( allRepositories );
    }

    @Override
    public String getKey()
    {
        return "repo";
    }

    @Override
    public List<String> getRepositories()
    {
        return allRepositories;
    }

}
