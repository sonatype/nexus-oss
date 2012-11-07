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
package org.sonatype.security.realms.tools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.rest.users.AbstractSecurityRestTest;

public class AllPermissionsAreDefinedTest
    extends AbstractSecurityRestTest
{
    private static String SECURITY_FILE = "./target/security.xml";

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // FileUtils.copyFile( new File(""), new File( SECURITY_FILE ) );
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        context.put( "security-xml-file", SECURITY_FILE );
    }

    public List<PlexusResource> getPlexusResources()
        throws ComponentLookupException
    {
        return this.getContainer().lookupList( PlexusResource.class );
    }

    @SuppressWarnings( "unchecked" )
    public void testEnsurePermissions()
        throws Exception
    {
        Set<String> restPerms = new HashSet<String>();
        Set<String> staticPerms = new HashSet<String>();

        for ( PlexusResource plexusResource : this.getPlexusResources() )
        {
            PathProtectionDescriptor ppd = plexusResource.getResourceProtection();

            String expression = ppd.getFilterExpression();
            if ( expression.contains( "[" ) )
            {
                String permission =
                    ppd.getFilterExpression().substring( expression.indexOf( '[' ) + 1, expression.indexOf( ']' ) );
                restPerms.add( permission );
            }
        }

        // now we have a list of permissions, we need to make sure all of these are in the static security xml.

        StaticSecurityResource restResource =
            this.lookup( StaticSecurityResource.class, "SecurityRestStaticSecurityResource" );
        Configuration staticConfig = restResource.getConfiguration();

        List<CPrivilege> privs = staticConfig.getPrivileges();
        for ( CPrivilege privilege : privs )
        {
            staticPerms.add( this.getPermssionFromPrivilege( privilege ) );
        }

        // make sure everything in the restPerms is in the staticPerms
        for ( String perm : restPerms )
        {

            // TODO: need to find a way of dealing with test resources
            if ( !perm.startsWith( "sample" ) )
            {
                Assert.assertTrue( "Permission: " + perm + " is missing from SecurityRestStaticSecurityResource",
                                   staticPerms.contains( perm ) );
            }
        }

    }

    private String getPermssionFromPrivilege( CPrivilege privilege )
    {
        for ( Iterator<CProperty> iter = privilege.getProperties().iterator(); iter.hasNext(); )
        {
            CProperty prop = iter.next();
            if ( prop.getKey().equals( "permission" ) )
            {
                return prop.getValue();
            }
        }
        return null;
    }
}
