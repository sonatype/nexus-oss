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
 * Http request settings fields.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.HttpRequestSettings', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-httprequestsettings',

  items: [
    {
      xtype: 'textfield',
      name: 'userAgentCustomisation',
      fieldLabel: 'User-agent customization'
    },
    {
      xtype: 'textfield',
      name: 'urlParameters',
      fieldLabel: 'URL parameters'
    },
    {
      xtype: 'numberfield',
      name: 'timeout',
      fieldLabel: 'Timeout seconds',
      allowDecimals: false,
      allowExponential: false,
      minValue: 0,
      maxValue: 3600
    },
    {
      xtype: 'numberfield',
      name: 'retries',
      fieldLabel: 'Retry attempts',
      allowDecimals: false,
      allowExponential: false,
      minValue: 0,
      maxValue: 10
    }
  ]

});