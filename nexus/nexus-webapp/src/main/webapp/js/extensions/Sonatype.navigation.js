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

  this.delayedItems = {};

  Sonatype.navigation.NavigationPanel.superclass.constructor.call( this, {
    cls: 'st-server-panel',
    layout:'fit',
    border: false
  });
};

Ext.extend( Sonatype.navigation.NavigationPanel, Ext.Panel, {
  add: function( c ) {
    var arr = null;
    var a = arguments;
    if ( a.length > 1 ) {
      arr = a;
    }
    else if ( Ext.isArray( c ) ) {
      arr = c;
    }
    if ( arr != null ) {
      for( var i = 0; i < arr.length; i++ ) {
        this.add( arr[i] );
      }
      return;
    }

    // check if this is an attempt to add a navigation item to an existing section
    if ( c.sectionId ) {
      var panel = this.findById( c.sectionId );
      if ( panel ) {
        return panel.add( c );
      }
      else {
        if ( this.delayedItems[c.sectionId] == null ) {
          this.delayedItems[c.sectionId] = [];
        }
        this.delayedItems[c.sectionId].push( c );
        return null;
      }
    }
    
    var panel = new Sonatype.navigation.Section( c );
    panel = Sonatype.navigation.NavigationPanel.superclass.add.call( this, panel );
    if ( panel.id && this.delayedItems[panel.id] ) {
      panel.add( this.delayedItems[panel.id] );
      this.delayedItems[panel.id] = null;
    }
    return panel;
  }
});


Sonatype.navigation.Section = function( config ) {
  var config = config || {};
  var defaultConfig = {
    collapsible: true,
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
    
    if ( ! c.xtype ) {
      if ( c.href ) {
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
    }
    return c;
  },

  add: function( c ) {
    var arr = null;
    var a = arguments;
    if ( a.length > 1 ) {
      arr = a;
    }
    else if ( Ext.isArray( c ) ) {
      arr = c;
    }
    if ( arr != null ) {
      for( var i = 0; i < arr.length; i++ ) {
        this.add( arr[i] );
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


Ext.namespace( 'Sonatype.menu' );

Sonatype.menu.Menu = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  Sonatype.menu.Menu.superclass.constructor.call( this );
};

Ext.extend( Sonatype.menu.Menu, Ext.menu.Menu, {
  add: function( c ) {
    if ( c == null ) return null;

    var a = arguments;
    if ( a.length > 1 ) {
      for( var i = 0; i < a.length; i++ ) {
        this.add( a[i] );
      }
      return;
    }

    var item = Sonatype.menu.Menu.superclass.add.call( this, c );
    var param = c.payload ? c.payload : this.payload;
    if ( c.handler && param ) {
      // create a delegate to pass the payload object to the handler
      item.setHandler( c.handler.createDelegate( c.scope ? c.scope : this.scope, [param], 0 ) );
    }
    return item;
  }
});
