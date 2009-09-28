package com.sonatype.nexus.proxy.maven.site.ui;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

@Component( role = NexusResourceBundle.class, hint = "MavenSiteNexusResourceBundle" )
public class MavenSiteNexusResourceBundle
    extends AbstractNexusResourceBundle
{
    @Override
    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> result = new ArrayList<StaticResource>();

        DefaultStaticResource resource =
            new DefaultStaticResource( getClass().getResource( "/static/js/nexus-maven-site-plugin-all.js" ),
                                       "/js/repoServer/nexus-maven-site-plugin-all.js", "application/x-javascript" );

        result.add( resource );

        return result;
    }
}
