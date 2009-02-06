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
 * Repository panel superclass
 */

/* config options:
  {
    id: the is of this panel instance [required]
    title: title of this panel (shows in tab)
  }
*/
Sonatype.repoServer.AbstractRepoPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  this.sp = Sonatype.lib.Permissions;

  this.ctxRecord = null;
  this.reposGridPanel == null;
  
  this.repoActions = {
    clearCache: {
      text: 'Expire Cache',
      handler: this.clearCacheHandler
    },
    reIndex: {
      text: 'Re-Index',
      handler: this.reIndexHandler
    },
    rebuildMetadata: {
      text: 'Rebuild Metadata',
      handler: this.rebuildMetadataHandler
    },
    deleteRepoItem: {
      text: 'Delete',
      scope:this,
      handler: this.deleteRepoItemHandler
    },
    uploadArtifact: {
      text: 'Upload Artifact...',
      handler: this.uploadArtifactHandler
    }
  };

  Sonatype.repoServer.AbstractRepoPanel.superclass.constructor.call(this, {
  });
};

Ext.extend(Sonatype.repoServer.AbstractRepoPanel, Ext.Panel, {
  hasSelection: function() {
    return this.ctxRecord || this.reposGridPanel.getSelectionModel().hasSelection();
  },

  viewHandler : function(){
    if (this.ctxRecord || this.reposGridPanel.getSelectionModel().hasSelection()){
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      this.viewRepo(rec);
    }
  },
  
  clearCacheHandler: function( rec ) {
    var url = Sonatype.config.repos.urls.cache + rec.data.resourceURI.slice(
      Sonatype.config.host.length + Sonatype.config.servicePath.length );
    
    if ( url.indexOf( Sonatype.config.browseIndexPathSnippet ) > -1 ) {
      url = url.replace( Sonatype.config.browseIndexPathSnippet, Sonatype.config.browsePathSnippet );
    }
    
    //make sure to provide /content path for repository root requests like ../repositories/central
    if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url)){
      url += '/content';
    }
    
    Ext.Ajax.request({
      url: url,
      callback: this.clearCacheCallback,
      scope: this,
      method: 'DELETE'
    });
  },
  
  clearCacheCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not clear the repository\'s cache.' );
    }
  },

  
  reIndexHandler: function( rec ){
    var url = Sonatype.config.repos.urls.index +
      rec.data.resourceURI.slice(Sonatype.config.host.length + Sonatype.config.servicePath.length);
    
    if ( url.indexOf( Sonatype.config.browseIndexPathSnippet ) > -1 ) {
      url = url.replace( Sonatype.config.browseIndexPathSnippet, Sonatype.config.browsePathSnippet );
    }
    
    //make sure to provide /content path for repository root requests like ../repositories/central
    if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url)){
      url += '/content';
    }
    
    Ext.Ajax.request({
      url: url,
      callback: this.reIndexCallback,
      scope: this,
      method: 'DELETE'
    });
  },
  
  reIndexCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){

    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not re-index the repository.' );
    }
  },
  
  rebuildMetadataHandler: function( rec ){
    var url = Sonatype.config.repos.urls.metadata +
      rec.data.resourceURI.slice(Sonatype.config.host.length + Sonatype.config.servicePath.length);
    
    if ( url.indexOf( Sonatype.config.browseIndexPathSnippet ) > -1 ) {
      url = url.replace( Sonatype.config.browseIndexPathSnippet, Sonatype.config.browsePathSnippet );
    }
    
    //make sure to provide /content path for repository root requests like ../repositories/central
    if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url)){
      url += '/content';
    }
    
    Ext.Ajax.request({
      url: url,
      callback: this.rebuildMetadataCallback,
      scope: this,
      method: 'DELETE'
    });
  },
  
  rebuildMetadataCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){

    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not rebuild metadata in the repository.' );
    }
  },
  
  putInServiceHandler : function(){
    if (this.hasSelection()){
      //@todo: start updating messaging here
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      
      var out = {
        data : {
          id : rec.data.id,
          repoType : rec.get('repoType'),
          localStatus : 'inService'
        }
      };
      
      Ext.Ajax.request({
        url: rec.data.resourceURI + '/status',
        jsonData: out,
        callback: this.putInServiceCallback,
        scope: this,
        method: 'PUT'
      });
    }
  },
  
  putInServiceCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      var statusResp = Ext.decode(response.responseText);
      this.updateRepoStatuses(statusResp.data);
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not put the repository into service.' );
    }
  },

  putOutOfServiceHandler : function(){
    if (this.hasSelection()){
      //@todo: start updating messaging here
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      
      var out = {
        data : {
          id : rec.data.id,
          repoType : rec.get('repoType'),
          localStatus : 'outOfService'
        }
      };
      
      Ext.Ajax.request({
        url: rec.data.resourceURI + '/status',
        jsonData: out,
        callback: this.putOutOfServiceCallback,
        scope: this,
        method: 'PUT'
      });
    }
  },
  
  allowProxyHandler : function(){
    if (this.hasSelection()){
      //@todo: start updating messaging here
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      
      var out = {
        data : {
          id : rec.data.id,
          repoType : rec.get('repoType'),
          localStatus : rec.get('localStatus'),
          remoteStatus : rec.get('remoteStatus'),
          proxyMode : 'allow'
        }
      };
      
      Ext.Ajax.request({
        url: rec.data.resourceURI + '/status',
        jsonData: out,
        callback: this.allowProxyCallback,
        scope: this,
        method: 'PUT'
      });
    }
  },
  
  allowProxyCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      var statusResp = Ext.decode(response.responseText);
      this.updateRepoStatuses(statusResp.data);
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not update the proxy repository status to allow.' );
    }
  },
  
  blockProxyHandler : function(){
    if (this.hasSelection()){
      //@todo: start updating messaging here
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      
      var out = {
        data : {
          id : rec.data.id,
          repoType : rec.get('repoType'),
          localStatus : rec.get('localStatus'),
          remoteStatus : rec.get('remoteStatus'),
          proxyMode : 'blockedManual'
        }
      };
      
      Ext.Ajax.request({
        url: rec.data.resourceURI + '/status',
        jsonData: out,
        callback: this.blockProxyCallback,
        scope: this,
        method: 'PUT'
      });
    }
  },
  
  blockProxyCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      var statusResp = Ext.decode(response.responseText);
      this.updateRepoStatuses(statusResp.data);
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not update the proxy repository status to blocked.' );
    }
  },
  
  putOutOfServiceCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      var statusResp = Ext.decode(response.responseText);
      this.updateRepoStatuses(statusResp.data);
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not put the repository out of service.' );
    }
  },
  
  updateRepoStatuses: function(data) {
  },
  
  uploadArtifactHandler : function( rec ){
      
    Ext.Ajax.request({
      url: rec.data.resourceURI,
      scope: this,
      callback: function(options, success, response) {
        if ( success ) {
          var statusResp = Ext.decode(response.responseText);
          if (statusResp.data) {
            if ( statusResp.data.allowWrite ) {
              if ( ! this.formCards ) {
                this.formCards = this.cardPanel;
              }
                
              var oldItem = this.formCards.getLayout().activeItem;
              this.formCards.remove(oldItem, true);
              
              if ( this.formCards.tbar ) {
                this.formCards.tbar.oldSize = this.formCards.tbar.getSize(); 
                this.formCards.tbar.hide();
                this.formCards.tbar.setHeight(0);
              }

              var panel = new Ext.Panel({
                layout: 'fit',
                frame: true,
                items: [ new Sonatype.repoServer.FileUploadPanel({
                  title: 'Artifact Upload to ' + rec.get('name'),
//                  repoPanel: this,
                  payload: rec
                }) ]
              });
              this.formCards.insert(1, panel);
              this.formCards.getLayout().setActiveItem(panel);
              panel.doLayout();
            }
            else {
              Sonatype.MessageBox.show({
                title: 'Deployment Disabled',
                icon: Sonatype.MessageBox.ERROR,
                buttons: Sonatype.MessageBox.OK,
                msg: 'Deployment is disabled for the selected repository.<br /><br />' +
                  'You can enable it in the "Access Settings" section of the repository configuration'
              });
            }
            return;
          }
        }
        Sonatype.utils.connectionError( response, 'There was a problem obtaining repository status.' );
      }
    });
  },

  restToContentUrl: function( r ) {
    var isGroup = r.indexOf( Sonatype.config.repos.urls.groups ) > -1;
    var hasHost = r.indexOf( Sonatype.config.host ) > -1;

    var snippet = r.indexOf( Sonatype.config.browseIndexPathSnippet ) == -1 ?
      Sonatype.config.browsePathSnippet : Sonatype.config.browseIndexPathSnippet;
    r = r.replace( snippet, '' );

    if ( isGroup ) {
      r = r.replace( Sonatype.config.repos.urls.groups, Sonatype.config.content.groups );
    }
    else {
      r = r.replace( Sonatype.config.repos.urls.repositories, Sonatype.config.content.repositories );
    }

    return hasHost ? r : ( Sonatype.config.host + r );
  },
  
  restToRemoteUrl: function( node, repoRecord ) {
    var r = node.id;

    var snippet = r.indexOf( Sonatype.config.browseIndexPathSnippet ) == -1 ?
        Sonatype.config.browsePathSnippet : Sonatype.config.browseIndexPathSnippet;
    r = r.replace( snippet, '' );

    return repoRecord.data.remoteUri + r.replace( repoRecord.data.resourceURI, '' );
  },
  
  downloadHandler: function( node, item, event ) {
    event.stopEvent();
    window.open( this.restToContentUrl( node.id ) );
  },
  
  downloadFromRemoteHandler: function( node, item, event ) {
    event.stopEvent();
    window.open( this.restToRemoteUrl( node, node.attributes.repoRecord ) );
  },  
  
  deleteRepoItemHandler : function( node ) {
    var url = Sonatype.config.repos.urls.repositories + 
      node.id.slice( Sonatype.config.host.length + Sonatype.config.repos.urls.repositories.length );
    //make sure to provide /content path for repository root requests like ../repositories/central
    if (/.*\/repositories\/[^\/]*$/i.test(url)){
      url += '/content';
    }
    Sonatype.MessageBox.show( {
      animEl: node.getOwnerTree().getEl(),
      title : 'Delete Repository Item?',
      msg : 'Delete the selected ' + ( node.isLeaf() ? 'file' : 'folder' ) + '?',
      buttons: Sonatype.MessageBox.YESNO,
      scope: this,
      icon: Sonatype.MessageBox.QUESTION,
      fn: function( btnName ) {
        if (btnName == 'yes' || btnName == 'ok') {
          Ext.Ajax.request( {
            url: url,
            callback: this.deleteRepoItemCallback,
            scope: this,
            contentNode: node,
            method: 'DELETE'
          } );
        }
      }
    } );
  },
  
  deleteRepoItemCallback: function( options, isSuccess, response ) {
    //@todo: stop updating messaging here
    if ( isSuccess ) {
      options.contentNode.getOwnerTree().root.reload();
    }
    else {
      Sonatype.MessageBox.alert( 'Error', response.status == 401 ?
        'You don\'t have permission to delete artifacts in this repository' :
        'The server did not delete the file/folder from the repository' );
    }
  },
  
  onRepositoryMenuInit: function( menu, repoRecord ) {
    if ( repoRecord.id.substring( 0, 4 ) == 'new_' ) return;

    var isVirtual = repoRecord.get( 'repoType' ) == 'virtual';

    if ( this.sp.checkPermission(
          'nexus:cache', this.sp.DELETE ) &&
        ! isVirtual ) {
      menu.add( this.repoActions.clearCache );
    }

    if ( this.sp.checkPermission(
          'nexus:index', this.sp.DELETE ) &&
        ! isVirtual ) {
      menu.add( this.repoActions.reIndex );
    }

    if ( this.sp.checkPermission(
          'nexus:metadata', this.sp.DELETE ) ){
      menu.add( this.repoActions.rebuildMetadata );
    }

    if ( this.sp.checkPermission(
          'nexus:artifact', this.sp.CREATE ) &&
        repoRecord.get('repoType') == 'hosted' &&
        repoRecord.get('repoPolicy') == 'release' ){
      menu.add( this.repoActions.uploadArtifact );
    }
  },
  
  onRepositoryContentMenuInit: function( menu, repoRecord, contentRecord ) {
    if ( contentRecord.data.resourceURI == null ) {
      contentRecord.data.resourceURI = contentRecord.data.id;
    }

    var isVirtual = repoRecord.data.repoType == 'virtual';
    var isProxy = repoRecord.data.repoType == 'proxy';
    var isGroup = repoRecord.data.repoType == 'group';

    if ( this.sp.checkPermission( 'nexus:cache', this.sp.DELETE ) && ! isVirtual ) {
      menu.add( this.repoActions.clearCache );
    }
    if ( this.sp.checkPermission( 'nexus:index', this.sp.DELETE ) && ! isVirtual ) {
      menu.add( this.repoActions.reIndex );
    }
    if ( this.sp.checkPermission( 'nexus:metadata', this.sp.DELETE ) ) {
      menu.add( this.repoActions.rebuildMetadata );
    }

    if ( contentRecord.isLeaf() ) {
      if ( isProxy ) {
        menu.add( {
          text: 'Download From Remote',
          scope: this,
          handler: this.downloadFromRemoteHandler,
          href: this.restToRemoteUrl( contentRecord, repoRecord )
        } );
      }
      menu.add( {
        text: 'Download',
        scope: this,
        handler: this.downloadHandler,
        href: this.restToContentUrl( contentRecord.id )
      } );
    }

    if ( ! contentRecord.isRoot && ! isGroup ) {
      if ( isProxy && ! contentRecord.isLeaf() ) {
        menu.add( {
          text: 'View Remote',
          scope: this,
          handler: this.downloadFromRemoteHandler,
          href: this.restToRemoteUrl( contentRecord, repoRecord )
        } );
      }
      
      // only allow delete for local browsing
      if ( contentRecord.data.resourceURI.indexOf( Sonatype.config.browseIndexPathSnippet ) == -1 ) {
        menu.add( this.repoActions.deleteRepoItem );
      }
    }
  }
} );

Sonatype.repoServer.DefaultRepoHandler = new Sonatype.repoServer.AbstractRepoPanel();
Sonatype.Events.addListener( 'repositoryMenuInit',
  Sonatype.repoServer.DefaultRepoHandler.onRepositoryMenuInit,
  Sonatype.repoServer.DefaultRepoHandler );
Sonatype.Events.addListener( 'repositoryContentMenuInit',
  Sonatype.repoServer.DefaultRepoHandler.onRepositoryContentMenuInit,
  Sonatype.repoServer.DefaultRepoHandler );
