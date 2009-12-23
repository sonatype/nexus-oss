package org.sonatype.nexus.buup.ui;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

@Component( role = NexusResourceBundle.class, hint = "bundleUpgrader" )
public class BundleUpgraderNeuxsResourceBundle
    extends AbstractNexusResourceBundle
{
    @Override
    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> result = new ArrayList<StaticResource>();

        result.add( new DefaultStaticResource(
            getClass().getResource( "/static/js/nexus-buup-plugin-all.js" ),
            "/js/repoServer/nexus-buup-plugin-all.js",
            "application/x-javascript" ) );

        result.add( new DefaultStaticResource(
            getClass().getResource( "/static/html/license.html" ),
            "/html/license.html",
            "text/html" ) );

        return result;
    }
}
