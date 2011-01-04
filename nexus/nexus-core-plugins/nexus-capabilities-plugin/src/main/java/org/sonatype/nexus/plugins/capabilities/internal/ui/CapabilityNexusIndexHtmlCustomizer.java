/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.ui;

import java.util.Map;

import javax.inject.Singleton;

import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Singleton
public class CapabilityNexusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
    implements NexusIndexHtmlCustomizer
{

    @Override
    public String getPostHeadContribution( final Map<String, Object> ctx )
    {
        final String version =
            getVersionFromJarFile( "/META-INF/maven/org.sonatype.nexus.plugins/nexus-capabilities-plugin/pom.properties" );

        if ( System.getProperty( "useOriginalJS" ) == null )
        {
            return "<script src=\"static/js/org.sonatype.nexus.plugins.capabilities.imp-all.js"
                + ( version == null ? "" : "?" + version )
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
        }
        else
        {
            return "<script src=\"js/repoServer/repoServer.CapabilitiesNavigation.js"
                + ( version == null ? "" : "?" + version )
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>"
                + "<script src=\"js/repoServer/repoServer.CapabilitiesPanel.js"
                + ( version == null ? "" : "?" + version )
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
        }
    }

}
