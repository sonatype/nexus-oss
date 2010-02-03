package org.sonatype.nexus.plugins.rrb;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

public class RrbResourceBundle
    extends AbstractNexusResourceBundle
    implements NexusResourceBundle
{
    @Override
    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> result = new ArrayList<StaticResource>();

        result.add( new DefaultStaticResource( getClass().getResource( "/static/js/nexus-rrb-plugin-all.js" ),
                                               "/js/repoServer/nexus-rrb-plugin-all.js", "application/x-javascript" ) );

        return result;
    }
}
