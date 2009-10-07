package org.sonatype.nexus.mock.firebug;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "FirebugNexusIndexHtmlCustomizer" )
public class FirebugNexusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
    implements NexusIndexHtmlCustomizer
{
    @Override
    public String getPreHeadContribution( Map<String, Object> context )
    {
        return "<script src=\"firebug-lite-1.2-compressed.js?\" type=\"text/javascript\" charset=\"utf-8\"></script>\n"
            + "  <script type=\"text/javascript\">" + "firebug.env.debug = false;" + "</script>";
    }
}
