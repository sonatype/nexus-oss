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
 * Header help controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Help', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Icons',
    'NX.Messages',
    'NX.Windows'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'header.Help',
    'AboutWindow'
  ],

  refs: [
    {
      ref: 'featureHelp',
      selector: 'nx-header-help menuitem[action=feature]'
    }
  ],

  /**
   * @private
   * @type {NX.model.Feature}
   */
  selectedFeature: undefined,

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
      'help-issuetracker': {
        file: 'bug.png',
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
      },
      component: {
        'nx-header-help menuitem[action=feature]': {
          click: me.onFeatureHelp
        },
        'nx-header-help menuitem[action=about]': {
          click: me.onAbout
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

    me.selectedFeature = feature;

    featureHelp.setText('Help for: ' + text);
    featureHelp.setIconCls(NX.Icons.cls(iconName, 'x16'));
  },

  /**
   * @private
   */
  onFeatureHelp: function() {
    var me = this,
        baseUrl = 'http://links.sonatype.com/products/nexus/docs-search/' + NX.State.getVersionMajorMinor(),
        url = baseUrl + '/' + me.selectedFeature.get('helpKeyword');

    NX.Windows.open(url);
  },

  /**
   * @private
   */
  onAbout: function() {
    Ext.widget('nx-aboutwindow');
  }
});