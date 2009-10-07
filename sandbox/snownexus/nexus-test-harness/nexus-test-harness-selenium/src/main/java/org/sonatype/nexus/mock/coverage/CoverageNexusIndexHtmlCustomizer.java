package org.sonatype.nexus.mock.coverage;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "CoverageNexusIndexHtmlCustomizer" )
public class CoverageNexusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
    implements NexusIndexHtmlCustomizer
{
    @Override
    public String getPreHeadContribution( Map<String, Object> context )
    {
        return "<script src=\"jscoverage.js?\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }
}
