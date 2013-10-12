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
 * System information panel.
 *
 * @since 2.7
 */
NX.define('Nexus.atlas.SystemInformationPanel', {
  extend: 'Ext.Panel',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.siesta'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var self = this;

    Ext.apply(self, {
      cls: 'nx-atlas-SystemInformationPanel',
      title: 'System Information',
      layout: 'fit',
      autoScroll: true,
      tbar: [
        {
          xtype: 'button',
          text: 'Refresh',
          scope: self,
          handler: self.refresh
        }
      ]
    });

    // TODO: Automatically refresh when visible

    self.sectionTpl = NX.create('Ext.XTemplate',
        '<div>',
        '<h2>{name}</h2>',
        '<table>',
        '<tpl for="props">',
        '<tr>',
        '<td>{name}</td>',
        '<td>{value}</td>',
        '</tr>',
        '</tpl>',
        '</table>',
        '</div>',
        {
          compiled: true
        }
    );

    self.mainTpl = NX.create('Ext.XTemplate',
        '<div>',
        '{[ this.section("system-time", values) ]}',
        '{[ this.section("system-properties", values) ]}',
        '{[ this.section("system-environment", values) ]}',
        '{[ this.section("system-runtime", values) ]}',
        '{[ this.section("system-threads", values) ]}',
        // FIXME: filestores is a complex structure, need to have a separate handler for it
        //'{[ this.section("system-filestores", values) ]}',
        '{[ this.section("nexus-configuration", values) ]}',
        '{[ this.section("nexus-properties", values) ]}',
        // FIXME: plugins is a complex structure ...
        //'{[ this.section("nexus-plugins", values) ]}',
        '</div>',
        {
          compiled: true,

          section: function (name, values) {
            // pull off the section of data we want to render
            var data = values[name];

            // convert object into array of name/value objects for xtemplate to render
            var props = [];
            Ext.iterate(data, function(key, value) {
              props.push({
                name: key,
                value: value
              })
            });

            return self.sectionTpl.apply({
              name: name,
              props: props
            });
          }
        }
    );

    self.constructor.superclass.initComponent.apply(self, arguments);
  },

  refresh: function () {
    var self = this,
        mask = NX.create('Ext.LoadMask', self.getEl(), { msg: 'Loading...' });

    self.logDebug('Refreshing');

    mask.show();
    Ext.Ajax.request({
      url: Nexus.siesta.basePath + '/atlas/system-information',

      scope: self,
      callback: function () {
        mask.hide()
      },
      success: function (response, opts) {
        var obj = Ext.decode(response.responseText);
        self.mainTpl.overwrite(self.body, obj);
      }
    });
  }
});