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

package org.sonatype.sisu.ehcache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Code borrowed from Plexus Utils to keep dependencies minimal. Only sole purpose of this code is to read up EHCache
 * configuration from classpath as string.
 *
 * @author cstamas
 * @since 1.0
 */
public class IO
{
  public static String toString(final InputStream input)
      throws IOException
  {
    final StringWriter sw = new StringWriter();
    copy(new InputStreamReader(input, "UTF-8"), sw, 4096);
    return sw.toString();
  }

  public static void copy(final Reader input, final Writer output, final int bufferSize)
      throws IOException
  {
    final char[] buffer = new char[bufferSize];
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
    }
    output.flush();
  }
}
