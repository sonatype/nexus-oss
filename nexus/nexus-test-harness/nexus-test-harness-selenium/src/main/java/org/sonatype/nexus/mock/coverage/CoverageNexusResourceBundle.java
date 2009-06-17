package org.sonatype.nexus.mock.coverage;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;

@Component( role = NexusResourceBundle.class, hint = "CoverageNexusResourceBundle" )
public class CoverageNexusResourceBundle
    extends AbstractNexusResourceBundle
    implements NexusResourceBundle
{

    @Override
    public String getPreHeadContribution( Map<String, Object> context )
    {
        return "<script src=\"js/jscoverage.js?\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }

}
