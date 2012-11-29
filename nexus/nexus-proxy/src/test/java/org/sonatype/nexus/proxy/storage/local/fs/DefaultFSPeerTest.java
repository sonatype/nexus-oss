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
package org.sonatype.nexus.proxy.storage.local.fs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import java.io.File;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;

public class DefaultFSPeerTest
{

    @Test
    public void testGetHiddenTarget()
        throws Exception
    {
        File repoBase = new File( "target/a" );
        File base = new File( repoBase, "b/c/d" );
        base.getParentFile().mkdirs();
        base.createNewFile();

        final StorageFileItem file = Mockito.mock( StorageFileItem.class );
        Mockito.when( file.getPath() ).thenReturn( "/b/c/d" );
        File created = new DefaultFSPeer().getHiddenTarget( null, repoBase, base, file );
        assertThat( created, notNullValue() );
        assertThat( created, exists() );
    }

}
