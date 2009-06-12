package org.sonatype.nexus.templates.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.AbstractTemplateProvider;
import org.sonatype.nexus.templates.Template;
import org.sonatype.nexus.templates.TemplateProvider;

@Component( role = TemplateProvider.class, hint = "default-repository" )
public class DefaultRepositoryTemplateProvider
    extends AbstractTemplateProvider<Repository>
{
    private static final String DEFAULT_HOSTED_RELEASE = "default_hosted_release";

    private static final String DEFAULT_HOSTED_SNAPSHOT = "default_hosted_snapshot";

    private static final String DEFAULT_PROXY_RELEASE = "default_proxy_release";

    private static final String DEFAULT_PROXY_SNAPSHOT = "default_proxy_snapshot";

    private static final String DEFAULT_VIRTUAL = "default_virtual";

    private static final String DEFAULT_GROUP = "default_group";

    @Requirement
    private Nexus nexus;

    private List<Template<Repository>> templates;

    public Class<Repository> getImplementationClass()
    {
        return Repository.class;
    }

    public List<Template<Repository>> getTemplates()
    {
        if ( templates == null )
        {
            templates = new ArrayList<Template<Repository>>( 6 );
/*
            Maven2HostedRepositoryTemplate dhr =
                new Maven2HostedRepositoryTemplate( nexus, DEFAULT_HOSTED_RELEASE, "Maven2 Hosted Release Repository" );

            dhr.getTemplateHolder().getConfiguration().getExternalConfiguration()
                .setRepositoryPolicy( RepositoryPolicy.RELEASE );

            templates.add( new Maven2HostedRepositoryTemplate( nexus, DEFAULT_HOSTED_SNAPSHOT,
                                                               "Maven2 Hosted Snapshot Repository" ) );
            templates.add( new Maven2HostedRepositoryTemplate( nexus, DEFAULT_PROXY_RELEASE,
                                                               "Maven2 Proxy Release Repository" ) );

            templates.add( new Maven2HostedRepositoryTemplate( nexus, DEFAULT_PROXY_SNAPSHOT,
                                                               "Maven2 Proxy Snapshot Repository" ) );

            templates.add( new Maven2HostedRepositoryTemplate( nexus, DEFAULT_VIRTUAL,
                                                               "MAven1-to-Maven2 Virtual Repository" ) );

            templates.add( new Maven2GroupRepositoryTemplate( nexus, DEFAULT_GROUP, "Maven2 Group Repository" ) );
            */
        }
        return Collections.unmodifiableList( templates );
    }
}
