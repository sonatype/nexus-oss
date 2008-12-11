/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
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
