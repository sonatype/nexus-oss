/*
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
Sonatype.repoServer.SearchPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  var detailViewTpl = new Ext.XTemplate([
    '<h2>Artifact Detail View</h3>',
    '<div class="result-item">',
      '<ul>',
        '<li>groupId:{groupId:htmlEncode}</li>',
        '<li>artifactId:{artifactId:htmlEncode}</li>',
        '<li>version:{version:htmlEncode}</li>',
        '<li>jarUrl:{jarUrl:htmlEncode}</li>',
      '</ul>',
    '</div>']
  );
  
  // render event handler config that overrides <a/> click events
  var linkInterceptor = {
    render: function(p){
      p.body.on(
        {
          'mousedown': function(e, t){ // try to intercept the easy way
            t.target = '_blank';
          },
          'click': function(e, t){ // if they tab + enter a link, need to do it old fashioned way
            if(String(t.target).toLowerCase() != '_blank'){
              e.stopEvent();
              window.open(t.href);
            }
          },
          delegate:'a'
        });
    }
  };
    
  this.grid = new Sonatype.repoServer.SearchResultGrid({
    
  });
  this.paramName = 'q';

  this.searchComponents = {
    'quick' : [
      {
        xtype: 'nexussearchfield',
        name: 'search-all-field',
        paramName: 'q',
        searchPanel: this,
        width: 300
      }
    ],
    'classname' : [
      {
        xtype: 'nexussearchfield',
        id: 'search-all-field',
        paramName: 'cn',
        searchPanel: this,
        width: 300
      }
    ],
    'gav' : [
      'Group:',
      { 
        xtype: 'textfield',
        id: 'gavsearch-group',
        size: 100,
        listeners: {
          'specialkey': {
            fn: this.gavEnterHandler,
            scope: this
          }
        }
      },
      { xtype: 'tbspacer' },
      'Artifact:',
      { 
        xtype: 'textfield',
        id: 'gavsearch-artifact',
        size: 100,
        listeners: {
          'specialkey': {
            fn: this.gavEnterHandler,
            scope: this
          }
        }
      },
      { xtype: 'tbspacer' },
      'Version:',
      { 
        xtype: 'textfield',
        id: 'gavsearch-version',
        size: 100,
        listeners: {
          'specialkey': {
            fn: this.gavEnterHandler,
            scope: this
          }
        }
      },
      { xtype: 'tbspacer' },
      'Packaging:',
      { 
        xtype: 'textfield',
        id: 'gavsearch-classifier',
        size: 100,
        listeners: {
          'specialkey': {
            fn: this.gavEnterHandler,
            scope: this
          }
        }
      },
      { xtype: 'tbspacer' },
      {
        icon: Sonatype.config.resourcePath + '/images/icons/search.gif',
        cls: 'x-btn-icon',
        scope: this,
        handler: this.startGAVSearch
      }
    ]
  };
  
  this.searchField = new Ext.app.SearchField({
    id: 'search-all-field',
    paramName: 'q',
    searchPanel: this,
    width: 300
  });

  this.appletPanel = new Ext.Panel({
    fieldLabel: '',
    html: '<div style="width:10px"></div>'
  });
  
  this.filenameLabel = null;
  
  this.sp = Sonatype.lib.Permissions;

  this.searchTypeButtonConfig = {
    text: 'Keyword Search',
    value: 'quick',
    tooltip: 'Click for more search options',
    handler: this.switchSearchType,
    scope: this,
    menu: {
      items: [
        {
          text: 'Keyword Search',
          value: 'quick',
          scope: this,
          handler: this.switchSearchType
        },
        {
          text: 'Classname Search',
          value: 'classname',
          scope: this,
          handler: this.switchSearchType
        },
        {
          text: 'GAV Search',
          value: 'gav',
          scope: this,
          handler: this.switchSearchType
        }
      ]
    }
  };

  if (this.sp.checkPermission('nexus:identify', this.sp.READ)){
    this.searchComponents['checksum'] = [
      {
        xtype: 'nexussearchfield',
        id: 'search-all-field',
        paramName: 'sha1',
        searchPanel: this,
        width: 300
      },
      {
        xtype: Ext.isGecko3 ? 'button' : 'browsebutton',
        text: 'Browse...',
        searchPanel: this,
        tooltip: 'Click to select a file. It will not be uploaded to the ' +
          'remote server, an SHA1 checksum is calculated locally and sent to ' +
          'Nexus to find a match. This feature requires Java applet ' +
          'support in your web browser.',
        handler: function( b ) {
          if ( ! document.digestApplet ) {
            b.searchPanel.grid.fetchMoreBar.addText(
              '<div id="checksumContainer" style="width:10px">' +
                '<applet code="org/sonatype/nexus/applet/DigestApplet.class" ' +
                  'archive="' + Sonatype.config.resourcePath + '/digestapplet.jar" ' +
                  'width="1" height="1" name="digestApplet"></applet>' +
                '</div>'
            ); 
          }

          var filename = null;
          
          if ( Ext.isGecko3 ) {
            filename = document.digestApplet.selectFile();
          }
          else {
            var fileInput = b.detachInputFile();
            filename = fileInput.getValue();
          }

          if ( ! filename ) {
            return;
          }
          
          b.disable();
          b.searchPanel.setFilenameLabel( b.searchPanel, 'Calculating checksum...' );

          var f = function( b, filename ) {
            var sha1 = 'error calculating checksum';
            if ( document.digestApplet ) {
              sha1 = document.digestApplet.digest( filename );
            }
              
            b.searchPanel.searchField.setRawValue( sha1 );
            b.searchPanel.setFilenameLabel( b.searchPanel, filename );
            b.enable();
            b.searchPanel.startSearch( b.searchPanel );
          }
          f.defer( 200, b, [b, filename] );
        }
      }
    ];
    this.searchTypeButtonConfig.menu.items[3] = {
      text: 'Checksum Search',
      value: 'checksum',
      scope: this,
      handler: this.switchSearchType
    };
  }
  
  this.gavFields = [];
  this.gavParams = [ 'g', 'a', 'v', 'p' ];

  this.searchTypeButton = new Ext.Button( this.searchTypeButtonConfig );

  var toolbaritems = [
    this.searchTypeButton,
    this.searchField
  ];
  
  this.searchToolbar = new Ext.Toolbar({
      ctCls:'search-all-tbar',
      items: toolbaritems
   });
  
  this.artifactInformationPanel = new Sonatype.repoServer.ArtifactInformationPanel({});
  
  Sonatype.repoServer.SearchPanel.superclass.constructor.call(this, {
    layout: 'border',
    hideMode: 'offsets',
    tbar: this.searchToolbar,
    items: [
      this.grid,
      this.artifactInformationPanel
    ]
  });

  this.on({
    'render' : function(){
      this.searchField.focus();
    },
    scope: this
  });
  
  this.grid.on( 'rowclick', this.displayArtifactInformation, this );
  this.grid.clearButton.on( 'click', this.clearArtifactInformation, this );
};

//@todo: generalize this search panel for other ST servers to use by providing their own store & reader
Ext.extend(Sonatype.repoServer.SearchPanel, Ext.Panel, {

  clearArtifactInformation: function( button, e ) {
    this.artifactInformationPanel.showArtifact( {
      groupId: '',
      artifactId: '',
      version: ''
    }, true );
  },

  displayArtifactInformation: function( grid, index, e ) {
    var rec = grid.store.getAt( index );
    this.artifactInformationPanel.showArtifact( rec.data );
  },
  
  startSearch: function( p ) {
    p.searchField.triggers[0].show();

    p.grid.store.baseParams = {};
    p.grid.store.baseParams[p.searchField.paramName] = p.searchField.getRawValue();
    p.fetchFirst50( p );
  },
  
  fetchFirst50: function( p ) {
    p.artifactInformationPanel.collapse();
    p.grid.totalRecords = 0;
    p.grid.store.load({
      params: {
        from: 0,
        count: 50
      }
    });
  },
  
  setFilenameLabel: function( p, s ) {
    if ( p.filenameLabel ) {
      p.filenameLabel.destroy();
    }
    p.filenameLabel = s ? p.searchToolbar.addText( '<span style="color:#808080;">'+s+'</span>' ) : null;
  },
  
  switchSearchType: function( button, event ) {
    this.setSearchType( this, button.value );
  },

  setSearchType: function( panel, t ) {
    if ( t != panel.searchTypeButton.value ) {
      var items = panel.searchTypeButtonConfig.menu.items;
      panel.searchTypeButton.value = t;
      for ( var i = 0; i < items.length; i++ ) {
        if ( items[i].value == t ) {
          panel.searchTypeButton.setText( items[i].text );
        }
      }
      
      panel.createToolbarItems( panel, t );
    }
  },
  
  createToolbarItems: function( panel, searchType ) {
    var oldSearchValue = '';
    if ( panel.searchField ) {
      oldSearchValue = panel.searchField.getRawValue();
    }

    while ( panel.searchToolbar.items.length > 1 ) {
      var item = panel.searchToolbar.items.last();
      panel.searchToolbar.items.remove( item );
      item.destroy();
    }

    if ( searchType == 'gav' ) {
      this.gavFields = [];
    }
    
    var items = panel.searchComponents[searchType];
    for ( var i = 0; i < items.length; i++ ) {
      var item = items[i];

      if ( item.xtype == 'nexussearchfield' ) {
        item = new Ext.app.SearchField( item ); 
      }
      else if ( item.xtype == 'textfield' ) {
        item = new Ext.form.TextField( item );
        this.gavFields[this.gavFields.length] = item;
      }

      panel.searchToolbar.add( item );
    }

    if ( oldSearchValue ) {
      panel.searchField.setRawValue( oldSearchValue );
      panel.searchField.triggers[0].show();
    }
  },

  startGAVSearch: function() {
    this.grid.store.baseParams = {};
    
    var n = 0;
    for ( var i = 0; i < this.gavFields.length; i++ ) {
      var v = this.gavFields[i].getRawValue();
      if ( v ) {
        this.grid.store.baseParams[this.gavParams[i]] = v;
        n++;
      }
    }
    
    if ( n ) {
      this.fetchFirst50( this );
    }
  },
  
  gavEnterHandler: function(f, e) {
    if(e.getKey() == e.ENTER){
      this.startGAVSearch();
    }
  }

});
