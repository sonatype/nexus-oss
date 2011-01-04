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
package org.sonatype.nexus.configuration.model;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.validator.ApplicationValidationContext;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;

public class CRepositoryGroupingCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CRepositoryGroupingCoreConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    public CRepositoryGrouping getConfiguration( boolean forWrite )
    {
        return (CRepositoryGrouping) super.getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryGrouping extractConfiguration( Configuration configuration )
    {
        return configuration.getRepositoryGrouping();
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        CRepositoryGrouping settings = (CRepositoryGrouping)changedConfiguration;
        
        ValidationResponse response = new ApplicationValidationResponse();

        ApplicationValidationContext context = (ApplicationValidationContext) response.getContext();

        context.addExistingPathMappingIds();

        if ( settings.getPathMappings() != null )
        {
            for ( CPathMappingItem item : (List<CPathMappingItem>) settings.getPathMappings() )
            {
                response.append( validateGroupsSettingPathMappingItem( context, item ) );
            }
        }

        return response;
    }

    // ==

    private Random rand = new Random( System.currentTimeMillis() );

    public String generateId()
    {
        return Long.toHexString( System.nanoTime() + rand.nextInt( 2008 ) );
    }

    protected boolean isValidRegexp( String regexp )
    {
        if ( regexp == null )
        {
            return false;
        }

        try
        {
            Pattern.compile( regexp );

            return true;
        }
        catch ( PatternSyntaxException e )
        {
            return false;
        }
    }

    public ValidationResponse validateGroupsSettingPathMappingItem( ApplicationValidationContext ctx,
                                                                    CPathMappingItem item )
    {
        ValidationResponse response = new ApplicationValidationResponse();

        if ( ctx != null )
        {
            response.setContext( ctx );
        }

        ApplicationValidationContext context = (ApplicationValidationContext) response.getContext();

        if ( StringUtils.isEmpty( item.getId() )
            || "0".equals( item.getId() )
            || ( context.getExistingPathMappingIds() != null && context.getExistingPathMappingIds()
                .contains( item.getId() ) ) )
        {
            String newId = generateId();

            item.setId( newId );

            response.addValidationWarning( "Fixed wrong route ID from '" + item.getId() + "' to '" + newId + "'" );

            response.setModified( true );
        }

        if ( StringUtils.isEmpty( item.getGroupId() ) )
        {
            item.setGroupId( CPathMappingItem.ALL_GROUPS );

            response
                .addValidationWarning( "Fixed route without groupId set, set to ALL_GROUPS to keep backward comp, ID='"
                    + item.getId() + "'." );

            response.setModified( true );
        }

        if ( item.getRoutePatterns() == null || item.getRoutePatterns().isEmpty() )
        {
            response.addValidationError( "The Route with ID='" + item.getId()
                + "' must contain at least one Route Pattern." );
        }

        for ( String regexp : (List<String>) item.getRoutePatterns() )
        {
            if ( !isValidRegexp( regexp ) )
            {
                response.addValidationError( "The regexp in Route with ID='" + item.getId() + "' is not valid: "
                    + regexp );
            }
        }

        if ( context.getExistingPathMappingIds() != null )
        {
            context.getExistingPathMappingIds().add( item.getId() );
        }

        if ( !CPathMappingItem.INCLUSION_RULE_TYPE.equals( item.getRouteType() )
            && !CPathMappingItem.EXCLUSION_RULE_TYPE.equals( item.getRouteType() )
            && !CPathMappingItem.BLOCKING_RULE_TYPE.equals( item.getRouteType() ) )
        {
            response.addValidationError( "The groupMapping pattern with ID=" + item.getId()
                + " have invalid routeType='" + item.getRouteType() + "'. Valid route types are '"
                + CPathMappingItem.INCLUSION_RULE_TYPE + "', '" + CPathMappingItem.EXCLUSION_RULE_TYPE + "' and '"
                + CPathMappingItem.BLOCKING_RULE_TYPE + "'." );
        }

        if ( !CPathMappingItem.BLOCKING_RULE_TYPE.equals( item.getRouteType() ) )
        {
            // NOT TRUE ANYMORE:
            // if you delete a repo(ses) that were belonging to a route, we insist on
            // leaving the route "empty" (to save a users hardly concieved regexp) but with empty
            // repo list

            // here we must have a repo list
            // if ( item.getRepositories() == null || item.getRepositories().size() == 0 )
            // {
            // response.addValidationError( "The repository list in Route with ID='" + item.getId()
            // + "' is not valid: it cannot be empty!" );
            // }
        }

        if ( context.getExistingRepositoryIds() != null && context.getExistingRepositoryShadowIds() != null )
        {
            List<String> existingReposes = context.getExistingRepositoryIds();

            List<String> existingShadows = context.getExistingRepositoryShadowIds();

            for ( String repoId : (List<String>) item.getRepositories() )
            {
                if ( !existingReposes.contains( repoId ) && !existingShadows.contains( repoId ) )
                {
                    response.addValidationError( "The groupMapping pattern with ID=" + item.getId()
                        + " refers to a nonexistent repository with repoID = " + repoId );
                }
            }
        }

        return response;
    }
}
