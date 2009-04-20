package org.sonatype.nexus.rest;

import org.sonatype.plexus.rest.RetargetableRestlet;

public interface NexusApplicationCustomizer
{
    void customize( NexusApplication nexusApplication, RetargetableRestlet root );
}
