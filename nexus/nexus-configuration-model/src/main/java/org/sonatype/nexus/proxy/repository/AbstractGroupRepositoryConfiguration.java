/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;

public class AbstractGroupRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String MEMBER_REPOSITORIES = "memberRepositories";

    public AbstractGroupRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public List<String> getMemberRepositoryIds()
    {
        return getCollection( getRootNode(), MEMBER_REPOSITORIES );
    }

    public void setMemberRepositoryIds( List<String> ids )
    {
        setCollection( getRootNode(), MEMBER_REPOSITORIES, ids );
    }

    public void clearMemberRepositoryIds()
    {
        List<String> empty = Collections.emptyList();

        setCollection( getRootNode(), MEMBER_REPOSITORIES, empty );
    }

    public void addMemberRepositoryId( String repositoryId )
    {
        addToCollection( getRootNode(), MEMBER_REPOSITORIES, repositoryId, true );
    }

    public void removeMemberRepositoryId( String repositoryId )
    {
        removeFromCollection( getRootNode(), MEMBER_REPOSITORIES, repositoryId );
    }

    @Override
    public ValidationResponse doValidateChanges( ApplicationConfiguration applicationConfiguration,
                                                 CoreConfiguration owner, Xpp3Dom config )
    {
        ValidationResponse response = super.doValidateChanges( applicationConfiguration, owner, config );

        // validate members existence

        List<CRepository> allReposes = applicationConfiguration.getConfigurationModel().getRepositories();

        List<String> allReposesIds = new ArrayList<String>( allReposes.size() );

        for ( CRepository repository : allReposes )
        {
            allReposesIds.add( repository.getId() );
        }

        if ( !allReposesIds.containsAll( getMemberRepositoryIds() ) )
        {
            ValidationMessage message =
                new ValidationMessage( MEMBER_REPOSITORIES, "Group repository points to nonexistent members!",
                    "The source nexus repository is not existing." );

            response.addValidationError( message );
        }

        // we cannot check for cycles here, since this class is not a component and to unravel groups, you would need
        // repo registry to do so. But the AbstractGroupRepository checks and does not allow itself to introduce cycles
        // anyway.

        return response;
    }
}
