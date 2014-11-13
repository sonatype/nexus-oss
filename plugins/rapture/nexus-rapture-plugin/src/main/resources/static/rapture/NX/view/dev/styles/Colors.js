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
 * Color styles.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.styles.Colors', {
  extend: 'NX.view.dev.styles.StyleSection',
  requires: [
    'Ext.XTemplate'
  ],

  title: 'Colors',

  /**
   * @protected
   */
  initComponent: function () {
    var me = this;

    var rowTemplate = Ext.create('Ext.XTemplate',
        '<div>',
        '<tpl for=".">',
        '<div class="hbox">{.}</div>',
        '</tpl>',
        '</div>'
    );

    var columnTemplate = Ext.create('Ext.XTemplate',
        '<div>',
        '<tpl for=".">',
        '<div class="vbox">{.}</div>',
        '</tpl>',
        '</div>'
    );

    var labelTemplate = Ext.create('Ext.XTemplate',
        '<span class="{clz}">{text}</span>'
    );

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

    me.items = [
      {
        xtype: 'container',
        layout: {
          type: 'vbox',
          padding: 4
        },
        items: [
          me.html(columnTemplate.apply([
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
          ])),

          me.html(rowTemplate.apply([
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
          ])),

          me.html(columnTemplate.apply([
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
          ])),

          me.html(rowTemplate.apply([
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
          ]))
        ]
      }
    ];

    me.callParent();
  }
});