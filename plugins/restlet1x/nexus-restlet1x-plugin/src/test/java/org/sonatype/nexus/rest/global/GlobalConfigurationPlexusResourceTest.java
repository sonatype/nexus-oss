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

package org.sonatype.nexus.rest.global;

import org.sonatype.nexus.rest.model.SmtpSettings;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link GlobalConfigurationPlexusResource} behavior.
 */
public class GlobalConfigurationPlexusResourceTest
    extends TestSupport
{

  private GlobalConfigurationPlexusResource testSubject = new GlobalConfigurationPlexusResource();

  @Test
  public void unescapeHTMLInSMTPPassword() {
    // settings object as it would come in via REST, with escaped HTML
    SmtpSettings settings = new SmtpSettings();
    settings.setPassword("asdf&amp;qwer");
    settings.setUsername("asdf&amp;qwer");

    // make sure the configuration resource configures xstream to unescape
    final XStream xStream = new XStream();
    testSubject.configureXStream(xStream);

    final String xml = xStream.toXML(settings);
    settings = (SmtpSettings) xStream.fromXML(xml);

    assertThat(settings.getUsername(), is("asdf&qwer"));
    assertThat(settings.getPassword(), is("asdf&qwer"));
  }
}
