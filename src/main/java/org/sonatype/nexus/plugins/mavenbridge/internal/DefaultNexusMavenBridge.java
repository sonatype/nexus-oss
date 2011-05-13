package org.sonatype.nexus.plugins.mavenbridge.internal;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.sonatype.nexus.plugins.mavenbridge.NexusMavenBridge;
import org.sonatype.sisu.maven.bridge.MavenBridge;

@Named
@Singleton
public class DefaultNexusMavenBridge
    implements NexusMavenBridge
{

    private final MavenBridge mavenBridge;

    @Inject
    DefaultNexusMavenBridge( final MavenBridge mavenBridge )
    {
        this.mavenBridge = mavenBridge;
    }

    @Override
    public Model buildModel( final File pom, final Map<String, String> repositories )
        throws ModelBuildingException
    {
        return mavenBridge.buildModel( pom, repositories );
    }

}
