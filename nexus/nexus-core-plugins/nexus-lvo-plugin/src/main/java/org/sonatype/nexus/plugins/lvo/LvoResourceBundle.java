package org.sonatype.nexus.plugins.lvo;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

@Component( role = NexusResourceBundle.class, hint = "LvoResourceBundle" )
public class LvoResourceBundle
    extends AbstractNexusResourceBundle
{
    @Override
    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> result = new ArrayList<StaticResource>();

        result.add( new DefaultStaticResource( getClass().getResource( "/static/js/nexus-lvo-plugin-all.js" ),
                                               "/js/repoServer/nexus-lvo-plugin-all.js", "application/x-javascript" ) );

        return result;
    }
}
