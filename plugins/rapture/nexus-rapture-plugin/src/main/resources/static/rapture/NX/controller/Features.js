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
 * Features registration controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Features', {
  extend: 'Ext.app.Controller',
  mixins: {
    logAware: 'NX.LogAware'
  },

  models: [
    'Feature'
  ],
  stores: [
    'Feature',
    'FeatureMenu'
  ],

  statics: {
    alwaysVisible: function () {
      return true;
    },

    alwaysHidden: function () {
      return false;
    }
  },

  /**
   * Registers features.
   * @param {Array/Object} features to be registered
   */
  registerFeature: function (features) {
    var me = this,
        path;

    if (features) {
      Ext.each(Ext.Array.from(features), function (feature) {
        if (!feature.path) {
          throw Ext.Error.raise('Feature missing path');
        }

        if (!feature.mode) {
          feature.mode = 'admin';
        }

        if (!feature.view && feature.group === true) {
          feature.view = 'NX.view.feature.Group';
        }

        // complain if there is no view configuration
        if (!feature.view && !feature.href) {
          me.logError('Missing view configuration for feature at path: ' + feature.path);
          // TODO: Maybe raise an error instead?
        }

        if (feature.href && !feature.hrefTarget) {
          feature.hrefTarget = feature.href;
        }

        path = feature.path;
        if (path.charAt(0) === '/') {
          path = path.substr(1, path.length);
        }

        me.configureIcon(path, feature);

        path = feature.mode + '/' + path;
        feature.path = '/' + path;

        // auto-set bookmark
        if (!feature.bookmark) {
          feature.bookmark = NX.Bookmarks.encode(path).toLowerCase();
        }

        if (Ext.isDefined(feature.visible)) {
          if (!Ext.isFunction(feature.visible)) {
            if (feature.visible) {
              feature.visible = NX.controller.Features.alwaysVisible;
            }
            else {
              feature.visible = NX.controller.Features.alwaysHidden;
            }
          }
        }
        else {
          feature.visible = NX.controller.Features.alwaysVisible;
        }

        me.getFeatureStore().addSorted(me.getFeatureModel().create(feature));
      });
    }
  },

  /**
   * Un-registers features.
   * @param {Object[]/Object} features to be unregistered
   */
  unregisterFeature: function (features) {
    var me = this;

    if (features) {
      Ext.each(Ext.Array.from(features), function (feature) {
        var path, model;

        if (!feature.mode) {
          feature.mode = 'admin';
        }
        path = feature.path;
        if (path.charAt(0) === '/') {
          path = path.substr(1, path.length);
        }
        path = feature.mode + '/' + path;
        feature.path = '/' + path;

        model = me.getFeatureStore().getById(feature.path);
        if (model) {
          me.getFeatureStore().remove(model);
        }
      });
    }
  },

  /**
   * @private
   * @param feature
   */
  configureIcon: function (path, feature) {
    var me = this,
        defaultIconName = 'feature-' + feature.mode + '-' + path.toLowerCase().replace(/\//g, '-').replace(/\s/g, '');

    // inline icon registration for feature
    if (feature.iconConfig) {
      var icon = feature.iconConfig;
      delete feature.iconConfig;
      if (icon.name) {
        feature.iconName = icon.name;
      }
      else {
        icon.name = defaultIconName;
      }
      me.getApplication().getIconController().addIcon(icon);
    }

    // default icon name if not set
    if (!feature.iconName) {
      feature.iconName = defaultIconName;
    }
  }

});