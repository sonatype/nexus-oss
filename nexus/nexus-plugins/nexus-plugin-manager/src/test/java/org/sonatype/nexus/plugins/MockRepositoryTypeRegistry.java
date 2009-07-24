package org.sonatype.nexus.plugins;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;

@Component( role = RepositoryTypeRegistry.class )
public class MockRepositoryTypeRegistry
    implements RepositoryTypeRegistry
{
    private HashSet<RepositoryTypeDescriptor> types = new HashSet<RepositoryTypeDescriptor>();

    public Set<RepositoryTypeDescriptor> getRegisteredRepositoryTypeDescriptors()
    {
        return Collections.unmodifiableSet( types );
    }

    public boolean registerRepositoryTypeDescriptors( RepositoryTypeDescriptor d )
    {
        return types.add( d );
    }

    public boolean unregisterRepositoryTypeDescriptors( RepositoryTypeDescriptor d )
    {
        return types.remove( d );
    }

    // == neglected all below

    public Set<ContentClass> getContentClasses()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<String> getExistingRepositoryHints( String role )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ContentClass getRepositoryContentClass( String role, String hint )
    {
        // TODO Auto-generated method stub
        return null;
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

}
