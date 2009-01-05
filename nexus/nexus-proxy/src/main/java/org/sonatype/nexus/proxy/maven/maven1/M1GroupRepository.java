package org.sonatype.nexus.proxy.maven.maven1;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.DefaultGroupRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;

@Component( role = GroupRepository.class, hint = "maven1", instantiationStrategy = "per-lookup" )
public class M1GroupRepository
    extends DefaultGroupRepository
{
    @Requirement( hint = "maven1" )
    private ContentClass contentClass;

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }
}
