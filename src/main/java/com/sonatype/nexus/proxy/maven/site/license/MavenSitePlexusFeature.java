package com.sonatype.nexus.proxy.maven.site.license;

import org.codehaus.plexus.component.annotations.Component;

import com.sonatype.license.feature.AbstractPlexusFeature;
import com.sonatype.license.feature.PlexusFeature;

@Component( role = PlexusFeature.class, hint = MavenSitePlexusFeature.ID )
public class MavenSitePlexusFeature
    extends AbstractPlexusFeature
{
    public static final String ID = "MavenSite";

    public static final String DESCRIPTION = "Maven Site Plugin";

    public static final String NAME = "Maven Site";

    public static final String SHORT_NAME = "MVNSITE";

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return NAME;
    }

    public String getDescription()
    {
        return DESCRIPTION;
    }

    public String getShortName()
    {
        return SHORT_NAME;
    }
}

