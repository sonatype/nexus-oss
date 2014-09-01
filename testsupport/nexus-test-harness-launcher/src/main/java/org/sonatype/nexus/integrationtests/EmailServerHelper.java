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

package org.sonatype.nexus.integrationtests;

import javax.mail.internet.MimeMessage;

import org.sonatype.nexus.test.utils.TestProperties;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

/**
 * Helper for tests that require an email server.
 *
 * @since 3.0
 */
public class EmailServerHelper
  extends ExternalResource
{
  private static final Logger log = LoggerFactory.getLogger(EmailServerHelper.class);

  private GreenMail server;

  @Override
  protected void before() throws Throwable {
    startServer();
  }

  @Override
  protected void after() {
    stopServer();
  }

  public GreenMail getServer() {
    checkState(server != null, "Server not started");
    return server;
  }

  public void startServer() {
    checkState(server == null, "Server already started");

    int port = TestProperties.getInteger("email.server.port");
    log.info("Starting; port: {}", port);

    GreenMail server = new GreenMail(new ServerSetup(port, null, ServerSetup.PROTOCOL_SMTP));
    server.setUser("system@nexus.org", "smtp-username", "smtp-password");
    server.start();

    this.server = server;
  }

  public void stopServer() {
    if (server != null) {
      log.info("Stopping");
      server.stop();
      server = null;
    }
  }

  public MimeMessage[] getReceivedMessages() {
    return getServer().getReceivedMessages();
  }

  public boolean waitForMail(int count) {
    return waitForMail(count, 5000);
  }

  public boolean waitForMail(int count, long timeout) {
    try {
      return getServer().waitForIncomingEmail(timeout, count);
    }
    catch (InterruptedException e) {
      return false;
    }
  }
}
