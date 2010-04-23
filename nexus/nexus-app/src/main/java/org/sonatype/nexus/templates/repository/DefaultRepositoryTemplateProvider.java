package org.sonatype.nexus.templates.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
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

/**
 * A template provider implementation that covers core-supported repositories.
 * 
 * @author cstamas
 */
@Component( role = TemplateProvider.class, hint = DefaultRepositoryTemplateProvider.PROVIDER_ID )
public class DefaultRepositoryTemplateProvider
    extends AbstractRepositoryTemplateProvider
{
    public static final String PROVIDER_ID = "default-repository";

    private static final String DEFAULT_HOSTED_RELEASE = "default_hosted_release";

    private static final String DEFAULT_HOSTED_SNAPSHOT = "default_hosted_snapshot";

    private static final String DEFAULT_PROXY_RELEASE = "default_proxy_release";

    private static final String DEFAULT_PROXY_SNAPSHOT = "default_proxy_snapshot";

    private static final String DEFAULT_VIRTUAL = "default_virtual";

    private static final String DEFAULT_GROUP = "default_group";

    public TemplateSet getTemplates()
    {
        TemplateSet templates = new TemplateSet( null );

        try
        {
            templates.add( new Maven2HostedRepositoryTemplate( this, DEFAULT_HOSTED_RELEASE,
                "Maven2 (hosted, release)", RepositoryPolicy.RELEASE ) );

            templates.add( new Maven2HostedRepositoryTemplate( this, DEFAULT_HOSTED_SNAPSHOT,
                "Maven2 (hosted, snapshot)", RepositoryPolicy.SNAPSHOT ) );

            templates.add( new Maven2ProxyRepositoryTemplate( this, DEFAULT_PROXY_RELEASE,
                "Maven2 (proxy, release)", RepositoryPolicy.RELEASE ) );

            templates.add( new Maven2ProxyRepositoryTemplate( this, DEFAULT_PROXY_SNAPSHOT,
                "Maven2 (proxy, snapshot)", RepositoryPolicy.SNAPSHOT ) );

            templates.add( new Maven1Maven2ShadowRepositoryTemplate( this, DEFAULT_VIRTUAL,
                "Maven1-to-Maven2 (vitual)" ) );

            templates.add( new Maven2Maven1ShadowRepositoryTemplate( this, DEFAULT_VIRTUAL,
                "Maven2-to-Maven1 (virtual)" ) );

            templates.add( new Maven1HostedRepositoryTemplate( this, "maven1_hosted_release",
                "Maven1 (hosted, release)", RepositoryPolicy.RELEASE ) );

            templates.add( new Maven1HostedRepositoryTemplate( this, "maven1_hosted_snapshot",
                "Maven1 (hosted, snapshot)", RepositoryPolicy.SNAPSHOT ) );

            templates.add( new Maven1ProxyRepositoryTemplate( this, "maven1_proxy_release",
                "Maven1 (proxy, release)", RepositoryPolicy.RELEASE ) );

            templates.add( new Maven1ProxyRepositoryTemplate( this, "maven1_proxy_snapshot",
                "Maven1 (proxy, snapshot)", RepositoryPolicy.SNAPSHOT ) );

            templates.add( new Maven1GroupRepositoryTemplate( this, "maven1_group", "Maven1 (group)" ) );

            templates.add( new Maven2GroupRepositoryTemplate( this, DEFAULT_GROUP, "Maven2 (group)" ) );
        }
        catch ( Exception e )
        {
            // will not happen
        }

        return templates;
    }
}
