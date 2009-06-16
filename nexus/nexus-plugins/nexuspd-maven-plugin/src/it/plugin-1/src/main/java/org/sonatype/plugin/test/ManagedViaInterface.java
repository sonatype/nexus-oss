package org.sonatype.plugin.test;

import javax.inject.Inject;

public class ManagedViaInterface implements ManagedInterface
{
    
    @Inject
    private ComponentManaged mangedComponent;

    public ComponentManaged getMangedComponent()
    {
        return mangedComponent;
    }
}
