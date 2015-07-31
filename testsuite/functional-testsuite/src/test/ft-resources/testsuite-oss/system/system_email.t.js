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
  var originalValues, smtpSettingsCQ = '>>nx-coreui-system-smtp-settings', smtpSettingsFormCQ = smtpSettingsCQ +
          ' nx-settingsform',
      waitForUnmasked = function() {
        return !t.cq1(smtpSettingsFormCQ).el.isMasked();
      },
      uniqueId = t.uniqueId();

  t.describe('Given an admin administering Email Server', function(t) {
    t.it('Should be able to view the configuration', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/system/emailserver'),
          {wait: 'CQVisible', args: smtpSettingsFormCQ},
          {waitFor: waitForUnmasked},
          // capture the values to use later to verify, discard, and reset form
          function(next) {
            var form = t.cq1(smtpSettingsFormCQ).getForm();
            originalValues = form.getFieldValues();
            next();
          }
      );
    });
    t.it('Should be able to discard settings on the form', function(t) {
      t.chain(
          // type in some test strings to empty fields
          t.selectAndType(uniqueId, smtpSettingsCQ + ' field[name=subjectPrefix]'),
          t.selectAndType(uniqueId, smtpSettingsCQ + ' field[name=username]'),
          t.selectAndType(uniqueId, smtpSettingsCQ + ' field[name=password]'),
          {
            waitFor: 'Event', args: [smtpSettingsFormCQ, 'load'],
            trigger: {click: smtpSettingsCQ + ' button[action=discard]', desc: 'Discard all changes'}
          },
          {waitFor: waitForUnmasked},
          // test that we actually discarded the new values
          function(next) {
            var form = t.cq1(smtpSettingsFormCQ).getForm();
            t.isDeeply(originalValues, form.getFieldValues(), 'Form should be back in original state');
            next();
          }
      )
    });
    t.it('Should be able to enable the server with test values', function(t) {
      t.chain(
          // type in some test strings to empty fields
          t.selectAndType(uniqueId, smtpSettingsFormCQ + ' field[name=subjectPrefix]'),
          t.selectAndType(uniqueId, smtpSettingsFormCQ + ' field[name=username]'),
          t.selectAndType(uniqueId, smtpSettingsFormCQ + ' field[name=password]'),
          // press the "Save" button
          {
            waitFor: 'Event', args: [smtpSettingsFormCQ, 'submitted'],
            trigger: {click: '>>nx-coreui-system-smtp-settings button[action=save][disabled=false]', desc: 'Save new settings'}
          },
          {waitFor: waitForUnmasked},
          // verify new values
          function(next) {
            var form = t.cq1(smtpSettingsFormCQ).getForm();
            t.isDeeply(Ext.apply(Ext.clone(originalValues), {
              subjectPrefix: uniqueId,
              username: uniqueId,
              password: uniqueId
            }), form.getFieldValues(), 'Form should be updated');
            next();
          }
      )
    });
    t.it('Should accept default values', function(t) {
      t.chain(
          t.selectAndType(originalValues.subjectPrefix, smtpSettingsFormCQ + ' field[name=subjectPrefix]'), //value is undefined to start when we dump the fields
          t.selectAndType(originalValues.username, smtpSettingsFormCQ + ' field[name=username]'),
          t.selectAndType(originalValues.password, smtpSettingsFormCQ + ' field[name=password]'),
          // press the "Save" button
          {
            waitFor: 'Event', args: [smtpSettingsFormCQ, 'submitted'],
            trigger: {click: smtpSettingsCQ + ' button[action=save][disabled=false]', desc: 'Save new settings'},
          },
          {waitFor: waitForUnmasked},
          function(next) {
            var form = t.cq1(smtpSettingsFormCQ).getForm();
            t.isDeeply(originalValues, form.getFieldValues(), 'Form should be back in original state');
            next();
          }
      )
    });
  });
});
