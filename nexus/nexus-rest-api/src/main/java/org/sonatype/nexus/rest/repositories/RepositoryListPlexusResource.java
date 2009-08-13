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
package org.sonatype.nexus.rest.repositories;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.util.EnumUtil;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * A resource list for Repository list.
 *
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "RepositoryListPlexusResource" )
public class RepositoryListPlexusResource
    extends AbstractRepositoryPlexusResource
{

    public RepositoryListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/repositories";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repositories]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return listRepositories( request, false, false );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryResourceResponse repoRequest = (RepositoryResourceResponse) payload;
        String repoId = null;

        if ( repoRequest != null )
        {
            RepositoryBaseResource resource = repoRequest.getData();
            repoId = resource.getId();

            try
            {
                CRepository normal = getRepositoryAppModel( resource, null );

                getNexus().createRepository( normal );
            }
            catch ( ConfigurationException e )
            {
                handleConfigurationException( e );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
            }
        }

        return getRepositoryResourceResponse( repoId );
    }

    // --

    /**
     * Converting REST DTO + possible App model to App model. If app model is given, "update" happens, otherwise if
     * target is null, "create".
     *
     * @param model
     * @param target
     * @return app model, merged or created
     * @throws ResourceException
     */
    public CRepository getRepositoryAppModel( RepositoryBaseResource resource, CRepository target )
        throws ResourceException
    {
        CRepository appModel = new DefaultCRepository();

        Xpp3Dom ex = null;

        appModel.setLocalStatus( LocalStatus.IN_SERVICE.name() );
        if ( target != null )
        {
            appModel.setLocalStatus( target.getLocalStatus() );

            ex = (Xpp3Dom) target.getExternalConfiguration();
        }
        else
        {
            ex = new Xpp3Dom( "externalConfiguration" );
        }

        appModel.setId( resource.getId() );

        appModel.setName( resource.getName() );

        appModel.setExposed( resource.isExposed() );

        if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
        {
            appModel.setProviderRole( ShadowRepository.class.getName() );

            appModel.setExternalConfiguration( ex );

            //indexer is unaware of the m2 layout conversion
            appModel.setIndexable( false );

            RepositoryShadowResource repoResource = (RepositoryShadowResource) resource;

            M2LayoutedM1ShadowRepositoryConfiguration exConf = new M2LayoutedM1ShadowRepositoryConfiguration( ex );

            exConf.setMasterRepositoryId( repoResource.getShadowOf() );

            exConf.setSynchronizeAtStartup( repoResource.isSyncAtStartup() );

        }
        else if ( REPO_TYPE_GROUP.equals( resource.getRepoType() ) )
        {
            appModel.setProviderRole( GroupRepository.class.getName() );
        }
        else
        {

            appModel.setProviderRole( Repository.class.getName() );

            RepositoryResource repoResource = (RepositoryResource) resource;

            // we can use the default if the value is empty
            if( StringUtils.isNotEmpty( repoResource.getWritePolicy() ))
            {
                appModel.setWritePolicy( repoResource.getWritePolicy() );
            }

            appModel.setBrowseable( repoResource.isBrowseable() );

            appModel.setIndexable( repoResource.isIndexable() );

            appModel.setNotFoundCacheTTL( repoResource.getNotFoundCacheTTL() );

            appModel.setExternalConfiguration( ex );

            M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );

            exConf.setRepositoryPolicy( EnumUtil.valueOf( repoResource.getRepoPolicy(), RepositoryPolicy.class ) );

            if ( repoResource.getOverrideLocalStorageUrl() != null )
            {
                appModel.setLocalStorage( new CLocalStorage() );

                appModel.getLocalStorage().setUrl( repoResource.getOverrideLocalStorageUrl() );

                appModel.getLocalStorage().setProvider( "file" );
            }
            else
            {
                appModel.setLocalStorage( null );
            }

            RepositoryResourceRemoteStorage remoteStorage = repoResource.getRemoteStorage();
            if ( remoteStorage != null )
            {
                appModel.setRemoteStorage( new CRemoteStorage() );

                appModel.getRemoteStorage().setUrl( remoteStorage.getRemoteStorageUrl() );

                appModel.getRemoteStorage().setProvider( "apacheHttpClient3x" );
            }
        }

        appModel.setProviderHint( resource.getProvider() );

        if ( RepositoryProxyResource.class.isAssignableFrom( resource.getClass() ) )
        {
            appModel = getRepositoryProxyAppModel( (RepositoryProxyResource) resource, appModel );
        }

        return appModel;
    }

    /**
     * Converting REST DTO + possible App model to App model. If app model is given, "update" happens, otherwise if
     * target is null, "create".
     *
     * @param model
     * @param target
     * @return app model, merged or created
     * @throws PlexusResourceException
     */
    public CRepository getRepositoryProxyAppModel( RepositoryProxyResource model, CRepository target )
        throws PlexusResourceException
    {
        M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( (Xpp3Dom) target.getExternalConfiguration() );

        exConf.setChecksumPolicy( EnumUtil.valueOf( model.getChecksumPolicy(), ChecksumPolicy.class ) );

        exConf.setDownloadRemoteIndex( model.isDownloadRemoteIndexes() );

        exConf.setArtifactMaxAge( model.getArtifactMaxAge() );

        exConf.setMetadataMaxAge( model.getMetadataMaxAge() );

        if ( model.getRemoteStorage() != null )
        {
            if ( target.getRemoteStorage() == null )
            {
                target.setRemoteStorage( new CRemoteStorage() );
            }

            // url
            target.getRemoteStorage().setUrl( model.getRemoteStorage().getRemoteStorageUrl() );

            // remote auth
            target.getRemoteStorage().setAuthentication( this.convertAuthentication(  model.getRemoteStorage().getAuthentication(), null ) );

            // connection settings
            target.getRemoteStorage().setConnectionSettings( this.convertRemoteConnectionSettings( model.getRemoteStorage().getConnectionSettings() ));

            // http proxy settings
            target.getRemoteStorage().setHttpProxySettings( this.convertHttpProxySettings( model.getRemoteStorage().getHttpProxySettings(), null ) );
        }

        return target;
    }

}
