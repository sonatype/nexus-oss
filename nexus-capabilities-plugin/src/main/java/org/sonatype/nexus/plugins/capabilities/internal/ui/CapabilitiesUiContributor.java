/*
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.capabilities.internal.ui;

import org.sonatype.nexus.plugins.rest.UiContributionBuilder;
import org.sonatype.nexus.plugins.rest.UiContributor;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Capabilities {@link UiContributor}.
 */
@Named
@Singleton
public class CapabilitiesUiContributor
    implements UiContributor
{
    private static final String GROUP_ID = "org.sonatype.nexus.plugins";

    private static final String ARTIFACT_ID = "nexus-capabilities-plugin";

    @Override
    public UiContribution contribute( final boolean debug )
    {
        return new UiContributionBuilder( this, GROUP_ID, ARTIFACT_ID ).build(debug);
    }
}
