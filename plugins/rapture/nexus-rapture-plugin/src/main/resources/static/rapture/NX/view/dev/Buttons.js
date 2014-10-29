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
 * Shows examples of buttons in various styles.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.Buttons', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-dev-buttons',

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

    /*
     * Logo
     */
    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [

          // Section title
          {
            xtype: 'label',
            text: 'Logo',
            cls: 'category-title'
          },

          // Logo example
          {
            xtype: 'container',
            items: [
              {
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

                items: [
                  { xtype: 'nx-header-logo' },
                  {
                    xtype: 'container',
                    items: [
                      {
                        xtype: 'label',
                        text: 'Sonatype Nexus',
                        cls: 'nx-header-productname'
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    );

    /*
     * Colors
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [

          // Section title
          {
            xtype: 'label',
            text: 'Colors',
            cls: 'category-title'
          },

          // Shell colors
          {
            xtype: 'container',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: 'Shell',
                cls: 'section-header'
              },
              {
                xtype: 'container',

                layout: {
                  type: 'hbox'
                },

                items: [

                  // Black
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color black" },
                      { xtype: 'label', text: 'Black' },
                      { xtype: 'label', text: '#000000' }
                    ]
                  },

                  // Night Rider
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color night-rider" },
                      { xtype: 'label', text: 'Night Rider' },
                      { xtype: 'label', text: '#333333' }
                    ]
                  },

                  // Charcoal
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color charcoal" },
                      { xtype: 'label', text: 'Charcoal' },
                      { xtype: 'label', text: '#444444' }
                    ]
                  },

                  // Dark Gray
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color dark-gray" },
                      { xtype: 'label', text: 'Dark Gray' },
                      { xtype: 'label', text: '#777777' }
                    ]
                  },

                  // Gray
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color gray" },
                      { xtype: 'label', text: 'Gray' },
                      { xtype: 'label', text: '#AAAAAA' }
                    ]
                  },

                  // Light Gray
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color light-gray" },
                      { xtype: 'label', text: 'Light Gray' },
                      { xtype: 'label', text: '#CBCBCB' }
                    ]
                  },

                  // Gainsboro
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color gainsboro" },
                      { xtype: 'label', text: 'Gainsboro' },
                      { xtype: 'label', text: '#DDDDDD' }
                    ]
                  },

                  // Smoke
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color smoke" },
                      { xtype: 'label', text: 'Smoke' },
                      { xtype: 'label', text: '#EBEBEB' }
                    ]
                  },

                  // Light Smoke
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color light-smoke" },
                      { xtype: 'label', text: 'Light Smoke' },
                      { xtype: 'label', text: '#F4F4F4' }
                    ]
                  },

                  // White
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color white" },
                      { xtype: 'label', text: 'White' },
                      { xtype: 'label', text: '#FFFFFF' }
                    ]
                  }
                ]
              }
            ]
          },
          {
            xtype: 'container',

            layout: {
              type: 'hbox'
            },

            items: [
              {
                xtype: 'container',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [

                  // Section title
                  {
                    xtype: 'label',
                    text: 'Severity',
                    cls: 'section-header'
                  },

                  // Severity colors
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Cerise
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color cerise" },
                          { xtype: 'label', text: 'Cerise' },
                          { xtype: 'label', text: '#DB2852' }
                        ]
                      },

                      // Sun
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color sun" },
                          { xtype: 'label', text: 'Sun' },
                          { xtype: 'label', text: '#F2862F' }
                        ]
                      },

                      // Energy Yellow
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color energy-yellow" },
                          { xtype: 'label', text: 'Energy Yellow' },
                          { xtype: 'label', text: '#F5C649' }
                        ]
                      },

                      // Cobalt
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color cobalt" },
                          { xtype: 'label', text: 'Cobalt' },
                          { xtype: 'label', text: '#0047B2' }
                        ]
                      },

                      // Cerulean Blue
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color cerulean-blue" },
                          { xtype: 'label', text: 'Cerulean Blue' },
                          { xtype: 'label', text: '#2476C3' }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                xtype: 'container',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [

                  // Section title
                  {
                    xtype: 'label',
                    text: 'Forms',
                    cls: 'section-header'
                  },

                  // Form colors
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Citrus
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color citrus" },
                          { xtype: 'label', text: 'Citrus' },
                          { xtype: 'label', text: '#84C900' }
                        ]
                      },

                      // Free Speech Red
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color free-speech-red" },
                          { xtype: 'label', text: 'Free Speech Red' },
                          { xtype: 'label', text: '#C70000' }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                xtype: 'container',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [
                  // Section title
                  {
                    xtype: 'label',
                    text: 'Tooltip',
                    cls: 'section-header'
                  },

                  // Tooltip colors
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Energy Yellow
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color energy-yellow" },
                          { xtype: 'label', text: 'Energy Yellow' },
                          { xtype: 'label', text: '#F5C649' }
                        ]
                      },

                      // Floral White
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color floral-white" },
                          { xtype: 'label', text: 'Floral White' },
                          { xtype: 'label', text: '#FFFAEE' }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            xtype: 'container',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              // Section header
              {
                xtype: 'label',
                text: 'Dashboard',
                cls: 'section-header'
              },

              // Dashboard colors
              {
                xtype: 'container',

                layout: {
                  type: 'hbox'
                },

                items: [

                  // Pigment Green
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color pigment-green" },
                      { xtype: 'label', text: 'Pigment Green' },
                      { xtype: 'label', text: '#0B9743' }
                    ]
                  },

                  // Madang
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color madang" },
                      { xtype: 'label', text: 'Madang' },
                      { xtype: 'label', text: '#B6E9AB' }
                    ]
                  },

                  // Venetian Red
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color venetian-red" },
                      { xtype: 'label', text: 'Venetian Red' },
                      { xtype: 'label', text: '#BC0430' }
                    ]
                  },

                  // Beauty Bush
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color beauty-bush" },
                      { xtype: 'label', text: 'Beauty Bush' },
                      { xtype: 'label', text: '#EDB2AF' }
                    ]
                  },

                  // Navy Blue
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color navy-blue" },
                      { xtype: 'label', text: 'Navy Blue' },
                      { xtype: 'label', text: '#006BBF' }
                    ]
                  },

                  // Cornflower
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color cornflower" },
                      { xtype: 'label', text: 'Cornflower' },
                      { xtype: 'label', text: '#96CAEE' }
                    ]
                  },

                  // Affair
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color affair" },
                      { xtype: 'label', text: 'Affair' },
                      { xtype: 'label', text: '#875393' }
                    ]
                  },

                  // East Side
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color east-side" },
                      { xtype: 'label', text: 'East Side' },
                      { xtype: 'label', text: '#B087B9' }
                    ]
                  },

                  // Blue Chalk
                  { xtype: 'container', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'container', height: 40, width: 80, cls: "color blue-chalk" },
                      { xtype: 'label', text: 'Blue Chalk' },
                      { xtype: 'label', text: '#DAC5DF' }
                    ]
                  }
                ]
              }
            ]
          },
          {
            xtype: 'container',

            layout: {
              type: 'hbox'
            },

            items: [
              {
                xtype: 'container',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Buttons',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // White
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color white" },
                          { xtype: 'label', text: 'White' },
                          { xtype: 'label', text: '#FFFFFF' }
                        ]
                      },

                      // Light Gainsboro
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color light-gainsboro" },
                          { xtype: 'label', text: 'Light Gainsboro' },
                          { xtype: 'label', text: '#E6E6E6' }
                        ]
                      },

                      // Light Gray
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color light-gray" },
                          { xtype: 'label', text: 'Light Gray' },
                          { xtype: 'label', text: '#CBCBCB' }
                        ]
                      },

                      // Silver
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color silver" },
                          { xtype: 'label', text: 'Silver' },
                          { xtype: 'label', text: '#B8B8B8' }
                        ]
                      },

                      // Suva Gray
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color suva-gray" },
                          { xtype: 'label', text: 'Suva Gray' },
                          { xtype: 'label', text: '#919191' }
                        ]
                      },

                      // Gray
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color gray" },
                          { xtype: 'label', text: 'Gray' },
                          { xtype: 'label', text: '#808080' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Denim
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color denim" },
                          { xtype: 'label', text: 'Denim' },
                          { xtype: 'label', text: '#197AC5' }
                        ]
                      },

                      // Light Cobalt
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color light-cobalt" },
                          { xtype: 'label', text: 'Light Cobalt' },
                          { xtype: 'label', text: '#0161AD' }
                        ]
                      },

                      // Dark Denim
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color dark-denim" },
                          { xtype: 'label', text: 'Dark Denim' },
                          { xtype: 'label', text: '#14629E' }
                        ]
                      },

                      // Smalt
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color smalt" },
                          { xtype: 'label', text: 'Smalt' },
                          { xtype: 'label', text: '#014E8A' }
                        ]
                      },

                      // Dark Cerulean
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color dark-cerulean" },
                          { xtype: 'label', text: 'Dark Cerulean' },
                          { xtype: 'label', text: '#0F4976' }
                        ]
                      },

                      // Prussian Blue
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color prussian-blue" },
                          { xtype: 'label', text: 'Prussian Blue' },
                          { xtype: 'label', text: '#013A68' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Light Cerise
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color light-cerise" },
                          { xtype: 'label', text: 'Light Cerise' },
                          { xtype: 'label', text: '#DE3D63' }
                        ]
                      },

                      // Brick Red
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color brick-red" },
                          { xtype: 'label', text: 'Brick Red' },
                          { xtype: 'label', text: '#C6254B' }
                        ]
                      },

                      // Old Rose
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color old-rose" },
                          { xtype: 'label', text: 'Old Rose' },
                          { xtype: 'label', text: '#B2314F' }
                        ]
                      },

                      // Fire Brick
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color fire-brick" },
                          { xtype: 'label', text: 'Fire Brick' },
                          { xtype: 'label', text: '#9E1E3C' }
                        ]
                      },

                      // Shiraz
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color shiraz" },
                          { xtype: 'label', text: 'Shiraz' },
                          { xtype: 'label', text: '#85253B' }
                        ]
                      },

                      // Falu Red
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color falu-red" },
                          { xtype: 'label', text: 'Falu Red' },
                          { xtype: 'label', text: '#77162D' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Sea Buckthorn
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color sea-buckthorn" },
                          { xtype: 'label', text: 'Sea Buckthorn' },
                          { xtype: 'label', text: '#F39244' }
                        ]
                      },

                      // Tahiti Gold
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color tahiti-gold" },
                          { xtype: 'label', text: 'Tahiti Gold' },
                          { xtype: 'label', text: '#DA792B' }
                        ]
                      },

                      // Zest
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color zest" },
                          { xtype: 'label', text: 'Zest' },
                          { xtype: 'label', text: '#C17536' }
                        ]
                      },

                      // Rich Gold
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color rich-gold" },
                          { xtype: 'label', text: 'Rich Gold' },
                          { xtype: 'label', text: '#AE6122' }
                        ]
                      },

                      // Afghan Tan
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color afghan-tan" },
                          { xtype: 'label', text: 'Afghan Tan' },
                          { xtype: 'label', text: '#925829' }
                        ]
                      },

                      // Russet
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color russet" },
                          { xtype: 'label', text: 'Russet' },
                          { xtype: 'label', text: '#83491A' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Elf Green
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color elf-green" },
                          { xtype: 'label', text: 'Elf Green' },
                          { xtype: 'label', text: '#23A156' }
                        ]
                      },

                      // Dark Pigment Green
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color dark-pigment-green" },
                          { xtype: 'label', text: 'Dark Pigment Green' },
                          { xtype: 'label', text: '#0B893D' }
                        ]
                      },

                      // Salem
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color salem" },
                          { xtype: 'label', text: 'Salem' },
                          { xtype: 'label', text: '#1C8145' }
                        ]
                      },

                      // Jewel
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color jewel" },
                          { xtype: 'label', text: 'Jewel' },
                          { xtype: 'label', text: '#096E31' }
                        ]
                      },

                      // Fun Green
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color fun-green" },
                          { xtype: 'label', text: 'Fun Green' },
                          { xtype: 'label', text: '#156134' }
                        ]
                      },

                      // Dark Jewel
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color dark-jewel" },
                          { xtype: 'label', text: 'Dark Jewel' },
                          { xtype: 'label', text: '#0C4F26' }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                xtype: 'container',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Font Awesome Icons',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Navy Blue
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color navy-blue" },
                          { xtype: 'label', text: 'Navy Blue' },
                          { xtype: 'label', text: '#006BBF' }
                        ]
                      },

                      // Smalt
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color smalt" },
                          { xtype: 'label', text: 'Smalt' },
                          { xtype: 'label', text: '#014E8A' }
                        ]
                      },

                      // Prussian Blue
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color prussian-blue" },
                          { xtype: 'label', text: 'Prussian Blue' },
                          { xtype: 'label', text: '#013A68' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // White
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color white" },
                          { xtype: 'label', text: 'White' },
                          { xtype: 'label', text: '#FFFFFF' }
                        ]
                      },

                      // Gainsboro
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color gainsboro" },
                          { xtype: 'label', text: 'Gainsboro' },
                          { xtype: 'label', text: '#DDDDDD' }
                        ]
                      },

                      // Gray
                      { xtype: 'container', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'container', height: 40, width: 80, cls: "color gray" },
                          { xtype: 'label', text: 'Gray' },
                          { xtype: 'label', text: '#AAAAAA' }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    );

    /*
     * Fonts
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [

          // Section title
          {
            xtype: 'label',
            text: 'Fonts',
            cls: 'category-title'
          },

          {
            xtype: 'container',

            layout: {
              type: 'hbox'
            },

            items: [
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                padding: "0 8px 4x 0",

                items: [
                  {
                    xtype: 'label',
                    text: 'Proxima Nova Regular',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',
                    cls: 'proxima-nova-regular',

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
              },
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                padding: "0 8px 4x 0",

                items: [
                  {
                    xtype: 'label',
                    text: 'Proxima Nova Bold',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',
                    cls: 'proxima-nova-bold',

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
              },
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                padding: "0 8px 4x 0",

                items: [
                  {
                    xtype: 'label',
                    text: 'Courier New',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',
                    cls: 'source-code-pro-regular',

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
            ]
          }
        ]
      }
    );

    /*
     * Type Styles
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [
          {
            xtype: 'label',
            text: 'Type Styles',
            cls: 'category-title'
          },
          {
            xtype: 'container',

            layout: {
              type: 'table',
              columns: 6
            },

            defaults: {
              bodyStyle: 'padding: 0px 20px 20px 0'
            },

            items: [
              {
                html: 'Name',
                baseCls: 'column-header'
              },
              {
                html: 'Description',
                baseCls: 'column-header'
              },
              {
                html: 'Font & Weight',
                baseCls: 'column-header'
              },
              {
                html: 'Use Cases',
                baseCls: 'column-header'
              },
              {
                html: 'Pixels',
                baseCls: 'column-header'
              },
              {
                html: 'Sample',
                baseCls: 'column-header'
              },

              { html: 'h1' },
              { html: 'Page Title' },
              { html: 'Proxima Nova Light' },
              { html: 'Header' },
              { html: '40' },
              {
                html: 'Sonatype Nexus',
                baseCls: 'sample-h1'
              },

              { html: 'h2' },
              { html: 'Header' },
              { html: 'Proxima Nova Bold' },
              { html: 'Header' },
              { html: '26' },
              {
                html: 'Development',
                baseCls: 'sample-h2'
              },

              { html: 'h3' },
              { html: 'Header' },
              { html: 'Proxima Nova Bold' },
              { html: 'Header' },
              { html: '22' },
              {
                html: 'Development',
                baseCls: 'sample-h3'
              },

              { html: 'h4' },
              { html: 'Sub-Header' },
              { html: 'Proxima Nova Bold' },
              { html: 'Sub-Header' },
              { html: '18' },
              {
                html: 'Development',
                baseCls: 'sample-h4'
              },

              { html: 'h5' },
              { html: 'Sub-Header' },
              { html: 'Proxima Nova Bold' },
              { html: 'Sub-Header' },
              { html: '13' },
              {
                html: 'Development',
                baseCls: 'sample-h5'
              },

              { html: 'p/ul/ol' },
              { html: 'Body' },
              { html: 'Proxima Nova Regular' },
              { html: 'Body text, lists, default size' },
              { html: '13' },
              {
                html: 'Development',
                baseCls: 'sample-body'
              },

              { html: 'code' },
              { html: 'Code' },
              { html: 'Courier New Regular' },
              { html: 'Code examples' },
              { html: '13' },
              {
                html: 'Development',
                baseCls: 'sample-code'
              },

              { html: 'utility' },
              { html: 'Small Text' },
              { html: 'Proxima Nova Regular' },
              { html: 'Labels, Side-Nav' },
              { html: '10' },
              {
                html: 'Development',
                baseCls: 'sample-utility'
              }
            ]
          }
        ]
      }
    );

    /*
     * Buttons
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [

          // Section title
          {
            xtype: 'label',
            text: 'Buttons',
            cls: 'category-title'
          },

          {
            xtype: 'container',

            layout: {
              type: 'table',
              columns: 3
            },

            defaults: {
              bodyStyle: "padding: 0 20px 20px 0"
            },

            // Default buttons
            items: [
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Default',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'table',
                      columns: 2
                    },

                    items: [
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Default', glyph: 'xf000@FontAwesome', ui: 'default', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color white' },
                              { xtype: 'label', text: '$color-white', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color light-gainsboro' },
                              { xtype: 'label', text: '$color-light-gainsboro', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Default', glyph: 'xf000@FontAwesome',  ui: 'default', disabled: true, margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Default', glyph: 'xf000@FontAwesome', ui: 'default', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color light-gray' },
                              { xtype: 'label', text: '$color-light-gray', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color silver' },
                              { xtype: 'label', text: '$color-silver', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Default', glyph: 'xf000@FontAwesome',  ui: 'default', margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Default', glyph: 'xf000@FontAwesome', ui: 'default', pressed: 'true', enableToggle: 'true', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color suva-gray' },
                              { xtype: 'label', text: '$color-suva-gray', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color gray' },
                              { xtype: 'label', text: '$color-gray', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Default', glyph: 'xf000@FontAwesome', ui: 'default', menu: [ { text: 'First' }, '-', { text: 'Second' } ], margin: "0 10 10 0" }
                    ]
                  }
                ]
              },

              // Primary buttons
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Primary',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'table',
                      columns: 2
                    },

                    items: [
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Primary', glyph: 'xf000@FontAwesome', ui: 'primary', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color denim' },
                              { xtype: 'label', text: '$color-denim', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color light-cobalt' },
                              { xtype: 'label', text: '$color-light-cobalt', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Primary', glyph: 'xf000@FontAwesome', ui: 'primary', disabled: true, margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Primary', glyph: 'xf000@FontAwesome', ui: 'primary', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color dark-denim' },
                              { xtype: 'label', text: '$color-dark-denim', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color smalt' },
                              { xtype: 'label', text: '$color-smalt', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Primary', glyph: 'xf000@FontAwesome', ui: 'primary', margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Primary', glyph: 'xf000@FontAwesome', ui: 'primary', pressed: 'true', enableToggle: 'true', margin: "0 10 10 0"  },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color dark-cerulean' },
                              { xtype: 'label', text: '$color-dark-cerulean', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color prussian-blue' },
                              { xtype: 'label', text: '$color-prussian-blue', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Primary', glyph: 'xf000@FontAwesome', ui: 'primary', menu: [ { text: 'First' }, '-', { text: 'Second' } ], margin: "0 10 10 0" }
                    ]
                  }
                ]
              },

              // Danger buttons
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Danger',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'table',
                      columns: 2
                    },

                    items: [
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Danger', glyph: 'xf000@FontAwesome', ui: 'danger', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color light-cerise' },
                              { xtype: 'label', text: '$color-light-cerise', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color brick-red' },
                              { xtype: 'label', text: '$color-brick-red', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Danger', glyph: 'xf000@FontAwesome', ui: 'danger', disabled: true, margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Danger', glyph: 'xf000@FontAwesome', ui: 'danger', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color old-rose' },
                              { xtype: 'label', text: '$color-old-rose', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color fire-brick' },
                              { xtype: 'label', text: '$color-fire-brick', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Danger', glyph: 'xf000@FontAwesome', ui: 'danger' },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Danger', glyph: 'xf000@FontAwesome', ui: 'danger', pressed: 'true', enableToggle: 'true', margin: "0 10 10 0"  },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color shiraz' },
                              { xtype: 'label', text: '$color-shiraz', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color falu-red' },
                              { xtype: 'label', text: '$color-falu-red', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Danger', glyph: 'xf000@FontAwesome', ui: 'danger', menu: [ { text: 'First' }, '-', { text: 'Second' } ], margin: "0 10 10 0" }
                    ]
                  }
                ]
              },

              // Warning buttons
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Warning',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'table',
                      columns: 2
                    },

                    items: [
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Warning', glyph: 'xf000@FontAwesome', ui: 'warning', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color sea-buckthorn' },
                              { xtype: 'label', text: '$color-sea-buckthorn', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color tahiti-gold' },
                              { xtype: 'label', text: '$color-tahiti-gold', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Warning', glyph: 'xf000@FontAwesome', ui: 'warning', disabled: true, margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Warning', glyph: 'xf000@FontAwesome', ui: 'warning', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color zest' },
                              { xtype: 'label', text: '$color-zest', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color rich-gold' },
                              { xtype: 'label', text: '$color-rich-gold', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Warning', glyph: 'xf000@FontAwesome', ui: 'warning', margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Warning', glyph: 'xf000@FontAwesome', ui: 'warning', pressed: 'true', enableToggle: 'true', margin: "0 10 10 0"  },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color afghan-tan' },
                              { xtype: 'label', text: '$color-afghan-tan', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color russet' },
                              { xtype: 'label', text: '$color-russet', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Warning', glyph: 'xf000@FontAwesome', ui: 'warning', menu: [ { text: 'First' }, '-', { text: 'Second' } ], margin: "0 10 10 0" }
                    ]
                  }
                ]
              },

              // Success buttons
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Success',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'table',
                      columns: 2
                    },

                    items: [
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Success', glyph: 'xf000@FontAwesome', ui: 'success', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color elf-green' },
                              { xtype: 'label', text: '$color-elf-green', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color dark-pigment-green' },
                              { xtype: 'label', text: '$color-dark-pigment-green', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Success', glyph: 'xf000@FontAwesome', ui: 'success', disabled: true, margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Success', glyph: 'xf000@FontAwesome', ui: 'success', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color salem' },
                              { xtype: 'label', text: '$color-salem', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color jewel' },
                              { xtype: 'label', text: '$color-jewel', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Success', glyph: 'xf000@FontAwesome', ui: 'success', margin: "0 10 10 0" },
                      {
                        xtype: 'container',

                        layout: {
                          type: 'hbox'
                        },

                        items: [
                          { xtype: 'button', text: 'Success', glyph: 'xf000@FontAwesome', ui: 'success', pressed: 'true', enableToggle: 'true', margin: "0 10 10 0" },
                          {
                            xtype: 'container',

                            layout: {
                              type: 'table',
                              columns: 2
                            },

                            items: [
                              { xtype: 'container', width: 10, height: 10, cls: 'color fun-green' },
                              { xtype: 'label', text: '$color-fun-green', padding: '0 10px 0 0' },
                              { xtype: 'container', width: 10, height: 10, cls: 'color dark-jewel' },
                              { xtype: 'label', text: '$color-dark-jewel', padding: '0 10px 0 0' }
                            ]
                          }
                        ]
                      },
                      { xtype: 'button', text: 'Success', glyph: 'xf000@FontAwesome', ui: 'success', menu: [ { text: 'First' }, '-', { text: 'Second' } ], margin: "0 10 10 0" }
                    ]
                  }
                ]
              },

              // Buttons without icons
              {
                xtype: 'container',

                layout: {
                  type: 'vbox'
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'w/ Icons',
                    cls: 'section-header'
                  },
                  {
                    xtype: 'container',

                    layout: {
                      type: 'table',
                      columns: 3
                    },

                    items: [
                      { xtype: 'button', text: 'Default', glyph: 'xf055@FontAwesome', ui: 'plain', margin: "0 10 10 0" },
                      { xtype: 'button', text: 'Default', glyph: 'xf057@FontAwesome', ui: 'plain', margin: "0 10 10 0" },
                      { xtype: 'button', text: 'Default', glyph: 'xf036@FontAwesome', ui: 'plain', menu: [
                        { text: 'First' },
                        '-',
                        { text: 'Second' }
                      ], margin: "0 10 10 0" },
                      { xtype: 'button', text: 'Default', glyph: 'xf055@FontAwesome', ui: 'plain', disabled: true, margin: "0 10 10 0" },
                      { xtype: 'button', text: 'Default', glyph: 'xf057@FontAwesome', ui: 'plain', disabled: true, margin: "0 10 10 0" },
                      { xtype: 'button', text: 'Default', glyph: 'xf036@FontAwesome', ui: 'plain', disabled: true, menu: [
                        { text: 'First' },
                        '-',
                        { text: 'Second' }
                      ], margin: "0 10 10 0" }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    );

    /*
     * Icons
     */

    /*
     * Form Elements
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [{
          xtype: 'label',
          text: 'Form Elements',
          cls: 'category-title'
        },
          {
            xtype: 'container',

            layout: {
              type: 'table',
              padding: 4,
              columns: 4
            },

            items: [
              {
                xtype: 'textfield',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]',
                value: 'Text Input'
              },
              {
                xtype: 'textfield',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]',
                value: 'Text Input',
                allowBlank: false
              },
              {
                xtype: 'textfield',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]',
                allowBlank: false
              },
              {
                xtype: 'textfield',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200,
                fieldLabel: '[Label]',
                colspan: 2,
                helpText: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]',
                value: 'Text Input'
              },
              {
                xtype: 'textarea',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]',
                value: 'Text Input',
                allowBlank: false
              },
              {
                xtype: 'textarea',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]',
                allowBlank: false
              },
              {
                xtype: 'textarea',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200,
                fieldLabel: '[Label]',
                helpText: '[Optional description text]'
              },
            ]
          },
          {
            xtype: 'container',

            layout: {
              type: 'table',
              padding: 4,
              columns: 4
            },

            items: [
              {
                xtype: 'checkbox',
                boxLabel: 'Checkbox'
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Checkbox'
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Checkbox',
                checked: true
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Checkbox',
                checked: true
              },
              {
                xtype: 'radio',
                boxLabel: 'Radio Button'
              },
              {
                xtype: 'radio',
                boxLabel: 'Radio Button'
              },
              {
                xtype: 'radio',
                boxLabel: 'Radio Button',
                checked: true
              },
              {
                xtype: 'radio',
                boxLabel: 'Radio Button',
                checked: true
              }
            ]
          }
        ]
      }
    );

    /*
     * Notifications messages.
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
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [
          {
            xtype: 'label',
            text: 'Notifications',
            cls: 'category-title'
          },
          {
            xtype: 'container',

            layout: {
              type: 'hbox',
              defaultMargins: '0px 10px 0px 0px'
            },

            items: [
              notificationWindow('default'),
              notificationWindow('primary'),
              notificationWindow('danger'),
              notificationWindow('warning'),
              notificationWindow('success')
            ]
          }
        ]
      }
    );

    /*
     * Modals
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [
          {
            xtype: 'label',
            text: 'Modals',
            cls: 'category-title'
          },
          {
            xtype: 'container',

            layout: {
              type: 'hbox',
              defaultMargins: '0 40px 0 0'
            },

            items: [
              {
                xtype: 'nx-signin',
                hidden: false,
                collapsible: false,
                floating: false,
                closable: false,
                draggable: false,
                resizable: false,
                cls: 'fixed-modal'
              },
              {
                xtype: 'nx-expire-session',
                hidden: false,
                collapsible: false,
                floating: false,
                closable: false,
                draggable: false,
                resizable: false,
                cls: 'fixed-modal'
              }
            ]
          }
        ]
      }
    );

    /*
     * Menu
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [
          {
            xtype: 'label',
            text: 'Menu',
            cls: 'category-title'
          },
          {
            xtype: 'container',

            layout: {
              type: 'hbox',
              defaultMargins: '0 20px 0 0'
            },

            items: [
              {
                xtype: 'menu',

                floating: false,

                items: [
                  {
                    // text and iconCls is dynamic
                    text: 'Help for [Feature]',
                    iconCls: 'nx-icon-search-default-x16',
                    tooltip: 'Help and documentation for the currently selected feature',
                    action: 'feature'
                  },
                  '-',
                  {
                    text: 'About',
                    iconCls: 'nx-icon-nexus-x16',
                    tooltip: 'About Sonatype Nexus',
                    action: 'about'
                  },
                  {
                    text: 'Documentation',
                    iconCls: 'nx-icon-help-manual-x16',
                    tooltip: 'Sonatype Nexus product documentation',
                    action: 'docs'
                  },
                  {
                    text: 'Knowledge Base',
                    iconCls: 'nx-icon-help-kb-x16',
                    tooltip: 'Sonatype Nexus knowledge base',
                    action: 'kb'
                  },
                  {
                    text: 'Community',
                    iconCls: 'nx-icon-help-community-x16',
                    tooltip: 'Sonatype Nexus community information',
                    action: 'community'
                  },
                  {
                    text: 'Issue Tracker',
                    iconCls: 'nx-icon-help-issues-x16',
                    tooltip: 'Sonatype Nexus issue and bug tracker',
                    action: 'issues'
                  },
                  '-',
                  {
                    text: 'Support',
                    iconCls: 'nx-icon-help-support-x16',
                    tooltip: 'Sonatype Nexus product support',
                    action: 'support'
                  }
                ]
              }
            ]
          }
        ]
      }
    );

    /*
     * Header
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [
          {
            xtype: 'label',
            text: 'Header',
            cls: 'category-title'
          },
          {
            xtype: 'container',

            layout: {
              type: 'vbox',
              defaultMargins: '0 0 20px 0'
            },

            items: [
              {
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

                items: [
                  { xtype: 'nx-header-logo' },
                  {
                    xtype: 'container',
                    items: [
                      {
                        xtype: 'label',
                        text: 'Sonatype Nexus',
                        cls: 'nx-header-productname'
                      },
                      {
                        xtype: 'label',
                        text: NX.State.getEdition() + ' ' + NX.State.getVersion(),
                        cls: 'nx-header-productversion',
                        style: {
                          'padding-left': '8px'
                        }
                      }
                    ]
                  },
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
                ]
              }
            ]
          }
        ]
      }
    );

    /*
     * Tooltip
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [
          {
            xtype: 'label',
            text: 'Tooltip',
            cls: 'category-title'
          },
          {
            xtype: 'button',
            text: 'Mouse over me',
            tooltip: 'This is a tooltip'
          }
        ]
      }
    );

    /*
     * Table
     */

    /*me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [
          {
            xtype: 'label',
            text: 'Table',
            cls: 'category-title'
          }
        ]
      }
    );*/

    /*
     * Panels
     */

    /*
     * Tabs
     */

    me.items.push(
      {
        xtype: 'container',

        layout: {
          type: 'vbox',
          padding: 4
        },

        cls: 'category',

        items: [
          {
            xtype: 'label',
            text: 'Tabs',
            cls: 'category-title'
          },
          {
            xtype: 'tabpanel',

            width: 500,
            height: 150,
            activeTab: 0,
            ui: 'light',

            header: {
              height: 30
            },

            items: [
              {
                title: 'Settings',
                bodyPadding: 10,
                html: 'A simple tab'
              },
              {
                title: 'Routing',
                html: 'Another one'
              },
              {
                title: 'Smart Proxy',
                html: 'Yet another'
              },
              {
                title: 'Health Check',
                html: 'And one more'
              }
            ]
          }
        ]
      }
    );

    /*
     * Picker
     */

    me.callParent();
  }
});