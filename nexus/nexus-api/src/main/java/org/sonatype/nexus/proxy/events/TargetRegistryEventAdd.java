package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;

public class TargetRegistryEventAdd
    extends TargetRegistryEvent
{
    public TargetRegistryEventAdd( final TargetRegistry targetRegistry, final Target target )
    {
        super( targetRegistry, target );
    }
}
