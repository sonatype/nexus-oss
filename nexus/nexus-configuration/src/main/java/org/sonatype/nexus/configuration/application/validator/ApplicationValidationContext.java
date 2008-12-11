/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.application.validator;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.configuration.validator.AbstractValidationContext;

public class ApplicationValidationContext
    extends AbstractValidationContext
{
    private List<String> existingRepositoryIds;

    private List<String> existingRepositoryShadowIds;

    private List<String> existingRepositoryGroupIds;

    private List<String> existingPathMappingIds;

    private List<String> existingRealms;

    private List<String> existingRepositoryTargetIds;

    public void addExistingRepositoryIds()
    {
        if ( this.existingRepositoryIds == null )
        {
            this.existingRepositoryIds = new ArrayList<String>();
        }
    }

    public void addExistingRepositoryShadowIds()
    {
        if ( this.existingRepositoryShadowIds == null )
        {
            this.existingRepositoryShadowIds = new ArrayList<String>();
        }
    }

    public void addExistingRepositoryGroupIds()
    {
        if ( this.existingRepositoryGroupIds == null )
        {
            this.existingRepositoryGroupIds = new ArrayList<String>();
        }
    }

    public void addExistingPathMappingIds()
    {
        if ( this.existingPathMappingIds == null )
        {
            this.existingPathMappingIds = new ArrayList<String>();
        }
    }

    public void addExistingRealms()
    {
        if ( this.existingRealms == null )
        {
            this.existingRealms = new ArrayList<String>();
        }
    }

    public void addExistingRepositoryTargetIds()
    {
        if ( this.existingRepositoryTargetIds == null )
        {
            this.existingRepositoryTargetIds = new ArrayList<String>();
        }
    }

    public List<String> getExistingRepositoryIds()
    {
        return existingRepositoryIds;
    }

    public List<String> getExistingRepositoryShadowIds()
    {
        return existingRepositoryShadowIds;
    }

    public List<String> getExistingRepositoryGroupIds()
    {
        return existingRepositoryGroupIds;
    }

    public List<String> getExistingPathMappingIds()
    {
        return existingPathMappingIds;
    }

    public List<String> getExistingRealms()
    {
        return existingRealms;
    }

    public List<String> getExistingRepositoryTargetIds()
    {
        return existingRepositoryTargetIds;
    }

}
