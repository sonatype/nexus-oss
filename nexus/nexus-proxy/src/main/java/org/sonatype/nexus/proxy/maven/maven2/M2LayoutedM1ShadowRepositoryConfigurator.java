package org.sonatype.nexus.proxy.maven.maven2;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.ShadowRepositoryConfigurator;

@Component( role = ShadowRepositoryConfigurator.class, hint = "m1-m2-shadow" )
public class M2LayoutedM1ShadowRepositoryConfigurator
    extends AbstractShadowRepositoryConfigurator
{

}
