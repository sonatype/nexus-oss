/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
            getVersionFromJarFile( "/META-INF/maven/org.sonatype.nexus.plugins.ldap/nexus-ldap-realm-plugin/pom.properties" );

         return "<script src=\"static/js/nexus-ldap-realm-plugin-all.js"
         + ( version == null ? "" : "?" + version )
         + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";

    }
}
