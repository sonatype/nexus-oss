package org.sonatype.nexus.proxy.mapping;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;

/**
 * The mapping.
 * 
 * @author cstamas
 */
public class RepositoryPathMapping
{
    private String groupId;

    private boolean allGroups;

    private Pattern pattern;

    private List<ResourceStore> resourceStores;

    public RepositoryPathMapping( boolean allGroups, String groupId, String regexp, List<ResourceStore> resourceStores )
        throws PatternSyntaxException
    {
        if ( allGroups )
        {
            this.groupId = "*";

            this.allGroups = true;
        }
        else
        {
            this.groupId = groupId;

            this.allGroups = false;
        }

        this.pattern = Pattern.compile( regexp );

        this.resourceStores = resourceStores;
    }

    public boolean matches( ResourceStoreRequest request )
    {
        if ( allGroups || groupId.equals( request.getRequestRepositoryGroupId() ) )
        {
            return pattern.matcher( request.getRequestPath() ).matches();
        }
        else
        {
            return false;
        }
    }

    public String getGroupId()
    {
        return groupId;
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public List<ResourceStore> getResourceStores()
    {
        return resourceStores;
    }
}
