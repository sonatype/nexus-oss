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
/*global Ext*/

/**
 * Visual style sheet for the application.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.Styles', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-dev-styles',
  mixins: {
    logAware: 'NX.LogAware'
  },

  title: 'Styles',

  layout: {
    type: 'vbox',
    padding: 4,
    defaultMargins: {top: 0, right: 0, bottom: 4, left: 0}
  },

  defaults: {
    width: '100%'
  },

  /**
   * @protected
   */
  initComponent: function () {
    var me = this;

    // build guide components on activate as this is a heavy view
    me.on('activate', function() {
      Ext.each(me.buildItems(), function(it) {
        me.add(it);
      });
    });
    // and destroy on deactivate to save memory
    me.on('deactivate', function() {
      me.removeAll(true);
    });

    me.callParent();
  },

  /**
   * @private
   */
  buildItems: function () {
    var me = this,
        items = [];

    me.logDebug('Building visual guide components');

    // Convert strings to { html: '' } objects
    function sanitizeArguments(args) {
      var items = [],
          i;

      for (i in args) {
        if (args[i] instanceof Object) {
          items.push(args[i])
        } else {
          items.push({html: args[i]})
        }
      }

      return items;
    }

    // Create a new section in the visual style guide
    function styleSection(name) {
      var content = sanitizeArguments(Array.prototype.slice.call(arguments, 1));
      var items = [];

      items.push( { xtype: 'label', text: name, cls: 'category-title' } );

      Ext.each(content, function(it) {
        items.push(it);
      });

      return {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: items
      }
    }

    // Create a horizontal row of containers
    var rowTemplate = Ext.create('Ext.XTemplate',
      '<div>',
        '<tpl for=".">',
          '<div class="hbox">{.}</div>',
        '</tpl>',
      '</div>'
    );

    // Create a vertical column of containers
    var columnTemplate = Ext.create('Ext.XTemplate',
      '<div>',
        '<tpl for=".">',
          '<div class="vbox">{.}</div>',
        '</tpl>',
      '</div>'
    );

    // Create a table
    var tableTemplate = Ext.create('Ext.XTemplate',
      '<table cellpadding="5">',
        '<thead>{thead}</thead>',
        '<tbody>{tbody}</tbody>',
      '</table>'
    );

    // Create a table head
    var theadTemplate = Ext.create('Ext.XTemplate',
      '<tpl for=".">',
        '<th>{.}</th>',
      '</tpl>'
    );

    // Create a table body
    var tbodyTemplate = Ext.create('Ext.XTemplate',
      '<tpl foreach=".">',
        '<tr>',
          '<td>{$}</td>',
          '<tpl for=".">',
            '<tpl if="clz">',
              '<td class="{clz}">{text}</td>',
            '<tpl else>',
              '<td>{.}</td>',
            '</tpl>',
          '</tpl>',
        '</tr>',
      '</tpl>'
    );

    // Create a horizontal row of containers
    function styleRow() {
      if (arguments.length == 1) {
        return arguments[0]
      }

      return {
        xtype: 'container',

        layout: {
          type: 'hbox',
          defaultMargins: '0 20px 20px 0'
        },

        items: sanitizeArguments(Array.prototype.slice.call(arguments, 0))
      }
    }

    // Create a vertical column of containers
    function styleColumn() {
      if (arguments.length == 1) {
        return arguments[0]
      }

      return {
        xtype: 'container',

        layout: {
          type: 'vbox',
          defaultMargins: '0 0 20px 0'
        },

        items: sanitizeArguments(Array.prototype.slice.call(arguments, 0))
      }
    }

    // Create a table of containers
    function styleTable(columns) {
      if (arguments.length == 2) {
        return arguments[1]
      }

      return {
        xtype: 'container',

        layout: {
          type: 'table',
          columns: columns
        },

        defaults: {
          bodyStyle: 'padding: 0 20px 20px 0'
        },

        items: sanitizeArguments(Array.prototype.slice.call(arguments, 1))
      }
    }

    // Create a label
    var labelTemplate = Ext.create('Ext.XTemplate',
      '<span class="{clz}">{text}</span>'
    );

    /*
     * Logo
     */
    items.push(
      styleSection('Logo',
        styleRow(
          toolbarBlock(true)
        )
      )
    );

    /*
     * Colors
     */

    var paletteTemplate = Ext.create('Ext.XTemplate',
      '<div style="margins: 0 20px 20px 0">',
        '<tpl for="."><div style="float: left;">{.}</div></tpl>',
      '</div>'
    );

    var colorTemplate = Ext.create('Ext.XTemplate',
      '<div>',
        '<div height="40" width="80" class="{clz}"></div>',
        '<div>{name}</div>',
        '<div>{value}</div>',
      '</div>'
    );

    items.push(
      styleSection('Colors',
        columnTemplate.apply([
          labelTemplate.apply({text: 'Shell', clz: 'section-header' }),
          paletteTemplate.apply([
            colorTemplate.apply({clz: 'color black', name: 'Black', value: '#000000'}),
            colorTemplate.apply({clz: 'color night-rider', name: 'Night Rider', value: '#333333'}),
            colorTemplate.apply({clz: 'color charcoal', name: 'Charcoal', value: '#444444'}),
            colorTemplate.apply({clz: 'color dark-gray', name: 'Dark Gray', value: '#777777'}),
            colorTemplate.apply({clz: 'color gray', name: 'Gray', value: '#AAAAAA'}),
            colorTemplate.apply({clz: 'color light-gray', name: 'Light Gray', value: '#CBCBCB'}),
            colorTemplate.apply({clz: 'color gainsboro', name: 'Gainsboro', value: '#DDDDDD'}),
            colorTemplate.apply({clz: 'color smoke', name: 'Smoke', value: '#EBEBEB'}),
            colorTemplate.apply({clz: 'color light-smoke', name: 'Light Smoke', value: '#F4F4F4'}),
            colorTemplate.apply({clz: 'color white', name: 'White', value: '#FFFFFF'})
          ])
        ]),
        rowTemplate.apply([
          columnTemplate.apply([
            labelTemplate.apply({text: 'Severity', clz: 'section-header' }),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color cerise', name: 'Cerise', value: '#DB2852'}),
              colorTemplate.apply({clz: 'color sun', name: 'Sun', value: '#F2862F'}),
              colorTemplate.apply({clz: 'color energy-yellow', name: 'Energy Yellow', value: '#F5C649'}),
              colorTemplate.apply({clz: 'color cobalt', name: 'Cobalt', value: '#0047B2'}),
              colorTemplate.apply({clz: 'color cerulean-blue', name: 'Cerulean Blue', value: '#2476C3'})
            ])
          ]),
          columnTemplate.apply([
            labelTemplate.apply({text: 'Forms', clz: 'section-header' }),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color citrus', name: 'Citrus', value: '#84C900'}),
              colorTemplate.apply({clz: 'color free-speech-red', name: 'Free Speech Red', value: '#C70000'})
            ])
          ]),
          columnTemplate.apply([
            labelTemplate.apply({text: 'Tooltip', clz: 'section-header' }),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color energy-yellow', name: 'Energy Yellow', value: '#F5C649'}),
              colorTemplate.apply({clz: 'color floral-white', name: 'Floral White', value: '#FFFAEE'})
            ])
          ])
        ]),
        columnTemplate.apply([
          labelTemplate.apply({text: 'Dashboard', clz: 'section-header' }),
          paletteTemplate.apply([
            colorTemplate.apply({clz: 'color pigment-green', name: 'Pigment Green', value: '#0B9743'}),
            colorTemplate.apply({clz: 'color madang', name: 'Madang', value: '#B6E9AB'}),
            colorTemplate.apply({clz: 'color venetian-red', name: 'Venetian Red', value: '#BC0430'}),
            colorTemplate.apply({clz: 'color beauty-bush', name: 'Beauty Bush', value: '#EDB2AF'}),
            colorTemplate.apply({clz: 'color navy-blue', name: 'Navy Blue', value: '#006BBF'}),
            colorTemplate.apply({clz: 'color cornflower', name: 'Cornflower', value: '#96CAEE'}),
            colorTemplate.apply({clz: 'color east-side', name: 'East Side', value: '#B087B9'}),
            colorTemplate.apply({clz: 'color blue-chalk', name: 'Blue Chalk', value: '#DAC5DF'})
          ])
        ]),
        rowTemplate.apply([
          columnTemplate.apply([
            labelTemplate.apply({text: 'Buttons', clz: 'section-header' }),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color white', name: 'White', value: '#FFFFFF'}),
              colorTemplate.apply({clz: 'color light-gainsboro', name: 'Light Gainsboro', value: '#E6E6E6'}),
              colorTemplate.apply({clz: 'color light-gray', name: 'Light Gray', value: '#CBCBCB'}),
              colorTemplate.apply({clz: 'color silver', name: 'Silver', value: '#B8B8B8'}),
              colorTemplate.apply({clz: 'color suva-gray', name: 'Suva Gray', value: '#919191'}),
              colorTemplate.apply({clz: 'color gray', name: 'Gray', value: '#808080'})
            ]),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color denim', name: 'Denim', value: '#197AC5'}),
              colorTemplate.apply({clz: 'color light-cobalt', name: 'Light Cobalt', value: '#0161AD'}),
              colorTemplate.apply({clz: 'color dark-denim', name: 'Dark Denim', value: '#14629E'}),
              colorTemplate.apply({clz: 'color smalt', name: 'Smalt', value: '#014E8A'}),
              colorTemplate.apply({clz: 'color dark-cerulean', name: 'Dark Cerulean', value: '#0F4976'}),
              colorTemplate.apply({clz: 'color prussian-blue', name: 'Prussian Blue', value: '#013A68'})
            ]),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color light-cerise', name: 'Light Cerise', value: '#DE3D63'}),
              colorTemplate.apply({clz: 'color brick-red', name: 'Brick Red', value: '#C6254B'}),
              colorTemplate.apply({clz: 'color old-rose', name: 'Old Rose', value: '#B2314F'}),
              colorTemplate.apply({clz: 'color fire-brick', name: 'Fire Brick', value: '#9E1E3C'}),
              colorTemplate.apply({clz: 'color shiraz', name: 'Shiraz', value: '#85253B'}),
              colorTemplate.apply({clz: 'color falu-red', name: 'Falu Red', value: '#77162D'})
            ]),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color sea-buckthorn', name: 'Sea Buckthorn', value: '#F39244'}),
              colorTemplate.apply({clz: 'color tahiti-gold', name: 'Tahiti Gold', value: '#DA792B'}),
              colorTemplate.apply({clz: 'color zest', name: 'Zest', value: '#C17536'}),
              colorTemplate.apply({clz: 'color rich-gold', name: 'Rich Gold', value: '#AE6122'}),
              colorTemplate.apply({clz: 'color afghan-tan', name: 'Afghan Tan', value: '#925829'}),
              colorTemplate.apply({clz: 'color russet', name: 'Russet', value: '#83491A'})
            ]),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color elf-green', name: 'Elf Green', value: '#23A156'}),
              colorTemplate.apply({clz: 'color dark-pigment-green', name: 'Dark Pigment Green', value: '#0B893D'}),
              colorTemplate.apply({clz: 'color salem', name: 'Salem', value: '#1C8145'}),
              colorTemplate.apply({clz: 'color jewel', name: 'Jewel', value: '#096E31'}),
              colorTemplate.apply({clz: 'color fun-green', name: 'Fun Green', value: '#156134'}),
              colorTemplate.apply({clz: 'color dark-jewel', name: 'Dark Jewel', value: '#0C4F26'})
            ])
          ]),
          columnTemplate.apply([
            labelTemplate.apply({text: 'Font Awesome Icons', clz: 'section-header' }),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color navy-blue', name: 'Navy Blue', value: '#006BBF'}),
              colorTemplate.apply({clz: 'color smalt', name: 'Smalt', value: '#014E8A'}),
              colorTemplate.apply({clz: 'color prussian-blue', name: 'Prussian Blue', value: '#013A68'})
            ]),
            paletteTemplate.apply([
              colorTemplate.apply({clz: 'color white', name: 'White', value: '#FFFFFF'}),
              colorTemplate.apply({clz: 'color gainsboro', name: 'Gainsboro', value: '#DDDDDD'}),
              colorTemplate.apply({clz: 'color gray', name: 'Gray', value: '#AAAAAA'})
            ])
          ])
        ])
      )
    );

    /*
     * Fonts
     */

    var fontTemplate = Ext.create('Ext.XTemplate',
      '<div>',
        '<span class="section-header">{text}</span>',
        '<p class="{clz}">Trusted applications at the speed of deployment<br/>abcdefghijklmnopqrstuvwxyz<br/>ABCDEFGHIJKLMNOPQRSTUVWXYZ<br/>,1234567890?¿¡;.:*@#£$%&/()=[]+</p>',
      '</div>'
    );

    items.push(
      styleSection('Fonts',
        rowTemplate.apply([
          fontTemplate.apply({text: 'Proxima Nova Regular', clz: 'proxima-nova-regular'}),
          fontTemplate.apply({text: 'Proxima Nova Bold', clz: 'proxima-nova-bold'}),
          fontTemplate.apply({text: 'Courier New', clz: 'courier-new-regular'})
        ])
      )
    );

    /*
     * Type Styles
     */

    items.push(
      styleSection('Type Styles',
        tableTemplate.apply({
          thead: theadTemplate.apply(['Name', 'Description', 'Font & Weight', 'Use Cases', 'Pixels', 'Sample']),
          tbody: tbodyTemplate.apply({
            'h1': [
              'Header', 'Proxima Nova Light', 'Logo', '20', { text: 'Sonatype Nexus', clz: 'sample-h1' }
             ],
            'h2': [
              'Header', 'Proxima Nova Bold', 'Page Title', '26', { text: 'Development', clz: 'sample-h2' }
            ],
            'h3': [
              'Header', 'Proxima Nova Bold', 'Header', '22', { text: 'Development', clz: 'sample-h3' }
            ],
            'h4': [
              'Header', 'Proxima Nova Bold', 'Sub-Header', '18', { text: 'Development', clz: 'sample-h4' }
            ],
            'h5': [
              'Header', 'Proxima Nova Bold', 'Sub-Header', '13', { text: 'Development', clz: 'sample-h5' }
            ],
            'p/ul/ol': [
              'Body', 'Proxima Nova Regular', 'Body text, lists, default size', '13', { text: 'Development', clz: 'sample-body' }
            ],
            'code': [
              'Code', 'Courier New Regular', 'Code examples', '13', { text: 'Development', clz: 'sample-code' }
            ],
            'utility': [
              'Small Text', 'Proxima Nova Regular', 'Labels, Side-Nav', '10', { text: 'Development', clz: 'sample-utility' }
            ]
          })
        })
      )
    );

    /*
     * Buttons
     */

    var sampleTemplate = Ext.create('Ext.XTemplate',
      '<table>',
        '<tpl for=".">',
          '<tr>',
            '<td><div class="color {.}"></div></td>',
            '<td><div style="padding: 0 10px 0 0">$color-{.}</div></td>',
          '</tr>',
        '</tpl>',
      '</table>'
    );

    function buttonBlock(ui, text, disabled, pressed, menu) {
      var button = { xtype: 'button', text: text, ui: ui, margin: "0 10 10 0" };

      // Initialize optional button parameters
      if (disabled) {
        button['disabled'] = true;
      }
      if (pressed) {
        button['pressed'] = true;
        button['enableToggle'] = true;
      }
      if (menu) {
        button['menu'] = [
          { text: 'First' },
          '-',
          { text: 'Second' }
        ];
      } else {
        button['glyph'] = 'xf036@FontAwesome';
      }

      return button;
    }

    items.push(
      styleSection('Buttons',
        styleTable(3,

          // Default buttons
          styleColumn(
            { xtype: 'label', text: 'Default', cls: 'section-header' },
            styleRow(
              styleColumn(
                buttonBlock('default', 'Default', false, false, false),
                buttonBlock('default', 'Default', true, false, false),
                buttonBlock('default', 'Default', false, false, true)
              ),
              sampleTemplate.apply(['white', 'light-gainsboro', 'light-gray', 'silver', 'suva-gray', 'gray'])
            )
          ),

          // Plain buttons
          styleColumn(
            { xtype: 'label', text: 'Plain', cls: 'section-header' },
            styleRow(
              styleColumn(
                buttonBlock('plain', 'Plain', false, false, false),
                buttonBlock('plain', 'Plain', true, false, false),
                buttonBlock('plain', 'Plain', false, false, true)
              ),
              sampleTemplate.apply(['white', 'light-gainsboro', 'light-gray', 'silver', 'suva-gray', 'gray'])
            )
          ),

          // Primary buttons
          styleColumn(
            { xtype: 'label', text: 'Primary', cls: 'section-header' },
            styleRow(
              styleColumn(
                buttonBlock('primary', 'Primary', false, false, false),
                buttonBlock('primary', 'Primary', true, false, false),
                buttonBlock('primary', 'Primary', false, false, true)
              ),
              sampleTemplate.apply(['denim', 'light-cobalt', 'dark-denim', 'smalt', 'dark-cerulean', 'prussian-blue'])
            )
          ),

          // Danger buttons
          styleColumn(
            { xtype: 'label', text: 'Danger', cls: 'section-header' },
            styleRow(
              styleColumn(
                buttonBlock('danger', 'Danger', false, false, false),
                buttonBlock('danger', 'Danger', true, false, false),
                buttonBlock('danger', 'Danger', false, false, true)
              ),
              sampleTemplate.apply(['light-cerise', 'brick-red', 'old-rose', 'fire-brick', 'shiraz', 'falu-red'])
            )
          ),

          // Warning buttons
          styleColumn(
            { xtype: 'label', text: 'Warning', cls: 'section-header' },
            styleRow(
              styleColumn(
                buttonBlock('warning', 'Warning', false, false, false),
                buttonBlock('warning', 'Warning', true, false, false),
                buttonBlock('warning', 'Warning', false, false, true)
              ),
              sampleTemplate.apply(['sea-buckthorn', 'tahiti-gold', 'zest', 'rich-gold', 'afghan-tan', 'russet'])
            )
          ),

          // Success buttons
          styleColumn(
            { xtype: 'label', text: 'Success', cls: 'section-header' },
            styleRow(
              styleColumn(
                buttonBlock('success', 'Success', false, false, false),
                buttonBlock('success', 'Success', true, false, false),
                buttonBlock('success', 'Success', false, false, true)
              ),
              sampleTemplate.apply(['elf-green', 'dark-pigment-green', 'salem', 'jewel', 'fun-green', 'dark-jewel'])
            )
          )
        )
      )
    );

    //
    // Forms
    //

    items.push(
        styleSection('Forms',
            styleRow(
                {
                  xtype: 'form',
                  items: [
                    { xtype: 'textfield', value: 'Text Input', allowBlank: false, fieldLabel: '[Label]', helpText: '[Optional description text]', width: 200 },
                    { xtype: 'textarea', value: 'Text Input', allowBlank: false, fieldLabel: '[Label]', helpText: '[Optional description text]', width: 200 },
                    { xtype: 'checkbox', boxLabel: 'Checkbox', checked: true, fieldLabel: null, helpText: null },
                    { xtype: 'radio', boxLabel: 'Radio Button', checked: true, fieldLabel: null, helpText: null }
                  ],
                  buttons: [
                    { text: 'Submit', ui: 'primary' },
                    { text: 'Discard' }
                  ]
                }
            )
        )
    );

    /*
     * Notification messages
     */

    function notificationWindow(type) {
      var style = 'message-' + type;
      return {
        xtype: 'window',
        ui: style,
        iconCls: NX.Icons.cls(style, 'x16'),
        title: type,
        html: "ui: '" + style + "'",
        hidden: false,
        collapsible: false,
        floating: false,
        closable: false,
        draggable: false,
        resizable: false,
        width: 200
      };
    }

    items.push(
      styleSection('Notifications',
        styleRow(
          notificationWindow('default'),
          notificationWindow('primary'),
          notificationWindow('danger'),
          notificationWindow('warning'),
          notificationWindow('success')
        )
      )
    );

    /*
     * Modals
     */

    function modalBlock(xtype) {
      return {
        xtype: xtype,
        hidden: false,
        collapsible: false,
        floating: false,
        closable: false,
        draggable: false,
        resizable: false,
        cls: 'fixed-modal'
      }
    }

    items.push(
      styleSection('Modals',
        styleRow(
          {
            xtype: 'window',

            title: 'Sign In',

            hidden: false,
            collapsible: false,
            floating: false,
            closable: false,
            draggable: false,
            resizable: false,
            width: 320,
            cls: 'fixed-modal',

            items: {
              xtype: 'form',
              bodyPadding: 10,
              defaultType: 'textfield',
              defaults: {
                labelAlign: 'left',
                labelWidth: 100,
                anchor: '100%'
              },
              items: [
                {
                  name: 'username',
                  itemId: 'username',
                  fieldLabel: 'Username',
                  emptyText: 'enter your username',
                  allowBlank: false,
                  validateOnBlur: false // allow cancel to be clicked w/o validating this to be non-blank
                },
                {
                  name: 'password',
                  itemId: 'password',
                  fieldLabel: 'Password',
                  inputType: 'password',
                  emptyText: 'enter your password',
                  allowBlank: false,
                  validateOnBlur: false // allow cancel to be clicked w/o validating this to be non-blank
                },
                {
                  xtype: 'checkbox',
                  fieldLabel: 'Remember me',
                  name: 'remember'
                }
              ],

              buttonAlign: 'left',
              buttons: [
                { text: 'Sign In', formBind: true, bindToEnter: true, ui: 'primary' },
                { text: 'Cancel', scope: me }
              ]
            }
          },
          {
            xtype: 'window',

            title: 'Session',

            hidden: false,
            collapsible: false,
            floating: false,
            closable: false,
            draggable: false,
            resizable: false,
            width: 320,
            cls: 'fixed-modal',

            items: [
              {
                xtype: 'label',
                id: 'expire',
                text: 'Session is about to expire',
                style: {
                  'color': 'red',
                  'font-size': '20px',
                  'margin': '10px'
                }
              }
            ],
            buttons: [
              { text: 'Cancel' }
            ]
          }
        )
      )
    );

    /*
     * Menu
     */

    function menuItemBlock(text, iconCls, tooltip, action) {
      return {
        text: text,
        iconCls: iconCls,
        tooltip: tooltip,
        action: action
      }
    }

    items.push(
      styleSection('Menu',
        styleRow(
          {
            xtype: 'menu', floating: false, items: [
              menuItemBlock('Help for [Feature]', 'nx-icon-search-default-x16', 'Help for the current feature', 'feature'),
              '-',
              menuItemBlock('About', 'nx-icon-nexus-x16', 'About Sonatype Nexus', 'about'),
              menuItemBlock('Documentation', 'nx-icon-help-manual-x16', 'Sonatype Nexus product documentation', 'docs'),
              menuItemBlock('Knowledge Base', 'nx-icon-help-kb-x16', 'Sonatype Nexus knowledge base', 'kb'),
              menuItemBlock('Community', 'nx-icon-help-community-x16', 'Sonatype Nexus community information', 'community'),
              menuItemBlock('Issue Tracker', 'nx-icon-help-issues-x16', 'Sonatype Nexus issue and bug tracker', 'issues'),
              '-',
              menuItemBlock('Support', 'nx-icon-help-support-x16', 'Sonatype Nexus product support', 'support')
            ]
          }
        )
      )
    );

    /*
     * Header
     */

    function toolbarBlock(logoOnly) {
      var items = [];
      var logo = [];

      logo.push( { xtype: 'label', text: 'Sonatype Nexus', cls: 'nx-header-productname' } );

      if (!logoOnly) {
        logo.push(
          {
            xtype: 'label',
            text: NX.State.getEdition() + ' ' + NX.State.getVersion(),
            cls: 'nx-header-productversion',
            style: {
              'padding-left': '8px'
            }
          }
        )
      }

      items.push( { xtype: 'nx-header-logo' } );
      items.push( { xtype: 'container', items: logo } );

      if (!logoOnly) {
        items.push(
          ' ', ' ', // 2x pad
          {
            xtype: 'button',
            ui: 'header',
            cls: 'modebutton',
            toggleGroup: 'examplemode',
            title: 'Browse',
            tooltip: 'Browse server contents',
            glyph: 'xf1b2@FontAwesome' /* fa-cube */
          },
          {
            xtype: 'button',
            ui: 'header',
            cls: 'modebutton',
            toggleGroup: 'examplemode',
            title: 'Administration',
            tooltip: 'Server administration and configuration',
            glyph: 'xf013@FontAwesome' /* fa-gear */
          },
          ' ',
          {
            xtype: 'nx-searchbox',
            cls: 'quicksearch',
            width: 200,
            emptyText: 'Search…',
            inputAttrTpl: "data-qtip='Quick component keyword search'" // field tooltip
          },
          '->',
          {
            xtype: 'button',
            ui: 'header',
            glyph: 'xf0f3@FontAwesome',
            tooltip: 'Toggle messages display'
          },
          {
            xtype: 'button',
            ui: 'header',
            tooltip: 'Refresh current view and data',
            glyph: 'xf021@FontAwesome' // fa-refresh
          },
          {
            xtype: 'button',
            ui: 'header',
            text: 'Sign In',
            tooltip: 'Have an account?',
            glyph: 'xf090@FontAwesome'
          },
          {
            xtype: 'nx-header-mode',
            ui: 'header',
            hidden: true,
            mode: 'user',
            title: 'User',
            text: 'User',
            tooltip: 'User profile and options',
            glyph: 'xf007@FontAwesome',
            autoHide: false,
            collapseMenu: false
          },
          {
            xtype: 'button',
            ui: 'header',
            tooltip: "Sign out",
            hidden: true,
            glyph: 'xf08b@FontAwesome'
          },
          {
            xtype: 'button',
            ui: 'header',
            tooltip: 'Help',
            glyph: 'xf059@FontAwesome', // fa-question-circle
            arrowCls: '', // hide the menu button arrow
            menu: [
              {
                text: 'Menu item 1'
              },
              '-',
              {
                text: 'Menu item 2'
              },
              {
                text: 'Menu item 3'
              }
            ]
          }
        )
      }

      return {
        xtype: 'toolbar',

        // set height to ensure we have uniform size and not depend on what is in the toolbar
        height: 40,

        style: {
          backgroundColor: '#000000'
        },
        anchor: '100%',
        padding: "0 0 0 16px",

        defaults: {
          scale: 'medium'
        },

        items: items
      }
    }

    items.push(
      styleSection('Header',
        styleRow(
          toolbarBlock()
        )
      )
    );

    /*
     * Tooltip
     */

    items.push(
      styleSection('Tooltip',
        styleRow(
          { xtype: 'button', text: 'Mouse over me', tooltip: 'This is a tooltip' }
        )
      )
    );

    /*
     * Tabs
     */

    items.push(
      styleSection('Tabs',
        styleRow(
          {
            xtype: 'tabpanel',

            width: 500,
            height: 60,  // FIXME: This masks problem in current style in application in style guide
            activeTab: 0,
            ui: 'light',

            header: {
              height: 30
            },

            items: [
              { title: 'Settings', bodyPadding: 10, html: 'A simple tab' },
              { title: 'Routing', html: 'Another one' },
              { title: 'Smart Proxy', html: 'Yet another' },
              { title: 'Health Check', html: 'And one more' }
            ]
          }
        )
      )
    );

    //
    // Picker
    //

    var pickerStore = Ext.create('Ext.data.ArrayStore', {
      fields: [
        'id',
        'name'
      ],
      data: [
          [ 'foo', 'Foo' ],
          [ 'bar', 'Bar' ],
          [ 'baz', 'Baz' ]
      ]
    });

    items.push(
      styleSection('Picker',
        styleRow(
          {
            xtype: 'nx-itemselector',

            name: 'realms',
            buttons: ['up', 'add', 'remove', 'down'],
            fromTitle: 'Available',
            toTitle: 'Selected',
            store: pickerStore,
            valueField: 'id',
            displayField: 'name',
            delimiter: null
          }
        )
      )
    );

    //
    // Panels
    //

    items.push(
        styleSection('Panels',
            styleRow(
                {
                  xtype: 'panel',
                  title: 'Normal',
                  height: 100,
                  width: 200,
                  items: [
                    {
                      xtype: 'container',
                      html: 'normal'
                    }
                  ]
                },
                {
                  xtype: 'panel',
                  title: 'Framed',
                  frame: true,
                  height: 100,
                  width: 200,
                  items: [
                    {
                      xtype: 'container',
                      html: 'frame: true'
                    }
                  ]
                }
            ),

            styleRow(
                {
                  xtype: 'panel',
                  title: 'Light',
                  ui: 'light',
                  height: 100,
                  width: 200,
                  items: [
                    {
                      xtype: 'container',
                      html: 'ui: light'
                    }
                  ]
                },
                {
                  xtype: 'panel',
                  title: 'Light Framed',
                  ui: 'light',
                  frame: true,
                  height: 100,
                  width: 200,
                  items: [
                    {
                      xtype: 'container',
                      html: 'ui: light, frame: true'
                    }
                  ]
                }
            )
        )
    );

    //
    // Toolbar
    //

    function toolbar(cfg) {
      var defaults = {
        xtype: 'toolbar',
        items: [
          {
            xtype: 'button',
            text: 'plain'
          },
          {
            xtype: 'button',
            text: 'with glyph',
            glyph: 'xf1b2@FontAwesome'
          },
          {
            xtype: 'button',
            text: 'with icon',
            iconCls: 'nx-icon-help-kb-x16'
          },
          ' ', // spacer
          {
            xtype: 'button',
            text: 'button menu',
            menu: [
              { text: 'plain' },
              { text: 'with glyph', glyph: 'xf059@FontAwesome' },
              { text: 'with icon', iconCls: 'nx-icon-help-kb-x16'}
            ]
          },
          '-', // seperator
          {
            xtype: 'nx-searchbox',
            width: 200
          }
        ]
      };

      return Ext.applyIf(defaults, cfg);
    }

    items.push(
        styleSection('Toolbar',
            styleRow(
              'normal'
            ),
            styleRow(
              toolbar()
            ),
            styleRow(
              'medium'
            ),
            styleRow(
              toolbar({
                defaults: {
                  scale: 'medium'
                }
              })
            ),
            styleRow(
              'large'
            ),
            styleRow(
              toolbar({
                defaults: {
                  scale: 'large'
                }
              })
            )
        )
    );

    //
    // Grid
    //

    var gridStore = Ext.create('Ext.data.ArrayStore', {
      storeId: 'gridStore',
      fields: [
        'id',
        'name'
      ],
      data: [
        [ 'foo', 'Foo' ],
        [ 'bar', 'Bar' ],
        [ 'baz', 'Baz' ]
      ]
    });

    items.push(
        styleSection('Grid',
            styleRow(
                {
                  xtype: 'grid',
                  store: gridStore,
                  height: 200,
                  width: 200,
                  columns: [
                    { text: 'ID', dataIndex: 'id' },
                    { text: 'Name', dataIndex: 'name' }
                  ]
                }
            )
        )
    );

    return items;
  }
});