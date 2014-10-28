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
        xtype: 'panel',

        layout: {
          type: 'vbox',
          padding: 4
        },

        items: [

          // Section title
          {
            xtype: 'label',
            text: 'Logo'
          },

          // Logo example
          {
            xtype: 'panel',
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
        xtype: 'panel',

        layout: {
          type: 'vbox',
          padding: 4
        },

        items: [

          // Section title
          {
            xtype: 'label',
            text: 'Colors'
          },

          // Shell colors
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: 'Shell'
              },
              {
                xtype: 'panel',

                layout: {
                  type: 'hbox'
                },

                items: [

                  // Black
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-black" },
                      { xtype: 'label', text: 'Black' },
                      { xtype: 'label', text: '#000000' }
                    ]
                  },

                  // Night Rider
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-night-rider" },
                      { xtype: 'label', text: 'Night Rider' },
                      { xtype: 'label', text: '#333333' }
                    ]
                  },

                  // Charcoal
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-charcoal" },
                      { xtype: 'label', text: 'Charcoal' },
                      { xtype: 'label', text: '#444444' }
                    ]
                  },

                  // Dark Gray
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-dark-gray" },
                      { xtype: 'label', text: 'Dark Gray' },
                      { xtype: 'label', text: '#777777' }
                    ]
                  },

                  // Gray
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-gray" },
                      { xtype: 'label', text: 'Gray' },
                      { xtype: 'label', text: '#AAAAAA' }
                    ]
                  },

                  // Light Gray
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-light-gray" },
                      { xtype: 'label', text: 'Light Gray' },
                      { xtype: 'label', text: '#CBCBCB' }
                    ]
                  },

                  // Gainsboro
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-gainsboro" },
                      { xtype: 'label', text: 'Gainsboro' },
                      { xtype: 'label', text: '#DDDDDD' }
                    ]
                  },

                  // Smoke
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-smoke" },
                      { xtype: 'label', text: 'Smoke' },
                      { xtype: 'label', text: '#EBEBEB' }
                    ]
                  },

                  // Light Smoke
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-light-smoke" },
                      { xtype: 'label', text: 'Light Smoke' },
                      { xtype: 'label', text: '#F4F4F4' }
                    ]
                  },

                  // White
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-white" },
                      { xtype: 'label', text: 'White' },
                      { xtype: 'label', text: '#FFFFFF' }
                    ]
                  }
                ]
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'hbox'
            },

            items: [
              {
                xtype: 'panel',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [

                  // Section title
                  {
                    xtype: 'label',
                    text: 'Severity'
                  },

                  // Severity colors
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Cerise
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-cerise" },
                          { xtype: 'label', text: 'Cerise' },
                          { xtype: 'label', text: '#DB2852' }
                        ]
                      },

                      // Sun
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-sun" },
                          { xtype: 'label', text: 'Sun' },
                          { xtype: 'label', text: '#F2862F' }
                        ]
                      },

                      // Energy Yellow
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-energy-yellow" },
                          { xtype: 'label', text: 'Energy Yellow' },
                          { xtype: 'label', text: '#F5C649' }
                        ]
                      },

                      // Cobalt
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-cobalt" },
                          { xtype: 'label', text: 'Cobalt' },
                          { xtype: 'label', text: '#0047B2' }
                        ]
                      },

                      // Cerulean Blue
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-cerulean-blue" },
                          { xtype: 'label', text: 'Cerulean Blue' },
                          { xtype: 'label', text: '#2476C3' }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                xtype: 'panel',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [

                  // Section title
                  {
                    xtype: 'label',
                    text: 'Forms'
                  },

                  // Form colors
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Citrus
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-citrus" },
                          { xtype: 'label', text: 'Citrus' },
                          { xtype: 'label', text: '#84C900' }
                        ]
                      },

                      // Free Speech Red
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-free-speech-red" },
                          { xtype: 'label', text: 'Free Speech Red' },
                          { xtype: 'label', text: '#C70000' }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                xtype: 'panel',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [
                  // Section title
                  {
                    xtype: 'label',
                    text: 'Tooltip'
                  },

                  // Tooltip colors
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Energy Yellow
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-energy-yellow" },
                          { xtype: 'label', text: 'Energy Yellow' },
                          { xtype: 'label', text: '#F5C649' }
                        ]
                      },

                      // Floral White
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-floral-white" },
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
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              // Section header
              {
                xtype: 'label',
                text: 'Dashboard'
              },

              // Dashboard colors
              {
                xtype: 'panel',

                layout: {
                  type: 'hbox'
                },

                items: [

                  // Pigment Green
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-pigment-green" },
                      { xtype: 'label', text: 'Pigment Green' },
                      { xtype: 'label', text: '#0B9743' }
                    ]
                  },

                  // Madang
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-madang" },
                      { xtype: 'label', text: 'Madang' },
                      { xtype: 'label', text: '#B6E9AB' }
                    ]
                  },

                  // Venetian Red
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-venetian-red" },
                      { xtype: 'label', text: 'Venetian Red' },
                      { xtype: 'label', text: '#BC0430' }
                    ]
                  },

                  // Beauty Bush
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-beauty-bush" },
                      { xtype: 'label', text: 'Beauty Bush' },
                      { xtype: 'label', text: '#EDB2AF' }
                    ]
                  },

                  // Navy Blue
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-navy-blue" },
                      { xtype: 'label', text: 'Navy Blue' },
                      { xtype: 'label', text: '#006BBF' }
                    ]
                  },

                  // Cornflower
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-cornflower" },
                      { xtype: 'label', text: 'Cornflower' },
                      { xtype: 'label', text: '#96CAEE' }
                    ]
                  },

                  // Affair
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-affair" },
                      { xtype: 'label', text: 'Affair' },
                      { xtype: 'label', text: '#875393' }
                    ]
                  },

                  // East Side
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-east-side" },
                      { xtype: 'label', text: 'East Side' },
                      { xtype: 'label', text: '#B087B9' }
                    ]
                  },

                  // Blue Chalk
                  { xtype: 'panel', layout: { type: 'vbox' },
                    items: [
                      { xtype: 'panel', height: 40, width: 80, bodyCls: "color-blue-chalk" },
                      { xtype: 'label', text: 'Blue Chalk' },
                      { xtype: 'label', text: '#DAC5DF' }
                    ]
                  }
                ]
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'hbox'
            },

            items: [
              {
                xtype: 'panel',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Buttons'
                  },
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // White
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-white" },
                          { xtype: 'label', text: 'White' },
                          { xtype: 'label', text: '#FFFFFF' }
                        ]
                      },

                      // Light Gainsboro
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-light-gainsboro" },
                          { xtype: 'label', text: 'Light Gainsboro' },
                          { xtype: 'label', text: '#E6E6E6' }
                        ]
                      },

                      // Light Gray
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-light-gray" },
                          { xtype: 'label', text: 'Light Gray' },
                          { xtype: 'label', text: '#CBCBCB' }
                        ]
                      },

                      // Silver
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-silver" },
                          { xtype: 'label', text: 'Silver' },
                          { xtype: 'label', text: '#B8B8B8' }
                        ]
                      },

                      // Suva Gray
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-suva-gray" },
                          { xtype: 'label', text: 'Suva Gray' },
                          { xtype: 'label', text: '#919191' }
                        ]
                      },

                      // Gray
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-gray" },
                          { xtype: 'label', text: 'Gray' },
                          { xtype: 'label', text: '#808080' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Denim
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-denim" },
                          { xtype: 'label', text: 'Denim' },
                          { xtype: 'label', text: '#197AC5' }
                        ]
                      },

                      // Light Cobalt
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-light-cobalt" },
                          { xtype: 'label', text: 'Light Cobalt' },
                          { xtype: 'label', text: '#0161AD' }
                        ]
                      },

                      // Dark Denim
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-dark-denim" },
                          { xtype: 'label', text: 'Dark Denim' },
                          { xtype: 'label', text: '#14629E' }
                        ]
                      },

                      // Smalt
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-smalt" },
                          { xtype: 'label', text: 'Smalt' },
                          { xtype: 'label', text: '#014E8A' }
                        ]
                      },

                      // Dark Cerulean
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-dark-cerulean" },
                          { xtype: 'label', text: 'Dark Cerulean' },
                          { xtype: 'label', text: '#0F4976' }
                        ]
                      },

                      // Prussian Blue
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-prussian-blue" },
                          { xtype: 'label', text: 'Prussian Blue' },
                          { xtype: 'label', text: '#013A68' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Light Cerise
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-light-cerise" },
                          { xtype: 'label', text: 'Light Cerise' },
                          { xtype: 'label', text: '#DE3D63' }
                        ]
                      },

                      // Brick Red
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-brick-red" },
                          { xtype: 'label', text: 'Brick Red' },
                          { xtype: 'label', text: '#C6254B' }
                        ]
                      },

                      // Old Rose
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-old-rose" },
                          { xtype: 'label', text: 'Old Rose' },
                          { xtype: 'label', text: '#B2314F' }
                        ]
                      },

                      // Fire Brick
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-fire-brick" },
                          { xtype: 'label', text: 'Fire Brick' },
                          { xtype: 'label', text: '#9E1E3C' }
                        ]
                      },

                      // Shiraz
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-shiraz" },
                          { xtype: 'label', text: 'Shiraz' },
                          { xtype: 'label', text: '#85253B' }
                        ]
                      },

                      // Falu Red
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-falu-red" },
                          { xtype: 'label', text: 'Falu Red' },
                          { xtype: 'label', text: '#77162D' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Sea Buckthorn
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-sea-buckthorn" },
                          { xtype: 'label', text: 'Sea Buckthorn' },
                          { xtype: 'label', text: '#F39244' }
                        ]
                      },

                      // Tahiti Gold
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-tahiti-gold" },
                          { xtype: 'label', text: 'Tahiti Gold' },
                          { xtype: 'label', text: '#DA792B' }
                        ]
                      },

                      // Zest
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-zest" },
                          { xtype: 'label', text: 'Zest' },
                          { xtype: 'label', text: '#C17536' }
                        ]
                      },

                      // Rich Gold
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-rich-gold" },
                          { xtype: 'label', text: 'Rich Gold' },
                          { xtype: 'label', text: '#AE6122' }
                        ]
                      },

                      // Afghan Tan
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-afghan-tan" },
                          { xtype: 'label', text: 'Afghan Tan' },
                          { xtype: 'label', text: '#925829' }
                        ]
                      },

                      // Russet
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-russet" },
                          { xtype: 'label', text: 'Russet' },
                          { xtype: 'label', text: '#83491A' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Elf Green
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-elf-green" },
                          { xtype: 'label', text: 'Elf Green' },
                          { xtype: 'label', text: '#23A156' }
                        ]
                      },

                      // Dark Pigment Green
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-dark-pigment-green" },
                          { xtype: 'label', text: 'Dark Pigment Green' },
                          { xtype: 'label', text: '#0B893D' }
                        ]
                      },

                      // Salem
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-salem" },
                          { xtype: 'label', text: 'Salem' },
                          { xtype: 'label', text: '#1C8145' }
                        ]
                      },

                      // Jewel
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-jewel" },
                          { xtype: 'label', text: 'Jewel' },
                          { xtype: 'label', text: '#096E31' }
                        ]
                      },

                      // Fun Green
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-fun-green" },
                          { xtype: 'label', text: 'Fun Green' },
                          { xtype: 'label', text: '#156134' }
                        ]
                      },

                      // Dark Jewel
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-dark-jewel" },
                          { xtype: 'label', text: 'Dark Jewel' },
                          { xtype: 'label', text: '#0C4F26' }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                xtype: 'panel',

                layout: {
                  type: 'vbox',
                  padding: 4
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Font Awesome Icons'
                  },
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // Navy Blue
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-navy-blue" },
                          { xtype: 'label', text: 'Navy Blue' },
                          { xtype: 'label', text: '#006BBF' }
                        ]
                      },

                      // Smalt
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-smalt" },
                          { xtype: 'label', text: 'Smalt' },
                          { xtype: 'label', text: '#014E8A' }
                        ]
                      },

                      // Prussian Blue
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-prussian-blue" },
                          { xtype: 'label', text: 'Prussian Blue' },
                          { xtype: 'label', text: '#013A68' }
                        ]
                      }
                    ]
                  },
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'hbox'
                    },

                    items: [

                      // White
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-white" },
                          { xtype: 'label', text: 'White' },
                          { xtype: 'label', text: '#FFFFFF' }
                        ]
                      },

                      // Gainsboro
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-gainsboro" },
                          { xtype: 'label', text: 'Gainsboro' },
                          { xtype: 'label', text: '#DDDDDD' }
                        ]
                      },

                      // Gray
                      { xtype: 'panel', layout: { type: 'vbox' },
                        items: [
                          { xtype: 'panel', height: 40, width: 80, bodyCls: "color-gray" },
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
        xtype: 'panel',

        layout: {
          type: 'vbox',
          padding: 4
        },

        items: [

          // Section title
          {
            xtype: 'label',
            text: 'Fonts'
          },

          {
            xtype: 'panel',

            layout: {
              type: 'hbox'
            },

            items: [
              {
                xtype: 'panel',

                layout: {
                  type: 'vbox'
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Proxima Nova Regular'
                  },
                  {
                    xtype: 'panel',

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
                xtype: 'panel',

                layout: {
                  type: 'vbox'
                },

                items: [
                  {
                    xtype: 'label',
                    text: 'Proxima Nova Bold'
                  },
                  {
                    xtype: 'panel',

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
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox'
            },

            items: [
              {
                xtype: 'label',
                text: 'Source Code Pro Regular'
              },
              {
                xtype: 'panel',

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
    );

    /*
     * Type Styles
     */

    me.items.push(
      {
        xtype: 'panel',

        layout: {
          type: 'vbox',
          padding: 4
        },

        items: [
          {
            xtype: 'label',
            text: 'Type Styles'
          },
          {
            xtype: 'panel',

            layout: {
              type: 'table',
              columns: 6,
            },

            items: [
              { html: 'Name' }, { html: 'Description' }, { html: 'Font & Weight' }, { html: 'Use Cases' }, { html: 'Pixels' }, { html: 'Sample' },
              { html: 'h1' }, { html: 'Page Title' }, { html: 'Proxima Nova Light' }, { html: 'Header' }, { html: '40' }, { html: 'Sonatype Nexus' },
              { html: 'h2' }, { html: 'Header' }, { html: 'Proxima Nova Bold' }, { html: 'Header' }, { html: '26' }, { html: 'Development' },
              { html: 'h3' }, { html: 'Header' }, { html: 'Proxima Nova Bold' }, { html: 'Header' }, { html: '22' }, { html: 'Development' },
              { html: 'h4' }, { html: 'Sub-Header' }, { html: 'Proxima Nova Bold' }, { html: 'Sub-Header' }, { html: '18' }, { html: 'Development' },
              { html: 'h5' }, { html: 'Sub-Header' }, { html: 'Proxima Nova Bold' }, { html: 'Sub-Header' }, { html: '16' }, { html: 'Development' },
              { html: 'p/ul/ol' }, { html: 'Body' }, { html: 'Proxima Nova Regular' }, { html: 'Body text, lists, default size' }, { html: '16' }, { html: 'Development' },
              { html: 'code' }, { html: 'Code' }, { html: 'Source Code Pro Regular' }, { html: 'Code examples' }, { html: '16' }, { html: 'Development' },
              { html: 'utility' }, { html: 'Small Text' }, { html: 'Proxima Nova Regular' }, { html: 'Labels, Side-Nav' }, { html: '16' }, { html: 'Development' }
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
        xtype: 'panel',

        layout: {
          type: 'vbox',
          padding: 4
        },

        items: [

          // Section title
          {
            xtype: 'label',
            text: 'Buttons'
          },

          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'panel',

                layout: {
                  type: 'hbox'
                },

                items: [
                  {
                    xtype: 'panel',

                    layout: {
                      type: 'vbox'
                    },

                    items: [
                      {
                        xtype: 'label',
                        text: 'Default'
                      },
                      {
                        xtype: 'panel',

                        layout: {
                          type: 'table',
                          columns: 2
                        },

                        items: [
                          {
                            xtype: 'panel',

                            layout: {
                              type: 'hbox'
                            },

                            items: [
                              { xtype: 'button', text: 'normal', glyph: 'xf000@FontAwesome', ui: 'default' },
                              {
                                xtype: 'panel',

                                layout: {
                                  type: 'vbox'
                                },

                                items: [
                                  {
                                    xtype: 'panel',

                                    layout: {
                                      type: 'hbox'
                                    },

                                    items: [
                                      {
                                        xtype: 'panel',
                                        width: 10,
                                        height: 10,
                                        bodyCls: 'color-white'
                                      },
                                      {
                                        xtype: 'label',
                                        text: '#FFFFFF'
                                      }
                                    ]
                                  },
                                  {
                                    xtype: 'panel',

                                    layout: {
                                      type: 'hbox'
                                    },

                                    items: [
                                      {
                                        xtype: 'panel',
                                        width: 10,
                                        height: 10,
                                        bodyCls: 'color-light-gainsboro'
                                      },
                                      {
                                        xtype: 'label',
                                        text: '#E6E6E6'
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            xtype: 'panel',

                            layout: {
                              type: 'hbox'
                            },

                            items: [
                              { xtype: 'button', text: 'normal', glyph: 'xf000@FontAwesome', ui: 'default' },
                              {
                                xtype: 'panel',

                                layout: {
                                  type: 'vbox'
                                },

                                items: [
                                  {
                                    xtype: 'panel',

                                    layout: {
                                      type: 'hbox'
                                    },

                                    items: [
                                      {
                                        xtype: 'panel',
                                        width: 10,
                                        height: 10,
                                        bodyCls: 'color-light-gray'
                                      },
                                      {
                                        xtype: 'label',
                                        text: '#CBCBCB'
                                      }
                                    ]
                                  },
                                  {
                                    xtype: 'panel',

                                    layout: {
                                      type: 'hbox'
                                    },

                                    items: [
                                      {
                                        xtype: 'panel',
                                        width: 10,
                                        height: 10,
                                        bodyCls: 'color-silver'
                                      },
                                      {
                                        xtype: 'label',
                                        text: '#B8B8B8'
                                      }
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
                ]
              }
            ]
          },

          // Default buttons
          {
            xtype: 'panel',
            layout: {
              type: 'hbox',
              padding: 4
            },
            defaults: {
              width: 100
            },
            items: [
              { xtype: 'button', text: 'disabled', glyph: 'xf000@FontAwesome',  ui: 'default', disabled: true },
              { xtype: 'button', text: 'menu', glyph: 'xf000@FontAwesome', ui: 'default', menu: [ { text: 'First' }, '-', { text: 'Second' } ] }
            ]
          },

          // Primary buttons
          {
            xtype: 'panel',
            layout: {
              type: 'hbox',
              padding: 4
            },
            defaults: {
              width: 100
            },
            items: [
              { xtype: 'label', text: "ui: 'primary'" },
              { xtype: 'button', text: 'normal', glyph: 'xf000@FontAwesome', ui: 'primary' },
              { xtype: 'button', text: 'disabled', glyph: 'xf000@FontAwesome',  ui: 'primary', disabled: true },
              { xtype: 'button', text: 'menu', glyph: 'xf000@FontAwesome', ui: 'primary', menu: [ { text: 'First' }, '-', { text: 'Second' } ] }
            ]
          },

          // Danger buttons
          {
            xtype: 'panel',
            layout: {
              type: 'hbox',
              padding: 4
            },
            defaults: {
              width: 100
            },
            items: [
              { xtype: 'label', text: "ui: 'danger'" },
              { xtype: 'button', text: 'normal', glyph: 'xf000@FontAwesome', ui: 'danger' },
              { xtype: 'button', text: 'disabled', glyph: 'xf000@FontAwesome',  ui: 'danger', disabled: true },
              { xtype: 'button', text: 'menu', glyph: 'xf000@FontAwesome', ui: 'danger', menu: [ { text: 'First' }, '-', { text: 'Second' } ] }
            ]
          },

          // Warning buttons
          {
            xtype: 'panel',
            layout: {
              type: 'hbox',
              padding: 4
            },
            defaults: {
              width: 100
            },
            items: [
              { xtype: 'label', text: "ui: 'warning'" },
              { xtype: 'button', text: 'normal', glyph: 'xf000@FontAwesome', ui: 'warning' },
              { xtype: 'button', text: 'disabled', glyph: 'xf000@FontAwesome',  ui: 'warning', disabled: true },
              { xtype: 'button', text: 'menu', glyph: 'xf000@FontAwesome', ui: 'warning', menu: [ { text: 'First' }, '-', { text: 'Second' } ] }
            ]
          },

          // Success buttons
          {
            xtype: 'panel',
            layout: {
              type: 'hbox',
              padding: 4
            },
            defaults: {
              width: 100
            },
            items: [
              { xtype: 'label', text: "ui: 'success'" },
              { xtype: 'button', text: 'normal', glyph: 'xf000@FontAwesome', ui: 'success' },
              { xtype: 'button', text: 'disabled', glyph: 'xf000@FontAwesome',  ui: 'success', disabled: true },
              { xtype: 'button', text: 'menu', glyph: 'xf000@FontAwesome', ui: 'success', menu: [ { text: 'First' }, '-', { text: 'Second' } ] }
            ]
          },

          // Plain buttons
          {
            xtype: 'panel',
            layout: {
              type: 'hbox',
              padding: 4
            },
            defaults: {
              width: 100
            },
            items: [
              { xtype: 'label', text: "ui: 'plain'" },
              { xtype: 'button', text: 'normal', glyph: 'xf000@FontAwesome', ui: 'plain' },
              { xtype: 'button', text: 'disabled', glyph: 'xf000@FontAwesome',  ui: 'plain', disabled: true },
              { xtype: 'button', text: 'menu', glyph: 'xf000@FontAwesome', ui: 'plain', menu: [ { text: 'First' }, '-', { text: 'Second' } ] }
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
        xtype: 'label',
        text: 'Form Elements'
      },
      {
        xtype: 'panel',

        layout: {
          type: 'table',
          padding: 4,
          columns: 4
        },

        items: [
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200,
                value: 'Text Input'
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200,
                value: 'Text Input',
                allowBlank: false
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200,
                allowBlank: false
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200
              }
            ]
          },
          {
            xtype: 'panel',

            colspan: 2,

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textfield',
                width: 200
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200,
                value: 'Text Input'
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200,
                value: 'Text Input',
                allowBlank: false
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200,
                allowBlank: false
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200
              }
            ]
          },
          {
            xtype: 'panel',

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200
              }
            ]
          },
          {
            xtype: 'panel',

            colspan: 2,

            layout: {
              type: 'vbox',
              padding: 4
            },

            items: [
              {
                xtype: 'label',
                text: '[Label]'
              },
              {
                xtype: 'label',
                text: '[Optional description text]'
              },
              {
                xtype: 'textarea',
                width: 200
              }
            ]
          }
        ]
      },
      {
        xtype: 'panel',

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
    );

    /*
     * Notifications
     */

    me.items.push(
      {
        xtype: 'label',
        text: 'Notifications'
      }/*,
      {
        xtype: 'panel',

        layout: {
          type: 'hbox',
          padding: 4
        },

        items: [
          {
            xtype: 'nx-message-notification',
            ui: 'message-default',
            iconCls: NX.Icons.cls('message-default', 'x16'),
            title: 'Default',
            html: 'Message',
            autoClose: false,
            slideInAnimation: 'none',
            paddingX: 30
          },
          {
            xtype: 'nx-message-notification',
            ui: 'message-primary',
            iconCls: NX.Icons.cls('message-primary', 'x16'),
            title: 'Primary',
            html: 'Message',
            autoClose: false,
            slideInAnimation: 'none',
            paddingX: 30
          },
          {
            xtype: 'nx-message-notification',
            ui: 'message-danger',
            iconCls: NX.Icons.cls('message-danger', 'x16'),
            title: 'Danger',
            html: 'Message',
            autoClose: false,
            slideInAnimation: 'none',
            paddingX: 30
          },
          {
            xtype: 'nx-message-notification',
            ui: 'message-warning',
            iconCls: NX.Icons.cls('message-warning', 'x16'),
            title: 'Warning',
            html: 'Message',
            autoClose: false,
            slideInAnimation: 'none',
            paddingX: 30
          },
          {
            xtype: 'nx-message-notification',
            ui: 'message-success',
            iconCls: NX.Icons.cls('message-success', 'x16'),
            title: 'Success',
            html: 'Message',
            autoClose: false,
            slideInAnimation: 'none',
            paddingX: 30
          }
        ]
      }*/
    );

    /*
     * Modals
     */

    me.items.push(
      {
        xtype: 'label',
        text: 'Modals'
      }/*,
      {
        xtype: 'panel',

        layout: {
          type: 'hbox',
          padding: 4
        },

        items: [
          // TODO
        ]
      }*/
    );

    /*
     * Menu
     */

    /*
     * Header
     */

    /*
     * Tooltip
     */

    /*
     * Table
     */

    /*
     * Panels
     */

    /*
     * Tabs
     */

    /*
     * Picker
     */

    me.callParent();
  }
});