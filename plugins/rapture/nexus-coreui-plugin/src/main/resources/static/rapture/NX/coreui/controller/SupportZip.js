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
 * Support Zip controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.SupportZip', {
  extend: 'Ext.app.Controller',
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'support.SupportZip',
    'support.SupportZipCreated'
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'feature-support-supportzip': {
        file: 'file_extension_zip.png',
        variants: ['x16', 'x32']
      },
      'supportzip-zip': {
        file: 'file_extension_zip.png',
        variants: ['x16', 'x32']
      },
      'supportzip-truncated': {
        file: 'warning.png',
        variants: ['x16', 'x32']
      }
    });

    me.getApplication().getFeaturesController().registerFeature([
      {
        path: '/Support/Support ZIP',
        view: { xtype: 'nx-coreui-support-supportzip' },
        description: 'Creates a ZIP file containing useful support information about your server',
        visible: function () {
          return NX.Permissions.check('nexus:atlas', 'create');
        }
      }
    ]);

    me.listen({
      component: {
        'nx-coreui-support-supportzip form': {
          submitted: me.showSupportZipCreatedWindow
        },
        'nx-coreui-support-supportzipcreated button[action=download]': {
          click: me.download
        }
      }
    });
  },

  /**
   * @private
   */
  showSupportZipCreatedWindow: function (form, action) {
    Ext.widget('nx-coreui-support-supportzipcreated').setValues(action.result.data);
  },

  /**
   * @private
   * Download support ZIP file.
   */
  download: function (button) {
    var win = button.up('window'),
        fileName = win.down('form').getValues().name;

    NX.Security.doWithAuthenticationToken(
        'Downloading support ZIP requires validation of your credentials.',
        {
          success: function (authToken) {
            NX.util.DownloadHelper.downloadUrl(NX.util.Url.urlOf(
                'service/siesta/wonderland/download/' + fileName + '?t=' + NX.util.Base64.encode(authToken)
            ));
            win.close()
          }
        }
    );
  }

});