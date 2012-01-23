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
package org.sonatype.nexus.plugin.discovery.fixture;

import org.sonatype.nexus.plugin.discovery.NexusTestClientManager;

public final class ClientManagerFixture
    implements NexusTestClientManager
{

    public String testUrl;

    public String testUser;

    public String testPassword;

    public boolean testConnection( final String url, final String user, final String password )
    {
        return ( testUrl == null || url.equals( testUrl ) ) && ( testUser == null || user.equals( testUser ) )
            && ( testPassword == null || password.equals( testPassword ) );
    }

}
