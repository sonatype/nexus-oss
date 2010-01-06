/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.security.ldap.realms.persist.InvalidConfigurationException;

@Component(role=PlexusResource.class, hint="LdapConnectionInfoPlexusResource")
public class LdapConnectionInfoPlexusResource
    extends AbstractLdapRealmPlexusResource
{

    public LdapConnectionInfoPlexusResource()
    {
        this.setModifiable( true );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return new LdapConnectionInfoResponse();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:ldapconninfo]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/ldap/conn_info";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#get(org.restlet.Context, org.restlet.data.Request,
     *      org.restlet.data.Response, org.restlet.resource.Variant)
     */
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        // this could be null, if so do we want to return defaults? I would guess no...
        CConnectionInfo connInfo = this.getConfiguration().readConnectionInfo();
        
        LdapConnectionInfoResponse result = new LdapConnectionInfoResponse();
        
        result.setData( this.ldapToRestModel( connInfo ) );

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#put(org.restlet.Context, org.restlet.data.Request,
     *      org.restlet.data.Response, java.lang.Object)
     */
    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        LdapConnectionInfoResponse connResponse = (LdapConnectionInfoResponse) payload;
       
        if( connResponse.getData() == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "LDAP Connection Info was missing from Request.");
        }
        
        CConnectionInfo connInfo = this.restToLdapModel( connResponse.getData() );
        try
        {
            // validation happens in this method
            this.getConfiguration().updateConnectionInfo( connInfo );
            // if it didn't throw an InvalidConfigurationException, we are good to go.
            this.getConfiguration().save();
        }
        catch ( InvalidConfigurationException e )
        {
            // this will build and thrown an exception.
            this.handleConfigurationException( e );
        }
                
        // just do a get.
        return this.get( context, request, response, null );
    }

}
