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
 
Sonatype.repoServer.LogsViewPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  this.currentLogUrl = null;
  this.currentContentType = null;
  this.totalSize = 0;
  this.currentSize = 0;
  
  this.listeners = {
    'beforerender' : {
      fn: this.updateFileList,
      scope: this
    }
  };

  this.fetchMoreButton = new Ext.SplitButton({
    text: 'Fetch Next 100Kb',
    icon: Sonatype.config.resourcePath + '/images/icons/search.gif',
    cls: 'x-btn-text-icon',
    value: '100',
    handler: this.fetchMore,
    disabled: true,
    scope: this,
    menu: {
      items: [
        {
          text: 'Fetch Next 100Kb',
          value: '100',
          scope: this,
          handler: this.fetchMore
        },
        {
          text: 'Fetch Next 200Kb',
          value: '200',
          scope: this,
          handler: this.fetchMore
        },
        {
          text: 'Fetch Next 500Kb',
          value: '500',
          scope: this,
          handler: this.fetchMore
        }
      ]
    }
    
  });

  this.fetchMoreBar = new Ext.Toolbar({
    ctCls: 'search-all-tbar',
    items: [ 
      'Displaying 0 of 0Kb',
      this.fetchMoreButton
    ]
  });

  Sonatype.repoServer.LogsViewPanel.superclass.constructor.call(this, {
    autoScroll: false,
    border: false,
    frame: false,
    collapsible: false,
    collapsed: false,
    tbar: [
      {
        text: "Refresh",
        tooltip: {text:'Reloads the current document'},
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        handler: this.reloadLogFile,
        scope: this
      },
      {
        text: 'Download',
        icon: Sonatype.config.resourcePath + '/images/icons/page_white_put.png',
        cls: 'x-btn-text-icon',
        scope:this,
        handler: function(){
          if ( this.currentLogUrl ) {
            window.open(this.currentLogUrl);
          }
        }
      },
      {
        id: 'log-btn',
        text:'Select a document...',
        icon: Sonatype.config.resourcePath + '/images/icons/page_white_stack.png',
        cls: 'x-btn-text-icon',
        tooltip: {text:'Select the file to display'},
        //handler: this.movePreview.createDelegate(this, []),
        menu:{
          id:'log-menu',
          width:200,
          items: [
            {
              text: 'nexus.xml',
              value: 0,
              checked: false,
              group:'rp-group',
              checkHandler: this.logMenuBtnClick.createDelegate(this, [Sonatype.config.repos.urls.configCurrent,'application/xml'], 0),
              scope:this
            }
          ]
        }
      }      
    ],
    bbar: this.fetchMoreBar,
    items: [
      {
        xtype: 'textarea',
        id: 'log-text',
        readOnly: true,
        hideLabel: true,
        anchor: '100% 100%',
        emptyText: 'Select a document to view'
      }
    ]
  });
  
  this.logTextArea = this.findById('log-text');
};


Ext.extend(Sonatype.repoServer.LogsViewPanel, Ext.form.FormPanel, {
  renderLogList : function(options, success, response){
    if (success){
      var resp = Ext.decode(response.responseText);
      var myMenu = Ext.menu.MenuMgr.get('log-menu');

      for (var i=0; i< resp.data.length; i++) {
        var name = resp.data[i].name;
        var size = resp.data[i].size;
        var text = name + ' (' + this.printKb(size) + ')';
        var uri = resp.data[i].resourceURI;

        var existingItem = myMenu.items.find( function( o ) {
          return o.id == uri;
        } );

        if ( existingItem ) {
          existingItem.setText( text );
          existingItem.value = size;
          if ( this.currentLogUrl == uri ) {
            this.totalSize = size;
            this.getTopToolbar().items.get( 2 ).setText( text );
            this.updateTotals();
          }
        }
        else {
          myMenu.addMenuItem({
            id: uri, 
            text: text,
            value: size,
            checked: false,
            group:'rp-group',
            checkHandler: this.logMenuBtnClick.createDelegate(this, [uri,'text/plain'], 0),
            scope:this
          });
        }
      }
    }
    else {
      Sonatype.MessageBox.alert('Failed to get file list from server.');
    }
  },
  
  logMenuBtnClick : function(resourceURI, contentType, mItem, pressed){
    if ( ! pressed ) return;
    this.currentSize = 0;
    this.totalSize = mItem.value;
    this.currentContentType = contentType;
    this.getTopToolbar().items.get(2).setText(mItem.text);
    this.currentLogUrl = resourceURI;
    this.getLogFile();
  },
  
  //gets the log file specified by this.currentLogUrl
  reloadLogFile : function(){
    this.currentSize = 0;
    this.getLogFile();
  },
  
  //gets the log file specified by this.currentLogUrl
  getLogFile : function(){
    //Don't bother refreshing if no files are currently shown
    if (this.currentLogUrl){
      var toFetch = Number(this.fetchMoreButton.value) * 1024;
      if ( toFetch == 0 ) {
        toFetch = this.totalSize - this.currentSize;
      }
      Ext.Ajax.request({
        callback: this.renderLog,
        scope: this,
        method: 'GET',
        params: {
          from: this.currentSize,
          count: toFetch
        },
        headers: {'accept' : this.currentContentType ? this.currentContentType : 'text/plain'},
        url: this.currentLogUrl
      });
    }
  },
  
  renderLog : function(options, success, response){
    if (success){
      var newValue = this.currentSize == 0 ? response.responseText :
        this.logTextArea.getRawValue() + response.responseText;
      this.logTextArea.setRawValue(newValue);
      this.currentSize += response.responseText.length;
      this.updateTotals();
      this.updateFileList();
    }
    else {
      Sonatype.utils.connectionError( response, 'The file failed to load from the server.' )
    }
  },

  fetchMore: function( button, event ) {
    if ( button.value != this.fetchMoreButton.value ) {
      this.fetchMoreButton.value = button.value;
      this.fetchMoreButton.setText( button.text );
    }
    this.getLogFile();
  },
  
  updateTotals: function() {
    if ( this.currentSize == 0 || this.currentSize > this.totalSize ) {
      this.totalSize = this.currentSize;
    }
    
    this.fetchMoreBar.items.items[0].destroy();
    this.fetchMoreBar.items.removeAt( 0 );
    this.fetchMoreBar.insertButton( 0, new Ext.Toolbar.TextItem(
      'Displaying ' + this.printKb(this.currentSize) + ' of ' + this.printKb(this.totalSize) ) );

    this.fetchMoreButton.setDisabled( this.currentSize >= this.totalSize );
  },
  
  printKb: function( n ) {
    var kb = 'b';
    if ( n > 1024 ) {
      n = (n/1024).toFixed(0);
      kb = 'Kb';
    }
    if ( n > 1024 ) {
      n = (n/1024).toFixed(1);
      kb = 'Mb';
    }
    return n + ' ' + kb;
  },
  
  updateFileList: function() {
    Ext.Ajax.request({
      callback: this.renderLogList,
      scope: this,
      method: 'GET',
      url: Sonatype.config.repos.urls.logs
    });
    return true;
  }
});