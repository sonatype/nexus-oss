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
 * View Nexus serer XML configuration file
 */

Sonatype.repoServer.ConfigViewPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  this.listeners = {
    //note: this isn't pre-render dependent, we just need an early event to start this off
    'beforerender' : this.getConfigFile,
    scope: this
  };
  
  Sonatype.repoServer.ConfigViewPanel.superclass.constructor.call(this, {
    autoScroll: false,
    border: false,
    frame: false,
    collapsible: false,
    collapsed: false,
    tbar: [
      {
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        tooltip: {text:'Reloads the config file'},
        scope: this,
        handler: this.getConfigFile
      },
      {
        text: 'Download Config',
        icon: Sonatype.config.resourcePath + '/images/icons/page_white_put.png',
        cls: 'x-btn-text-icon',
        scope:this,
        handler: function(){
          Sonatype.utils.openWindow(Sonatype.config.repos.urls.configCurrent);
        }
      }
    ],
    items: [
      {
        xtype: 'textarea',
        id: 'config-text',
        readOnly: true,
        hideLabel: true,
        anchor: '100% 100%'
      }
    ]
  });
  
  this.configTextArea = this.findById('config-text');
};


Ext.extend(Sonatype.repoServer.ConfigViewPanel, Ext.form.FormPanel, {
  getConfigFile : function(){
    Ext.Ajax.request({
      callback: this.renderResponse,
      scope: this,
      method: 'GET',
      headers: {'accept' : 'application/xml'},
      url: Sonatype.config.repos.urls.configCurrent
    });
  },
  
  renderResponse : function(options, success, response){
    if (success){
      this.configTextArea.setRawValue(response.responseText);
    }
    else {
      Sonatype.MessageBox.alert('The data failed to load from the server.');
    }
  }
  
});