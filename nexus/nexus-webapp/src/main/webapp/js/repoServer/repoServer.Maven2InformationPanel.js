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
Sonatype.repoServer.Maven2InformationPanel = function(config) {
  var config = config || {};
  var defaultConfig = {
    halfSize : false
  };
  Ext.apply(this, config, defaultConfig);

  this.sp = Sonatype.lib.Permissions;

  this.linkDivId = Ext.id();
  this.linkLabelId = Ext.id();

  var items = [];

    items.push({
          xtype : 'panel',
          layout : 'form',
          anchor : Sonatype.view.FIELD_OFFSET + ' -10',
          labelWidth : 70,
          items : [{
                xtype : 'displayfield',
                fieldLabel : 'Group',
                name : 'groupId',
                anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                allowBlank : true,
                readOnly : true
              }, {
                xtype : 'displayfield',
                fieldLabel : 'Artifact',
                name : 'artifactId',
                anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                allowBlank : true,
                readOnly : true
              }, {
                xtype : 'displayfield',
                fieldLabel : 'Version',
                name : 'baseVersion',
                anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                allowBlank : true,
                readOnly : true
              }, {
                xtype : 'displayfield',
                fieldLabel : 'Classifier',
                name : 'classifier',
                anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                allowBlank : true,
                readOnly : true
              }, {
                xtype : 'displayfield',
                fieldLabel : 'Extention',
                name : 'extension',
                anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                allowBlank : true,
                  readOnly : true
              }, {
                xtype : 'textarea',
                fieldLabel : 'XML',
                anchor : Sonatype.view.FIELD_OFFSET,
                height : 120,
                name : 'dependencyXmlChunk',
                allowBlank : true,
                readOnly : true
              }]
        });

  this.formPanel = new Ext.form.FormPanel({
        autoScroll : true,
        border : false,
        frame : true,
        collapsible : false,
        collapsed : false,
        items : items
      });

  Sonatype.repoServer.Maven2InformationPanel.superclass.constructor.call(this, {
        title : 'Maven Information',
        layout : 'fit',
        collapsible : false,
        collapsed : false,
        split : true,
        frame : false,
        autoScroll : true,

        items : [this.formPanel]
      });
};

Ext.extend(Sonatype.repoServer.Maven2InformationPanel, Ext.Panel, {

      showArtifact : function(data) {
        this.formPanel.form.setValues(data);
        
        // hide classifier if empty
        if( data != null && data.classifier == null )
        {
          this.find('name', 'classifier')[0].hide();
        }
        else
        {
          this.find('name', 'classifier')[0].show();
        }
      }
});

Sonatype.Events.addListener('artifactContainerInit', function(artifactContainer) {
      artifactContainer.add(new Sonatype.repoServer.Maven2InformationPanel({
            name : 'maven2InformationPanel',
            tabTitle : 'Maven Information',
            halfSize : artifactContainer.halfSize
          }));
    });

Sonatype.Events.addListener('artifactContainerUpdate', function(artifactContainer, payload) {
      var panel = artifactContainer.find('name', 'maven2InformationPanel')[0];
      
      if (payload == null || !payload.leaf)
      {
         panel.showArtifact(null);
      }
      else
      {
      
        Ext.Ajax.request({
          url : payload.resourceURI + '?describe=maven2',
          callback : function(options, isSuccess, response) {
            if (isSuccess)
            {
              artifactContainer.tabPanel.unhideTabStripItem( panel );
              //panel.show();
              var infoResp = Ext.decode(response.responseText);
              panel.showArtifact(infoResp.data);
            }
            else
            {
              if( response.stats = 404 )
              {
              
                artifactContainer.tabPanel.hideTabStripItem( panel );
                
                // this works but is a hack (we don't know the position
                artifactContainer.tabPanel.setActiveTab(1);
                
                //panel.hide(); // this hides the panel but leaves it white
                
                // FIXME: tried these...
                //artifactContainer.tabPanel.doLayout();
                //artifactContainer.doLayout();
                
              }
              else
              {
                Sonatype.utils.connectionError(response, 'Unable to retrieve Maven information.');
              }
            }
          },
          scope : this,
          method : 'GET',
          suppressStatus : 404,
        });

      }
      
    });
    