package com.sonatype.nexus.unpack.ui;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;

@Component( role = NexusResourceBundle.class, hint = "UnpackDocumentationResourceBundle" )
public class UnpackDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    public String getPluginId()
    {
        return "nexus-unpack-plugin";
    }

    @Override
    public String getUrlSnippet()
    {
        return "unpack";
    }

}
