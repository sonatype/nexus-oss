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
 * Tests HTTP Configuration CRUD.
 */
StartTest(function(t) {
  var userAgentSuffix,
      timeout,
      retries;

  t.describe('HTTP configuration administration', function(t) {
    t.it('Can view the configuration', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/system/http'),
          {wait: 'CQVisible', args: '>>nx-coreui-system-http-settings'},
          function(next) {
            var settings = t.cq1('nx-coreui-system-http-settings');
            //capture present settings
            userAgentSuffix = settings.down('field[name=userAgentSuffix]').getValue();
            timeout = settings.down('field[name=timeout]').getValue();
            retries = settings.down('field[name=retries]').getValue();
            next()
          }
      );
    });
    t.it('Can update the configuration', function(t) {
      t.chain(
          {type: '_test', target: '>>nx-coreui-system-http-settings field[name=userAgentSuffix]'},
          {type: '[UP]', target: '>>nx-coreui-system-http-settings field[name=timeout]'},
          {type: '[UP]', target: '>>nx-coreui-system-http-settings field[name=retries]'},
          {click: '>>nx-coreui-system-http-settings button[action=save][disabled=false]'},
          {waitFor: 'CQVisible', args: 'window[ui=nx-message-success]'},
          function(next) {
            // FIXME: refactor/redesign how we test for message notifications
            //var messages = t.Ext().StoreManager.get('Message'),
            //    message = messages.getAt(0);

            var settings = t.cq1('nx-coreui-system-http-settings');

            //the form is updated
            t.expect(settings.down('field[name=userAgentSuffix]').getValue()).toBe(userAgentSuffix + '_test');
            t.expect(settings.down('field[name=timeout]').getValue()).toBe(++timeout);
            t.expect(settings.down('field[name=retries]').getValue()).toBe(++retries);

            //a success message is displayed
            // FIXME: refactor/redesign how we test for message notifications
            //t.expect(message.get('text')).toBe('HTTP system settings updated');
            //t.expect(message.get('type')).toBe('success');
            next()
          }
      )
    });
    t.it('The form will only allow a timeout between 1 and 3600', function(t) {
      t.chain(
          function(next) {
            var settings = t.cq1('>>nx-coreui-system-http-settings'),
                form = settings.down('form').getForm(),
                timeoutField = settings.down('field[name=timeout]'),
                timeout = timeoutField.getValue();
            
            t.expect(form.isValid()).toBe(true);
            timeoutField.setValue(3601);
            t.expect(form.isValid()).toBe(false);
            timeoutField.setValue(3600);
            t.expect(form.isValid()).toBe(true);
            timeoutField.setValue(0);
            t.expect(form.isValid()).toBe(false);
            timeoutField.setValue(1);
            t.expect(form.isValid()).toBe(true);
            next();
          }
      )
    });
    t.it('The form will only allow retries between 0 and 10', function(t) {
      t.chain(
          function(next) {
            var settings = t.cq1('>>nx-coreui-system-http-settings'),
                form = settings.down('form').getForm(),
                retriesField = settings.down('field[name=retries]'),
                retries = retriesField.getValue();
            
            t.expect(form.isValid()).toBe(true);
            retriesField.setValue(11);
            t.expect(form.isValid()).toBe(false);
            retriesField.setValue(10);
            t.expect(form.isValid()).toBe(true);
            retriesField.setValue(-10);
            t.expect(form.isValid()).toBe(false);
            retriesField.setValue(0);
            t.expect(form.isValid()).toBe(true);
            next();
          }
      )
    });
    t.it('Can discard changes on the form', function(t) {
      t.chain(
          {click: '>>nx-coreui-system-http-settings button[action=discard]'},
          function(next) {
            var settings = t.cq1('>>nx-coreui-system-http-settings'),
                form = settings.down('form').getForm(),
                retriesField = settings.down('field[name=retries]'),
                timeoutField = settings.down('field[name=timeout]');

            //compare with values trapped before testing form validation
            t.expect(timeoutField.getValue()).toBe(timeout);
            t.expect(retriesField.getValue()).toBe(retries);
            
            next();
          }
      )
    });
  });
});
