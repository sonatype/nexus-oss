/*
 * Sonatype Nexus (TM) Open Source Version. Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at
 * http://nexus.sonatype.org/dev/attributions.html This program is licensed to
 * you under Version 3 only of the GNU General Public License as published by
 * the Free Software Foundation. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License Version 3 for more details. You should have received a copy of
 * the GNU General Public License Version 3 along with this program. If not, see
 * http://www.gnu.org/licenses/. Sonatype Nexus (TM) Professional Version is
 * available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc.
 */
// must pass in feedUrl that's local to our domain
// config: feedUrl required
Sonatype.ext.FeedGrid = function(config) {
  Ext.apply(this, config);

  this.store = new Ext.data.Store({
        // note: IE requires text/xml or application/xml to parse via XmlReader
        // because it uses response.responseXML
        proxy : new Ext.data.HttpProxy({
              url : this.feedUrl,
              headers : {
                'accept' : 'text/xml'
              }
            }),
        reader : new Ext.data.XmlReader({
              record : 'channel/item'
            }, ['title', 'author', {
                  name : 'pubDate',
                  type : 'date',
                  dateFormat : 'D, d M Y H:i:s T'
                }, 'link', 'description', 'content']),
        sortInfo : {
          field : 'pubDate',
          direction : 'DESC'
        },
        autoLoad : false
      });

  this.columns = [{
        id : 'title',
        header : "Title",
        dataIndex : 'title',
        sortable : true,
        width : 420,
        renderer : this.formatTitle
      }, {
        id : 'last',
        header : "Date",
        dataIndex : 'pubDate',
        width : 150,
        renderer : this.formatDate,
        sortable : true
      }];

  Sonatype.ext.FeedGrid.superclass.constructor.call(this, {
        title : 'Select a feed from the list',
        region : 'center',
        id : 'topic-grid',
        loadMask : {
          msg : 'Loading Feed...'
        },
        stripeRows : true,
        sm : new Ext.grid.RowSelectionModel({
              singleSelect : true
            }),
        viewConfig : {
          forceFit : true,
          enableRowBody : true,
          showPreview : true,
          getRowClass : this.applyRowClass,
          emptyText : 'No artifacts match this query'
        },
        tools : [{
              id : 'refresh',
              handler : function(e, toolEl, panel) {
                if (this.feedUrl)
                {
                  this.store.reload();
                }
              },
              scope : this
            }]
      });

  // this.on('rowcontextmenu', this.onContextClick, this);
};

Ext.extend(Sonatype.ext.FeedGrid, Ext.grid.GridPanel, {

      setFeed : function(name, url) {
        this.setTitle(name);
        this.feedUrl = url;
        this.store.proxy = new Ext.data.HttpProxy({
              url : this.feedUrl,
              headers : {
                'accept' : 'text/xml'
              }
            });
        this.reloadFeed();
      },

      reloadFeed : function() {
        if (this.feedUrl)
        {
          this.store.reload();
        }
      },

      togglePreview : function(show) {
        this.view.showPreview = show;
        this.view.refresh();
      },

      // within this function "this" is actually the GridView
      applyRowClass : function(record, rowIndex, p, ds) {
        if (this.showPreview)
        {
          var xf = Ext.util.Format;
          p.body = '<p>' + xf.ellipsis(xf.stripTags(record.data.description), 400) + '</p>';
          return 'x-grid3-row-expanded';
        }
        return 'x-grid3-row-collapsed';
      },

      formatDate : function(value, p, record, rowIndex, colIndex, store) {
        if (!value)
        {
          return '';
        }
        var now = new Date();
        var d = now.clearTime(true);

        // @todo: correct Ext's Date parsing of "GMT" timezone text, and text my
        // ISO8601 patches
        // @note: special handling for bad Ext.Date parsing
        value = value.add(Date.MINUTE, d.getTimezoneOffset() * (-1));

        var notime = value.clearTime(true).getTime();
        if (notime == d.getTime())
        {
          return 'Today ' + value.dateFormat('g:i a');
        }
        d = d.add('d', -6);
        if (d.getTime() <= notime)
        {
          return value.dateFormat('D g:i a');
        }
        return value.dateFormat('n/j g:i a');
      },

      formatTitle : function(value, p, record, rowIndex, colIndex, store) {
        return String.format('<div class="topic"><b><a href="{1}" target="_blank">{0}</a></b><span class="author">{2}</span></div>', value, record.data.link, record.data.author);
      }
    });
