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
/*global define, Ext, Nexus, Sonatype, NX*/
NX.define('Sonatype.repoServer.RepositoryWLPanel', {
  extend : 'Nexus.ext.FormPanel',

  requirejs : ['Nexus/config'],

  statics : {
    discoveryUpdateIntervalStore : new Ext.data.ArrayStore({
      fields : ['intervalLabel', 'valueHrs'],
      data : [
        ['1 hr', '1'],
        ['2 hr', '2'],
        ['3 hr', '3'],
        ['6 hr', '6'],
        ['9 hr', '9'],
        ['12 hr', '12'],
        ['Daily', '24'],
        ['Weekly', '168']
      ]
    }),

    publishStatusStore : new Ext.data.ArrayStore({
      fields : ['text', 'value'],
      data : [
        ['Not published.', '-1'],
        ['Unknown.', '0'],
        ['Published.', '1']
      ]
    }),

    discoveryStatusStore : new Ext.data.ArrayStore({
      fields : ['text', 'value'],
      data : [
        ['Unsuccessful.', '-1'],
        ['In Progress.', '0'],
        ['Successful.', '1']
      ]
    })
  },

  refreshContent : function() {
    this.loadData();
  },

  constructor : function(cfg) {
    if (!cfg || !cfg.payload) {
      throw new Error("payload missing: need repository record");
    }

    // should be static, but Sonatype.config is not defined yet when statics are defined (before requirejs dep resolution)
    this.resourceUrl = new Ext.Template(Sonatype.config.repos.urls.repositories + "/{0}/wl").compile();

    var
          self = this,
          payload = cfg.payload,
          subjectIsNotM2Proxy = cfg.payload.data.repoType !== 'proxy' || cfg.payload.data.format !== 'maven2',
          defaultConfig = {
            frame : true,
            autoScroll : true,
            readOnly : true, // don't want save/cancel buttons
            url : self.resourceUrl.apply([cfg.payload.data.id])
          };


    self.payload = {
      data : {
        id : cfg.payload.data.id,
        resourceURI : self.resourceUrl.apply([cfg.payload.data.id])
      }
    };

    // don't use payload directly, this panel does not behave as expected by Nexus.ext.FormPanel
    delete cfg.payload;

    Ext.apply(this, cfg, defaultConfig);

    self.dataModifiers = {
      load : {
        'publishedStatus' : function(value) {
          self.publishedStatus = value;
          var store = Sonatype.repoServer.RepositoryWLPanel.publishStatusStore;
          return store.getAt(store.find('value', value)).get('text');
        },

        'discovery.discoveryLastStatus' : function(value) {
          self.discoveryStatus = value;
          var store = Sonatype.repoServer.RepositoryWLPanel.discoveryStatusStore;
          return store.getAt(store.find('value', value)).get('text');
        },

        'publishedUrl' : function(value) {
          self.whitelistUrl = value;
          return value;
        }
      }
    };

    self.items = [
      {
        xtype : 'fieldset',
        title : 'Publishing',
        name : 'publishingFieldset',
        anchor : Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        layout : {
          type : 'hbox',
          align : 'stretchmax'
        },
        items : [
          {
            xtype : 'panel',
            layout : 'form',
            flex : 1,
            items : [
              {
                xtype : 'displayfield',
                fieldLabel : 'Status',
                name : 'publishedStatus'
              },
              {
                xtype : 'displayfield',
                fieldLabel : 'Message',
                name : 'publishedMessage'
              },
              {
                xtype : 'timestampDisplayField',
                fieldLabel : 'Published on',
                name : 'publishedTimestamp'
              }
            ]
          },
          {
            xtype : 'container',
            width : 80,
            layout : {
              type : 'vbox',
              pack : 'end'
            },
            items : [
              {
                xtype : 'link-button',
                text : 'Show prefix file',
                handler : function() {
                  window.open(self.whitelistUrl, '_blank');
                }
              }
            ]
          }
        ]
      },
      {
        xtype : 'fieldset',
        title : 'Discovery',
        anchor : Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        checkboxToggle : true,
        name : 'discoveryFieldset',
        hidden : subjectIsNotM2Proxy,
        listeners : {
          expand : function() {
            this.enableDiscoveryHandler(true);
          },
          collapse : function() {
            this.disableDiscovery();
          },
          scope : this
        },
        items : [
          {
            xtype : 'hidden',
            name : 'discovery.discoveryEnabled',
            listeners : {
              change : function(field, enabled, oldValue) {
                var discoveryFieldset = this.find('name', 'discoveryFieldset')[0];
                if (enabled) {
                  discoveryFieldset.expand();
                } else {
                  discoveryFieldset.collapse();
                }
              }
            }
          },
          {
            xtype : 'displayfield',
            fieldLabel : 'Status',
            name : 'discovery.discoveryLastStatus'
          },
          {
            xtype : 'displayfield',
            fieldLabel : 'Message',
            name : 'discovery.discoveryLastMessage'
          },
          {
            xtype : 'timestampDisplayField',
            fieldLabel : 'Last run',
            name : 'discovery.discoveryLastRunTimestamp'
          },
          {
            xtype : 'combo',
            fieldLabel : 'Update interval',
            name : 'discovery.discoveryIntervalHours',
            store : Sonatype.repoServer.RepositoryWLPanel.discoveryUpdateIntervalStore,
            displayField : 'intervalLabel',
            valueField : 'valueHrs',
            emptyText : 'Select...',
            iconCls : 'no-icon', //use iconCls if placing within menu to shift to right side of menu
            mode : 'local',
            editable : false,
            allowBlank : false,
            selectOnFocus : true,
            forceSelection : true,
            triggerAction : 'all',
            typeAhead : true,
            listeners : {
              select : function(combo) {
                this.enableDiscovery(parseInt(combo.getValue(), 10));
              },
              scope : this
            },
            width : 150
          },
          {
            xtype : 'button',
            hideLabel : true,
            text : 'Force remote discovery',
            handler : this.forceRemoteDiscoveryHandler,
            scope : this
          }
        ]
      }
    ];

    Sonatype.repoServer.RepositoryWLPanel.superclass.constructor.apply(self, arguments);

    self.on('actioncomplete', self.onActionComplete);
  },

  enableDiscoveryHandler : function(checked) {
    // 'this' is the checkbox
    var
          combo = this.find('name', 'discovery.discoveryIntervalHours'),
          fieldset = this.find('name', 'discoveryFieldset');

    if (!(fieldset.length > 0 && combo.length > 0)) {
      throw new Error('could not find interval combo box or fieldset');
    }

    if (Ext.isEmpty(combo[0].getValue())) {
      // still in loadData(), do nothing
      return;
    }

    if (checked) {
      this.enableDiscovery(parseInt(combo[0].getValue(), 10));
      fieldset[0].expand();
    } else {
      this.disableDiscovery();
      fieldset[0].collapse();
    }
  },

  disableDiscovery : function() {
    if (!this.submitDiscoverySetting) {
      return;
    }

    var
          self = this,
          mask = new Ext.LoadMask(this.el, {
            msg : 'Disabling discovery...'
          });
    mask.show();
    Ext.Ajax.request({
      method : 'PUT',
      url : this.resourceUrl.apply([this.payload.data.id]) + '/config',
      jsonData : {
        data : {
          discoveryEnabled : false,
          discoveryIntervalHours : -1
        }
      },
      callback : function() {
        this.loadData();
        mask.hide();
      },
      scope : this
    });
  },

  enableDiscovery : function(interval) {
    if (!this.submitDiscoverySetting) {
      return;
    }
    if (interval === 0) {
      interval = 24;
    }

    var mask = new Ext.LoadMask(this.el, {
      msg : 'Updating discovery...'
    });
    mask.show();
    Ext.Ajax.request({
      method : 'PUT',
      url : this.resourceUrl.apply([this.payload.data.id]) + '/config',
      jsonData : {
        data : {
          discoveryEnabled : true,
          discoveryIntervalHours : interval
        }
      },
      callback : function() {
        this.loadData();
        mask.hide();
      },
      scope : this
    });
  },

  forceRemoteDiscoveryHandler : function() {
    var
          self = this,
          mask = new Ext.LoadMask(this.el, {
      msg : 'Forcing discovery...'
    });

    mask.show();
    Ext.Ajax.request({
      method : 'DELETE',
      url : this.resourceUrl.apply([this.payload.data.id]),
      callback : function() {
        self.loadData();
        mask.hide();
      },
      scope : this
    });
  },
  onActionComplete : function(form, action) {
    if (action.type === 'sonatypeLoad') {
      // @note: this is a work around to get proper use of the isDirty()
      // function of this field
      var
            panel = action.options.fpanel,
            tstampField = panel.find('name', 'discovery.discoveryLastRunTimestamp')[0];

      if (panel.find('name', 'discovery.discoveryEnabled')[0].getValue() === 'true') {
        panel.find('name', 'discoveryFieldset')[0].expand();
      } else {
        panel.find('name', 'discoveryFieldset')[0].collapse();
      }
      panel.submitDiscoverySetting = true;

      // FIXME this does not change the height of the fieldset
      if (panel.publishedStatus === -1) {
        panel.find('xtype', 'container')[0].hide();
        panel.find('name', 'publishedTimestamp')[0].hide();
      }

      if (panel.discoveryStatus === 0) {
        tstampField.hide();
      } else {
        tstampField.show();
      }
    }
  }
}, function() {
  Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
    var sp = Sonatype.lib.Permissions, newRecord, maven2FormatRepo, notShadowType;

    // Check whether record is new or received from Nexus
    // (GridViewer convention for new records: id starts with 'new_')
    newRecord = rec.id && rec.id.indexOf('new_') === 0;

    maven2FormatRepo = rec.data.format === 'maven2';
    notShadowType = rec.data.repoType !== 'virtual';

    if (!newRecord && maven2FormatRepo && notShadowType &&
          ( sp.checkPermission('nexus:repositories', sp.CREATE) || sp.checkPermission('nexus:repositories', sp.DELETE) || sp.checkPermission('nexus:repositories', sp.EDIT) )) {
      cardPanel.add(new Sonatype.repoServer.RepositoryWLPanel({
        tabTitle : 'Whitelist',
        name : 'whitelist',
        payload : rec
      }));
    }
  });
});
