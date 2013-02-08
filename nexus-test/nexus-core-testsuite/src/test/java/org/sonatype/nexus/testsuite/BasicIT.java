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
package org.sonatype.nexus.testsuite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.sonatype.nexus.client.core.NexusStatus;

/**
 * Most basic IT just checking is bundle alive at all.
 * 
 * @since 2.4
 */
public class BasicIT
    extends NexusCoreITSupport
{
    public BasicIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void verifyNexusStatusReportsHealthyAndCorrect()
    {
        final NexusStatus nexusStatus = client().getStatus();

        assertThat( nexusStatus, is( notNullValue() ) );
        assertThat( nexusStatus.isFirstStart(), is( true ) );
        assertThat( nexusStatus.isInstanceUpgraded(), is( false ) );
        assertThat( nexusStatus.getVersion(), is( "1.0" ) );
    }
}
