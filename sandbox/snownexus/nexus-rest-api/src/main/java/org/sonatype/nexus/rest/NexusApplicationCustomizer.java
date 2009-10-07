package org.sonatype.nexus.rest;

import javax.inject.Singleton;

import org.sonatype.plexus.rest.RetargetableRestlet;
import org.sonatype.plugin.ExtensionPoint;

@ExtensionPoint
@Singleton
public interface NexusApplicationCustomizer
{
    void customize( NexusApplication nexusApplication, RetargetableRestlet root );
}
