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
/*global define*/
define('Nexus/configuration/Ajax', ['extjs'], function(Ext) {
  Ext.Ajax.defaultHeaders = {
    'accept' : 'application/json,application/vnd.siesta-error-v1+json,application/vnd.siesta-validation-errors-v1+json',

    // ensure that we send X-Requested-With so that server can omit WWW-Authenticate headers in responses
    'X-Requested-With': 'XMLHttpRequest'
  };

  Ext.Ajax.on('requestexception', function(connection, response) {
    if ( response && Ext.isFunction(response.getResponseHeader) ) { // timeouts/socket closed response does not have this method(?)
      var contentType = response.getResponseHeader('Content-Type');
      if ( contentType === 'application/vnd.siesta-error-v1+json') {
        response.siestaError = Ext.decode(response.responseText);
      } else if ( contentType === 'application/vnd.siesta-validation-errors-v1+json') {
        response.siestaValidationError = Ext.decode(response.responseText);
      }
    }
  });

  // Set default HTTP headers
  Ext.lib.Ajax.defaultPostHeader = 'application/json; charset=utf-8';
});