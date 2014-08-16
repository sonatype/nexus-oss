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
 * Analytics Event grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.analytics.AnalyticsEventList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-analytics-event-list',
  requires: [
    'Ext.XTemplate'
  ],

  store: 'AnalyticsEvent',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function (value, meta, record) {
        var type = record.get('type');
        switch (type) {
          case 'REST':
            return 'analyticsevent-rest';
          case 'Ext.Direct':
            return 'analyticsevent-ui';
          default:
            return 'analyticsevent-default';
        }
      }
    },
    {
      header: 'Type',
      dataIndex: 'type',
      flex: 1,
      tooltip: 'Event type'
    },
    {
      header: 'Timestamp',
      dataIndex: 'timestamp',
      flex: 1,
      tooltip: 'Event timestamp in milliseconds'
    },
    {
      header: 'Sequence',
      dataIndex: 'sequence',
      flex: 1,
      tooltip: 'Event sequence'
    },
    {
      header: 'Duration',
      dataIndex: 'duration',
      flex: 1,
      tooltip: 'Event duration in nanoseconds'
    },
    {
      header: 'User',
      dataIndex: 'userId',
      flex: 1,
      tooltip: 'Event user identifier.  This value is anonymized when exporting and submitting'
    },
    {
      header: 'Attributes',
      dataIndex: 'attributes',
      flex: 3,
      tooltip: 'Event attributes specific to the event type',
      renderer: function (value) {
        var text = '';
        Ext.Object.each(value, function (name, value) {
          if (text !== '') {
            text += ', ';
          }
          text += name + '=' + value;
        });
        return text;
      }
    }
  ],

  tbar: [
    {
      xtype: 'button',
      text: 'Clear',
      tooltip: 'Clear all event data',
      glyph: 'xf056@FontAwesome' /* fa-minus-circle */,
      action: 'clear',
      disabled: true
    },
    {
      xtype: 'button',
      text: 'Export',
      tooltip: 'Export and download event data',
      glyph: 'xf019@FontAwesome' /* fa-download */,
      action: 'export'
    },
    '-',
    {
      xtype: 'button',
      text: 'Submit',
      tooltip: 'Submit event data to Sonatype',
      glyph: 'xf0ee@FontAwesome' /* fa-cloud-upload */,
      action: 'submit',
      disabled: true
    }
  ],

  dockedItems: [
    {
      xtype: 'pagingtoolbar',
      store: 'AnalyticsEvent',
      dock: 'bottom',
      displayInfo: true,
      displayMsg: 'Displaying events {0} - {1} of {2}',
      emptyMsg: 'No events to display'
    }
  ],

  plugins: [
    {
      ptype: 'rowexpander',
      rowBodyTpl: Ext.create('Ext.XTemplate',
          '<table style="padding: 5px;">',
          '<tpl for="this.attributes(values)">',
          '<tr>',
          '<td class="x-selectable" style="padding-right: 5px;"><b>{name}</b></td>',
          '<td class="x-selectable">{value}</td>',
          '</tr>',
          '</tpl>',
          '</table>',
          {
            compiled: true,

            /**
             * Convert attributes field to array of name/value pairs for rendering in template.
             */
            attributes: function (values) {
              var result = [];
              Ext.iterate(values.attributes, function (name, value) {
                result.push({ name: name, value: value });
              });
              return result;
            }
          })
    },
    { ptype: 'gridfilterbox', emptyText: 'No analytics event matched criteria "$filter"' }
  ]

});
