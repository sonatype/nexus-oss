package org.sonatype.nexus.plugins.rrb;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;

@Component( role = NexusResourceBundle.class, hint = "RRBDocumentationResourceBundle" )
public class RRBDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    public String getPluginId()
    {
        return "nexus-rrb-plugin";
    }

    @Override
    public String getUrlSnippet()
    {
        return "rrb";
    }

}
