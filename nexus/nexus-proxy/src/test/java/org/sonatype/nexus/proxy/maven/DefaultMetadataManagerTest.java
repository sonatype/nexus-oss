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
