package org.sonatype.nexus.mock.state;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "StatusIndexHtmlCustomizer" )
public class StatusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
    implements NexusIndexHtmlCustomizer
{
    @Override
    public String getPreHeadContribution( Map<String, Object> context )
    {
        return "<script>\n" + //
            "function isRunning() { return true; }\n" + //
            "</script>\n";
    }
}
