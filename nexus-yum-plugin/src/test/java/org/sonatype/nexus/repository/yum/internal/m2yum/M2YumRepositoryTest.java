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
package org.sonatype.nexus.repository.yum.internal.m2yum;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith( Parameterized.class )
public class M2YumRepositoryTest
{

    private M2YumRepository repository;

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList( new Object[][]{ { "/repodata/repomd.xml", true }, { "/repodata/primary.xml.gz", true },
            { "/maven-metadata.xml", true }, { "/de/testproject/maven-metadata.xml", true },
            { "/de/testproject/test.rpm", false }, { "/de/testproject/repodata/repomd.xml", false } } );
    }

    private final String path;

    private final boolean expectedMetaData;

    public M2YumRepositoryTest( final String path, final Boolean isExpectedMetaData )
    {
        this.path = path;
        this.expectedMetaData = isExpectedMetaData;
    }

    @Before
    public void init()
    {
        repository = new M2YumRepository();
    }

    @Test
    public void should_mark_files_as_metadata()
    {
        Assert.assertEquals( "path: " + path + " has not the expected value: " + expectedMetaData, expectedMetaData,
                             repository.isMavenMetadataPath( path ) );

    }

}
