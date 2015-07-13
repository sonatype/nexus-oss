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
/**
 * Tests that sign-in/sign-out works.
 */
StartTest(function(t) {
  t.diag('Verify basic sign-in/sign-out');

  t.ok(Ext, 'Ext is here');
  t.ok(NX, 'NX is here');

  t.describe('Can login and out of the application', function(t) {
    var message = t.spyOn(NX.application.getController('Message'), 'addMessage').and.callThrough()
    
    t.it('Can login as admin and see a confirmation message', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/welcome'),
          // message will be shown on login
          function(next) {
            t.expect(message).toHaveBeenCalled();
            t.expect(message.calls.mostRecent().args[0].text).toBe('User signed in: admin');
            next();
          }
      );
    });

    t.it('Can logout and see a confirmation message', function(t) {
      t.chain(
          {click: '>>nx-header-signout'},
           // message will be shown on logout
          function(next) {
            t.expect(message).toHaveBeenCalled();
            t.expect(message.calls.mostRecent().args[0].text).toBe('User signed out');
            next();
          }
      );
    });
  });

});
