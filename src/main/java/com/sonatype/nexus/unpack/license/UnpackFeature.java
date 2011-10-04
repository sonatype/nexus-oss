/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.unpack.license;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.licensing.feature.AbstractFeature;
import org.sonatype.licensing.feature.Feature;

@Component( role = Feature.class, hint = UnpackFeature.ID )
public class UnpackFeature
    extends AbstractFeature
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

