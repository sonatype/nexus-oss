package org.sonatype.nexus.plugins.maven;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingException;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * This is a facade interface that simplifies and "nexusizes" the touchpoint of Nexus-Maven interaction.
 * 
 * @author cstamas
 */
public interface MavenGate
{
    /**
     * Calculates and returns the "effective POM" for the passed in POM item.
     * 
     * @param pomItem the StorageFileItem pointing to the POM you want to build project for. Must not be null.
     * @param usedNexusRepositoryIds the list of repositories to use in build. If null, "nexus-all" is used.
     * @param profileIds the profiles to activate during project build.
     * @param systemProperties system properties.
     * @param userProperties user properties.
     * @return
     * @throws ProjectBuildingException if some exception occurs during project build.
     * @throws IOException if some IOException occurs during project build.
     */
    Model getEffectiveModel( StorageFileItem pomItem, List<String> usedNexusRepositoryIds, List<String> profileIds,
                             Map<String, String> systemProperties, Map<String, String> userProperties )
        throws ProjectBuildingException, IOException;

    // get transitive dependencies trail
}
