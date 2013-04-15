/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/*global define*/
/**
 * FIXME This belongs to the timeline plugin and should be moved there.
 */
define('repoServer/FeedViewPanel', ['extjs', 'Sonatype/all', 'Nexus/ext/feedgrid'], function(Ext, Sonatype, FeedGrid) {

  var ns = Ext.namespace('Sonatype.repoServer');

  /*
   * config object: { feedUrl ; required title }
   */
  ns.FeedViewPanel = function(cfg) {
    Ext.apply(this, cfg || {}, {
      feedUrl : '',
      title : 'Feed Viewer'
    });

    this.feedRecordConstructor = Ext.data.Record.create([
      {
        name : 'resourceURI'
      },
      {
        name : 'name',
        sortType : Ext.data.SortTypes.asUCString
      }
    ]);

    this.feedReader = new Ext.data.JsonReader({
      root : 'data',
      id : 'resourceURI'
    }, this.feedRecordConstructor);

    this.feedsDataStore = new Ext.data.Store({
      url : Sonatype.config.repos.urls.feeds,
      reader : this.feedReader,
      sortInfo : {
        field : 'name',
        direction : 'ASC'
      },
      autoLoad : true
    });

    this.feedsGridPanel = new Ext.grid.GridPanel({
      id : 'st-feeds-grid',
      region : 'north',
      layout : 'fit',
      collapsible : true,
      split : true,
      height : 160,
      minHeight : 120,
      maxHeight : 400,
      frame : false,
      autoScroll : true,
      selModel : new Ext.grid.RowSelectionModel({
        singleSelect : true
      }),

      tbar : [
        {
          text : 'Refresh',
          icon : Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
          cls : 'x-btn-text-icon',
          scope : this,
          handler : function() {
            this.feedsDataStore.reload();
            this.grid.reloadFeed();
          }
        },
        {
          text : 'Subscribe',
          icon : Sonatype.config.resourcePath + '/images/icons/feed.png',
          cls : 'x-btn-text-icon',
          scope : this,
          handler : function() {
            if (this.feedsGridPanel.getSelectionModel().hasSelection()) {
              var rec = this.feedsGridPanel.getSelectionModel().getSelected();
              Sonatype.utils.openWindow(rec.get('resourceURI'));
            }
          }
        }
      ],

      // grid view options
      ds : this.feedsDataStore,
      sortInfo : {
        field : 'name',
        direction : "ASC"
      },
      loadMask : true,
      deferredRender : false,
      columns : [
        {
          header : 'Feed',
          dataIndex : 'name',
          width : 300
        },
        {
          header : 'URL',
          dataIndex : 'resourceURI',
          width : 300,
          id : 'feeds-url-col',
          renderer : function(s) {
            return '<a href="' + s + '" target="_blank">' + s + '</a>';
          },
          menuDisabled : true
        }
      ],
      autoExpandColumn : 'feeds-url-col',
      disableSelection : false
    });

    this.feedsGridPanel.getSelectionModel().on('rowselect', this.rowSelect, this);

    var tmplTxt = ['<div class="post-data">', '<span class="post-date">{pubDate:date("M j, Y, g:i a")}</span>', '<h3 class="post-title">{title}</h3>', '<h4 class="post-author">by {author:defaultValue("Unknown")}</h4>', '</div>',
      '<div class="post-body">{content:this.getBody}</div>'];

    this.viewItemTemplate = new Ext.Template(tmplTxt);
    this.viewItemTemplate.compile();
    this.viewItemTemplate.getBody = function(v, all) {
      return Ext.util.Format.stripScripts(v || all.description);
    };

    this.grid = new FeedGrid({});

    ns.FeedViewPanel.superclass.constructor.call(this, {
      layout : 'border',
      title : this.title,
      hideMode : 'offsets',
      items : [this.feedsGridPanel, this.grid
      ]
    });

    this.gsm = this.grid.getSelectionModel();
    this.grid.store.on('load', this.gsm.selectFirstRow, this.gsm);
  };

  Ext.extend(ns.FeedViewPanel, Ext.Panel, {

    rowSelect : function(selectionModel, index, rec) {
      this.grid.setFeed(rec.get('name'), rec.get('resourceURI'));
    }

  });

  return ns.FeedViewPanel;
});

