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
package org.sonatype.nexus.ruby;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MetadataSnapshotBuilderTest
    extends TestSupport
{
  private MetadataSnapshotBuilder builder;

  @Before
  public void setUp() throws Exception {
    builder = new MetadataSnapshotBuilder("jbundler", "9.2.1", 1397660433050l);
  }

  @Test
  public void testXml() throws Exception {
    String xml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("metadata-snapshot.xml"));
    //        System.err.println( builder.toString() );
    //        System.err.println( xml );
    assertThat(builder.toString(), equalTo(xml));
  }
}
