/*
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
/*
 * Repository Maintenance panel layout and controller
 */

/* config options:
  {
    id: the is of this panel instance [required]
    title: title of this panel (shows in tab)
  }
*/

Sonatype.repoServer.RepositoryPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Repositories'
  };
  Ext.apply( this, config, defaultConfig );

  var sp = Sonatype.lib.Permissions;

  this.repoStatusTask = {
    run: function() {
      if ( sp.checkPermission( 'nexus:repostatus', sp.READ ) ) {
        Ext.Ajax.request( {
          url: Sonatype.config.repos.urls.repositoryStatuses + ( this.forceStatuses ? '?forceCheck' : '' ),
          callback: this.statusCallback,
          scope: this
        } );
      }
      this.forceStatuses = false;
    },
    interval: 5000, // poll every 5 seconds
    scope: this
  };

  this.groupStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'resourceURI',
    fields: [
      { name: 'id' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString },
      { name: 'repoType', defaultValue: 'group' },
      { name: 'exposed', type: 'boolean', defaultValue: true },
      { name: 'userManaged', type: 'boolean', defaultValue: true },
      { name: 'resourceURI'},
      { name: 'format' },
      { name: 'policy' },
      { name: 'status' },
      { name:'repositories' },
      { name: 'displayStatus', mapping: 'repositories', 
        convert: function( v ) {
          return Sonatype.utils.joinArrayObject( v, 'name' );
        }
      },
      { name: 'displayURI', mapping: 'resourceURI',         
        convert: function( s ) {
          return Sonatype.config.repos.restToContentUrl( s );
        }
      }
    ],
    sortInfo: { field: 'name', direction: 'desc' },
    url: Sonatype.config.repos.urls.groups,
    listeners: {
      load: function( store, records, options ) {
        this.dataStore.insert( 0, store.data.items );
      },
      scope: this
    }
  } );

  this.browseTypeButton = new Ext.Button( {
    text: 'User Managed Repositories',
    icon: Sonatype.config.resourcePath + '/images/icons/page_white_stack.png',
    cls: 'x-btn-text-icon',
    value: 'user',
    tooltip: 'Click to browse other types of repositories.',
    scope: this,
    menu: {
      items: [
        {
          text: 'User Managed Repositories',
          value: 'user',
          checked: true,
          group: 'repo-view-selector',
          scope: this,
          handler: this.switchBrowseType
        },
        {
          text: 'Nexus Managed Repositories',
          value: 'nexus',
          checked: false,
          group: 'repo-view-selector',
          scope: this,
          handler: this.switchBrowseType
        }
      ]
    }
  } );
  
  var toolbar = [];
  if ( sp.checkPermission( 'nexus:wastebasket', sp.DELETE ) ) { 
    toolbar.push( {
      id: 'repo-trash-btn',
      text: 'Trash...',
      icon: Sonatype.config.resourcePath + '/images/icons/user-trash.png',
      cls: 'x-btn-text-icon',
      tooltip: { title: 'Trash', text: 'Manage the Trash contents' },
      menu: {
        width:125,
        items: [
          {
            text: 'Empty Trash',
            handler: this.deleteTrashHandler,
            scope: this
          }
        ]
      }
    } );
  }
  toolbar.push( this.browseTypeButton );

  Sonatype.Events.addListener( 'nexusRepositoryStatus', this.statusStart, this );
  
  Sonatype.repoServer.RepositoryPanel.superclass.constructor.call( this, {
    addMenuInitEvent: 'repositoryAddMenuInit',
    deleteButton: sp.checkPermission( 'nexus:repositories', sp.DELETE ),
    rowClickEvent: 'repositoryViewInit',
    rowContextClickEvent: 'repositoryMenuInit',
    url: Sonatype.config.repos.urls.repositories,
    dataAutoLoad: false,
    tabbedChildren: true,
    tbar: toolbar,
    columns: [
      { name: 'resourceURI' },
      { name: 'remoteUri' },
      { name: 'id' },
      { name: 'exposed' },
      { name: 'userManaged' },
      { name: 'status' },
      {
        name: 'name',
        sortType: Ext.data.SortTypes.asUCString,
        header: 'Repository', 
        width: 175,
        renderer: function( value, metadata, record, rowIndex, colIndex, store ) {
          return record.get('repoType') == 'group' ? ( '<b>' + value + '</b>' ) : value;
        }
      },
      { 
        name: 'repoType',
        header: 'Type', 
        width:50 
      },
      { 
        name: 'format',
        header: 'Format', 
        width: 50 
      },
      { 
        name: 'repoPolicy',
        header: 'Policy',
        width: 70
      },
      { 
        name: 'displayStatus',
        header: 'Repository Status',
        mapping: 'status',
        convert: Sonatype.repoServer.DefaultRepoHandler.statusConverter,
        width: 200
      },
      { 
        name: 'displayURI',
        header: 'Repository Path',
        autoExpand: true,
        renderer: function( s ) {
          return '<a href="' + s + ((s != null && (s.charAt(s.length)) == '/') ? '' : '/') +
            '" target="_blank">' + s + '</a>';
        },
        mapping: 'resourceURI',
        convert: function( s, parent ) {
          return Sonatype.config.repos.restToContentUrl( s );
        }
      }
    ]
  } );

  this.addListener( 'beforedestroy', function() { 
    Ext.TaskMgr.stop( this.repoStatusTask ); 
    Sonatype.Events.removeListener( 'nexusRepositoryStatus', this.statusStart, this );
  }, this );
  this.dataStore.addListener( 'load', this.onRepoStoreLoad, this );
  this.dataStore.load();
};

Ext.extend( Sonatype.repoServer.RepositoryPanel, Sonatype.panels.GridViewer, {
  
  applyBookmark: function( bookmark ) {
    if ( this.groupStore.lastOptions == null ) {
      this.groupStore.on( 'load', 
        function( store, recs, options ) {
          this.selectBookmarkedItem( bookmark );
        },
        this,
        { single: true } 
      );
    }
    else this.selectBookmarkedItem( bookmark );
  },

  deleteTrashHandler: function( button, e ) {
    Sonatype.utils.defaultToNo();
    
    Sonatype.MessageBox.show( {
      animEl: this.gridPanel,
      title: 'Empty Trash',
      msg : 'Delete the entire contents of the Trash?<br><br>This operation cannot be undone!',
      buttons: Sonatype.MessageBox.YESNO,
      scope: this,
      icon: Sonatype.MessageBox.QUESTION,
      fn: function(btnName){
        if ( btnName == 'yes' || btnName == 'ok' ) {
          Ext.Ajax.request( {
            callback: function( options, success, response ) {
              if ( ! success ) {
                Sonatype.utils.connectionError( response, 'Error emptying the trash!' );
              }
            },
            scope: this,
            method: 'DELETE',
            url:Sonatype.config.repos.urls.trash
          } );
        }
      }
    } );
  },
  
  onRepoStoreLoad: function( store, records, options ) {
    switch ( this.browseTypeButton.value ) {
      case 'nexus':
        for ( var i = 0; i < records.length; i++ ) {
          if ( records[i].data.userManaged ) {
            store.remove( records[i] );
          }
        }
        break;
      case 'user':
        this.groupStore.reload();
        break;
    }
    this.statusStart();
  },

  refreshHandler: function( button, e ) {
    if ( button == this.refreshButton ) {
      this.forceStatuses = true;
    }
    Sonatype.repoServer.RepositoryPanel.superclass.refreshHandler.call( this, button, e );
  },

  statusCallback : function( options, success, response ) {
    if ( response.status != 202 ) {
      Ext.TaskMgr.stop( this.repoStatusTask );
    }

    if ( success ) {
      var statusResp = Ext.decode( response.responseText );
      if ( statusResp.data ) {
        var data = statusResp.data;
        for ( var i = data.length - 1; i >= 0; i-- ) {
          var item = data[i];
          var rec = this.dataStore.getById( item.resourceURI.replace(
            Sonatype.config.repos.urls.repositoryStatuses,Sonatype.config.repos.urls.repositories ) );
          if ( rec ) {
            rec.beginEdit();
            rec.set( 'status', item.status );
            rec.set( 'displayStatus', Sonatype.repoServer.DefaultRepoHandler.statusConverter( item.status, item ) );
            rec.commit( true );
            rec.endEdit();
          }
        }
        if ( data.length ) {
          this.gridPanel.getView().refresh();
        }
      }
    }
    else {
      Sonatype.MessageBox.alert( 'Status retrieval failed' );
    }
  },

  statusStart: function() {
    Ext.TaskMgr.start( this.repoStatusTask );
  },
  
  switchBrowseType: function( button, e ) {
    this.browseTypeButton.setText( button.text );
    this.browseTypeButton.value = button.value;

    switch ( button.value ) {
      case 'nexus':
        if ( this.toolbarAddButton ) this.toolbarAddButton.disable();
        if ( this.toolbarDeleteButton ) this.toolbarDeleteButton.disable();
        this.dataStore.proxy.conn.url = Sonatype.config.repos.urls.allRepositories;
        break;
      case 'user':
        if ( this.toolbarAddButton ) this.toolbarAddButton.enable();
        if ( this.toolbarDeleteButton ) this.toolbarDeleteButton.enable();
        this.dataStore.proxy.conn.url = Sonatype.config.repos.urls.repositories;
        break;
    }
    this.refreshHandler( button, e );
  },
  
  showRecordContextMenu: function(rec) {
  	return rec.data.exposed;
  }
  
} );

Sonatype.repoServer.RepositoryBrowsePanel = function( config ) {
  var config = config || {};
  var defaultConfig = { 
    titleColumn: 'name'
  };
  Ext.apply( this, config, defaultConfig );
  
  this.oldSearchText = '';
  this.searchTask = new Ext.util.DelayedTask( this.startSearch, this, [this]);
  this.nodeContextMenuEvent = 'repositoryContentMenuInit';

  this.browseSelector = new Ext.Toolbar.Button(          
    {
      text: 'Browse Local Storage',
      icon: Sonatype.config.resourcePath + '/images/icons/page_white_stack.png',
      value: 0,
      cls: 'x-btn-text-icon',
      menu: {
        items: [
          {
            text: 'Browse Local Storage',
            value: 0,
            checked: true,
            group: 'repo-browse-selector-group',
            checkHandler: this.browseSelectorHandler,
            scope: this
          },
          {
            text: 'Browse Index',
            value: 1,
            checked: false,
            group: 'repo-browse-selector-group',
            checkHandler: this.browseSelectorHandler,
            scope: this,
            disabled: this.payload.data.repoType == 'virtual'
          }
        ]
      }
    }
  );
  
  Sonatype.repoServer.RepositoryBrowsePanel.superclass.constructor.call( this, {
    anchor: '0 -2',
    bodyStyle: 'background-color:#FFFFFF',
    animate: true,
    lines: false,
    autoScroll: true,
    containerScroll: true,
    rootVisible: true,
    enableDD: false,
    tbar: [
      {
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.refreshHandler
      },
      ' ',
      'Path Lookup:',
      {
        xtype: 'nexussearchfield',
        searchPanel: this,
        width: 400,
        enableKeyEvents: true,
        listeners: {
          'keyup': {
            fn: function( field, event ) {
              var key = event.getKey();
              if ( ! event.isNavKeyPress() ) {
                this.searchTask.delay( 200 );
              }
            },
            scope: this
          },
          'render': function(c) {
            Ext.QuickTips.register({
              target: c.getEl(),
              text: 'Enter a complete path to lookup, for example org/sonatype/nexus'
            });
          }
        }
      },
      ' ',
      this.browseSelector
    ],
    loader: new Ext.tree.SonatypeTreeLoader( {
      url: '',
      listeners: {
        loadexception: this.treeLoadExceptionHandler,
        scope: this
      }
    } ),
    listeners: {
      click: this.nodeClickHandler,
      contextMenu: this.nodeContextMenuHandler,
      expandnode: this.indexBrowserExpandFollowup,
      scope: this
    } 
  } );

  new Ext.tree.TreeSorter( this, { folderSort:true } );

  var root = new Ext.tree.AsyncTreeNode( {
    text: this.payload.data[this.titleColumn],
    id: this.payload.data.resourceURI + Sonatype.config.browsePathSnippet + '/',
    singleClickExpand: true,
    expanded: true
  } );
  
  this.setRootNode( root );
};

Ext.extend( Sonatype.repoServer.RepositoryBrowsePanel, Ext.tree.TreePanel, {

  browseSelectorHandler: function( item, e ) {
    if ( this.browseSelector.value != item.value ) {
      this.browseSelector.value = item.value;
      this.browseSelector.setText( item.text );
      this.browseIndex = item.value == 1;

      this.refreshHandler( item, e );
    }
  },

  getBrowsePath: function( baseUrl ) {
    return baseUrl + this.getBrowsePathSnippet() + '/'; 
  },

  getBrowsePathSnippet: function() {
    return this.browseIndex ?
      Sonatype.config.browseIndexPathSnippet : Sonatype.config.browsePathSnippet;
  },
  
  indexBrowserExpandFollowup: function( node ) {
    if ( this.browseIndex && ! node.attributes.localStorageUpdated && node.firstChild ) {
      node.attributes.localStorageUpdated = true;
      Ext.Ajax.request({
        url: node.id.replace( Sonatype.config.browseIndexPathSnippet, Sonatype.config.browsePathSnippet ) + '?isLocal',
        suppressStatus: 404,
        success: function( response, options ) {
          var decodedResponse = Ext.decode( response.responseText );
          if ( decodedResponse.data ) {
            var data = decodedResponse.data;
            for ( var j = 0; j < node.childNodes.length; j++ ) {
              var indexNode = node.childNodes[j];
              indexNode.attributes.localStorageUpdated = true;
              for ( var i = 0; i < data.length; i++ ) {
                var contentNode = data[i];
                if ( contentNode.text == indexNode.text ) {
                  indexNode.ui.iconNode.className = 'x-tree-node-nexus-icon';
                  indexNode.attributes.localStorageUpdated = false;
                  break;
                }
              }
            }
          }
        },
        failure: function( response, options ) {
          for ( var j = 0; j < node.childNodes.length; j++ ) {
            node.childNodes[j].attributes.localStorageUpdated = true;
          }
        },
        scope: this
      });
    }
  },

  nodeClickHandler: function( node, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( this.nodeClickEvent ) {
      Sonatype.Events.fireEvent( this.nodeClickEvent, node );
    }
  },

  nodeContextMenuHandler: function( node, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( !this.payload.data.showCtx ) {
    	return;
    }

    if ( this.nodeContextMenuEvent ) { 

      node.attributes.repoRecord = this.payload;
      node.data = node.attributes;
  
      var menu = new Sonatype.menu.Menu( {
        payload: node,
        scope: this,
        items: []
      } );
  
      Sonatype.Events.fireEvent( this.nodeContextMenuEvent, menu, this.payload, node );

      var item;
      while ( ( item = menu.items.first() ) && ! item.text ) {
        menu.remove( item ); // clean up if the first element is a separator
      }
      while ( ( item = menu.items.last() ) && ! item.text ) {
        menu.remove( item ); // clean up if the last element is a separator
      }
      if ( ! menu.items.first() ) return;

      e.stopEvent();
      menu.showAt( e.getXY() );
    }
  },

  refreshHandler: function( button, e ) {
    this.root.setText( this.payload ? this.payload.get( this.titleColumn ) : '/' );
    this.root.attributes.localStorageUpdated = false;
    this.root.id = this.getBrowsePath( this.payload.data.resourceURI );
    this.root.reload();
  },

  startSearch: function( p ) {
    var field = p.searchField;
    var searchText = field.getRawValue();

    var treePanel = p;
    if ( searchText ) {
      field.triggers[0].show();
      var justEdited = p.oldSearchText.length > searchText.length;

      var findMatchingNodes = function( root, textToMatch ) {
        var n = textToMatch.indexOf( '/' );
        var remainder = '';
        if ( n > -1 ) {
          remainder = textToMatch.substring( n + 1 );
          textToMatch = textToMatch.substring( 0, n );
        }

        var matchingNodes = [];
        var found = false;
        for ( var i = 0; i < root.childNodes.length; i++ ) {
          var node = root.childNodes[i];

          var text = node.text;
          if ( text == textToMatch ) {
            node.enable();
            node.ensureVisible();
            node.expand();
            found = true;
            if ( ! node.isLeaf() ) {
              var autoComplete = false;
              if ( ! remainder && node.childNodes.length == 1 ) {
                remainder = node.firstChild.text;
                autoComplete = true;
              }
              if ( remainder ) {
                var s = findMatchingNodes( node, remainder );
                if ( autoComplete || ( s && s != remainder ) ) {
                  return textToMatch + '/' + ( s ? s : remainder );
                }
              }
            }
          }
          else if ( text.substring( 0, textToMatch.length ) == textToMatch ) {
            matchingNodes[matchingNodes.length] = node;
            node.enable();
            if ( matchingNodes.length == 1 ) {
              node.ensureVisible();
            }
          }
          else {
            node.disable();
            node.collapse( false, false );
          }
        }
        
        // if only one non-exact match found, suggest the name
        return ! found && matchingNodes.length == 1 ?
          matchingNodes[0].text + '/' : null;
      };
      
      var s = findMatchingNodes( treePanel.root, searchText );

      p.oldSearchText = searchText;

      // if auto-complete is suggested, and the user hasn't just started deleting
      // their own typing, try the suggestion
      if ( s && ! justEdited && s != searchText ) {
        field.setRawValue( s );
        p.startSearch( p );
      }

    }
    else {
      p.stopSearch( p );
    }
  },

  stopSearch: function( p ) {
    p.searchField.triggers[0].hide();
    p.oldSearchText = '';

    var treePanel = p;

    var enableAll = function( root ) {
      for ( var i = 0; i < root.childNodes.length; i++ ) {
        var node = root.childNodes[i];
        node.enable();
        node.collapse( false, false );
        enableAll( node );
      }
    };
    enableAll( treePanel.root );
  },

  treeLoadExceptionHandler : function( treeLoader, node, response ) {
    if ( response.status == 503 ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();
      }
      node.setText( node.text + ' (Out of Service)' );
    }
    else if ( response.status == 404 || response.status == 400 ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();
      }
      node.setText( node.text + ( node.isRoot ? ' (Not Available)' : ' (Not Found)' ) );
    }
    else if ( response.status == 401 ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();   
      }
      node.setText( node.text + ' (Access Denied)' );
    }
  }
  
} );

Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  if ( rec.data.resourceURI ) {
    cardPanel.add( new Sonatype.repoServer.RepositoryBrowsePanel( { 
      payload: rec,
      tabTitle: 'Browse'
    } ) );
  }
} );
