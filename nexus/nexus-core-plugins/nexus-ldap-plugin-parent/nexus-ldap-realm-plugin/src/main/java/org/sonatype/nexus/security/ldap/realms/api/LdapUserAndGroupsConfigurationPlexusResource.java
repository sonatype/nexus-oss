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
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;
import org.sonatype.security.ldap.realms.persist.InvalidConfigurationException;

@Component(role=PlexusResource.class, hint="LdapUserAndGroupsConfigurationPlexusResource")
public class LdapUserAndGroupsConfigurationPlexusResource
    extends AbstractLdapRealmPlexusResource
{

    public LdapUserAndGroupsConfigurationPlexusResource()
    {
        this.setModifiable( true );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return new LdapUserAndGroupConfigurationResponse();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:ldapusergroupconf]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/ldap/user_group_conf";
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
        CUserAndGroupAuthConfiguration userGroupConf = this.getConfiguration().readUserAndGroupConfiguration();
        
        LdapUserAndGroupConfigurationResponse result = new LdapUserAndGroupConfigurationResponse();
        
        // convert and set the info in the result
        if( userGroupConf != null )
        {
            result.setData( this.ldapToRestModel( userGroupConf ) );
        }
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
        LdapUserAndGroupConfigurationResponse confResponse = (LdapUserAndGroupConfigurationResponse) payload;
       
        if( confResponse.getData() == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "LDAP Connection Info was missing from Request.");
        }
        
        CUserAndGroupAuthConfiguration userGroupConf = this.restToLdapModel( confResponse.getData() );
        try
        {
            // validation happens in this method
            this.getConfiguration().updateUserAndGroupConfiguration(  userGroupConf );
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
