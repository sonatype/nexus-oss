package org.sonatype.nexus.buup.ui;

import java.io.File;
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

        DefaultStaticResource resource = null;

        try
        {
            resource = new DefaultStaticResource(
                new File( "/home/juven/js/nexus-buup-plugin-all.js" ).toURL(),
                "/js/repoServer/nexus-buup-plugin-all.js",
                "application/x-javascript" );

            result.add( resource );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return result;
    }
}
