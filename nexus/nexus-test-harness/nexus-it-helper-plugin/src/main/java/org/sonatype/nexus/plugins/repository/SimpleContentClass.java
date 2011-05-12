package org.sonatype.nexus.plugins.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

@Component( role = ContentClass.class, hint = SimpleContentClass.ID )
public class SimpleContentClass
    extends AbstractIdContentClass
{
    public static final String ID = "simple";

    @Override
    public String getId()
    {
        return ID;
    }
}
