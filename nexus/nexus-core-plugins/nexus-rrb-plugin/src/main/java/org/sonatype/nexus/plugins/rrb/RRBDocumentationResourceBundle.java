package org.sonatype.nexus.plugins.rrb;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;

@Component( role = NexusResourceBundle.class, hint = "RRBDocumentationResourceBundle" )
public class RRBDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    protected String getPluginId()
    {
        return "nexus-rrb-plugin";
    }

}
