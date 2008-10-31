package org.sonatype.nexus.proxy.maven.maven1;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.ShadowRepositoryConfigurator;

@Component( role = ShadowRepositoryConfigurator.class, hint = "m2-m1-shadow" )
public class M1LayoutedM2ShadowRepositoryConfigurator
    extends AbstractShadowRepositoryConfigurator
{

}
