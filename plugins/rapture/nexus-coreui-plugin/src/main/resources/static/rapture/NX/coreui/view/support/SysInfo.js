/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * System Information panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.support.SysInfo', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-support-sysinfo',
  requires: [
    'Ext.XTemplate',
    'NX.I18n'
  ],
  ui: 'nx-inset',

  layout: 'fit',
  autoScroll: true,

  tbar: [
    {
      xtype: 'button',
      text: NX.I18n.get('ADMIN_SYSTEM_INFORMATION_DOWNLOAD_BUTTON'),
      glyph: 'xf019@FontAwesome' /* fa-download */,
      action: 'download'
    },
    '-',
    {
      xtype: 'button',
      text: NX.I18n.get('ADMIN_SYSTEM_INFORMATION_PRINT_BUTTON'),
      glyph: 'xf02f@FontAwesome' /* fa-print */,
      action: 'print'
    }
  ],

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    // simple named section with list of key-value properties
    me.sectionTpl = Ext.create('Ext.XTemplate',
        '<tr>',
        '<td colspan="2">',
        '<h2>{name}</h2>',
        '</td>',
        '</tr>',
        '<tpl for="props">',
        '<tr>',
        '<td class="property-name">{name}</td>',
        '<td class="property-value">{value}</td>',
        '</tr>',
        '</tpl>',
        {
          compiled: true
        }
    );

    // nested named section with list of child named sections
    me.nestedSectionTpl = Ext.create('Ext.XTemplate',
        '<tr>',
        '<td colspan="2">',
        '<h2>{name}</h2>',
        '</td>',
        '</tr>',
        '<tpl for="nested">',
        '<tr>',
        '<td colspan="2">',
        '<h3>{name}</h3>',
        '</td>',
        '</tr>',
        '<tpl for="props">',
        '<tr>',
        '<td class="property-name">{name}</td>',
        '<td class="property-value">{value}</td>',
        '</tr>',
        '</tpl>',
        '</tpl>',
        {
          compiled: true
        }
    );

    // Helper to convert an object into an array of {name,value} properties
    function objectToProperties(obj) {
      var props = [];
      Ext.iterate(obj, function(key, value) {
        props.push({
          name: key,
          value: value
        });
      });
      return props;
    }

    // Main template renders all sections
    me.mainTpl = Ext.create('Ext.XTemplate',
        '<div class="nx-atlas-view-sysinfo-body">',
        '<table>',
        // nexus details
        '{[ this.section("nexus-status", values) ]}',
        '{[ this.section("nexus-configuration", values) ]}',
        '{[ this.section("nexus-properties", values) ]}',
        '{[ this.section("nexus-license", values) ]}',
        '{[ this.nestedSection("nexus-plugins", values) ]}',
        // system details
        '{[ this.section("system-time", values) ]}',
        '{[ this.section("system-properties", values) ]}',
        '{[ this.section("system-environment", values) ]}',
        '{[ this.section("system-runtime", values) ]}',
        '{[ this.nestedSection("system-network", values) ]}',
        '{[ this.nestedSection("system-filestores", values) ]}',
        '</table>',
        '</div>',
        {
          compiled: true,

          /**
           * Render a section.
           */
          section: function(name, values) {
            var data = values[name];

            return me.sectionTpl.apply({
              name: name,
              props: objectToProperties(data)
            });
          },

          /**
           * Render a nested section.
           */
          nestedSection: function(name, values) {
            var data = values[name],
                nested = [];

            Ext.iterate(data, function(key, value) {
              nested.push({
                name: key,
                props: objectToProperties(value)
              });
            });

            return me.nestedSectionTpl.apply({
              name: name,
              nested: nested
            });
          }
        }
    );

    me.callParent(arguments);
  },

  /**
   * Update the system information display.
   *
   * @public
   */
  setInfo: function(info) {
    this.mainTpl.overwrite(this.body, info);
  }

});
