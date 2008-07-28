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
  
  this.listeners = {
    'beforerender' : function(){
    //note: this isn't pre-render dependent, we just need an early event to start this off
      Ext.Ajax.request({
        callback: this.renderLogList,
        scope: this,
        method: 'GET',
        url: Sonatype.config.repos.urls.logs
      });
      return true;
    },
    scope: this
  };
  
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
        handler: this.getLogFile,
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
              checked: false,
              group:'rp-group',
              checkHandler: this.logMenuBtnClick.createDelegate(this, [Sonatype.config.repos.urls.configCurrent,'application/xml'], 0),
              scope:this
            }
          ]
        }
      }      
    ],
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
        myMenu.addMenuItem({
          text: resp.data[i].name,
          checked: false,
          group:'rp-group',
          checkHandler: this.logMenuBtnClick.createDelegate(this, [resp.data[i].resourceURI,'text/plain'], 0),
          scope:this
        });
      }
    }
    else {
      Sonatype.MessageBox.alert('Failed to get file list from server.');
    }
  },
  
  logMenuBtnClick : function(resourceURI, contentType, mItem, pressed){
    if ( ! pressed ) return;
    this.currentContentType = contentType;
    this.getTopToolbar().items.get(2).setText(mItem.text);
    this.currentLogUrl = resourceURI;
    this.getLogFile(contentType);
  },
  
  //gets the log file specified by this.currentLogUrl
  getLogFile : function(){
    //Don't bother refreshing if no files are currently shown
    if (this.currentLogUrl){
      Ext.Ajax.request({
        callback: this.renderLog,
        scope: this,
        method: 'GET',
        headers: {'accept' : this.currentContentType ? this.currentContentType : 'text/plain'},
        url: this.currentLogUrl
      });
    }
  },
  
  renderLog : function(options, success, response){
    if (success){
      this.logTextArea.setRawValue(response.responseText);
    }
    else {
      Sonatype.utils.connectionError( response, 'The file failed to load from the server.' )
    }
  }
  
});