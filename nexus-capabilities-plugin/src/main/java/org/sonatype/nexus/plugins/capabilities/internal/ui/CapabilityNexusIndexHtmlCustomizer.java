/*
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

    private String getVersion()
    {
        return getVersionFromJarFile( "/META-INF/maven/org.sonatype.nexus.plugins/nexus-capabilities-plugin/pom.properties" );
    }
    
    private String getSuffixParameters()
    {
        String version = getVersion();
        if ( version == null ) {
            return "";
        }
        else if ( version.endsWith( "SNAPSHOT" ) ) { //NON-NLS
            return String.format( "?v=%s&t=%s", version, System.currentTimeMillis() ); //NON-NLS
        }
        else {
            return "?v=" + version; //NON-NLS
        }
    }

    @Override
    public String getPostHeadContribution( final Map<String, Object> ctx )
    {
        String suffixParameters = getSuffixParameters();

        if ( System.getProperty( "useOriginalJS" ) == null )
        {
            return "<script src=\"static/js/nexus-capabilities-plugin-all.js" + suffixParameters
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
        }
        else
        {
            return "<script src=\"js/repoServer/repoServer.CapabilitiesNavigation.js" + suffixParameters
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>"
                + "<script src=\"js/repoServer/repoServer.CapabilitiesPanel.js" + suffixParameters
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
        }
    }

}
