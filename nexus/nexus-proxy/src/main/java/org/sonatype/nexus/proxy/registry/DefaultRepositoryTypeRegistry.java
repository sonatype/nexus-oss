package org.sonatype.nexus.proxy.registry;

import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = RepositoryTypeRegistry.class )
public class DefaultRepositoryTypeRegistry
    extends AbstractLogEnabled
    implements RepositoryTypeRegistry
{
    @Requirement( role = Repository.class )
    private Map<String, Repository> existingRepositoryTypes;

    public Set<String> getExistingRepositoryTypes()
    {
        return existingRepositoryTypes.keySet();
    }

    public ContentClass getTypeContentClass( String repositoryType )
    {
        if ( existingRepositoryTypes.containsKey( repositoryType ) )
        {
            return existingRepositoryTypes.get( repositoryType ).getRepositoryContentClass();
        }
        else
        {
            return null;
        }
    }

}
