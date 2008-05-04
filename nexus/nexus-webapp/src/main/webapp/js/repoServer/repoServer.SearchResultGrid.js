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
// must pass in feedUrl that's local to our domain, 'cause we ain't got no proxy yet

// config: feedUrl required

Sonatype.repoServer.SearchResultGrid = function(config) {

  Ext.apply(this, config);

  var resultRecordConstructor = Ext.data.Record.create([
      {name:'groupId'},
      {name:'artifactId'},
      {name:'version'},
      {name:'repoId'},
      {name:'resourceURI'},
      {name:'contextId'}
  ]);

  var resultReader = new Ext.data.JsonReader({
      root: 'data',
      totalProperty: 'totalCount'
    },
    resultRecordConstructor );

  var requestProxy = new Ext.data.HttpProxy({
    url: Sonatype.config.repos.urls.index,
    method: 'GET'
    //headers: {Accept: 'application/json'}
  });
  
  //@todo: create stand alone data reader to read update/create responses as well
  //@ext: must use data.Store (not JsonStore) to pass in reader instead of using fields config array
  this.store = new Ext.data.Store({
    proxy: requestProxy,
    reader: resultReader
  });
  
  this.store.setDefaultSort('groupId', "ASC");

  this.columns = [
    {
      id: 'source',
      header: "Source Index",
      dataIndex: 'contextId',
      width: 100,
      sortable:true
    }/*,{
      id: 'repository',
      header: "Repository",
      dataIndex: 'repoId',
      width: 195,
      sortable:true
    }*/,{
      id: 'group',
      header: "Group",
      dataIndex: 'groupId',
      sortable:true,
      width: 200
    },{
      id: 'artifact',
      header: "Artifact",
      dataIndex: 'artifactId',
      width: 200,
      sortable:true
    },{
      id: 'version',
      header: "Version",
      dataIndex: 'version',
      width: 55,
      sortable:true
    },{
      id: 'jar',
      header: "Link",
      dataIndex: 'resourceURI',
      width: 65,
      sortable:false,
      renderer: this.formatJarLink
    }
//@note: NX-444 remove POM link functionality until it can work across browsers (firefix issue presently)
//  ,{
//    id: 'pom',
//    header: "POM Dependecy",
//    dataIndex: 'artifactId',
//    width: 105,
//    sortable:true,
//    renderer: this.formatPomLink
//  }
  ];

  Sonatype.repoServer.SearchResultGrid.superclass.constructor.call(this, {
      region: 'center',
      id: 'search-result-grid',
      loadMask: {msg:'Loading Results...'},
      stripeRows: true,
      sm: new Ext.grid.RowSelectionModel({
          singleSelect: true
      }),

      viewConfig: {
          forceFit:true,
          enableRowBody:true,
          getRowClass : this.applyRowClass
      },
      
      listeners: {
        render: function(panel){
          panel.body.on(
            {
              'mousedown': function(e, t){
                var i = t.getAttribute('index');
                this.toggleExtraInfo(parseInt(i, 10));
                e.stopEvent();
                return false;
              },
              'click' : function(e, t){
                e.stopEvent();
                return false;
              },
              delegate:'a.pom-link',
              scope:panel
            });
        }
      }
  });
};

Ext.extend(Sonatype.repoServer.SearchResultGrid, Ext.grid.GridPanel, {
  toggleExtraInfo : function(rowIndex){
    var rowEl = new Ext.Element(this.getView().getRow(rowIndex));
    var input = rowEl.child('.copy-pom-dep', true);
    input.select(); //@todo: why won't this field highlight?!!!!
    rowEl.toggleClass('x-grid3-row-expanded');
  },

  // within this function "this" is actually the GridView
  applyRowClass: function(record, rowIndex, p, ds) {
    var xf = Ext.util.Format;
    var xmlDep = Sonatype.repoServer.RepoServer.pomDepTmpl.apply(record.data);
    //note: wrapper div with overflow:auto and autocomplete="off" are attempts to avoid a FF bug
    p.body = '<span>POM Dependency: </span><input class="copy-pom-dep" type="text" autocomplete="off" value="'+xmlDep+'"/>';
    return 'x-grid3-row-collapsed';
  },

  formatJarLink: function(value, p, record, rowIndex, colIndex, store) {
      return String.format('<a target="_blank" href="{0}">Download</a>', value);
  },
  
  formatPomLink: function(value, p, record, rowIndex, colIndex, store) {
      return '<a class="pom-link" index="'+rowIndex+'" href="#">View & Copy</a>';
  }
});