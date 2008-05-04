/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.model.CAuthzSource;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.Configuration;

/**
 * The default configuration validator provider. It checks the model for semantical validity.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultConfigurationValidator
    extends AbstractLogEnabled
    implements ConfigurationValidator
{

    @SuppressWarnings( "unchecked" )
    public ValidationResponse validateModel( ValidationRequest request )
    {
        ValidationResponse response = new ValidationResponse();

        Configuration model = request.getConfiguration();

        // if security enabled, at least auth source must be defined
        if ( model.getSecurity() != null && model.getSecurity().isEnabled() )
        {
            if ( model.getSecurity().getAuthenticationSource() == null )
            {
                response.addValidationError( "Nexus security is enabled but no authentication source is defined!" );
            }
        }

        // collect existing realms, if any
        List<String> existingRealms = null;
        if ( model.getSecurity() != null && model.getSecurity().isEnabled() && model.getSecurity().getRealms() != null )
        {
            existingRealms = new ArrayList<String>( model.getSecurity().getRealms().size() );
            List<CAuthzSource> realms = model.getSecurity().getRealms();
            for ( CAuthzSource authz : realms )
            {
                existingRealms.add( authz.getId() );
            }
        }
        else
        {
            existingRealms = new ArrayList<String>( 1 );
        }

        // collect existing reposes and check their realms
        List<String> existingReposes = new ArrayList<String>( model.getRepositories().size() );
        List<CRepository> reposes = model.getRepositories();
        for ( CRepository repo : reposes )
        {
            response.append( validateRepository( repo ) );

            if ( existingReposes.contains( repo.getId() ) )
            {
                response.addValidationError( "Repository " + repo.getId() + " declared more than once!" );
            }
            if ( model.getSecurity() != null && model.getSecurity().isEnabled() && repo.getRealmId() != null )
            {
                if ( !existingRealms.contains( repo.getRealmId() ) )
                {
                    response.addValidationError( "The " + repo.getId()
                        + " repository points to a nonexistent security realm!" );
                }
            }

            existingReposes.add( repo.getId() );
        }

        // check that shadow reposes are showing to existing authz sources
        List<String> existingShadows = new ArrayList<String>();
        if ( model.getRepositoryShadows() != null )
        {
            List<CRepositoryShadow> shadows = model.getRepositoryShadows();

            for ( CRepositoryShadow shadow : shadows )
            {
                response.append( validateRepository( shadow ) );

                if ( existingShadows.contains( shadow.getId() ) )
                {
                    response.addValidationError( "Shadow repository " + shadow.getId() + " declared more than once!" );
                }

                if ( model.getSecurity() != null && model.getSecurity().isEnabled() && shadow.getRealmId() != null )
                {
                    if ( !existingReposes.contains( shadow.getShadowOf() ) )
                    {
                        response.addValidationError( "The shadow of repository " + shadow.getShadowOf() + " of type "
                            + shadow.getType() + " points to a nonexistent repository!" );
                    }
                    if ( !existingRealms.contains( shadow.getRealmId() ) )
                    {
                        response.addValidationError( "The shadow of repository " + shadow.getShadowOf() + " of type "
                            + shadow.getType() + " points to a nonexistent security realm!" );
                    }
                }
                existingShadows.add( shadow.getId() );
            }
        }

        // check that groups are pointing to existing reposes
        if ( model.getRepositoryGrouping() != null && model.getRepositoryGrouping().getRepositoryGroups() != null )
        {
            List<String> existingGroups = new ArrayList<String>( model
                .getRepositoryGrouping().getRepositoryGroups().size() );
            List<CRepositoryGroup> groups = model.getRepositoryGrouping().getRepositoryGroups();
            for ( CRepositoryGroup group : groups )
            {
                if ( StringUtils.isEmpty( group.getGroupId() ) )
                {
                    response.addValidationError( "RepositoryGroup ID's may not be empty!" );
                }
                if ( StringUtils.isEmpty( group.getName() ) )
                {
                    group.setName( group.getGroupId() );

                    response.addValidationWarning( "RepositoryGroup with ID='" + group.getGroupId()
                        + "' has no name, defaulted to it's ID." );

                    response.setModified( true );
                }

                if ( existingGroups.contains( group.getGroupId() ) )
                {
                    response.addValidationError( "The group with GroupID " + group.getGroupId()
                        + " is defined more than once!" );
                }
                List<String> members = group.getRepositories();
                for ( String repoId : members )
                {
                    if ( !existingReposes.contains( repoId ) && !existingShadows.contains( repoId ) )
                    {
                        response.addValidationError( "The group with GroupID " + group.getGroupId()
                            + " refers to a nonexistent repository with ID = " + repoId );
                    }
                }
            }
        }

        // check that groupmappings are pointing to existing reposes
        if ( model.getRepositoryGrouping() != null && model.getRepositoryGrouping().getPathMappings() != null )
        {
            List<String> itemIds = new ArrayList<String>( model.getRepositoryGrouping().getPathMappings().size() );

            long fixedCounter = System.currentTimeMillis();

            for ( CGroupsSettingPathMappingItem item : (List<CGroupsSettingPathMappingItem>) model
                .getRepositoryGrouping().getPathMappings() )
            {
                if ( StringUtils.isEmpty( item.getId() ) || "0".equals( item.getId() )
                    || itemIds.contains( item.getId() ) )
                {
                    String newId = Long.toHexString( fixedCounter++ );

                    response.addValidationWarning( "Fixed wrong/duplicate route ID from '" + item.getId() + "' to '"
                        + newId + "'" );

                    item.setId( newId );

                    response.setModified( true );
                }

                itemIds.add( item.getId() );

                if ( !item.getRouteType().equals( CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE )
                    && !item.getRouteType().equals( CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE )
                    && !item.getRouteType().equals( CGroupsSettingPathMappingItem.BLOCKING_RULE_TYPE ) )
                {
                    response.addValidationError( "The groupMapping pattern with ID=" + item.getId()
                        + " have invalid routeType='" + item.getRouteType() + "'. Valid route types are '"
                        + CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE + "', '"
                        + CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE + "' and '"
                        + CGroupsSettingPathMappingItem.BLOCKING_RULE_TYPE + "'." );
                }

                for ( String repoId : (List<String>) item.getRepositories() )
                {
                    if ( !existingReposes.contains( repoId ) && !existingShadows.contains( repoId ) )
                    {
                        response.addValidationError( "The groupMapping pattern with ID=" + item.getId()
                            + " refers to a nonexistent repository with repoID = " + repoId );
                    }
                }
            }
        }

        if ( model.getHttpProxy() != null )
        {
            if ( !CHttpProxySettings.PROXY_POLICY_PASS_THRU.equals( model.getHttpProxy().getProxyPolicy() )
                && !CHttpProxySettings.PROXY_POLICY_STRICT.equals( model.getHttpProxy().getProxyPolicy() ) )
            {
                response.addValidationError( "The HTTP Proxy policy settings is invalid: '"
                    + model.getHttpProxy().getProxyPolicy() + "'. Valid policies are '"
                    + CHttpProxySettings.PROXY_POLICY_STRICT + "' and '" + CHttpProxySettings.PROXY_POLICY_PASS_THRU
                    + "'." );
            }
        }
        else
        {
            model.setHttpProxy( new CHttpProxySettings() );

            response.addValidationWarning( "The HTTP Proxy section was missing from configuration, defaulted it." );

            response.setModified( true );
        }

        if ( model.getRestApi() != null )
        {
            // nothing
        }
        else
        {
            model.setRestApi( new CRestApiSettings() );

            response.addValidationWarning( "The REST API section was missing from configuration, defaulted it." );

            response.setModified( true );
        }

        // summary
        if ( response.getValidationErrors().size() > 0 || response.getValidationWarnings().size() > 0 )
        {
            getLogger().error( "* * * * * * * * * * * * * * * * * * * * * * * * * *" );

            getLogger().error( "Nexus configuration has validation errors/warnings" );

            getLogger().error( "* * * * * * * * * * * * * * * * * * * * * * * * * *" );

            if ( response.getValidationErrors().size() > 0 )
            {
                getLogger().error( "The ERRORS:" );

                for ( ValidationMessage msg : response.getValidationErrors() )
                {
                    getLogger().error( msg.toString() );
                }
            }

            if ( response.getValidationWarnings().size() > 0 )
            {
                getLogger().error( "The WARNINGS:" );

                for ( ValidationMessage msg : response.getValidationWarnings() )
                {
                    getLogger().error( msg.toString() );
                }
            }

            getLogger().error( "* * * * * * * * * * * * * * * * * * * * *" );
        }
        else
        {
            getLogger().info( "Nexus configuration validated succesfully." );
        }

        return response;
    }

    public ValidationResponse validateRepository( CRepository repo )
    {
        ValidationResponse response = new ValidationResponse();
        if ( StringUtils.isEmpty( repo.getId() ) )
        {
            response.addValidationError( "Repository ID's may not be empty!" );
        }

        if ( StringUtils.isEmpty( repo.getName() ) )
        {
            repo.setName( repo.getId() );

            response.addValidationWarning( "Repository with ID='" + repo.getId()
                + "' has no name, defaulted to it's ID." );

            response.setModified( true );
        }

        if ( !validateLocalStatus( repo.getLocalStatus() ) )
        {
            response.addValidationError( "LocalStatus of repository with ID='" + repo.getId() + "' is wrong:'"
                + repo.getType() + "'! (Allowed values are: '" + Configuration.LOCAL_STATUS_IN_SERVICE + "' and '"
                + Configuration.LOCAL_STATUS_OUT_OF_SERVICE + "')" );
        }

        if ( !validateRepositoryType( repo.getType() ) )
        {
            response.addValidationError( "Type of repository with ID='" + repo.getId()
                + "' is wrong! (Allowed values are: '" + CRepository.TYPE_MAVEN2 + "', '" + CRepository.TYPE_MAVEN1
                + "', '" + CRepository.TYPE_MAVEN_SITE + "', '" + CRepository.TYPE_ECLIPSE_UPDATE_SITE + "')" );
        }

        if ( !CRepository.PROXY_MODE_ALLOW.equals( repo.getProxyMode() )
            && !CRepository.PROXY_MODE_BLOCKED_MANUAL.equals( repo.getProxyMode() )
            && !CRepository.PROXY_MODE_BLOCKED_AUTO.equals( repo.getProxyMode() ) )
        {
            response.addValidationError( "ProxyMode of repository with ID='" + repo.getId()
                + "' is wrong! (Allowed values are: " + CRepository.PROXY_MODE_ALLOW + ", "
                + CRepository.PROXY_MODE_BLOCKED_MANUAL + " and " + CRepository.PROXY_MODE_BLOCKED_AUTO + ")" );
        }

        if ( repo.getRepositoryPolicy() == null
            || ( !CRepository.REPOSITORY_POLICY_RELEASE.equals( repo.getRepositoryPolicy() ) && !CRepository.REPOSITORY_POLICY_SNAPSHOT
                .equals( repo.getRepositoryPolicy() ) ) )
        {
            response.addValidationError( "Repository " + repo.getId() + " have wrong repository policy: \""
                + repo.getRepositoryPolicy() + "\". Repository policy may be \""
                + CRepository.REPOSITORY_POLICY_RELEASE + "\" or \"" + CRepository.REPOSITORY_POLICY_SNAPSHOT
                + "\" only." );
        }

        if ( repo.getChecksumPolicy() == null
            || ( !CRepository.CHECKSUM_POLICY_IGNORE.equals( repo.getChecksumPolicy() )
                && !CRepository.CHECKSUM_POLICY_WARN.equals( repo.getChecksumPolicy() )
                && !CRepository.CHECKSUM_POLICY_STRICT.equals( repo.getChecksumPolicy() ) && !CRepository.CHECKSUM_POLICY_STRICT_IF_EXISTS
                .equals( repo.getChecksumPolicy() ) ) )
        {
            response.addValidationError( "Repository " + repo.getId() + " have wrong checksum policy: \""
                + repo.getChecksumPolicy() + "\". Repository checksum policy may be \""
                + CRepository.CHECKSUM_POLICY_IGNORE + "\", \"" + CRepository.CHECKSUM_POLICY_WARN + "\", \""
                + CRepository.CHECKSUM_POLICY_STRICT_IF_EXISTS + "\" or \"" + CRepository.CHECKSUM_POLICY_STRICT
                + "\" only." );
        }

        if ( repo.getLocalStorage() != null && repo.getLocalStorage().getUrl() != null )
        {
            try
            {
                new URL( repo.getLocalStorage().getUrl() );
            }
            catch ( MalformedURLException e )
            {
                response.addValidationError( "Repository " + repo.getId() + " has malformed local storage URL!", e );
            }
        }
        if ( repo.getRemoteStorage() != null && repo.getRemoteStorage().getUrl() != null )
        {
            try
            {
                new URL( repo.getRemoteStorage().getUrl() );
            }
            catch ( MalformedURLException e )
            {
                response.addValidationError( "Repository " + repo.getId() + " has malformed remote storage URL!", e );
            }
        }
        return response;
    }

    public ValidationResponse validateRepository( CRepositoryShadow shadow )
    {
        ValidationResponse response = new ValidationResponse();

        if ( StringUtils.isEmpty( shadow.getId() ) )
        {
            response.addValidationError( "Repository shadow ID's may not be empty!" );
        }

        if ( StringUtils.isEmpty( shadow.getName() ) )
        {
            shadow.setName( shadow.getId() );

            response.addValidationWarning( "Repository shadow with ID='" + shadow.getId()
                + "' has no name, defaulted to it's ID." );

            response.setModified( true );
        }

        if ( !validateLocalStatus( shadow.getLocalStatus() ) )
        {
            response.addValidationError( "LocalStatus of repository with ID='" + shadow.getId()
                + "' is wrong! (Allowed values are: " + Configuration.LOCAL_STATUS_IN_SERVICE + " and "
                + Configuration.LOCAL_STATUS_OUT_OF_SERVICE + ")" );
        }

        if ( !validateShadowRepositoryType( shadow.getType() ) )
        {
            response.addValidationError( "Type of repository shadow with ID='" + shadow.getId() + "' is wrong: '"
                + shadow.getType() + "'! (Allowed values are: '" + CRepositoryShadow.TYPE_MAVEN2 + "', '"
                + CRepositoryShadow.TYPE_MAVEN1 + "', '" + CRepositoryShadow.TYPE_MAVEN2_CONSTRAINED + "')" );
        }

        return response;
    }

    protected boolean validateLocalStatus( String ls )
    {
        return Configuration.LOCAL_STATUS_IN_SERVICE.equals( ls )
            || Configuration.LOCAL_STATUS_OUT_OF_SERVICE.equals( ls );
    }

    protected boolean validateRepositoryType( String type )
    {
        return CRepository.TYPE_MAVEN2.equals( type ) || CRepository.TYPE_MAVEN1.equals( type )
            || CRepository.TYPE_MAVEN_SITE.equals( type ) || CRepository.TYPE_ECLIPSE_UPDATE_SITE.equals( type );
    }

    protected boolean validateShadowRepositoryType( String type )
    {
        return CRepositoryShadow.TYPE_MAVEN1.equals( type ) || CRepositoryShadow.TYPE_MAVEN2.equals( type )
            || CRepositoryShadow.TYPE_MAVEN2_CONSTRAINED.equals( type );
    }

}
