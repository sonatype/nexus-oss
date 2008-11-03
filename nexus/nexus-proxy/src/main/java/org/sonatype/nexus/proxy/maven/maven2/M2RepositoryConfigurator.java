package org.sonatype.nexus.proxy.maven.maven2;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.RepositoryConfigurator;

@Component( role = RepositoryConfigurator.class, hint = "maven2" )
public class M2RepositoryConfigurator
    extends AbstractMavenRepositoryConfigurator
    implements RepositoryConfigurator
{

}
