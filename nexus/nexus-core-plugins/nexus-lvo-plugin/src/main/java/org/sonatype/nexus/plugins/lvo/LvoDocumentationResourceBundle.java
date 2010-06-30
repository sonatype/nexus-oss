package org.sonatype.nexus.plugins.lvo;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;

@Component( role = NexusResourceBundle.class, hint = "LvoDocumentationResourceBundle" )
public class LvoDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    public String getPluginId()
    {
        return "nexus-lvo-plugin";
    }

    @Override
    public String getDescription()
    {
        return "LatestVersionOf Plugin API";
    }

}
