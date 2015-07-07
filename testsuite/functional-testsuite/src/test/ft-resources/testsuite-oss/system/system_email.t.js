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
 * Tests system email server viewing, updating, discarding.
 */
StartTest(function(t) {
  var enabled, 
      host, 
      port, 
      subject, 
      username, 
      password;

  t.describe('Given an admin administering Email Server', function(t) {
    t.it('Should be able to view the configuration', function (t) {
      t.chain(
          t.openPageAsAdmin('admin/system/emailserver'),
          {wait: 'CQVisible', args: '>>nx-coreui-system-smtp-settings'},
          // capture the values to use later to verify discard, reset form
          function (next) {
            var settings = t.cq1('nx-coreui-system-smtp-settings');
            enabled = settings.down('checkbox[name=enabled]').getValue();
            host = settings.down('field[name=host]').getValue();
            port = settings.down('field[name=port]').getValue();
            subject = settings.down('field[name=subjectPrefix]').getValue();
            username = settings.down('field[name=username]').getValue();
            password = settings.down('field[name=password]').getValue();
            next();
          }
      );
    }); 
    t.it('Should be able to discard settings on the form', function(t) {
      t.chain( 
          // click the enabled checkbox
          {click: '>>checkbox[name=enabled]'},
          // type in some test strings to empty fields
          {type: '_test', target: '>>nx-coreui-system-smtp-settings field[name=subjectPrefix]'},
          {type: '_testUser', target: '>>nx-coreui-system-smtp-settings field[name=username]'},
          {type: '_testPassword', target: '>>nx-coreui-system-smtp-settings field[name=password]'},
          // press the "Save" button
          {click: '>>nx-coreui-system-smtp-settings button[text=Discard]'},
          // let the UI update
          function(next) {
            t.waitForAnimations(next);
          },
          // test that we actually discarded the new values
          function(next) {
            var settings = t.cq1('nx-coreui-system-smtp-settings'),
                enabledField = settings.down('checkbox[name=enabled]').getValue(),
                hostField = settings.down('field[name=host]').getValue(),
                portField = settings.down('field[name=port]').getValue(),
                subjectField = settings.down('field[name=subjectPrefix]').getValue(),
                usernameField = settings.down('field[name=username]').getValue(),
                passwordField = settings.down('field[name=password]').getValue();
            
            // compare fields values after to fields state at first view
            t.expect(enabledField).toBe(enabled);
            t.expect(hostField).toBe(host);
            t.expect(portField).toBe(port);
            t.expect(subjectField).toBe(subject);
            t.expect(usernameField).toBe(username);
            t.expect(passwordField).toBe(password); 
            next();
          }
      )
    });
    t.it('Should be able to enable the server with test values', function(t) {
      t.chain(
          // click the enabled checkbox
          {click: '>>checkbox[name=enabled]'},
          // type in some test strings to empty fields
          {type: '_test', target: '>>nx-coreui-system-smtp-settings field[name=subjectPrefix]'},
          {type: '_testUser', target: '>>nx-coreui-system-smtp-settings field[name=username]'},
          {type: '_testPassword', target: '>>nx-coreui-system-smtp-settings field[name=password]'},
          // press the "Save" button
          {click: '>>nx-coreui-system-smtp-settings button[action=save]'},
          
          function(next) {
            t.waitForAnimations(next);
          }
        )
    });
    t.it('Should accept default values', function(t) {
      t.chain(
        function(next) {
          t.selectAndType(enabled, '>>nx-coreui-system-smtp-settings checkbox[name=enabled]');
          t.selectAndType(host, '>>nx-coreui-system-smtp-settings field[name=host]');
          t.selectAndType(port, '>>nx-coreui-system-smtp-settings field[name=port]');
          t.selectAndType(subject, '>>nx-coreui-system-smtp-settings field[name=subjectPrefix]');
          t.selectAndType(username, '>>nx-coreui-system-smtp-settings field[name=username]');
          t.selectAndType(password, '>>nx-coreui-system-smtp-settings field[name=password]');
          next();
        },
        function(next) {
          t.waitForAnimations(next);
        }, 
        // press the "Save" button
        {click: '>>nx-coreui-system-smtp-settings button[action=save]'},
        function(next) {
          t.waitForAnimations(next);
        } 
      )
    });
  });
});
