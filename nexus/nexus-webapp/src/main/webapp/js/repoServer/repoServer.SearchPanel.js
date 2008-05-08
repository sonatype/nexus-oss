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

//this.detailView = new Ext.Panel({
//    id: 'st-artifact-detail-view',
//    region: 'south',
//    layout: 'fit',
//    cls:'preview',
//    autoScroll: true,
//    height: 300,
//    split: true,
//    border:false,
//    listeners: linkInterceptor
//});
    
  this.grid = new Sonatype.repoServer.SearchResultGrid({
    
  });
  this.paramName = 'q';
  
  this.searchField = new Ext.app.SearchField({
    id: 'search-all-field',
    searchPanel: this,
    width: 400
  });

  this.appletPanel = new Ext.Panel({
    fieldLabel: '',
    html: '<div style="width:10px"></div>'
  });
  
  this.filenameLabel = null;
  
  this.searchToolbar = new Ext.Toolbar({
    ctCls:'search-all-tbar',
    items: [
      'Search:',
      this.searchField,
      { xtype: 'tbspacer' },
      new Ext.ux.form.BrowseButton({
        text: 'Checksum Search...',
        appletPanel: true,
        searchPanel: this,
        tooltip: 'Click to select a file. It will not be uploaded to the ' +
          'remote server, an SHA1 checksum is calculated locally and sent to ' +
          'Nexus to find a match. This feature requires Java applet ' +
          'support in your web browser.',
        handler: function( b ) {
          var filename = b.detachInputFile().getValue();

          if ( b.appletPanel ) {
            b.searchPanel.searchToolbar.addText(
              '<div id="checksumContainer" style="width:10px">' +
                '<applet code="org/sonatype/nexus/applet/DigestApplet.class" ' +
                  'archive="' + Sonatype.config.resourcePath + '/digestapplet.jar" ' +
                  'width="1" height="1" name="digestApplet"></applet>' +
                '</div>'
            ); 
            b.appletPanel = false;
          }

          b.disable();
          if ( document.digestApplet ) {
            b.searchPanel.setFilenameLabel( b.searchPanel, 'Loading...' );
            var f = function( b, filename ) {
              b.searchPanel.searchField.setRawValue(
                document.digestApplet.digest( filename ) );
              b.searchPanel.setFilenameLabel( b.searchPanel, filename );
              b.enable();
              b.searchPanel.startSearch( b.searchPanel );
            }
            f.defer( 200, b, [b, filename] );
          }
        }
      })
    ]
  });
  
  
  Sonatype.repoServer.SearchPanel.superclass.constructor.call(this, {
//  id: 'st-nexus-search-panel',
//  title: 'Nexus Search',
    layout: 'border',
    hideMode: 'offsets',
    tbar: this.searchToolbar,
    items: [
      this.grid
//    this.detailView
    ]
  });

//@note: commented out detail view  
//this.gsm = this.grid.getSelectionModel();
//
//this.gsm.on('rowselect', function(sm, index, record){
//    detailViewTpl.overwrite(this.detailView.body, record.data);
//}, this, {buffer:250}); //@todo: reduce the delay for invocation (buffer value)
//
//this.grid.store.on({
//  'datachanged' : function(){
//    this.detailView.body.update('');
//  },
//  'clear' : function(){
//    this.detailView.body.update('');
//  },
//  scope: this
//});
  
  this.on({
    'render' : function(){
      this.searchField.focus();
    },
    scope: this
  });
};

//@todo: generalize this search panel for other ST servers to use by providing their own store & reader
Ext.extend(Sonatype.repoServer.SearchPanel, Ext.Panel, {
  
  resetSearch: function( p ) {
    p.grid.store.baseParams = p.grid.store.baseParams || {};
    p.grid.store.baseParams[p.paramName] = '';
    //p.grid.store.reload({params:o});
    p.grid.store.removeAll();
    p.setFilenameLabel( p );
  },

  startSearch: function( p ) {
    p.searchField.hasSearch = true;
    p.searchField.triggers[0].show();

    var v = p.searchField.getRawValue();
    if ( v.search(/^[0-9a-f]{40}$/) == 0 ) {
      p.grid.store.removeAll();
      Ext.Ajax.request( {
        url: Sonatype.config.repos.urls.identify + '/' + v,
        callback: function(options, success, response) {
          if ( success && response.responseText ) {
            var statusResp = Ext.decode(response.responseText);
            if ( statusResp ) {
              this.grid.store.loadData({data:[statusResp]});
            }
          }
        },
        scope: p
      } );
    }
    else {
      p.grid.store.baseParams = p.grid.store.baseParams || {};
      p.grid.store.baseParams[p.paramName] = v;
      p.grid.store.reload();//{params:o});
    }
  },
  
  setFilenameLabel: function( p, s ) {
    if ( p.filenameLabel ) {
      p.filenameLabel.destroy();
    }
    p.filenameLabel = s ? p.searchToolbar.addText( '<span style="color:#808080;">'+s+'</span>' ) : null;
  }
});