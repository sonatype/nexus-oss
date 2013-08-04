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

package org.sonatype.nexus.error.report;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;

public class ErrorReportBundleEntry
{

  private InputStream content;

  private String entryName;

  public ErrorReportBundleEntry(String entryName, InputStream content) {
    super();
    this.entryName = entryName;
    this.content = content;
  }

  public InputStream getContent() {
    return content;
  }

  public String getEntryName() {
    return entryName;
  }

  public void releaseEntry()
      throws IOException
  {
    IOUtil.close(content);
    content = null;
  }

}
