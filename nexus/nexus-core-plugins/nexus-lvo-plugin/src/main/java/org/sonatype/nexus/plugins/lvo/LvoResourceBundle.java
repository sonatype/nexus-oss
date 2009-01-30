package org.sonatype.nexus.plugins.lvo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        result.add( new DefaultStaticResource(
            getClass().getResource( "/static/js/repoServer.NexusUpgradeChecker.js" ),
            "/js/repoServer/repoServer.NexusUpgradeChecker.js" ) );

        return result;
    }

    @Override
    public String getPostHeadContribution( Map<String, Object> ctx )
    {
        return "<script src=\"js/repoServer/repoServer.NexusUpgradeChecker.js\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }

}
