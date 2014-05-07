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
package org.sonatype.nexus.mime;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests for {@link RegexpMimeRulesSource}.
 */
public class RegexpMimeRulesSourceTest
    extends TestSupport
{
  @Test
  public void testRegectMimeRulesSourceTest() {
    final RegexpMimeRulesSource underTest = new RegexpMimeRulesSource();

    underTest.addRule(".*\\.foo\\z", "foo/bar");
    underTest.addRule(".*\\.pom\\z", "application/x-pom");
    underTest.addRule("(.*/maven-metadata.xml\\z)|(maven-metadata.xml\\z)", "application/x-maven-metadata");

    // "more specific" one
    underTest.addRule("\\A/atom-service/.*\\.xml\\z", "application/atom+xml");

    // and now the "general one"
    underTest.addRule(".*\\.xml\\z", "application/xml");

    assertThat(underTest.getRuleForPath("/some/repo/path/content.foo"), equalTo("foo/bar"));
    assertThat(underTest.getRuleForPath("/some/repo/path/content.foo.bar"), nullValue());
    assertThat(underTest.getRuleForPath("/log4j/log4j/1.2.12/log4j-1.2.12.pom"), equalTo("application/x-pom"));
    assertThat(underTest.getRuleForPath("maven-metadata.xml"), equalTo("application/x-maven-metadata"));
    assertThat(underTest.getRuleForPath("/maven-metadata.xml"), equalTo("application/x-maven-metadata"));
    assertThat(underTest.getRuleForPath("/org/sonatype/nexus/maven-metadata.xml"), equalTo("application/x-maven-metadata"));
    assertThat(underTest.getRuleForPath("/org/sonatype/nexus/maven-metadata.xml.bar"), nullValue());
    assertThat(underTest.getRuleForPath("/org/sonatype/nexus/maven-metadata.xml/tricky/path.pom"), equalTo("application/x-pom"));

    assertThat(underTest.getRuleForPath("/org/sonatype/nexus/maven-metadata1.xml"), equalTo("application/xml"));
    assertThat(underTest.getRuleForPath("/atom-service//org/sonatype/nexus/maven-metadata1.xml"), equalTo("application/atom+xml"));
  }
}
