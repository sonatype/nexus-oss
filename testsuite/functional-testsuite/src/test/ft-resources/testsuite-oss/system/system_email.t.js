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
  var originalValues;

  t.describe('Given an admin administering Email Server', function(t) {
    t.it('Should be able to view the configuration', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/system/emailserver'),
          {wait: 'CQVisible', args: '>>nx-coreui-system-smtp-settings'},
          // capture the values to use later to verify, discard, and reset form
          function(next) {
            var settings = t.cq1('nx-coreui-system-smtp-settings'),
                form = settings.down('form').getForm();
            originalValues = form.getFieldValues();
            next();
          }
      );
    });
    t.it('Should be able to discard settings on the form', function(t) {
      t.chain(
          // type in some test strings to empty fields
          t.selectAndType('_test', '>>nx-coreui-system-smtp-settings field[name=subjectPrefix]'),
          t.selectAndType('_testUser', '>>nx-coreui-system-smtp-settings field[name=username]'),
          t.selectAndType('_testPassword', '>>nx-coreui-system-smtp-settings field[name=password]'),
          {click: '>>nx-coreui-system-smtp-settings button[action=discard]', desc: 'Discard all changes'},
          // let the UI update
          function(next) {
            t.waitForAnimations(next);
          },
          // test that we actually discarded the new values
          function(next) {
            var form = t.cq1('nx-coreui-system-smtp-settings').down('form').getForm();
            t.isDeeply(originalValues, form.getFieldValues(), 'Form should be back in original state');
            next();
          }
      )
    });
    t.it('Should be able to enable the server with test values', function(t) {
      t.chain(
          // type in some test strings to empty fields
          t.selectAndType('_test', '>>nx-coreui-system-smtp-settings field[name=subjectPrefix]'),
          t.selectAndType('_testUser', '>>nx-coreui-system-smtp-settings field[name=username]'),
          t.selectAndType('_testPassword', '>>nx-coreui-system-smtp-settings field[name=password]'),
          // press the "Save" button
          {click: '>>nx-coreui-system-smtp-settings button[action=save]', desc: 'Save new settings'},
          // let the UI update
          function(next) {
            t.waitForAnimations(next);
          },
          // verify new values
          function(next) {
            var form = t.cq1('nx-coreui-system-smtp-settings').down('form').getForm();
            t.isDeeply(Ext.apply(Ext.clone(originalValues), {
                  subjectPrefix: '_test',
                  username: '_testUser',
                  password: '_testPassword'
                }), form.getFieldValues(), 'Form should be updated');
            next();
          }
      )
    });
    t.it('Should accept default values', function(t) {
      t.chain(
          t.selectAndType(originalValues.host, '>>nx-coreui-system-smtp-settings field[name=host]'),
          t.selectAndType(originalValues.port, '>>nx-coreui-system-smtp-settings field[name=port]'),
          t.selectAndType('', '>>nx-coreui-system-smtp-settings field[name=subjectPrefix]'), //value is undefined to start when we dump the fields
          t.selectAndType(originalValues.username, '>>nx-coreui-system-smtp-settings field[name=username]'),
          t.selectAndType(originalValues.password, '>>nx-coreui-system-smtp-settings field[name=password]'),
          // press the "Save" button
          {click: '>>nx-coreui-system-smtp-settings button[action=save]'},
          function(next) {
            t.waitForAnimations(next);
          },
          function(next) {
            var form = t.cq1('nx-coreui-system-smtp-settings').down('form').getForm();
            t.isDeeply(originalValues, form.getFieldValues(), 'Form should be back in original state');
            next();
          }
      )
    });
  });
});
