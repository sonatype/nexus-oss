/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * jruby's <code>Marshal.dump( v ).bytes.to_a</code> delivers <code>List</code> of <code>Long</code>
 * this InputStream wraps a given List of Long.
 *
 * @author christian
 */
public class ByteArrayInputStream
    extends InputStream
{
  private List<Long> bytes;

  private int cursor = 0;

  public ByteArrayInputStream(List<Long> bytes) {
    this.bytes = bytes;
  }

  @Override
  public int available() throws IOException {
    return bytes.size() - cursor;
  }

  @Override
  public void reset() throws IOException {
    cursor = 0;
  }

  @Override
  public int read() throws IOException {
    if (cursor < bytes.size()) {
      return bytes.get(cursor++).intValue();
    }
    else {
      return -1;
    }
  }
}