/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.mime;

import java.util.Collection;

import org.sonatype.nexus.mime.detectors.NexusExtensionMimeDetector;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NexusExtensionMimeDetector}.
 */
public class NexusExtensionMimeDetectorTest
    extends TestSupport
{

  public NexusExtensionMimeDetector underTest;

  @Mock
  private NexusMimeTypes mimeTypes;

  @Mock
  private NexusMimeTypes.NexusMimeType mimeType;

  @Before
  public void setup() {
    underTest = new NexusExtensionMimeDetector(mimeTypes);
  }

  @Test
  public void fallbackToDefaults() {
    Collection<String> fileNames = underTest.getMimeTypesFileName("foo.zip");
    assertThat(fileNames, hasItem("application/zip"));
  }

  @Test
  public void extendedMimeType() {
    when(mimeTypes.getMimeTypes("zip")).thenReturn(mimeType);
    when(mimeType.getMimetypes()).thenReturn(Lists.newArrayList("fake/mimetype"));

    Collection<String> fileNames = underTest.getMimeTypesFileName("foo.zip");
    assertThat(fileNames, hasItems("application/zip", "fake/mimetype"));
  }

  @Test
  public void overriddenMimeType() {
    when(mimeTypes.getMimeTypes("zip")).thenReturn(mimeType);
    when(mimeType.isOverride()).thenReturn(true);
    when(mimeType.getMimetypes()).thenReturn(Lists.newArrayList("fake/mimetype"));

    // Matchers.contains is an exact match!
    Collection<String> fileNames = underTest.getMimeTypesFileName("foo.zip");
    assertThat(fileNames, contains("fake/mimetype"));
  }
}
