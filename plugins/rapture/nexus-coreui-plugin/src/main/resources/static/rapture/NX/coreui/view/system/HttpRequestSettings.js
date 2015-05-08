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
/*global Ext, NX*/

/**
 * Http request settings fields.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.HttpRequestSettings', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-system-httprequestsettings',
  requires: [
    'NX.I18n'
  ],

  items: [
    {
      xtype: 'textfield',
      name: 'userAgentCustomisation',
      fieldLabel: NX.I18n.get('ADMIN_HTTP_CUSTOMIZATION'),
      helpText: NX.I18n.get('ADMIN_HTTP_CUSTOMIZATION_HELP')
    },
    {
      xtype: 'textfield',
      name: 'urlParameters',
      fieldLabel: NX.I18n.get('ADMIN_HTTP_PARAMETERS'),
      helpText: NX.I18n.get('ADMIN_HTTP_PARAMETERS_HELP')
    },
    {
      xtype: 'numberfield',
      name: 'timeout',
      fieldLabel: NX.I18n.get('ADMIN_HTTP_TIMEOUT'),
      helpText: NX.I18n.get('ADMIN_HTTP_TIMEOUT_HELP'),
      allowDecimals: false,
      allowExponential: false,
      minValue: 0,
      maxValue: 3600
    },
    {
      xtype: 'numberfield',
      name: 'retries',
      fieldLabel: NX.I18n.get('ADMIN_HTTP_ATTEMPTS'),
      allowDecimals: false,
      allowExponential: false,
      minValue: 0,
      maxValue: 10
    }
  ]

});
