package org.sonatype.nexus.plugins.mock;

import java.util.Collections;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.DefaultContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;

@Component( role = RepositoryTypeRegistry.class )
public class MockRepositoryTypeRegistry
    implements RepositoryTypeRegistry
{

    public Set<ContentClass> getContentClasses()
    {
        return Collections.emptySet();
    }

    public Set<String> getExistingRepositoryHints( String role )
    {
        return Collections.emptySet();
    }

    public ContentClass getRepositoryContentClass( String role, String hint )
    {
        return new DefaultContentClass( hint );
    }

    public String getRepositoryDescription( String role, String hint )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<String> getRepositoryRoles()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<RepositoryTypeDescriptor> getRepositoryTypeDescriptors()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
