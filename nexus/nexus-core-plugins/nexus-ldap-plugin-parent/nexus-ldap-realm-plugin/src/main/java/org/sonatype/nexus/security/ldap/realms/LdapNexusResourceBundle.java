/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

@Component( role = NexusResourceBundle.class, hint = "LdapResourceBundle" )
public class LdapNexusResourceBundle
    extends AbstractNexusResourceBundle
{
    @Override
    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> result = new ArrayList<StaticResource>();

        result.add( new DefaultStaticResource(
            getClass().getResource( "/static/js/nexus-ldap-realm-plugin-all.js" ),
            "/js/repoServer/nexus-ldap-realm-plugin-all.js",
            "application/x-javascript") );
        
//        result.add( new DefaultStaticResource(
//            getClass().getResource( "/static/js/repoServer.LdapConfigPanel.js" ),
//            "/js/repoServer/repoServer.LdapConfigPanel.js",
//            "application/x-javascript") );
//        
//        result.add( new DefaultStaticResource(
//            getClass().getResource( "/static/js/repoServer.LdapUserEditor.js" ),
//            "/js/repoServer/repoServer.LdapUserEditor.js",
//            "application/x-javascript") );
        
        
        return result;
    }
}
