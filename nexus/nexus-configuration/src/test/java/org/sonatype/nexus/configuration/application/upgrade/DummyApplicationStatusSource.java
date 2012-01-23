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
package org.sonatype.nexus.configuration.application.upgrade;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.SystemStatus;

@Component(role=ApplicationStatusSource.class)
public class DummyApplicationStatusSource
    implements ApplicationStatusSource
{

    @Override
    public SystemStatus getSystemStatus()
    {
        SystemStatus status = new SystemStatus();
        status.setVersion( "1.0" );
        return status;
    }

    @Override
    public boolean setState( SystemState state )
    {
        return false;
    }

}
