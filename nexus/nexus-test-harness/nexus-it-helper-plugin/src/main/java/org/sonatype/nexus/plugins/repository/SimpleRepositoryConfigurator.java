package org.sonatype.nexus.plugins.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfigurator;

@Component( role = SimpleRepositoryConfigurator.class )
public class SimpleRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{

}
