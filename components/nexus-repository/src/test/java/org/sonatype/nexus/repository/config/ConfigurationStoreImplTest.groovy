/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.config

import com.google.inject.util.Providers
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.sonatype.nexus.internal.orient.HexRecordIdObfuscator
import org.sonatype.nexus.internal.orient.MemoryDatabaseManager
import org.sonatype.nexus.internal.orient.MinimalDatabaseServer
import org.sonatype.nexus.orient.DatabaseInstance
import org.sonatype.sisu.litmus.testsupport.TestSupport

import static org.junit.Assert.fail

/**
 * Tests for {@link ConfigurationStoreImpl}.
 */
class ConfigurationStoreImplTest
    extends TestSupport
{
  private MinimalDatabaseServer databaseServer

  private MemoryDatabaseManager databaseManager

  private DatabaseInstance databaseInstance

  private ConfigurationStoreImpl underTest

  @Before
  void setUp() {
    databaseServer = new MinimalDatabaseServer()
    databaseServer.start()

    databaseManager = new MemoryDatabaseManager()
    databaseManager.start()

    databaseInstance = databaseManager.instance('test')

    underTest = new ConfigurationStoreImpl(
        Providers.of(databaseInstance),
        new HexRecordIdObfuscator(),
        new ConfigurationEntityAdapter()
    )
  }

  @After
  void tearDown() {
    databaseInstance = null

    if (databaseManager != null) {
      databaseManager.stop()
      databaseManager = null
    }

    if (databaseServer != null) {
      databaseServer.stop()
      databaseServer = null
    }
  }

  @Test
  void 'create entity'() {
    underTest.start()

    try {
      def entity = new Configuration(
          repositoryName: 'foo',
          recipeName: 'bar',
          attributes: [
              'baz': [
                  'a': false
              ]
          ]
      )
      log entity
      assert entity.entityMetadata == null

      entity = underTest.create(entity)
      log entity
      assert entity.entityMetadata != null
      log entity.entityMetadata
    }
    finally {
      underTest.stop()
    }
  }

  @Test
  void 'create entity with duplicate name disallowed'() {
    underTest.start()

    try {
      def create = { name ->
        underTest.create(new Configuration(
            repositoryName: name,
            recipeName: 'test'
        ))
      }

      create 'foo'

      try {
        create 'foo'
        fail()
      }
      catch (ORecordDuplicatedException e) {
        // expected
        log e.toString()
      }
    }
    finally {
      underTest.stop()
    }
  }

  @Test
  void 'browse entities'() {
    underTest.start()

    try {
      def create = { name ->
        underTest.create(new Configuration(
            repositoryName: name,
            recipeName: 'test'
        ))
      }

      create 'foo'
      create 'bar'

      underTest.browse().each { entity ->
        log entity
      }
    }
    finally {
      underTest.stop()
    }
  }
}
