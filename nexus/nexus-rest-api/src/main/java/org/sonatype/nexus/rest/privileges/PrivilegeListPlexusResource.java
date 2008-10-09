package org.sonatype.nexus.rest.privileges;

import java.util.Collection;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * Handles the GET and POST request for the Nexus privileges.
 * 
 * @author tstevens
 * @plexus.component role-hint="PrivilegeListPlexusResource"
 */
public class PrivilegeListPlexusResource
    extends AbstractPrivilegePlexusResource
{

    public PrivilegeListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new PrivilegeResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/privileges";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:privileges]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PrivilegeListResourceResponse result = new PrivilegeListResourceResponse();

        Collection<CPrivilege> privs = getNexusSecurity( request ).listPrivileges();

        for ( CPrivilege priv : privs )
        {
            PrivilegeBaseStatusResource res = nexusToRestModel( priv, request );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        PrivilegeResourceRequest resourceRequest = (PrivilegeResourceRequest) payload;
        PrivilegeListResourceResponse result = null;

        if ( resourceRequest != null )
        {
            result = new PrivilegeListResourceResponse();

            PrivilegeBaseResource resource = resourceRequest.getData();

            // currently we are allowing only of repotarget privs, so enforcing checkfor it
            if ( !TYPE_REPO_TARGET.equals( resource.getType() ) )
            {
                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration error.",
                    getNexusErrorResponse( "type", "Not allowed privilege type!" ) );
            }

            List<String> methods = resource.getMethod();

            if ( methods == null || methods.size() == 0 )
            {
                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration error.",
                    getNexusErrorResponse( "method", "No method(s) supplied, must select at least one method." ) );
            }
            else
            {
                try
                {
                    // Add a new privilege for each method
                    for ( String method : methods )
                    {
                        // Currently can only add new target types, application types are hardcoded
                        if ( PrivilegeTargetResource.class.isAssignableFrom( resource.getClass() ) )
                        {
                            PrivilegeTargetResource res = (PrivilegeTargetResource) resource;

                            CPrivilege priv = new CPrivilege();

                            priv.setName( res.getName() != null ? res.getName() + " - (" + method + ")" : null );
                            priv.setDescription( res.getDescription() );
                            priv.setType( "target" );

                            CProperty prop = new CProperty();
                            prop.setKey( "method" );
                            prop.setValue( method );

                            priv.addProperty( prop );

                            prop = new CProperty();
                            prop.setKey( "repositoryTargetId" );
                            prop.setValue( res.getRepositoryTargetId() );

                            priv.addProperty( prop );

                            prop = new CProperty();
                            prop.setKey( "repositoryId" );
                            prop.setValue( res.getRepositoryId() );

                            priv.addProperty( prop );

                            prop = new CProperty();
                            prop.setKey( "repositoryGroupId" );
                            prop.setValue( res.getRepositoryGroupId() );

                            priv.addProperty( prop );

                            getNexusSecurity( request ).createPrivilege( priv );

                            result.addData( nexusToRestModel( priv, request ) );
                        }
                        else
                        {
                            throw new PlexusResourceException(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Configuration error.",
                                getNexusErrorResponse( "type", "An invalid type was entered." ) );
                        }
                    }

                }
                catch ( InvalidConfigurationException e )
                {
                    // build and throw exctption
                    handleInvalidConfigurationException( e );
                }
            }
        }
        return result;
    }

}
