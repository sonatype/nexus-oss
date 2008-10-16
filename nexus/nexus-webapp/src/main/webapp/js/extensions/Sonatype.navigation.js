/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */

Ext.namespace( 'Sonatype.navigation' );

Sonatype.navigation.NavigationPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  Sonatype.navigation.NavigationPanel.superclass.constructor.call( this, {
    cls: 'st-server-panel',
    layout:'fit',
    border: false
  });
};

Ext.extend( Sonatype.navigation.NavigationPanel, Ext.Panel, {
  add: function( c ) {
    var a = arguments;
    if ( a.length > 1 ) {
      for( var i = 0; i < a.length; i++ ) {
        this.add( a[i] );
      }
      return;
    }

    // check if this is an attempt to add a navigation item to an existing section
    if ( c.sectionId ) {
      var panel = this.findById( c.sectionId );
      return panel ? panel.add( c ) : null;
    }
    
    var panel = new Sonatype.navigation.Section( c );
    return Sonatype.navigation.NavigationPanel.superclass.add.call( this, panel );
  }
});


Sonatype.navigation.Section = function( config ) {
  var config = config || {};
  var defaultConfig = {
    collapsible: false,
    collapsed: false
  };
  
  if ( config.items ) {
    config.items = this.transformItem( config.items );
  }
  if ( ! config.items || config.items.length == 0 ) {
    config.hidden = true;
  }
  
  Ext.apply( this, config, defaultConfig );

  Sonatype.navigation.Section.superclass.constructor.call( this, {
    cls: 'st-server-sub-container',
    layout: 'fit',
    frame: true,
    autoHeight: true
  });
};

Ext.extend( Sonatype.navigation.Section, Ext.Panel, {
  transformItem: function( c ) {
    if ( ! c ) return null;

    if ( Ext.isArray( c ) ) {
      var c2 = [];
      for ( var i = 0; i < c.length; i++ ) {
        var item = this.transformItem( c[i] );
        if ( item ) {
          c2.push( item );
        }
      }
      return c2;
    }
    else if ( c.href ) {
      // regular external link
      return {
        autoHeight: true,
        html: '<ul class="group-links"><li><a href="' + c.href + '" target="' + c.href + '">' + c.title + '</a></li></ul>'
      }
    }
    else if ( c.tabCode || c.handler ) {
      // panel open action
      return c.enabled == false ? null :
      {
        autoHeight: true,
        listeners: {
          render: {
            fn: function( panel ) {
              panel.body.on(
                'click',
                Ext.emptyFn,
                null,
                { delegate: 'a', preventDefault: true } );
              panel.body.on(
                'mousedown',
                function( e, target ) {
                  e.stopEvent();
                  if ( c.handler ) {
                    c.handler();
                  }
                  else {
                    Sonatype.view.mainTabPanel.addOrShowTab( c.tabId, c.tabCode, { title: c.tabTitle ? c.tabTitle : c.title } );
                  }
                },
                c.scope,
                { delegate: 'a' } );
            },
            scope: this
          }
        },
        html: '<ul class="group-links"><li><a href="#">' + c.title + '</a></li></ul>'
      }
    }
    return c;
  },

  add: function( c ) {
    var a = arguments;
    if ( a.length > 1 ) {
      for( var i = 0; i < a.length; i++ ) {
        this.add( a[i] );
      }
      return;
    }

    var c = this.transformItem( c );
    if ( c == null ) return;
    
    if ( this.hidden ) {
      this.show();
    }
    return Sonatype.navigation.Section.superclass.add.call( this, c );
  }
});
