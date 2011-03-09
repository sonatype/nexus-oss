/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.maven.site.license;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.licensing.feature.AbstractFeature;
import org.sonatype.licensing.feature.Feature;

@Component( role = Feature.class, hint = MavenSiteFeature.ID )
public class MavenSiteFeature
    extends AbstractFeature
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

