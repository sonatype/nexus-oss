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

  this.feedRecordConstructor = Ext.data.Record.create([
    {name:'resourceURI'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString}
  ]);

  this.feedReader = new Ext.data.JsonReader(
    { root: 'data', id: 'resourceURI'},
    this.feedRecordConstructor
  );

  this.feedsDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.feeds,
    reader: this.feedReader,
    sortInfo: { field: 'name', direction: 'ASC' },
    autoLoad: true
  });

  this.feedsGridPanel = new Ext.grid.GridPanel({
    id: 'st-feeds-grid',
    region: 'north',
    layout:'fit',
    collapsible: true,
    split:true,
    height: 160,
    minHeight: 120,
    maxHeight: 400,
    frame: false,
    autoScroll: true,
    selModel: new Ext.grid.RowSelectionModel({
      singleSelect: true
    }),

    tbar: [
      {
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: function() {
          this.feedsDataStore.reload();
          this.grid.reloadFeed();
        }
      },
      {
        text: 'Subscribe',
        icon: Sonatype.config.resourcePath + '/images/icons/feed.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: function() {
          if ( this.feedsGridPanel.getSelectionModel().hasSelection() ) {
            var rec = this.feedsGridPanel.getSelectionModel().getSelected();
            window.open( rec.get( 'resourceURI' ) );
          }
        }
      }
    ],

    //grid view options
    ds: this.feedsDataStore,
    sortInfo: { field: 'name', direction: "ASC" },
    loadMask: true,
    deferredRender: false,
    columns: [
      { header: 'Feed', dataIndex: 'name', width: 300 },
      { header: 'URL', dataIndex: 'resourceURI', width: 300, id: 'feeds-url-col',
        renderer: function( s ) {
          return '<a href="' + s + '" target="_blank">' + s + '</a>';
        },
        menuDisabled:true }
    ],
    autoExpandColumn: 'feeds-url-col',
    disableSelection: false,
    listeners: {
      'rowclick': {
        fn: function( grid, rowIndex, e ) {
          if ( e.target.nodeName.toUpperCase() == 'A' ) return; // no menu on links

          var rec = grid.store.getAt( rowIndex );
          this.grid.setFeed( rec.get( 'name' ), rec.get( 'resourceURI' ) );
        },
        scope: this
      }
    }
  });
  
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

  this.grid = new Sonatype.ext.FeedGrid({});

  Sonatype.repoServer.FeedViewPanel.superclass.constructor.call(this, {
    //id:'feed-view-' + this.title,
    layout:'border',
    title: this.title,
    hideMode:'offsets',
    items:[
        this.feedsGridPanel,
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