/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.odata;

import java.io.StringReader;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XmlSplicerTest
{
  static class NoOpSplicer
      extends XmlSplicer
  {
    NoOpSplicer(final StringBuilder xml) {
      super(xml);
    }

    @Override
    void started(final String name, final int len, final boolean isRoot) {
      // no-op
    }

    @Override
    void ended(final String name, final int len) {
      // no-op
    }
  }

  static class BraceSplicer
      extends XmlSplicer
  {
    BraceSplicer(final StringBuilder xml) {
      super(xml);
    }

    @Override
    void started(final String name, final int len, final boolean isRoot) {
      final int mark = xml.length();
      xml.replace(mark - len, mark, "{");
    }

    @Override
    void ended(final String name, final int len) {
      final int mark = xml.length();
      xml.replace(mark - len, mark, "}");
    }
  }

  private static final String XML = "<a><b>text</b><c/></a>";

  private static final String TXT = "{{text}{}}";

  @Test
  public void testRoundTrip()
      throws Exception
  {
    final StringBuilder xml = new StringBuilder();
    new NoOpSplicer(xml).consume(new StringReader(XML));
    assertThat(xml.toString(), is(XML));
  }

  @Test
  public void testSplicing()
      throws Exception
  {
    final StringBuilder xml = new StringBuilder();
    new BraceSplicer(xml).consume(new StringReader(XML));
    assertThat(xml.toString(), is(TXT));
  }
}
