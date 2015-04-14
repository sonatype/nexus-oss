/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
Ext.define('NX.coreui.service.RepositoryFormService', {
  singleton: true,
  mixins: [
    'NX.LogAware'
  ],
  requires: [
    'NX.I18n',
    'NX.coreui.service.ProxyFacet'
  ],
  facetNames: [
    'NX.coreui.service.ProxyFacet'
  ],

  facets: {
    negativeCache: {
      fields: Ext.emptyFn,
      toMap: Ext.emptyFn,
      toProperties: Ext.emptyFn
    },
    view: {
      fields: Ext.emptyFn,
      toMap: Ext.emptyFn,
      toProperties: Ext.emptyFn
    },
    storage: {
      fields: function() {
        return [
          {
            id: 'storage.writePolicy',
            type: 'combo',
            label: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT'),
            helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_HELP'),
            required: true,
            storeApi: 'coreui_Repository.readWritePolicies'
          }
        ];
      },
      toMap: function(properties, attributes) {
        Ext.apply(attributes, {
          storage: {
            writePolicy: properties['storage.writePolicy']
          }
        });
      },
      toProperties: function(attributes, properties) {
        Ext.apply(properties, {
          'storage.writePolicy': attributes.storage.writePolicy
        });
      }
    },
    maven: {
      fields: function() {
        return [
          {
            id: 'maven.versionPolicy',
            type: 'combo',
            label: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_POLICY'),
            helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_POLICY_HELP'),
            required: true,
            storeApi: 'coreui_Repository.readVersionPolicies'
          },
          {
            id: 'maven.checksumPolicy',
            type: 'combo',
            label: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CHECKSUM'),
            required: true,
            storeApi: 'coreui_Repository.readChecksumPolicies'
          },
          {
            id: 'maven.strictContentTypeValidation',
            type: 'checkbox',
            label: 'Strict Content Type Validation',
            required: true
          }
        ]
      },
      toMap: function(properties, attributes) {
        Ext.apply(attributes, {
          maven: {
            versionPolicy: properties['maven.versionPolicy'],
            checksumPolicy: properties['maven.checksumPolicy'],
            strictContentTypeValidation: Boolean.valueOf(properties['maven.strictContentTypeValidation'])
          }
        });
      },
      toProperties: function(attributes, properties) {
        Ext.apply(properties, {
          'maven.versionPolicy': attributes.maven.versionPolicy,
          'maven.checksumPolicy': attributes.maven.checksumPolicy,
          'maven.strictContentTypeValidation': attributes.maven.strictContentTypeValidation
        });
      }
    },
    httpclient: {
      fields: function() {
        return [
          {
            id: 'httpclient.connection.retries',
            type: 'number',
            label: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CONNECTION_RETRIES'),
            helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CONNECTION_RETRIES_HELP'),
            required: true,
            minValue: 0,
            maxValue: 20
          },
          {
            id: 'httpclient.connection.timeout',
            type: 'number',
            label: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CONNECTION_TIMEOUT'),
            helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CONNECTION_TIMEOUT_HELP'),
            required: true,
            minValue: 0
          }
        ]
      },
      toMap: function(properties, attributes) {
        Ext.apply(attributes, {
          httpclient: {
            connection: {
              retries: parseInt(properties['httpclient.connection.retries']),
              timeout: parseInt(properties['httpclient.connection.timeout'])
            }
          }
        });
      },
      toProperties: function(attributes, properties) {
        Ext.apply(properties, {
          'httpclient.connection.retries': attributes.httpclient.connection.retries,
          'httpclient.connection.timeout': attributes.httpclient.connection.timeout
        });
      }
    },
    group: {
      fields: function(recipeName) {
        var repositoryStore = Ext.create('NX.coreui.store.RepositoryReference');
        repositoryStore.filter([
          { property: 'format', value: recipeName.split('-')[0] }
        ]);
        repositoryStore.load();
        return [
          {
            id: 'group.memberNames',
            idMapping: 'name',
            type: 'itemselector',
            label: 'Group Members',
            required: true,
            helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_MEMBERS_HELP'),
            fromTitle: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_MEMBERS_FROM'),
            toTitle: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_MEMBERS_TO'),
            store: repositoryStore,
            valueField: 'id',
            displayField: 'name'
          }
        ]
      },
      toMap: function(properties, attributes) {
        Ext.apply(attributes, {
          group: {
            memberNames: properties['group.memberNames'].split(',')
          }
        });
      },
      toProperties: function(attributes, properties) {
        Ext.apply(properties, {
          'group.memberNames': attributes.group.memberNames
        });
      }
    }
  },
  getRecipe: function(recipeName) {
    var me = this, recipes = {
      'maven2-proxy': [
        me.facets.proxy, me.facets.maven, me.facets.storage, me.facets.httpclient
      ],
      'maven2-hosted': [
        me.facets.maven, me.facets.storage
      ],
      'maven2-group': [
        me.facets.maven, me.facets.storage, me.facets.group
      ],
      'nuget-hosted': [],
      'nuget-proxy': [
        me.facets.proxy,
        me.facets.httpclient
      ],
      'nuget-group': [me.facets.group]
    }, recipe = recipes[recipeName];
    if (!recipe) {
      me.logDebug('Unable to find recipe for recipe: ' + recipeName);
    }
    return recipe || [];
  },
  recipeName: function(model) {
    return model.get('format') + '-' + model.get('type');
  },
  fieldsForModel: function(model) {
    return this.fieldsFor(this.recipeName(model));
  },
  fieldsFor: function(recipeName) {
    var me = this, items = [];
    var recipe = me.getRecipe(recipeName);
    Ext.each(recipe, function(facet) {
      items.push.apply(items, facet.fields(recipeName));
    });
    return items;
  },
  propertiesForModel: function(model) {
    var me = this, attributes = model.get('attributesMap'), properties = {};
    var recipe = me.getRecipe(me.recipeName(model));
    Ext.each(recipe, function(facet) {
      facet.toProperties(attributes, properties);
    });
    return properties;
  },
  mapAttributes: function(recipeName, properties) {
    var me = this, attributes = {};
    var recipe = me.getRecipe(recipeName);
    Ext.each(recipe, function(facet) {
      facet.toMap(properties, attributes);
    });
    return Ext.encode(attributes);
  },
  constructor: function(config) {
    this.initConfig(config);
    Ext.each(this.facetNames, function(facetName){
      var proxyFacet = Ext.create(facetName);
      this.facets[proxyFacet.getName()] = proxyFacet;
    })
  }

});
