/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryRequest;

/**
 * The mapping.
 * 
 * @author cstamas
 */
public class RepositoryPathMapping
{
    private String groupId;

    private boolean allGroups;

    private List<Pattern> patterns;

    private List<Repository> resourceStores;

    public RepositoryPathMapping( boolean allGroups, String groupId, List<String> regexps,
        List<Repository> resourceStores )
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

        patterns = new ArrayList<Pattern>( regexps.size() );

        for ( String regexp : regexps )
        {
            patterns.add( Pattern.compile( regexp ) );
        }

        this.resourceStores = resourceStores;
    }

    public boolean matches( RepositoryRequest request )
    {
        if ( allGroups || groupId.equals( request.getRepository().getId() ) )
        {
            for ( Pattern pattern : patterns )
            {
                if ( pattern.matcher( request.getResourceStoreRequest().getRequestPath() ).matches() )
                {
                    return true;
                }
            }

            return false;
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

    public List<Pattern> getPatterns()
    {
        return patterns;
    }

    public List<Repository> getResourceStores()
    {
        return resourceStores;
    }
}
