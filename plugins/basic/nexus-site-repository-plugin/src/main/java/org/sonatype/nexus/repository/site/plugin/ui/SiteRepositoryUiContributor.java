package org.sonatype.nexus.repository.site.plugin.ui;

import org.sonatype.nexus.plugins.ui.contribution.UiContributionBuilder;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor;

/**
 * @since 2.6
 */
public class SiteRepositoryUiContributor
    implements UiContributor
{

    private static final String ARTIFACT_ID = "nexus-site-repository-plugin";

    public static final String GROUP_ID = "org.sonatype.nexus.plugins";

    @Override
    public UiContribution contribute( final boolean debug )
    {
        return new UiContributionBuilder( this, GROUP_ID, ARTIFACT_ID ).build( debug );
    }
}
