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
define('repoServer/RepositoryWLPanel', ['extjs', 'sonatype/all'], function(Ext, Sonatype) {

	Sonatype.repoServer.RepositoryWLPanel = function(cfg) {
		var config = cfg || {};
		var defaultConfig = {
			frame: true
		};
		var repoRecord = cfg.payload;

		var subjectIsNotProxy = repoRecord.data.repoType != 'proxy'; // true for proxy, false for everything else

		this.discoveryUpdateIntervalStore = new Ext.data.ArrayStore({
			autoDestroy: true,
			fields: ['intervalLabel', 'valueHrs'],
			data: [
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

		config.items = [{
			xtype: 'fieldset',
			title: 'Publishing',
			items: [{
				xtype: 'displayfield',
				fieldLabel: 'Status',
				name: 'pub_status',
				value: 'Published.'
			}, {
				xtype: 'displayfield',
				fieldLabel: 'Message',
				name: 'pub_message',
				value: 'Prefix file published.'
			}, {
				xtype: 'timestampDisplayField',
				fieldLabel: 'Published on',
				name: 'pub_lastRun',
				value: 123456789123456
			}, {
				xtype: 'link-button',
				fieldLabel: 'View prefix file',
				hideLabel: true,
				name: 'pub_fileLink',
				text: 'Show prefix file',
				href: 'http://localhost:8081/nexus/content/repositories/releases/.meta/prefixes.txt',
				target: '_blank'
			}]
		}, {
			xtype: 'checkbox',
			fieldLabel: '  Enable Discovery',
			name: 'dis_enable',
			value: false,
			hidden: subjectIsNotProxy,
			handler: this.enableDiscoveryHandler
		}, {
			xtype: 'fieldset',
			title: 'Discovery',
			name: 'dis_fieldset',
			hidden: subjectIsNotProxy,
			items: [{
				xtype: 'displayfield',
				fieldLabel: 'Status',
				name: 'dis_status',
				value: 'Remote content discovered successfully (prefix file).'
			}, {
				xtype: 'displayfield',
				fieldLabel: 'Message',
				name: 'dis_message',
				value: 'Remote prefix file published on XXXXX'
			}, {
				xtype: 'timestampDisplayField',
				fieldLabel: 'Last run',
				name: 'dis_lastRun',
				value: 123456789123456
			}, {
				xtype: 'combo',
				fieldLabel: 'Update interval',
				name: 'dis_updateInterval',
				store: this.discoveryUpdateIntervalStore,
				displayField: 'intervalLabel',
				valueField: 'valueHrs',
				emptyText: 'Select...',
				getListParent: function() {
					return this.el.up('.x-menu');
				},
				iconCls: 'no-icon', //use iconCls if placing within menu to shift to right side of menu
				mode: 'local',
				editable: false,
				allowBlank: false,
				selectOnFocus: true,
				forceSelection: true,
				triggerAction: 'all',
				typeAhead: true,
				width: 150
			}, {
				xtype: 'button',
				fieldLabel: 'Force remote discovery',
				hideLabel: true,
				name: 'dis_forceRemoteDiscovery',
				text: 'Force remote discovery',
				handler: this.forceRemoteDiscoveryHandler,
			}]
		}];

		Sonatype.repoServer.RepositoryWLPanel.superclass.constructor.call(this, Ext.apply(config, defaultConfig));
	};

	Ext.extend(Sonatype.repoServer.RepositoryWLPanel, Ext.FormPanel, {

		enableDiscoveryHandler: function(checkbox, checked) {
			fieldset = this.find('name', 'dis_fieldset');
			if (checked) {
				fieldset.hidden = false;
			} else {
				fieldset.hidden = true;
			}
		},

		forceRemoteDiscoveryHandler: function(button, event) {
			window.alert("bbrbrbrrrr, it's remote discovey working!");
		}
	});

	Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
		var sp = Sonatype.lib.Permissions;

		if (sp.checkPermission('nexus:repositories', sp.CREATE) || sp.checkPermission('nexus:repositories', sp.DELETE) || sp.checkPermission('nexus:repositories', sp.EDIT)) {
			cardPanel.add(new Sonatype.repoServer.RepositoryWLPanel({
				tabTitle: 'Whitelist',
				name: 'whitelist',
				payload: rec
			}));
		}
	});
});
