package org.sonatype.nexus.plugins.lvo.ui;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.ui.contribution.UiContributionBuilder;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor;

/**
 * @since 2.6
 */
@Named
@Singleton
public class LvoUiContributor implements UiContributor
{

    public static final String ARTIFACT_ID = "nexus-lvo-plugin";

    @Override
    public UiContribution contribute( final boolean debug )
    {
        return new UiContributionBuilder( this, OSS_PLUGIN_GROUP,
                                          ARTIFACT_ID ).boot( ARTIFACT_ID + "-all" ).build( debug );
    }
}
