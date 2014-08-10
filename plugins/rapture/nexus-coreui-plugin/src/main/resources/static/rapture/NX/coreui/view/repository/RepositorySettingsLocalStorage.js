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
/*global Ext, NX*/

/**
 * Local Storage repository settings fields.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositorySettingsLocalStorage', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-repository-settings-localstorage',

  defaults: {
    xtype: 'textfield',
    allowBlank: true
  },

  items: [
    {
      name: 'defaultLocalStorageUrl',
      fieldLabel: 'Default Local Storage Location',
      helpText: 'This is the location on the file system used to host the artifacts. It is contained by the Working Directory set in the Server configuration.',
      readOnly: true,
      submitValue: false
    },
    {
      name: 'overrideLocalStorageUrl',
      fieldLabel: 'Override Local Storage Location',
      helpText: 'This is used to override the default local storage. Leave it blank to use the default. Note, file:/{drive-letter}:/ urls are supported in windows.  All other operating systems will use file:// .',
      emptyText: 'enter an override url'
    }
  ]

});
