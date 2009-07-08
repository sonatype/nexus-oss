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
Sonatype.repoServer.ArtifactInformationPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );
  
  this.linkDivId = Ext.id();
  
  this.formPanel = new Ext.form.FormPanel( {
    autoScroll: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
        
    items: [
      { 
        xtype: 'panel',
        layout: 'column',
        items: [
          {
            xtype: 'panel',
            layout: 'form',
            columnWidth: .4,
            labelWidth: 70,
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Group',
                name: 'groupId',
                anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                allowBlank: true,
                readOnly: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Artifact',
                name: 'artifactId',
                anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                allowBlank: true,
                readOnly: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Version',
                name: 'version',
                anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                allowBlank: true,
                readOnly: true
              },
              {
                xtype: 'panel',
                html: '<div class="x-form-item" tabindex="-1">' + 
                  '<label class="x-form-item-label" style="width: 70px;">Download:</label>' +
                  '<div id="' + this.linkDivId + '" class="x-form-element" style="padding-left: 75px; padding-top: 3px">' +
                  '</div><div class="x-form-clear-left"/></div>'
              }
            ]
          },
          {
            xtype: 'panel',
            layout: 'form',
            columnWidth: .6,
            labelWidth: 30,
            items: [
              {
                xtype: 'textarea',
                fieldLabel: 'XML',
                name: 'xml',
                anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                height: 100,
                allowBlank: true,
                readOnly: true
              }
            ]
          }
        ]
      }
    ]
  } );

  Sonatype.repoServer.ArtifactInformationPanel.superclass.constructor.call( this, {
    title: 'Artifact Information',
    layout: 'fit',
    region: 'south',
    collapsible: true,
    collapsed: true,
    split: true,
    height: 150,
    minHeight: 100,
    maxHeight: 400,
    frame: false,
    autoScroll: true,

    items: [
      this.formPanel
    ],
    
    listeners: {
      expand: {
        fn: function( p ) {
          this.formPanel.doLayout();
        },
        scope: this
      }
    }
  } );
};

Ext.extend( Sonatype.repoServer.ArtifactInformationPanel, Ext.Panel, {
  formatJarLink: function( data ) {
    var r = data.repoId;
    var g = data.groupId;
    var a = data.artifactId;
    var v = data.version;
    var c = data.classifier;
    var p = data.packaging;
  
    if ( c ) {
      return this.makeArtifactLink( r, g, a, v, c, p, 'artifact' );
    }
    // no packaging, only shows a pom link
    else if ( ! p ) {
      return this.makeArtifactLink( r, g, a, v, null, 'pom', 'pom' )
    }
    else if ( p == 'pom') {
      return this.makeArtifactLink( r, g, a, v, c, p, 'pom' );
    }
    else {
      return this.makeArtifactLink( r, g, a, v, c, p, 'artifact' ) + ', ' +
      this.makeArtifactLink( r, g, a, v, null, 'pom', 'pom' );
    }
  },
  
  makeArtifactLink: function( r, g, a, v, c, p, title ) {
    var url = Sonatype.config.repos.urls.redirect +
      '?r=' + r + '&g=' + g + '&a=' + a + '&v=' + v;
    if ( c ) {
      url += '&c=' + c;
    }
    if ( p ) {
      url += '&p=' + p;
    }
    return String.format( '<a target="_blank" href="{0}">{1}</a>', Sonatype.utils.appendAuth( url ), title );
  },

  showArtifact: function( data, collapse ) {
    data.xml = '';
    var empty = data.groupId == null || data.groupId == ''; 
    if ( ! empty ) {
      data.xml = '<dependency>\n' +
        '  <groupId>' + data.groupId + '</groupId>\n' +
        '  <artifactId>' + data.artifactId + '</artifactId>\n' +
        '  <version>' + data.version + '</version>\n' +
        ( data.classifier ? 
          ( '  <classifier>' + data.classifier + '</classifier>\n' ) : '' ) +
        '</dependency>\n';
    }
    this.formPanel.form.setValues( data );
    var linkDiv = document.getElementById( this.linkDivId );
    linkDiv.innerHTML = empty ? '' : this.formatJarLink( data );
    if ( collapse ) {
      this.collapse();
    }
    else {
      this.expand();
    }
  }
} );

