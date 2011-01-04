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
package com.sonatype.nexus.oss;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.AbstractApplicationStatusSource;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;

@Component( role = ApplicationStatusSource.class )
public class OSSApplicationStatusSource
    extends AbstractApplicationStatusSource
    implements ApplicationStatusSource
{
    private static final String FORMATTED_APP_NAME_BASE = "Sonatype Nexus&trade;";

    public OSSApplicationStatusSource()
    {
        super();

        getSystemStatusInternal().setVersion( discoverApplicationVersion() );

        getSystemStatusInternal().setApiVersion( getSystemStatusInternal().getVersion() );

        getSystemStatusInternal().setFormattedAppName(
            FORMATTED_APP_NAME_BASE + " " + getSystemStatusInternal().getEditionLong() + " Edition, Version: "
                + getSystemStatusInternal().getVersion() );
    }

    @Override
    protected void renewSystemStatus( SystemStatus systemStatus )
    {
        // nothing changes in OSS yet
    }

    @Override
    protected String discoverApplicationVersion()
    {
        return readVersion( "/META-INF/maven/org.sonatype.nexus/nexus-oss-edition/pom.properties" );
    }
}
