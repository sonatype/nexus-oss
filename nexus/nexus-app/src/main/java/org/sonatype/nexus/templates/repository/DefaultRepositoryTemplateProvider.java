package org.sonatype.nexus.templates.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.templates.AbstractTemplateProvider;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.maven.Maven1GroupRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1Maven2ShadowRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1ProxyRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2GroupRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2Maven1ShadowRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;

@Component( role = TemplateProvider.class, hint = DefaultRepositoryTemplateProvider.PROVIDER_ID )
public class DefaultRepositoryTemplateProvider
    extends AbstractTemplateProvider<RepositoryTemplate>
{
    public static final String PROVIDER_ID = "default-repository";

    private static final String DEFAULT_HOSTED_RELEASE = "default_hosted_release";

    private static final String DEFAULT_HOSTED_SNAPSHOT = "default_hosted_snapshot";

    private static final String DEFAULT_PROXY_RELEASE = "default_proxy_release";

    private static final String DEFAULT_PROXY_SNAPSHOT = "default_proxy_snapshot";

    private static final String DEFAULT_VIRTUAL = "default_virtual";

    private static final String DEFAULT_GROUP = "default_group";

    @Requirement
    private Nexus nexus;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    protected Nexus getNexus()
    {
        return nexus;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration()
    {
        return super.getApplicationConfiguration();
    }

    public Class<RepositoryTemplate> getTemplateClass()
    {
        return RepositoryTemplate.class;
    }

    public TemplateSet getTemplates()
    {
        TemplateSet templates = new TemplateSet( null );

        try
        {
            templates.add( new Maven2HostedRepositoryTemplate( this, DEFAULT_HOSTED_RELEASE,
                                                               "Maven2 Hosted Release Repository",
                                                               RepositoryPolicy.RELEASE ) );

            templates.add( new Maven2HostedRepositoryTemplate( this, DEFAULT_HOSTED_SNAPSHOT,
                                                               "Maven2 Hosted Snapshot Repository",
                                                               RepositoryPolicy.SNAPSHOT ) );

            templates.add( new Maven2ProxyRepositoryTemplate( this, DEFAULT_PROXY_RELEASE,
                                                              "Maven2 Proxy Release Repository",
                                                              RepositoryPolicy.RELEASE ) );

            templates.add( new Maven2ProxyRepositoryTemplate( this, DEFAULT_PROXY_SNAPSHOT,
                                                              "Maven2 Proxy Snapshot Repository",
                                                              RepositoryPolicy.SNAPSHOT ) );

            templates.add( new Maven1Maven2ShadowRepositoryTemplate( this, DEFAULT_VIRTUAL,
                                                                     "Maven1-to-Maven2 Virtual Repository" ) );

            templates.add( new Maven2Maven1ShadowRepositoryTemplate( this, "maven2_maven1_virtual",
                                                                     "Maven2-to-Maven1 Virtual Repository" ) );

            templates.add( new Maven1HostedRepositoryTemplate( this, "maven1_hosted_release",
                                                               "Maven1 Hosted Release Repository",
                                                               RepositoryPolicy.RELEASE ) );

            templates.add( new Maven1HostedRepositoryTemplate( this, "maven1_hosted_snapshot",
                                                               "Maven1 Hosted Snapshot Repository",
                                                               RepositoryPolicy.SNAPSHOT ) );

            templates.add( new Maven1ProxyRepositoryTemplate( this, "maven1_proxy_release",
                                                              "Maven1 Proxy Release Repository",
                                                              RepositoryPolicy.RELEASE ) );

            templates.add( new Maven1ProxyRepositoryTemplate( this, "maven1_proxy_snapshot",
                                                              "Maven1 Proxy Snapshot Repository",
                                                              RepositoryPolicy.SNAPSHOT ) );

            templates.add( new Maven1GroupRepositoryTemplate( this, "maven1_group", "Maven1 Group Repository" ) );

            templates.add( new Maven2GroupRepositoryTemplate( this, DEFAULT_GROUP, "Maven2 Group Repository" ) );
        }
        catch ( Exception e )
        {
            // will not happen
        }

        return templates;
    }

    public TemplateSet getTemplates( Object filter )
    {
        return getTemplates().getTemplates( filter );
    }

    public TemplateSet getTemplates( Object... filters )
    {
        return getTemplates().getTemplates( filters );
    }

    public ManuallyConfiguredRepositoryTemplate createManuallyTemplate( CRepositoryCoreConfiguration configuration )
    {
        ContentClass contentClass =
            repositoryTypeRegistry
                .getRepositoryContentClass( configuration.getConfiguration( false ).getProviderRole(), configuration
                    .getConfiguration( false ).getProviderHint() );

        return new ManuallyConfiguredRepositoryTemplate( this, "manual", "Manually created template", contentClass,
                                                         null, configuration );
    }
}
