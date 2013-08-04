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

package org.sonatype.nexus.email;

import org.sonatype.nexus.NexusAppTestSupport;

import org.junit.Test;

public class DefaultNexusEmailerTest
    extends NexusAppTestSupport
{
  private NexusEmailer emailer;

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    emailer = lookup(NexusEmailer.class);
  }

  @Test
  public void testConfigChanged()
      throws Exception
  {
/*        CSmtpConfiguration newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname(  "1.2.3.4" );

        assertTrue( emailer.configChanged( newSmtp ) );

        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );

        assertTrue( emailer.configChanged( newSmtp ) );

        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );

        assertTrue( emailer.configChanged( newSmtp ) );

        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );

        assertTrue( emailer.configChanged( newSmtp ) );

        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        newSmtp.setPassword( "password" );

        assertTrue( emailer.configChanged( newSmtp ) );

        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        newSmtp.setPassword( "password" );
        newSmtp.setSslEnabled( true );

        assertTrue( emailer.configChanged( newSmtp ) );

        newSmtp = new CSmtpConfiguration();
        newSmtp.setHostname( "1.2.3.4" );
        newSmtp.setPort( 1234 );
        newSmtp.setSystemEmailAddress( "someemail" );
        newSmtp.setUsername( "username" );
        newSmtp.setPassword( "password" );
        newSmtp.setSslEnabled( true );
        newSmtp.setTlsEnabled( true );

        assertTrue( emailer.configChanged( newSmtp ) );

        assertFalse( emailer.configChanged( newSmtp ) );*/
  }
}
