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
package org.sonatype.nexus.plugins.lvo.strategy;

import org.sonatype.nexus.SystemStatus;

public abstract class AbstractRemoteDiscoveryStrategy
    extends AbstractDiscoveryStrategy
{
    /**
     * Format's the user agent string for remote discoveries, if needed. TODO: this method is a copy+paste of the one in
     * AbstractRemoteRepositoryStorage, fix this
     */
    protected String formatUserAgent()
    {
        SystemStatus status = getNexus().getSystemStatus();

        StringBuffer userAgentPlatformInfo = new StringBuffer( "Nexus/" )
            .append( status.getVersion() ).append( " (" ).append( status.getEditionShort() ).append( "; " ).append(
                System.getProperty( "os.name" ) ).append( "; " ).append( System.getProperty( "os.version" ) ).append(
                "; " ).append( System.getProperty( "os.arch" ) ).append( "; " ).append(
                System.getProperty( "java.version" ) ).append( ") " ).append( "LVOPlugin/1.0" );

        return userAgentPlatformInfo.toString();
    }
}
