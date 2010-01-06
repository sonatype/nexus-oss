/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "LdapNexusIndexHtmlCustomizer" )
public class LdapNexusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
{
    @Override
    public String getPostHeadContribution( Map<String, Object> ctx )
    {
        String version =
            getVersionFromJarFile( "/META-INF/maven/com.sonatype.nexus.plugin/nexus-ldap-realm-plugin/pom.properties" );

         return "<script src=\"js/repoServer/nexus-ldap-realm-plugin-all.js"
         + ( version == null ? "" : "?" + version )
         + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";

//        return "<script src=\"js/repoServer/repoServer.LdapConfigPanel.js" + ( version == null ? "" : "?" + version )
//            + "\" type=\"text/javascript\" charset=\"utf-8\"></script>" +
//
//            "<script src=\"js/repoServer/repoServer.LdapUserEditor.js" + ( version == null ? "" : "?" + version )
//            + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";

    }
}
