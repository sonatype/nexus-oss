package com.sonatype.nexus.unpack.license;

import org.codehaus.plexus.component.annotations.Component;

import com.sonatype.license.feature.AbstractPlexusFeature;
import com.sonatype.license.feature.PlexusFeature;

@Component( role = PlexusFeature.class, hint = UnpackPlexusFeature.ID )
public class UnpackPlexusFeature
    extends AbstractPlexusFeature
{
    public static final String ID = "Unpack";

    public static final String DESCRIPTION = "Unpack Plugin";

    public static final String NAME = "Unpack";

    public static final String SHORT_NAME = "UNPACK";

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

