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
define('repoServer/RepositoryWLPanel', ['extjs', 'sonatype/all'], function(Ext, Sonatype) {

  var discoveryUpdateIntervalStore = new Ext.data.ArrayStore({
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
  });

  Sonatype.repoServer.RepositoryWLPanel = function(cfg) {
    if (!cfg || !cfg.payload) {
      throw new Error("payload missing: need repository record");
    }

    var
          subjectIsNotProxy = cfg.payload.data.repoType !== 'proxy',
          defaultConfig = {
            frame : true,
            autoScroll : true,
            defaultAnchor : '-15'
          };

    Ext.apply(this, cfg, defaultConfig);

    this.items = [
      {
        xtype : 'fieldset',
        title : 'Publishing',
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
                name : 'pub_status',
                value : 'Published.'
              },
              {
                xtype : 'displayfield',
                fieldLabel : 'Message',
                name : 'pub_message',
                value : 'Prefix file published.'
              },
              {
                xtype : 'timestampDisplayField',
                fieldLabel : 'Published on',
                name : 'pub_lastRun',
                value : 123456789123456
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
                fieldLabel : 'View prefix file',
                hideLabel : true,
                name : 'pub_fileLink',
                text : 'Show prefix file',
                href : 'http://localhost:8081/nexus/content/repositories/releases/.meta/prefixes.txt',
                target : '_blank'
              }
            ]
          }
        ]
      },
      {
        xtype : 'checkbox',
        fieldLabel : 'Enable Discovery',
        name : 'dis_enable',
        value : false,
        hidden : subjectIsNotProxy,
        handler : this.enableDiscoveryHandler
      },
      {
        xtype : 'fieldset',
        title : 'Discovery',
        name : 'dis_fieldset',
        hidden : subjectIsNotProxy,
        items : [
          {
            xtype : 'displayfield',
            fieldLabel : 'Status',
            name : 'dis_status',
            value : 'Remote content discovered successfully (prefix file).'
          },
          {
            xtype : 'displayfield',
            fieldLabel : 'Message',
            name : 'dis_message',
            value : 'Remote prefix file published on XXXXX'
          },
          {
            xtype : 'timestampDisplayField',
            fieldLabel : 'Last run',
            name : 'dis_lastRun',
            value : 123456789123456
          },
          {
            xtype : 'combo',
            fieldLabel : 'Update interval',
            name : 'dis_updateInterval',
            store : discoveryUpdateIntervalStore,
            displayField : 'intervalLabel',
            valueField : 'valueHrs',
            emptyText : 'Select...',
            getListParent : function() {
              return this.el.up('.x-menu');
            },
            iconCls : 'no-icon', //use iconCls if placing within menu to shift to right side of menu
            mode : 'local',
            editable : false,
            allowBlank : false,
            selectOnFocus : true,
            forceSelection : true,
            triggerAction : 'all',
            typeAhead : true,
            width : 150
          },
          {
            xtype : 'button',
            fieldLabel : 'Force remote discovery',
            hideLabel : true,
            name : 'dis_forceRemoteDiscovery',
            text : 'Force remote discovery',
            handler : this.forceRemoteDiscoveryHandler
          }
        ]
      }
    ];

    Sonatype.repoServer.RepositoryWLPanel.superclass.constructor.call(this);
  };

  Ext.extend(Sonatype.repoServer.RepositoryWLPanel, Ext.FormPanel, {

    enableDiscoveryHandler : function(checkbox, checked) {
      // 'this' is the checkbox
      var fieldset = this.ownerCt.find('name', 'dis_fieldset');
      if (fieldset.length > 0) {
        fieldset[0].setVisible(checked);
      } else {
        throw new Error('could not find "dis_fieldset"');
      }

    },

    forceRemoteDiscoveryHandler : function(button, event) {
      window.alert("bbrbrbrrrr, it's remote discovey working!");
    }
  });

  Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
    var sp = Sonatype.lib.Permissions;

    if (sp.checkPermission('nexus:repositories', sp.CREATE) || sp.checkPermission('nexus:repositories', sp.DELETE)
          || sp.checkPermission('nexus:repositories', sp.EDIT)) {
      cardPanel.add(new Sonatype.repoServer.RepositoryWLPanel({
        tabTitle : 'Whitelist',
        name : 'whitelist',
        payload : rec
      }));
    }
  });
});
