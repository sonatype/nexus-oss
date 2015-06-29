/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * Tests the Validation helper.
 */
StartTest(function(t) {

  t.ok(Ext, 'Ext is here');
  t.ok(NX, 'NX is here');

  t.requireOk('NX.I18n', function() {
    t.requireOk('NX.util.Validator', function() {
      var nxName = Ext.form.field.VTypes['nx-name'],
          validNames = ['Foo_1.2-3', 'foo.', '-0.', 'a', '1'],
          invalidNames = [
            '#', ',', '*', ' ', '#', '\'', '\\', '/', '?', '<', '>', '|', ' ', '\r', '\n', '\t', ',',
            '+', '@', '&', 'å', '©', '不', 'β', 'خ', '_leadingUnderscore', '.', '..'
          ];

      Ext.each(invalidNames, function(value) {
        t.notOk(nxName(value), 'Name should not be allowed: \'' + value + '\'');
      });

      Ext.each(validNames, function(value) {
        t.ok(nxName(value), 'Valid name: \'' + value + '\'');
      });
    });
  });
});
