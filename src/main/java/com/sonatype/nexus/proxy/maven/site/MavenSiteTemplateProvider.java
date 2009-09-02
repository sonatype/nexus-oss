package com.sonatype.nexus.proxy.maven.site;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

@Component( role = TemplateProvider.class, hint = MavenSiteTemplateProvider.PROVIDER_ID )
public class MavenSiteTemplateProvider
    extends AbstractRepositoryTemplateProvider
{
    public static final String PROVIDER_ID = "site-repository";

    private static final String MAVEN_SITE_ID = "maven-site";

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
}
