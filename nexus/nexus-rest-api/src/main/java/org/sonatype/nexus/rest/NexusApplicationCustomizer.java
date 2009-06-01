package org.sonatype.nexus.rest;

import org.sonatype.plexus.plugin.ExtensionPoint;
import org.sonatype.plexus.rest.RetargetableRestlet;

@ExtensionPoint
public interface NexusApplicationCustomizer
{
    void customize( NexusApplication nexusApplication, RetargetableRestlet root );
}
