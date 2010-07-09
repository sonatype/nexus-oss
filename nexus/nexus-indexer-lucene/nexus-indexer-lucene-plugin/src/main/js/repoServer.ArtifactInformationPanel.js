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

Ext.form.RepositoryUrlDisplayField = Ext.extend(Ext.form.DisplayField, {
      setValue : function(repositories) {
        var links = '';

        for (var i = 0; i < repositories.length; i++)
        {
          if (i != 0)
          {
            links += ', ';
          }

          links += '<a href="' + repositories[i].artifactUrl + '">' + repositories[i].repositoryName + '</a>';
        }

        this.setRawValue(links);
        return this;
      }
    });

Ext.reg('repositoryUrlDisplayField', Ext.form.RepositoryUrlDisplayField);

Sonatype.repoServer.ArtifactInformationPanel = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  this.sp = Sonatype.lib.Permissions;

  var items = [{
        xtype : 'panel',
        layout : 'column',
        anchor : Sonatype.view.FIELD_OFFSET + ' -10',
        items : [{
              xtype : 'panel',
              layout : 'form',
              labelWidth : 90,
              items : [{
                    xtype : 'displayfield',
                    fieldLabel : 'Repository Path',
                    name : 'repositoryPath',
                    anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                    allowBlank : true,
                    readOnly : true
                  }, {
                    xtype : 'displayfield',
                    fieldLabel : 'Uploaded by',
                    name : 'uploader',
                    anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                    allowBlank : true,
                    readOnly : true
                  }, {
                    xtype : 'byteDisplayField',
                    fieldLabel : 'Size',
                    name : 'size',
                    anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                    allowBlank : true,
                    readOnly : true
                  }, {
                    xtype : 'timestampDisplayField',
                    fieldLabel : 'Uploaded Date',
                    name : 'uploaded',
                    anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                    allowBlank : true,
                    readOnly : true,
                    formatter : function(value) {
                      if (value)
                      {
                        return new Date.parseDate(value, 'u').format('m.d.Y  h:m:s');
                      }
                    }
                  }, {
                    xtype : 'timestampDisplayField',
                    fieldLabel : 'Last Modified',
                    name : 'lastChanged',
                    anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                    allowBlank : true,
                    readOnly : true,
                    dateFormat : Ext.util.Format.dateRenderer('m/d/Y')
                  }, {
                    xtype : 'panel',
                    layout : 'column',
                    items : [{
                          columnWidth : '.5',
                          items : {
                            xtype : 'button',
                            handler : this.artifactDownload,
                            scope : this,
                            text : 'Download'
                          }
                        }, {
                          columnWidth : '.5',
                          items : {
                            xtype : 'button',
                            handler : this.artifactDelete,
                            scope : this,
                            text : 'Delete'
                          }
                        }]
                  }, {
                    xtype : 'fieldset',
                    checkboxToggle : false,
                    title : 'Checksums',
                    anchor : Sonatype.view.FIELDSET_OFFSET,
                    collapsible : false,
                    autoHeight : true,
                    layoutConfig : {
                      labelSeparator : ''
                    },
                    items : [{
                          xtype : 'displayfield',
                          fieldLabel : 'SHA1',
                          name : 'sha1Hash',
                          anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                          allowBlank : true,
                          readOnly : true
                        }, {
                          xtype : 'displayfield',
                          fieldLabel : 'MD5',
                          name : 'md5Hash',
                          anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                          allowBlank : true,
                          readOnly : true
                        }]
                  }, {
                    xtype : 'fieldset',
                    checkboxToggle : false,
                    title : 'Contained In Repositories',
                    anchor : Sonatype.view.FIELDSET_OFFSET,
                    collapsible : false,
                    autoHeight : true,
                    layoutConfig : {
                      labelSeparator : ''
                    },
                    items : [{
                          xtype : 'panel',
                          items : [{
                                xtype : 'repositoryUrlDisplayField',
                                name : 'repositories',
                                anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                                allowBlank : true,
                                readOnly : true
                              }]
                        }]
                  }]
            }]
      }];

  this.formPanel = new Ext.form.FormPanel({
        autoScroll : true,
        border : false,
        frame : true,
        collapsible : false,
        collapsed : false,
        items : items
      });

  Sonatype.repoServer.ArtifactInformationPanel.superclass.constructor.call(this, {
        title : 'Artifact Information',
        collapsible : false,
        collapsed : false,
        split : true,
        frame : false,
        autoScroll : true,

        items : [this.formPanel]
      });
};

Ext.extend(Sonatype.repoServer.ArtifactInformationPanel, Ext.Panel, {

      artifactDownload : function() {
        if (this.data)
        {
          Sonatype.utils.openWindow(this.data.resourceURI);
        }
      },

      artifactDelete : function() {
        if (this.data)
        {
          var url = this.data.resourceURI;

          Sonatype.MessageBox.show({
                title : 'Delete Repository Item?',
                msg : 'Delete the selected artifact ?',
                buttons : Sonatype.MessageBox.YESNO,
                scope : this,
                icon : Sonatype.MessageBox.QUESTION,
                fn : function(btnName) {
                  if (btnName == 'yes' || btnName == 'ok')
                  {
                    Ext.Ajax.request({
                          url : url,
                          callback : this.deleteRepoItemCallback,
                          scope : this,
                          method : 'DELETE'
                        });
                  }
                }
              });
        }
      },

      deleteRepoItemCallback : function(options, isSuccess, response) {
        if (!isSuccess)
        {
          Sonatype.MessageBox.alert('Error', response.status == 401 ? 'You don\'t have permission to delete artifacts in this repository' : 'The server did not delete the file/folder from the repository');
        }
      },

      showArtifact : function(data) {
        this.data = data;
        if (data == null)
        {
          this.find('name', 'repositoryPath')[0].setRawValue(null);
          this.find('name', 'uploader')[0].setRawValue(null);
          this.find('name', 'size')[0].setRawValue(null);
          this.find('name', 'uploaded')[0].setRawValue(null);
          this.find('name', 'lastChanged')[0].setRawValue(null);
          this.find('name', 'sha1Hash')[0].setRawValue(null);
          this.find('name', 'md5Hash')[0].setRawValue(null);
          this.find('name', 'repositories')[0].setRawValue(null);
        }
        else
        {
          Ext.Ajax.request({
                url : this.data.resourceURI + '?describe=info',
                callback : function(options, isSuccess, response) {
                  if (isSuccess)
                  {
                    var infoResp = Ext.decode(response.responseText);
                    this.formPanel.form.setValues(infoResp.data);
                  }
                  else
                  {
                    Sonatype.utils.connectionError(response, 'Unable to retrieve artifact information.');
                  }
                },
                scope : this,
                method : 'GET'
              });
        }
      }
    });

Sonatype.Events.addListener('artifactContainerInit', function(artifactContainer) {
      artifactContainer.add(new Sonatype.repoServer.ArtifactInformationPanel({
            name : 'artifactInformationPanel',
            tabTitle : 'Artifact Information'
          }));
    });

Sonatype.Events.addListener('artifactContainerUpdate', function(artifactContainer, payload) {
      var panel = artifactContainer.find('name', 'artifactInformationPanel')[0];

      if (payload == null || !payload.leaf)
      {
        panel.showArtifact(null);
      }
      else
      {
        panel.showArtifact(payload);
      }
    });