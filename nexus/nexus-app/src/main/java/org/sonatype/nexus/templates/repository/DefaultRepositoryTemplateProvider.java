package org.sonatype.nexus.templates.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.templates.AbstractTemplateProvider;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.repository.maven.Maven1Maven2ShadowRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2GroupRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;

@Component( role = TemplateProvider.class, hint = "default-repository" )
public class DefaultRepositoryTemplateProvider
    extends AbstractTemplateProvider<RepositoryTemplate>
{
    private static final String DEFAULT_HOSTED_RELEASE = "default_hosted_release";

    private static final String DEFAULT_HOSTED_SNAPSHOT = "default_hosted_snapshot";

    private static final String DEFAULT_PROXY_RELEASE = "default_proxy_release";

    private static final String DEFAULT_PROXY_SNAPSHOT = "default_proxy_snapshot";

    private static final String DEFAULT_VIRTUAL = "default_virtual";

    private static final String DEFAULT_GROUP = "default_group";

    @Requirement
    private Nexus nexus;

    protected Nexus getNexus()
    {
        return nexus;
    }

    public Class<RepositoryTemplate> getTemplateClass()
    {
        return RepositoryTemplate.class;
    }

    public List<RepositoryTemplate> getTemplates()
    {
        ArrayList<RepositoryTemplate> templates = new ArrayList<RepositoryTemplate>( 6 );

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

            templates.add( new Maven2GroupRepositoryTemplate( this, DEFAULT_GROUP, "Maven2 Group Repository" ) );
        }
        catch ( Exception e )
        {
            // will not happen
        }

        return Collections.unmodifiableList( templates );
    }
}
