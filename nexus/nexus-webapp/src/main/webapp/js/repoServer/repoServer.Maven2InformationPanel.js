/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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

  Sonatype.repoServer.Maven2InformationPanel.superclass.constructor.call(this, {
        title : 'Maven Information',
        autoScroll : true,
        border : true,
        frame : true,
        collapsible : false,
        collapsed : false,
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
              fieldLabel : 'Extension',
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
};

Ext.extend(Sonatype.repoServer.Maven2InformationPanel, Ext.form.FormPanel, {

      showArtifact : function(data, artifactContainer) {
        this.data = data;
        if (data == null)
        {
          this.find('name', 'groupId')[0].setRawValue(null);
          this.find('name', 'artifactId')[0].setRawValue(null);
          this.find('name', 'baseVersion')[0].setRawValue(null);
          this.find('name', 'classifier')[0].setRawValue(null);
          this.find('name', 'extension')[0].setRawValue(null);
          this.find('name', 'dependencyXmlChunk')[0].setRawValue(null);
        }
        else
        {
          Ext.Ajax.request({
                url : this.data.resourceURI + '?describe=maven2&isLocal=true',
                callback : function(options, isSuccess, response) {
                  if (isSuccess)
                  {
                    var infoResp = Ext.decode(response.responseText);

                    // hide classifier if empty
                    if (this.data.classifier)
                    {
                      this.find('name', 'classifier')[0].show();
                    }
                    else
                    {
                      this.find('name', 'classifier')[0].hide();
                    }
                    this.form.setValues(infoResp.data);
                    artifactContainer.showTab(this);
                  }
                  else
                  {
                    if (response.status = 404)
                    {
                      artifactContainer.hideTab(this);
                    }
                    else
                    {
                      Sonatype.utils.connectionError(response, 'Unable to retrieve Maven information.');
                    }
                  }
                },
                scope : this,
                method : 'GET',
                suppressStatus : '404'
              });
        }
      }
    });

Sonatype.Events.addListener('fileContainerInit', function(items) {
      items.push(new Sonatype.repoServer.Maven2InformationPanel({
            name : 'maven2InformationPanel',
            tabTitle : 'Maven Information',
            preferredIndex : 10
          }));
    });

Sonatype.Events.addListener('fileContainerUpdate', function(artifactContainer, data) {
      var panel = artifactContainer.find('name', 'maven2InformationPanel')[0];

      if (data == null || !data.leaf)
      {
        panel.showArtifact(null, artifactContainer);
      }
      else
      {
        panel.showArtifact(data, artifactContainer);
      }
    });

Sonatype.Events.addListener('artifactContainerInit', function(items) {
      items.push(new Sonatype.repoServer.Maven2InformationPanel({
            name : 'maven2InformationPanel',
            tabTitle : 'Maven Information',
            preferredIndex : 10
          }));
    });

Sonatype.Events.addListener('artifactContainerUpdate', function(artifactContainer, payload) {
      var panel = artifactContainer.find('name', 'maven2InformationPanel')[0];

      if (payload == null || !payload.leaf)
      {
        panel.showArtifact(null, artifactContainer);
      }
      else
      {
        panel.showArtifact(payload, artifactContainer);
      }

    });
