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
package org.sonatype.nexus.plugin.discovery;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

public interface NexusInstanceDiscoverer
{

    NexusConnectionInfo discover( final Settings settings, final MavenProject project, final String defaultUser,
                                  final boolean fullyAutomatic )
        throws NexusDiscoveryException;

    NexusConnectionInfo fillAuth( final String nexusUrl, final Settings settings, final MavenProject project,
                                  final String defaultUser, final boolean fullyAutomatic )
        throws NexusDiscoveryException;
    
    SecDispatcher getSecDispatcher();
    
    void setSecDispatcher( SecDispatcher secDispatcher );

}
