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
    var me = this, i = 0;

    me.items = [];

    // Create a new section in the visual style guide
    function styleSection(name) {
      var content = sanitizeArguments(Array.prototype.slice.call(arguments, 1));
      var items = [];

      items.push( { xtype: 'label', text: name, cls: 'category-title' } );

      for (i in content) {
        items.push(content[i]);
      }

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

    // Convert strings to { html: '' } objects
    function sanitizeArguments(args) {
      var items = [];

      for (i in args) {
        if (args[i] instanceof Object) {
          items.push(args[i])
        } else {
          items.push({html: args[i]})
        }
      }

      return items;
    }

    // Create a horizontal row of containers
    function styleRow() {
      if (arguments.length == 1) {
        return arguments[0]
      }

      return {
        xtype: 'container',

        layout: {
          type: 'hbox',
          defaultMargins: '0 20px 0 0'
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

    /*
     * Logo
     */
    me.items.push(
      styleSection('Logo',
        styleRow(
          toolbarBlock(true)
        )
      )
    );

    /*
     * Colors
     */

    function colorPalette() {
      return {
        xtype: 'container',

        layout: {
          type: 'vbox'
        },

        margins: '0 20px 20px 0',

        items: [
          {
            xtype: 'container',

            layout: {
              type: 'hbox'
            },

            items: sanitizeArguments(Array.prototype.slice.call(arguments, 0))
          }
        ]
      }
    }

    function colorBlock(classes, name, value) {
      return {
        xtype: 'container',

        layout: {
          type: 'vbox'
        },

        items: [
          { xtype: 'container', height: 40, width: 80, cls: classes },
          { xtype: 'label', text: name },
          { xtype: 'label', text: value }
        ]
      }
    }

    me.items.push(
      styleSection('Colors',
        styleColumn(
          { xtype: 'label', text: 'Shell', cls: 'section-header' },
          colorPalette(
            colorBlock('color black', 'Black', '#000000'),
            colorBlock('color night-rider', 'Night Rider', '#333333'),
            colorBlock('color charcoal', 'Charcoal', '#444444'),
            colorBlock('color dark-gray', 'Dark Gray', '#777777'),
            colorBlock('color gray', 'Gray', '#AAAAAA'),
            colorBlock('color light-gray', 'Light Gray', '#CBCBCB'),
            colorBlock('color gainsboro', 'Gainsboro', '#DDDDDD'),
            colorBlock('color smoke', 'Smoke', '#EBEBEB'),
            colorBlock('color light-smoke', 'Light Smoke', '#F4F4F4'),
            colorBlock('color white', 'White', '#FFFFFF')
          )
        ),
        styleRow(
          styleColumn(
            { xtype: 'label', text: 'Severity', cls: 'section-header' },
            colorPalette(
              colorBlock('color cerise', 'Cerise', '#DB2852'),
              colorBlock('color sun', 'Sun', '#F2862F'),
              colorBlock('color energy-yellow', 'Energy Yellow', '#F5C649'),
              colorBlock('color cobalt', 'Cobalt', '#0047B2'),
              colorBlock('color cerulean-blue', 'Cerulean Blue', '#2476C3')
            )
          ),
          styleColumn(
            { xtype: 'label', text: 'Forms', cls: 'section-header' },
            colorPalette(
              colorBlock('color citrus', 'Citrus', '#84C900'),
              colorBlock('color free-speech-red', 'Free Speech Red', '#C70000')
            )
          ),
          styleColumn(
            { xtype: 'label', text: 'Tooltip', cls: 'section-header' },
            colorPalette(
              colorBlock('color energy-yellow', 'Energy Yellow', '#F5C649'),
              colorBlock('color floral-white', 'Floral White', '#FFFAEE')
            )
          )
        ),
        styleColumn(
          { xtype: 'label', text: 'Dashboard', cls: 'section-header' },
          colorPalette(
            colorBlock('color pigment-green', 'Pigment Green', '#0B9743'),
            colorBlock('color madang', 'Madang', '#B6E9AB'),
            colorBlock('color venetian-red', 'Venetian Red', '#BC0430'),
            colorBlock('color beauty-bush', 'Beauty Bush', '#EDB2AF'),
            colorBlock('color navy-blue', 'Navy Blue', '#006BBF'),
            colorBlock('color cornflower', 'Cornflower', '#96CAEE'),
            colorBlock('color east-side', 'East Side', '#B087B9'),
            colorBlock('color blue-chalk', 'Blue Chalk', '#DAC5DF')
          )
        ),
        styleRow(
          styleColumn(
            { xtype: 'label', text: 'Buttons', cls: 'section-header' },
            colorPalette(
              colorBlock('color white', 'White', '#FFFFFF'),
              colorBlock('color light-gainsboro', 'Light Gainsboro', '#E6E6E6'),
              colorBlock('color light-gray', 'Light Gray', '#CBCBCB'),
              colorBlock('color silver', 'Silver', '#B8B8B8'),
              colorBlock('color suva-gray', 'Suva Gray', '#919191'),
              colorBlock('color gray', 'Gray', '#808080')
            ),
            colorPalette(
              colorBlock('color denim', 'Denim', '#197AC5'),
              colorBlock('color light-cobalt', 'Light Cobalt', '#0161AD'),
              colorBlock('color dark-denim', 'Dark Denim', '#14629E'),
              colorBlock('color smalt', 'Smalt', '#014E8A'),
              colorBlock('color dark-cerulean', 'Dark Cerulean', '#0F4976'),
              colorBlock('color prussian-blue', 'Prussian Blue', '#013A68')
            ),
            colorPalette(
              colorBlock('color light-cerise', 'Light Cerise', '#DE3D63'),
              colorBlock('color brick-red', 'Brick Red', '#C6254B'),
              colorBlock('color old-rose', 'Old Rose', '#B2314F'),
              colorBlock('color fire-brick', 'Fire Brick', '#9E1E3C'),
              colorBlock('color shiraz', 'Shiraz', '#85253B'),
              colorBlock('color falu-red', 'Falu Red', '#77162D')
            ),
            colorPalette(
              colorBlock('color sea-buckthorn', 'Sea Buckthorn', '#F39244'),
              colorBlock('color tahiti-gold', 'Tahiti Gold', '#DA792B'),
              colorBlock('color zest', 'Zest', '#C17536'),
              colorBlock('color rich-gold', 'Rich Gold', '#AE6122'),
              colorBlock('color afghan-tan', 'Afghan Tan', '#925829'),
              colorBlock('color russet', 'Russet', '#83491A')
            ),
            colorPalette(
              colorBlock('color elf-green', 'Elf Green', '#23A156'),
              colorBlock('color dark-pigment-green', 'Dark Pigment Green', '#0B893D'),
              colorBlock('color salem', 'Salem', '#1C8145'),
              colorBlock('color jewel', 'Jewel', '#096E31'),
              colorBlock('color fun-green', 'Fun Green', '#156134'),
              colorBlock('color dark-jewel', 'Dark Jewel', '#0C4F26')
            )
          ),
          styleColumn(
            { xtype: 'label', text: 'Font Awesome Icons', cls: 'section-header' },
            colorPalette(
              colorBlock('color navy-blue', 'Navy Blue', '#006BBF'),
              colorBlock('color smalt', 'Smalt', '#014E8A'),
              colorBlock('color prussian-blue', 'Prussian Blue', '#013A68')
            ),
            colorPalette(
              colorBlock('color white', 'White', '#FFFFFF'),
              colorBlock('color gainsboro', 'Gainsboro', '#DDDDDD'),
              colorBlock('color gray', 'Gray', '#AAAAAA')
            )
          )
        )
      )
    );

    /*
     * Fonts
     */

    function fontBlock(name, classes) {
      return {
        xtype: 'container',

        layout: {
          type: 'vbox'
        },

        padding: "0 8px 4x 0",

        items: [
          {
            xtype: 'label',
            text: name,
            cls: 'section-header'
          },
          {
            xtype: 'container',
            cls: classes,

            layout: {
              type: 'vbox'
            },

            items: [
              { xtype: 'label', text: 'Trusted applications at the speed of deployment' },
              { xtype: 'label', text: 'abcdefghijklmnopqrstuvwxyz' },
              { xtype: 'label', text: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' },
              { xtype: 'label', text: '1234567890?¿¡;.:*@#£$%&/()=[]+'}
            ]
          }
        ]
      }
    }

    me.items.push(
      styleSection('Fonts',
        styleRow(
          fontBlock('Proxima Nova Regular', 'proxima-nova-regular'),
          fontBlock('Proxima Nova Bold', 'proxima-nova-bold'),
          fontBlock('Courier New', 'courier-new-regular')
        )
      )
    );

    /*
     * Type Styles
     */

    me.items.push(
      styleSection('Type Styles',
        styleTable(6,
          { html: 'Name', baseCls: 'column-header' },
          { html: 'Description', baseCls: 'column-header' },
          { html: 'Font & Weight', baseCls: 'column-header' },
          { html: 'Use Cases', baseCls: 'column-header' },
          { html: 'Pixels', baseCls: 'column-header' },
          { html: 'Sample', baseCls: 'column-header' },
          'h1', 'Page Title', 'Proxima Nova Light', 'Header', '40', { html: 'Sonatype Nexus', baseCls: 'sample-h1' },
          'h2', 'Header', 'Proxima Nova Bold', 'Header', '26', { html: 'Development', baseCls: 'sample-h2' },
          'h3', 'Header', 'Proxima Nova Bold', 'Header', '22', { html: 'Development', baseCls: 'sample-h3' },
          'h4', 'Sub-Header', 'Proxima Nova Bold', 'Sub-Header', '18', { html: 'Development', baseCls: 'sample-h4' },
          'h5', 'Sub-Header', 'Proxima Nova Bold', 'Sub-Header', '13', { html: 'Development', baseCls: 'sample-h5' },
          'p/ul/ol', 'Body', 'Proxima Nova Regular', 'Body text, lists, default size', '13', { html: 'Development', baseCls: 'sample-body' },
          'code', 'Code', 'Courier New Regular', 'Code examples', '13', { html: 'Development', baseCls: 'sample-code' },
          'utility', 'Small Text', 'Proxima Nova Regular', 'Labels, Side-Nav', '10', { html: 'Development', baseCls: 'sample-utility' }
        )
      )
    );

    /*
     * Buttons
     */

    function buttonBlock(ui, text, disabled, pressed, menu, gradient1, gradient2) {
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
      }

      // Add a sample section if requested
      if (gradient1 && gradient2) {
        var samples = {
            xtype: 'container',

            layout: {
              type: 'table',
              columns: 2
            },

            items: [
              { xtype: 'container', width: 10, height: 10, cls: 'color ' + gradient1 },
              { xtype: 'label', text: '$color-' + gradient1, padding: '0 10px 0 0' },
              { xtype: 'container', width: 10, height: 10, cls: 'color ' + gradient2 },
              { xtype: 'label', text: '$color-' + gradient2, padding: '0 10px 0 0' }
            ]
          };
        return {
          xtype: 'container',
          layout: { type: 'hbox' },
          items: [button, samples]
        }
      } else {
        // Return just the button otherwise
        return button
      }
    }

    me.items.push(
      styleSection('Buttons',
        styleTable(3,

          // Default buttons
          styleColumn(
            { xtype: 'label', text: 'Default', cls: 'section-header' },
            styleTable(2,
              buttonBlock('default', 'Default', false, false, false, 'white', 'light-gainsboro'),
              buttonBlock('default', 'Default', true, false, false),
              buttonBlock('default', 'Default', false, false, false, 'light-gray', 'silver'),
              buttonBlock('default', 'Default', false, false, false),
              buttonBlock('default', 'Default', false, true, false, 'suva-gray', 'gray'),
              buttonBlock('default', 'Default', false, false, true)
            )
          ),

          // Primary buttons
          styleColumn(
            { xtype: 'label', text: 'Primary', cls: 'section-header' },
            styleTable(2,
              buttonBlock('primary', 'Primary', false, false, false, 'denim', 'light-cobalt'),
              buttonBlock('primary', 'Primary', true, false, false),
              buttonBlock('primary', 'Primary', false, false, false, 'dark-denim', 'smalt'),
              buttonBlock('primary', 'Primary', false, false, false),
              buttonBlock('primary', 'Primary', false, true, false, 'dark-cerulean', 'prussian-blue'),
              buttonBlock('primary', 'Primary', false, false, true)
            )
          ),

          // Danger buttons
          styleColumn(
            { xtype: 'label', text: 'Danger', cls: 'section-header' },
            styleTable(2,
              buttonBlock('danger', 'Danger', false, false, false, 'light-cerise', 'brick-red'),
              buttonBlock('danger', 'Danger', true, false, false),
              buttonBlock('danger', 'Danger', false, false, false, 'old-rose', 'fire-brick'),
              buttonBlock('danger', 'Danger', false, false, false),
              buttonBlock('danger', 'Danger', false, true, false, 'shiraz', 'falu-red'),
              buttonBlock('danger', 'Danger', false, false, true)
            )
          ),

          // Warning buttons
          styleColumn(
            { xtype: 'label', text: 'Warning', cls: 'section-header' },
            styleTable(2,
              buttonBlock('warning', 'Warning', false, false, false, 'sea-buckthorn', 'tahiti-gold'),
              buttonBlock('warning', 'Warning', true, false, false),
              buttonBlock('warning', 'Warning', false, false, false, 'zest', 'rich-gold'),
              buttonBlock('warning', 'Warning', false, false, false),
              buttonBlock('warning', 'Warning', false, true, false, 'afghan-tan', 'russet'),
              buttonBlock('warning', 'Warning', false, false, true)
            )
          ),

          // Success buttons
          styleColumn(
            { xtype: 'label', text: 'Success', cls: 'section-header' },
            styleTable(2,
              buttonBlock('success', 'Success', false, false, false, 'elf-green', 'dark-pigment-green'),
              buttonBlock('success', 'Success', true, false, false),
              buttonBlock('success', 'Success', false, false, false, 'salem', 'jewel'),
              buttonBlock('success', 'Success', false, false, false),
              buttonBlock('success', 'Success', false, true, false, 'fun-green', 'dark-jewel'),
              buttonBlock('success', 'Success', false, false, true)
            )
          ),

          // Buttons without icons
          styleColumn(
            { xtype: 'label', text: 'w/ Icons', cls: 'section-header' },
            styleTable(2,
              { xtype: 'button', ui: 'plain', text: 'Default', margin: '0 10 10 0', glyph: 'xf055@FontAwesome' },
              { xtype: 'button', ui: 'plain', text: 'Default', margin: '0 10 10 0', glyph: 'xf055@FontAwesome', disabled: true },
              { xtype: 'button', ui: 'plain', text: 'Default', margin: '0 10 10 0', glyph: 'xf057@FontAwesome' },
              { xtype: 'button', ui: 'plain', text: 'Default', margin: '0 10 10 0', glyph: 'xf057@FontAwesome', disabled: true },
              { xtype: 'button', ui: 'plain', text: 'Default', margin: '0 10 10 0', glyph: 'xf036@FontAwesome', menu: [ { text: 'First' }, '-', { text: 'Second' } ] },
              { xtype: 'button', ui: 'plain', text: 'Default', margin: '0 10 10 0', glyph: 'xf036@FontAwesome', disabled: true, menu: [ { text: 'First' }, '-', { text: 'Second' } ] }
            )
          )
        )
      )
    );

    /*
     * Icons
     */

    /*
     * Form Elements
     */

    me.items.push(
      styleSection('Form Elements',
        styleRow(
          { xtype: 'textfield', value: 'Text Input', allowBlank: false, fieldLabel: '[Label]', helpText: '[Optional description text]', width: 200 },
          { xtype: 'textarea', value: 'Text Input', allowBlank: false, fieldLabel: '[Label]', helpText: '[Optional description text]', width: 200 },
          styleColumn(
            { xtype: 'checkbox', boxLabel: 'Checkbox', checked: true, fieldLabel: null, helpText: null },
            { xtype: 'radio', boxLabel: 'Radio Button', checked: true, fieldLabel: null, helpText: null }
          )
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

    me.items.push(
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

    me.items.push(
      styleSection('Modals',
        styleRow(
          modalBlock('nx-signin'),
          modalBlock('nx-expire-session')
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

    me.items.push(
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
          { xtype: 'nx-header-dashboard-mode', ui: 'header' },
          { xtype: 'nx-header-search-mode', ui: 'header' },
          { xtype: 'nx-header-browse-mode', ui: 'header' },
          { xtype: 'nx-header-admin-mode', ui: 'header' },
          ' ',
          { xtype: 'nx-header-quicksearch', hidden: true },
          '->',
          { xtype: 'nx-header-messages', ui: 'header' },
          { xtype: 'nx-header-refresh', ui: 'header' },
          { xtype: 'nx-header-signin', ui: 'header' },
          { xtype: 'nx-header-user-mode', ui: 'header', hidden: true },
          { xtype: 'nx-header-signout', ui: 'header' },
          { xtype: 'nx-header-help', ui: 'header' }
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

    me.items.push(
      styleSection('Header',
        styleRow(
          toolbarBlock()
        )
      )
    );

    /*
     * Tooltip
     */

    me.items.push(
      styleSection('Tooltip',
        styleRow(
          { xtype: 'button', text: 'Mouse over me', tooltip: 'This is a tooltip' }
        )
      )
    );

    /*
     * Table
     */

    /*
     * Panels
     */

    /*
     * Tabs
     */

    me.items.push(
      styleSection('Tabs',
        styleRow(
          {
            xtype: 'tabpanel',

            width: 500,
            height: 60,
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

    /*
     * Picker
     */

    me.items.push(
      styleSection('Picker',
        styleRow(
          {
            xtype: 'nx-itemselector',

            name: 'realms',
            buttons: ['up', 'add', 'remove', 'down'],
            fromTitle: 'Available',
            toTitle: 'Active',
            store: 'RealmType',
            valueField: 'id',
            displayField: 'name',
            delimiter: null
          }
        )
      )
    );

    me.callParent();
  }
});