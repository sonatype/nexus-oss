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
Ext.tree.SonatypeTreeLoader = function(config){
    config.requestMethod = "GET";

    Ext.tree.SonatypeTreeLoader.superclass.constructor.call(this, config);
};

Ext.extend(Ext.tree.SonatypeTreeLoader, Ext.tree.TreeLoader, {
  load : function(node, callback){
      if(this.clearOnLoad){
          while(node.firstChild){
              node.removeChild(node.firstChild);
          }
      }
      if(this.doPreload(node)){ // preloaded json children
          if(typeof callback == "function"){
              callback();
          }
      }else if(typeof(this.dataUrl) != 'undefined' || typeof(this.url) != 'undefined'){   //diff
          this.requestData(node, callback);
      }
  },
  
  //override to request data according ot Sonatype's Nexus REST service
  requestData : function(node, callback){
      if(this.fireEvent("beforeload", this, node, callback) !== false){
          this.transId = Ext.Ajax.request({
              method:this.requestMethod,
              url: node.id + '?isLocal',                                                     //diff
              success: this.handleResponse,
              failure: this.handleFailure,
              options: { dontForceLogout: true },
              scope: this,
              argument: {callback: callback, node: node},
              //disableCaching: false,                                          //diff
              params: this.getParams(node)
          });
      }else{
          // if the load is cancelled, make sure we notify
          // the node that we are done
          if(typeof callback == "function"){
              callback();
          }
      }
  },
  
  getParams: function(node){
      var buf = [], bp = this.baseParams;
      for(var key in bp){
          if(typeof bp[key] != "function"){
              buf.push(encodeURIComponent(key), "=", encodeURIComponent(bp[key]), "&");
          }
      }
      //buf.push("node=", encodeURIComponent(node.id));                      //diff
      return buf.join("");
  },
  
  processResponse : function(response, node, callback){
      var json = response.responseText;
      try {
          var o = eval("("+json+")");
          o = o.data;                                                        //diff
          node.beginUpdate();
          for(var i = 0, len = o.length; i < len; i++){
              var n = this.createNode(o[i]);
              if(n){
                  node.appendChild(n);
              }
          }
          node.endUpdate();
          if(typeof callback == "function"){
              callback(this, node);
          }
      }catch(e){
          this.handleFailure(response);
      }
  },
  
  createNode : function(attr){
      // apply baseAttrs, nice idea Corey!
      if(this.baseAttrs){
          Ext.applyIf(attr, this.baseAttrs);
      }
      if(!attr.id){                                                        //diff
        attr.id = attr.resourceURI;                                        //diff
      }                                                                    //diff
      if(this.applyLoader !== false){
          attr.loader = this;
      }
      if(typeof attr.uiProvider == 'string'){
         attr.uiProvider = this.uiProviders[attr.uiProvider] || eval(attr.uiProvider);
      }
      
      attr.singleClickExpand = true;                                      //diff
      
      return(attr.leaf ?
                      new Ext.tree.TreeNode(attr) :
                      new Ext.tree.AsyncTreeNode(attr));
  }
});

//create constructor for new class
Ext.tree.SonatypeTreeSorter = function(el, config){
    Ext.tree.SonatypeTreeSorter.superclass.constructor.call(this, el, config);
};
 
Ext.extend(Ext.tree.SonatypeTreeSorter, Ext.tree.TreeSorter, {
    disableSort : function( tree ){
      tree.un("beforechildrenrendered", this.doSort, this);    
      tree.un("append", this.updateSort, this);    
      tree.un("insert", this.updateSort, this);    
      tree.un("textchange", this.updateSortParent, this);
    },
    enableSort : function( tree ){
      tree.on("beforechildrenrendered", this.doSort, this);    
      tree.on("append", this.updateSort, this);    
      tree.on("insert", this.updateSort, this);    
      tree.on("textchange", this.updateSortParent, this);
      
      this.doSort( tree.root );
    }
});