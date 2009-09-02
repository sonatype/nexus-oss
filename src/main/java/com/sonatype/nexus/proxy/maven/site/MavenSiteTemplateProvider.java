package com.sonatype.nexus.proxy.maven.site;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.templates.AbstractTemplateProvider;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

@Component( role = TemplateProvider.class, hint = MavenSiteTemplateProvider.PROVIDER_ID )
public class MavenSiteTemplateProvider
    extends AbstractTemplateProvider<RepositoryTemplate>
{

    public static final String PROVIDER_ID = "site-repository";

    private static final String MAVEN_SITE_ID = "maven-site";

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
            templates.add( new MavenSiteTemplate( this, MAVEN_SITE_ID, "Maven Hosted Site Repository" ) );
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

}
