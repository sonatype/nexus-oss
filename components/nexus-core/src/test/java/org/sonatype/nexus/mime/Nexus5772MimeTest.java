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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.mime.detectors.NexusMagicMimeMimeDetector;
import org.sonatype.nexus.mime.detectors.NexusOpendesktopMimeDetector;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.MimeUtil2;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class Nexus5772MimeTest
    extends TestSupport
{
  public static final String CLASSIC_MAGIC_FILE_PATH = "src/test/resources/mime/mime.magic";

  public static final String OPENDESKTOP_MAGIC_FILE_PATH = "src/test/resources/mime/mime.cache";

  @After
  public void cleanUp()
      throws Exception
  {
    // properties
    System.clearProperty(DefaultMimeSupport.MIME_MAGIC_OPENDESKTOP_KEY);
    System.clearProperty(DefaultMimeSupport.MIME_MAGIC_FILE_KEY);

    // unregister
    MimeUtil.unregisterMimeDetector(NexusMagicMimeMimeDetector.class.getName());
    MimeUtil.unregisterMimeDetector(NexusOpendesktopMimeDetector.class.getName());

    // stupid static map in MimeUtil2 that would make 1st positive test load MIME types and all subsequent tests
    // pass
    final Field mimeTypesFld = MimeUtil2.class.getDeclaredField("mimeTypes");
    mimeTypesFld.setAccessible(true);
    Map<?, ?> mimeTypes = (Map) mimeTypesFld.get(null);
    mimeTypes.clear();
  }

  protected void assertComplete(final DefaultMimeSupport mimeSupport)
      throws IOException
  {
    assertComplete(mimeSupport, new FileContentLocator(util.resolveFile("src/test/resources/mime/file.gif"),
        "application/octet-stream"), "image/gif");
    assertComplete(mimeSupport, new FileContentLocator(util.resolveFile("src/test/resources/mime/file.zip"),
        "application/octet-stream"), "application/zip");
    assertComplete(mimeSupport, new FileContentLocator(util.resolveFile("src/test/resources/mime/file.jar"),
        "application/octet-stream"), "application/zip");
  }

  protected void assertComplete(final DefaultMimeSupport mimeSupport, final ContentLocator contentLocator,
                                String expectedMimeType)
      throws IOException
  {
    final Set<String> mimeTypes = mimeSupport.detectMimeTypesFromContent(contentLocator);
    assertThat("Expected MIME type not returned for content:", mimeTypes, hasItem(expectedMimeType));
  }

  /**
   * Should work.
   */
  @Test
  public void testClassicWithClassicFile()
      throws IOException
  {
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_OPENDESKTOP_KEY, Boolean.FALSE.toString());
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_FILE_KEY, util.resolvePath(CLASSIC_MAGIC_FILE_PATH));
    assertComplete(new DefaultMimeSupport());
  }

  /**
   * Should work, reason: logs will be full of problems as mime.cache cannot be parsed by "classic" detector, but
   * classic detector contains a "fall-back" database that is always used. File to be loaded is only to "augment" the
   * existing known entries, and hence even matching in assertions will work.
   */
  @Test
  public void testClassicWithOpendesktopFile()
      throws IOException
  {
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_OPENDESKTOP_KEY, Boolean.FALSE.toString());
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_FILE_KEY, util.resolvePath(OPENDESKTOP_MAGIC_FILE_PATH));
    assertComplete(new DefaultMimeSupport());
  }

  /**
   * Not works, as OpenDesktop will not be able to load classic file, but it also does not have any "fallback"
   * database.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testOpendesktopWithClassicFile()
      throws IOException
  {
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_OPENDESKTOP_KEY, Boolean.TRUE.toString());
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_FILE_KEY, util.resolvePath(CLASSIC_MAGIC_FILE_PATH));
    assertComplete(new DefaultMimeSupport());
  }

  /**
   * Should work as expected.
   */
  @Test
  public void testOpendesktopWithOpendesktopFile()
      throws IOException
  {
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_OPENDESKTOP_KEY, Boolean.TRUE.toString());
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_FILE_KEY, util.resolvePath(OPENDESKTOP_MAGIC_FILE_PATH));
    assertComplete(new DefaultMimeSupport());
  }

  /**
   * Component constructor will fail, as user specified file is enforced.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWithNonExistentFile()
      throws IOException
  {
    System.setProperty(DefaultMimeSupport.MIME_MAGIC_FILE_KEY, util.resolvePath("foo/bar"));
    assertComplete(new DefaultMimeSupport());
  }
}
