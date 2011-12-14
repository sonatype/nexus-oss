package org.sonatype.nexus.rest.docs;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;

@Component( role = NexusResourceBundle.class, hint = "TimelineDocumentationNexusResourceBundle" )
public class TimelineDocumentationNexusResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    public String getPluginId()
    {
        return "nexus-timeline-plugin";
    }

    @Override
    public String getDescription()
    {
        return "Timeline Plugin API";
    }
}
