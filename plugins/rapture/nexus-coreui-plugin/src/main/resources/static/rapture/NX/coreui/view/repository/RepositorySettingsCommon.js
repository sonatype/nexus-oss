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
Ext.define('NX.coreui.view.repository.RepositorySettingsCommon', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-repository-settings-common',

  defaults: {
    xtype: 'textfield',
    allowBlank: false
  },

  items: [
    {
      name: 'id',
      itemId: 'id',
      fieldLabel: 'Id',
      emptyText: 'enter a repository id',
      readOnly: true
    },
    {
      name: 'name',
      fieldLabel: 'Name',
      emptyText: 'enter a repository name'
    },
    {
      name: 'providerName',
      itemId: 'providerName',
      fieldLabel: 'Provider',
      readOnly: true,
      submitValue: false,
      allowBlank: true
    },
    {
      name: 'formatName',
      itemId: 'formatName',
      fieldLabel: 'Format',
      readOnly: true,
      submitValue: false,
      allowBlank: true
    }
  ]

});
