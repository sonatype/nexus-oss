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
package org.sonatype.nexus.client.testsuite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status;
import org.sonatype.nexus.client.core.subsystem.whitelist.Status.Outcome;
import org.sonatype.nexus.client.core.subsystem.whitelist.Whitelist;

/**
 * Will not work until proxy404 merged into master, AND at least one CI build/deploys of that master, as it seems Sisu
 * Maven Bridge will download the "latest" from remote, not use the build from branch.
 * 
 * @author cstamas
 */
@Ignore
public class WhitelistIT
    extends NexusClientITSupport
{

    public WhitelistIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void getStatus()
    {
        final Status status = whitelist().getWhitelistStatus( "releases" );
        assertThat( status, is( not( nullValue() ) ) );
        assertThat( status.getPublishedStatus(), equalTo( Outcome.SUCCEEDED ) );
    }

    private Whitelist whitelist()
    {
        return client().getSubsystem( Whitelist.class );
    }
}
