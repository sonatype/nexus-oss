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
 * Commons repository settings fields.
 *
 * @since 3.0
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
      fieldLabel: 'ID',
      helpText: 'The unique id for the repository. This id will become part of the url so it should not contain spaces.',
      emptyText: 'enter a repository id',
      readOnly: true,
      validator: function(value) {
        if (/^[a-zA-Z0-9_\-\.]+$/.test(value)) {
          return true;
        }
        return 'Only letters, digits, underscores(_), hyphens(-), and dots(.) are allowed in ID';
      }
    },
    {
      name: 'name',
      fieldLabel: 'Name',
      helpText: 'The Repository Name which is referenced in the UI and Logs.',
      emptyText: 'enter a repository name'
    },
    {
      name: 'providerName',
      itemId: 'providerName',
      fieldLabel: 'Provider',
      helpText: 'The content provider of the repository.',
      readOnly: true,
      submitValue: false,
      allowBlank: true
    },
    {
      name: 'formatName',
      itemId: 'formatName',
      fieldLabel: 'Format',
      helpText: 'Repository format.',
      readOnly: true,
      submitValue: false,
      allowBlank: true
    }
  ]

});
