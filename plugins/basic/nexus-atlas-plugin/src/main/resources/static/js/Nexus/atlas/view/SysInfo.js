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
 * System information view.
 *
 * @since 2.7
 */
NX.define('Nexus.atlas.view.SysInfo', {
  extend: 'Ext.Panel',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.atlas.Icons'
  ],

  xtype: 'nx-atlas-view-sysinfo',
  title: 'System Information',
  id: 'nx-atlas-view-sysinfo',
  cls: 'nx-atlas-view-sysinfo',
  layout: 'fit',
  autoScroll: true,

  /**
   * @override
   */
  initComponent: function() {
    var me = this,
        icons = Nexus.atlas.Icons;

    me.sectionTpl = NX.create('Ext.XTemplate',
        '<div class="nx-atlas-view-sysinfo-section">',
        '<h2>{name}</h2>',
        '<table>',
        '<tpl for="props">',
        '<tr>',
        '<td class="property-name">{name}</td>',
        '<td class="property-value">{value}</td>',
        '</tr>',
        '</tpl>',
        '</table>',
        '</div>',
        {
          compiled: true
        }
    );

    me.mainTpl = NX.create('Ext.XTemplate',
        '<div class="nx-atlas-view-sysinfo-body">',
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

          section: function(name, values) {
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

            return me.sectionTpl.apply({
              name: name,
              props: props
            });
          }
        }
    );

    Ext.apply(me, {
      tbar: [
        {
          xtype: 'button',
          id: 'nx-atlas-view-sysinfo-button-refresh',
          text: 'Refresh',
          tooltip: 'Refresh system information',
          iconCls: icons.get('refresh').cls
        }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  },

  /**
   * Update the system information display.
   *
   * @public
   */
  setInfo: function(info) {
    var me = this;
    me.mainTpl.overwrite(me.body, info);
  }
});