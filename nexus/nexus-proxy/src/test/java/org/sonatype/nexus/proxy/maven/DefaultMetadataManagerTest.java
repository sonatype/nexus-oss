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
package org.sonatype.nexus.proxy.maven;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DefaultMetadataManagerTest
{

    @Test
    public void getTimeForMetadataTimestampMaven2Normal()
    {
        assertThat(DefaultMetadataManager.getTimeFromMetadataTimestampMaven2( "??" ), nullValue());
        assertThat(DefaultMetadataManager.getTimeFromMetadataTimestampMaven2( "20101224.124422" ), is(1293194662000L));
    }

    @Test(expected=NullPointerException.class)
    public void getTimeForMetadataTimestampMaven2Arg1Null()
    {
        assertThat(DefaultMetadataManager.getTimeFromMetadataTimestampMaven2( null ), is(1293207262000L));
    }

    @Test
    public void getTimeForMetadataTimestampMaven3UpdatedNormal()
    {
        assertThat(DefaultMetadataManager.getTimeFromMetadataTimestampMaven3Updated( "??" ), nullValue());
        assertThat(DefaultMetadataManager.getTimeFromMetadataTimestampMaven3Updated( "20101224124422" ), is(1293194662000L));
    }

    @Test
    public void getTimeForMetadataTimestampMaven3UpdatedUsingMaven2TimestampFailsParse()
    {
        assertThat(DefaultMetadataManager.getTimeFromMetadataTimestampMaven3Updated( "20101224.124422" ), nullValue());
    }

    @Test(expected=NullPointerException.class)
    public void getTimeForMetadataTimestampMaven3UpdatedArg1Null()
    {
        DefaultMetadataManager.getTimeFromMetadataTimestampMaven3Updated( null );
    }

    //================

    @Test(expected=NullPointerException.class)
    public void getBuildNumberForMetadataMaven3ValueArg1Null()
    {
        DefaultMetadataManager.getBuildNumberForMetadataMaven3Value( null );
    }

    @Test
    public void getBuildNumberForMetadataMaven3ValueNoDashIsZero()
    {
        assertThat(DefaultMetadataManager.getBuildNumberForMetadataMaven3Value( "foo" ), is(0));
    }

    @Test
    public void getBuildNumberForMetadataMaven3ValueDashAtEndBogusSanity()
    {
        assertThat(DefaultMetadataManager.getBuildNumberForMetadataMaven3Value( "1.2.1-20110719.092007-" ), nullValue());
    }

    @Test
    public void getBuildNumberForMetadataMaven3ValueNormal()
    {
        assertThat(DefaultMetadataManager.getBuildNumberForMetadataMaven3Value( "1.2.1-20110719.092007-17" ), is(17));
    }


}
