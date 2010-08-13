package org.sonatype.nexus.plugins.capabilities.internal.ui;

import javax.inject.Singleton;

import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;

@Singleton
public class CapabilityDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    public String getPluginId()
    {
        return "org.sonatype.nexus.plugins.capabilities.imp";
    }

    @Override
    public String getDescription()
    {
        return "LatestVersionOf Capabilities API";
    }

}