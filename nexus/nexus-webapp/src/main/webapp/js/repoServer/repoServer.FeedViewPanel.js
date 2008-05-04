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
/*
  config object:
  {
    feedUrl ; required
    title
  }

*/

Sonatype.repoServer.FeedViewPanel = function(config){
  var config = config || {};
  var defaultConfig = {
    feedUrl : '', 
    title : 'Feed Viewer'
  };
  Ext.apply(this, config, defaultConfig);
  
  var tmplTxt = [
    '<div class="post-data">',
      '<span class="post-date">{pubDate:date("M j, Y, g:i a")}</span>',
      '<h3 class="post-title">{title}</h3>',
      '<h4 class="post-author">by {author:defaultValue("Unknown")}</h4>',
    '</div>',
    '<div class="post-body">{content:this.getBody}</div>'
  ];
  
  this.viewItemTemplate = new Ext.Template(tmplTxt);
  this.viewItemTemplate.compile();
  this.viewItemTemplate.getBody = function(v, all){
    return Ext.util.Format.stripScripts(v || all.description);
  };
  
  // render event handler config that overrides <a/> click events
  this.LinkInterceptor = {
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
  
//this.preview = new Ext.Panel({
//    id: 'feed-item-preview',
//    region: 'south',
//    cls:'preview',
//    layout:'fit',
//    height: 300,
//    split: true,
//    border:false,
//    autoScroll: true,
//    listeners: this.LinkInterceptor
//});

  this.grid = new Sonatype.ext.FeedGrid({
    feedUrl : this.feedUrl
  });

  Sonatype.repoServer.FeedViewPanel.superclass.constructor.call(this, {
    //id:'feed-view-' + this.title,
    layout:'border',
    title: this.title,
    hideMode:'offsets',
    items:[
        this.grid 
//      ,this.preview
    ]
  });

  this.gsm = this.grid.getSelectionModel();
//
//this.gsm.on('rowselect', function(sm, index, record){
//    this.viewItemTemplate.overwrite(this.preview.body, record.data);
//}, this, {buffer:250}); //@todo: reduce the delay for invocation (buffer value)

  //this.grid.store.on('beforeload', this.preview.clear, this.preview);
  this.grid.store.on('load', this.gsm.selectFirstRow, this.gsm);
};

Ext.extend(Sonatype.repoServer.FeedViewPanel, Ext.Panel, {
  //extend here
});