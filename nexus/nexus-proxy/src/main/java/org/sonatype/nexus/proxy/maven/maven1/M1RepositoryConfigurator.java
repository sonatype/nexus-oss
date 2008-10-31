package org.sonatype.nexus.proxy.maven.maven1;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.RepositoryConfigurator;

@Component( role = RepositoryConfigurator.class, hint = "maven1" )
public class M1RepositoryConfigurator
    extends AbstractRepositoryConfigurator
    implements RepositoryConfigurator
{

}
