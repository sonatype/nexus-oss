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

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The mapping.
 * 
 * @author cstamas
 */
public class RepositoryPathMapping
{
    public enum MappingType
    {
        BLOCKING, INCLUSION, EXCLUSION;
    };

    private String id;

    private MappingType mappingType;

    private String groupId;

    private List<Pattern> patterns;

    private List<String> mappedRepositories;

    public RepositoryPathMapping( String id, MappingType mappingType, String groupId, List<String> regexps,
                                  List<String> mappedRepositories )
        throws PatternSyntaxException
    {
        this.id = id;

        this.mappingType = mappingType;

        if ( StringUtils.isBlank( groupId ) || "*".equals( groupId ) )
        {
            this.groupId = "*";
        }
        else
        {
            this.groupId = groupId;
        }

        patterns = new ArrayList<Pattern>( regexps.size() );

        for ( String regexp : regexps )
        {
            if( StringUtils.isNotEmpty( regexp ) )
            {
                patterns.add( Pattern.compile( regexp ) );
            }
        }

        this.mappedRepositories = mappedRepositories;
    }

    public boolean isAllGroups()
    {
        return "*".equals( getGroupId() );
    }

    public boolean matches( Repository repository, ResourceStoreRequest request )
    {
        if ( isAllGroups()
            || ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) && groupId.equals( repository
                .getId() ) ) )
        {
            for ( Pattern pattern : patterns )
            {
                if ( pattern.matcher( request.getRequestPath() ).matches() )
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

    public String getId()
    {
        return id;
    }

    public MappingType getMappingType()
    {
        return mappingType;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public List<Pattern> getPatterns()
    {
        return patterns;
    }

    public List<String> getMappedRepositories()
    {
        return mappedRepositories;
    }

    // ==

    public String toString()
    {
        StringBuilder sb = new StringBuilder( getId() );
        sb.append( "=[" );
        sb.append( "type=" );
        sb.append( getMappingType().toString() );
        sb.append( ", groupId=" );
        sb.append( getGroupId() );
        sb.append( ", patterns=" );
        sb.append( getPatterns().toString() );
        sb.append( ", mappedRepositories=" );
        sb.append( getMappedRepositories() );
        sb.append( "]" );
        return sb.toString();
    }
}
