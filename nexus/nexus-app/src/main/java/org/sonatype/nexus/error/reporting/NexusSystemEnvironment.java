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
package org.sonatype.nexus.error.reporting;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.sisu.pr.SystemEnvironmentContributor;

@Component( role = SystemEnvironmentContributor.class, hint = "nexus")
public class NexusSystemEnvironment
    implements SystemEnvironmentContributor
{
    private static String LINE_SEPERATOR = System.getProperty( "line.separator" );

    @Requirement( role = ApplicationStatusSource.class )
    ApplicationStatusSource applicationStatus;
    
    @Override
    public String asDiagnosticsFormat()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "Nexus Version: " );
        sb.append( applicationStatus.getSystemStatus().getVersion() );
        sb.append( LINE_SEPERATOR );

        sb.append( "Nexus Edition: " );
        sb.append( applicationStatus.getSystemStatus().getEditionLong() );
        sb.append( LINE_SEPERATOR );
        
        return sb.toString();
    }

}
