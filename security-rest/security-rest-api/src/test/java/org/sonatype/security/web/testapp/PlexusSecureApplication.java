/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.web.testapp;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.restlet.Application;
import org.restlet.Router;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.web.ProtectedPathManager;

@Singleton
@Typed( value = Application.class )
@Named( value = "secureApplication" )
public class PlexusSecureApplication
    extends PlexusRestletApplicationBridge
{

    @Inject
    private ProtectedPathManager protectedPathManager;

    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        super.doCreateRoot( root, isStarted );

        this.protectedPathManager.addProtectedResource( "/**", "authcBasic,perms[sample:permToCatchAllUnprotecteds]" );
    }

    @Override
    protected void handlePlexusResourceSecurity( PlexusResource resource )
    {
        PathProtectionDescriptor descriptor = resource.getResourceProtection();

        if ( descriptor == null )
        {
            return;
        }

        this.protectedPathManager.addProtectedResource( descriptor.getPathPattern(), descriptor.getFilterExpression() );

    }
}
