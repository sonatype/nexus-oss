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
/**
 * Tests that login/logout works.
 *
 * @since 2.8
 */
StartTest(function (t) {

  t.chain(
      { waitFor: 'stateReceived' },
      { waitFor: 'userToBeLoggedOut' },

      { waitFor: 'CQVisible', args: 'nx-header-login' },
      { click: '>>nx-header-login' },
      { waitFor: 'CQVisible', args: 'nx-login' },
      { type: 'admin[TAB]' },
      { type: 'admin123[ENTER]' },
      { waitFor: 'CQVisible', args: 'nx-header-logout' },
      { click: '>>nx-header-logout' },
      { waitFor: 'CQVisible', args: 'nx-header-login' }
  );

});
