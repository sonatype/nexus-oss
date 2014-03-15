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
/**
 * Controls header menu.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Help', {
  extend: 'Ext.app.Controller',
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'header.Help'
  ],

  refs: [
    {
      ref: 'featureHelp',
      selector: 'nx-header-help menuitem[action=feature]'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'help-support': {
        file: 'support.png',
        variants: ['x16', 'x32']
      },
      'help-manual': {
        file: 'book_picture.png',
        variants: ['x16', 'x32']
      }
    });

    me.listen({
      controller: {
        '#Menu': {
          featureselected: me.onFeatureSelected
        }
      }
    });
  },

  /**
   * @private
   * Update help menu content.
   * @param {NX.model.Feature} feature selected feature
   */
  onFeatureSelected: function (feature) {
    var me = this,
        text = feature.get('text'),
        iconName = feature.get('iconName'),
        featureHelp = me.getFeatureHelp();

    featureHelp.setText('Help for: ' + text);
    featureHelp.setIconCls(NX.Icons.cls(iconName, 'x16'));
  }

});