/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2014 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2014-09-02 11:12:40 (ef1fa70924f51a26dacbe29644ca3f31501a5fce)
*/
/**
 * The TreeStore is a store implementation that is backed by by an {@link Ext.data.Tree}.
 * It provides convenience methods for loading nodes, as well as the ability to use
 * the hierarchical tree structure combined with a store. This class is generally used
 * in conjunction with {@link Ext.tree.Panel}. This class also relays many events from
 * the Tree for convenience.
 *
 * # Using Models
 *
 * If no Model is specified, an implicit model will be created that extends {@link Ext.data.TreeModel}.
 * The standard Tree fields will also be copied onto the Model for maintaining their state. These fields are listed
 * in the {@link Ext.data.NodeInterface} documentation.
 *
 * # Reading Nested Data
 *
 * For the tree to read nested data, the {@link Ext.data.reader.Reader} must be configured with a root property,
 * so the reader can find nested data for each node (if a root is not specified, it will default to
 * 'children'). This will tell the tree to look for any nested tree nodes by the same keyword, i.e., 'children'.
 * If a root is specified in the config make sure that any nested nodes with children have the same name.
 * Note that setting {@link #defaultRootProperty} accomplishes the same thing.
 */
Ext.define('Ext.data.TreeStore', {
    extend: 'Ext.data.AbstractStore',
    alias: 'store.tree',
    requires: [
        'Ext.util.Sorter',
        'Ext.data.Tree',
        'Ext.data.TreeModel',
        'Ext.data.NodeInterface'
    ],

    /**
     * @cfg {Ext.data.TreeModel/Ext.data.NodeInterface/Object} root
     * The root node for this store. For example:
     *
     *     root: {
     *         expanded: true,
     *         text: "My Root",
     *         children: [
     *             { text: "Child 1", leaf: true },
     *             { text: "Child 2", expanded: true, children: [
     *                 { text: "GrandChild", leaf: true }
     *             ] }
     *         ]
     *     }
     *
     * Setting the `root` config option is the same as calling {@link #setRootNode}.
     */

    /**
     * @cfg {Boolean} [clearOnLoad=true]
     * Remove previously existing child nodes before loading.
     */
    clearOnLoad : true,

    /**
     * @cfg {Boolean} [clearRemovedOnLoad=true]
     * If `true`, when a node is reloaded, any records in the {@link #removed} record collection that were previously descendants of the node being reloaded will be cleared from the {@link #removed} collection.
     * Only applicable if {@link #clearOnLoad} is `true`.
     */
    clearRemovedOnLoad: true,

    /**
     * @cfg {String} [nodeParam="node"]
     * The name of the parameter sent to the server which contains the identifier of the node.
     */
    nodeParam: 'node',

    /**
     * @cfg {String} [defaultRootId="root"]
     * The default root id.
     */
    defaultRootId: 'root',

    /**
     * @cfg {String} [defaultRootText="Root"]
     * The default root text (if not specified)/
     */
    defaultRootText: 'Root',

    /**
     * @cfg {String} [defaultRootProperty="children"]
     * The root property to specify on the automatically generated {@link Ext.data.Reader Reader} used to extract
     * child nodes from a raw data packet if one is not explicitly defined in the configured {@link #cfg-proxy}.
     *
     * If the nodes in this store may be of different types (a heterogeneous tree), then it is possible that node
     * data at different levels may use different property names to reference an array of child nodes.
     *
     * To handle this situation, configure the {@link Ext.data.TreeModel} subclasses with a specific
     * {@link Ext.data.TreeModel#cfg-proxy proxy} which contains a reader definition:
     *
     *    Ext.define('myApp.Folder', {
     *        extend: 'Ext.data.TreeModel',
     *
     *        // When loading child nodes for a Folder we look at the "files" property of the incoming object.
     *        proxy: {
     *            type: 'ajax',
     *            reader: {
     *                root: 'files'
     *            }
     *        },
     *
     *        fields: [{
     *            name: 'text',
     *            mapping: 'directoryName'
     *        }]
     *    });
     */
    defaultRootProperty: 'children',

    fillCount: 0,

    /**
     * @cfg {Boolean} [folderSort=false]
     * Set to true to automatically prepend a leaf sorter.
     */
    folderSort: false,

    constructor: function(config) {
        var me = this,
            root,
            fields,
            model;

        config = Ext.apply({}, config);

        /**
         * @cfg {Object[]} [fields]
         * If you wish to create a Tree*Grid*, and configure your tree with a {@link Ext.panel.Table#cfg-columns} columns configuration,
         * it is possilble to define the set of fields you wish to use in the Store instead of configuting the store with a {@link #cfg-model}.
         *
         * By default, the Store uses an {@link Ext.data.TreeModel}. If you configure fields, it uses a subclass of {@link Ext.data.TreeModel}
         * defined with the set of fields that you specify (In addition to the fields which it uses for storing internal state).
         */
        fields = config.fields || me.fields;
        model = config.model || me.model;

        // If not using a configured or prototype Model, we should use a subclass of Ext.data.TreeModel.
        if (!model) {
            if (!fields) {
                fields = [{
                    name: 'text', type: 'string'
                }];
            }
            if (me.defaultRootProperty !== me.self.prototype.defaultRootProperty) {
                fields.push({
                    name: me.defaultRootProperty,
                    type: 'auto',
                    defaultValue: null,
                    persist: false
                });
            }

            // Our model will be a subclass of Ext.data.TreeModel augmented with the necessary fields.
            config.model = Ext.define(null, {
                extend: 'Ext.data.TreeModel',
                fields: fields,
                proxy: me.proxy || me.defaultProxyType
            });
            delete me.fields;
            me.implicitModel = true;
        }

        me.callParent([config]);

        // We create our data tree.
        me.tree = new Ext.data.Tree();

        // data tree has an upward link
        me.tree.treeStore = me;

        // The following events are fired on this TreeStore by the bubbling from NodeInterface.fireEvent
        /**
         * @event append
         * @inheritdoc Ext.data.Tree#append
         */
        /**
         * @event remove
         * @inheritdoc Ext.data.Tree#remove
         */
        /**
         * @event move
         * @inheritdoc Ext.data.Tree#move
         */
        /**
         * @event insert
         * @inheritdoc Ext.data.Tree#insert
         */
        /**
         * @event beforeappend
         * @inheritdoc Ext.data.Tree#beforeappend
         */
        /**
         * @event beforeremove
         * @inheritdoc Ext.data.Tree#beforeremove
         */
        /**
         * @event beforemove
         * @inheritdoc Ext.data.Tree#beforemove
         */
        /**
         * @event beforeinsert
         * @inheritdoc Ext.data.Tree#beforeinsert
         */
        /**
         * @event expand
         * @inheritdoc Ext.data.Tree#expand
         */
        /**
         * @event collapse
         * @inheritdoc Ext.data.Tree#collapse
         */
        /**
         * @event beforeexpand
         * @inheritdoc Ext.data.Tree#beforeexpand
         */
        /**
         * @event beforecollapse
         * @inheritdoc Ext.data.Tree#beforecollapse
         */
        /**
         * @event sort
         * @inheritdoc Ext.data.Tree#sort
         */

        me.onBeforeSort();

        root = me.root;
        if (root) {
            delete me.root;
            me.setRootNode(root);
        }

        //<deprecated since=0.99>
        if (Ext.isDefined(me.nodeParameter)) {
            if (Ext.isDefined(Ext.global.console)) {
                Ext.global.console.warn('Ext.data.TreeStore: nodeParameter has been deprecated. Please use nodeParam instead.');
            }
            me.nodeParam = me.nodeParameter;
            delete me.nodeParameter;
        }
        //</deprecated>
    },

    /**
     * @method
     * @inheritdoc
     */
    setProxy: function(proxy) {
        var reader,
            needsRoot;

        if (proxy instanceof Ext.data.proxy.Proxy) {
            // proxy instance, check if a root was set
            needsRoot = Ext.isEmpty(proxy.getReader().root);
        } else if (Ext.isString(proxy)) {
            // string type, means a reader can't be set
            needsRoot = true;
        } else {
            // object, check if a reader and a root were specified.
            reader = proxy.reader;
            needsRoot = !(reader && !Ext.isEmpty(reader.root));
        }
        proxy = this.callParent(arguments);

        // The proxy sets a parameter to carry the entity ID based upon the Operation's id
        // That partameter name defaults to "id".
        // TreeStore however uses a nodeParam configuration to specify the entity id
        proxy.idParam = this.nodeParam;

        if (needsRoot) {
            reader = proxy.getReader();
            reader.root = this.defaultRootProperty;
            // force rebuild
            reader.buildExtractors(true);
        }
        return proxy;
    },

    /**
     * @inheritdoc Ext.data.Store#filter
     */
    filter: function(filters, value) {
        if (Ext.isString(filters)) {
            filters = {
                property: filters,
                value: value
            };
        }

        var me = this,
            decoded = me.decodeFilters(filters),
            i,
            length = decoded.length,
            root = me.getRootNode(),
            filteredNodes;

        // Merge new filters into current filter set.
        for (i = 0; i < length; i++) {
            me.filters.replace(decoded[i]);
        }

        filters = me.filters.items;

        if (filters.length) {
            filteredNodes = [];
            me.filterFn = Ext.util.Filter.createFilterFn(filters);

            root.cascadeBy({
                after: function(node) {
                    node.set('visible', me.filterFn(node));
                }
            });
            for (i = 0, length = root.childNodes.length; i < length; i++) {
                if (root.childNodes[i].get('visible')) {
                    filteredNodes.push(root.childNodes[i]);
                }
            }

            me.fireEvent('datachanged', me);
            me.fireEvent('refresh', me);
        } else {
            filteredNodes = root.childNodes;
        }
        root.fireEvent('filterchange', root, filteredNodes);
        me.fireEvent('filterchange', me, filters);
    },

    /**
     * @inheritdoc Ext.data.Store#clearFilter
     */
    clearFilter: function() {
        var me = this,
            root = me.getRootNode();

        me.filters.clear();
        me.filterFn = null;
        root.cascadeBy(function(node) {
            node.set('visible', true);
        });
        root.fireEvent('filterchange', root, root.childNodes);
        me.fireEvent('filterchange', me, []);
    },

    onBeforeSort: function() {
        if (this.folderSort) {
            this.sort({
                property: 'leaf',
                direction: 'ASC'
            }, 'prepend', false);
        }
    },

    /**
     * Fired by the root node.
     *
     * Called before a node is expanded.
     *
     * This ensures that the child nodes are available before calling the passed callback.
     * @private
     * @param {Ext.data.NodeInterface} node The node being expanded.
     * @param {Function} callback The function to run after the expand finishes
     * @param {Object} scope The scope in which to run the callback function
     * @param {Array} args The extra args to pass to the callback after the new child nodes
     */
    onBeforeNodeExpand: function(node, callback, scope, args) {
        var me = this,
            reader = me.proxy.getReader(),
            children,
            callbackArgs;

        // childNodes are loaded: go ahead with expand
        // This will also expand phantom nodes with childNodes.
        if (node.isLoaded()) {
            callbackArgs = [node.childNodes];
            if (args) {
                callbackArgs.push.apply(callbackArgs, args);
            }
            Ext.callback(callback, scope || node, callbackArgs);
        }
        // The node is loading
        else if (node.isLoading()) {
            me.on('load', function() {
                callbackArgs = [node.childNodes];
                if (args) {
                    callbackArgs.push.apply(callbackArgs, args);
                }
                Ext.callback(callback, scope || node, callbackArgs);
            }, me, {
                single: true,
                priority: 1001
            });
        }
        // There are unloaded child nodes in the raw data because of the lazy configuration, load them then call back.
        else {
            // 1. If the raw data read in for the node contains a root (children array), then read it.
            // 2. If a phantom w/o any children, it should still be processed if expanded so check for
            //    that here as well. See EXTJS-13509.
            children = reader.getRoot(node.raw || node[node.persistenceProperty]);

            if (children || node.phantom) {
                if (children) {
                    me.fillNode(node, reader.extractData(children));
                }

                callbackArgs = [node.childNodes];

                if (args) {
                    callbackArgs.push.apply(callbackArgs, args);
                }

                Ext.callback(callback, scope || node, callbackArgs);
            }
            // Node needs loading
            else {
                me.read({
                    node: node,
                    // We use internalCallback here because we want trigger to
                    // the loading event after we've loaded children
                    internalCallback: function() {
                        // Clear the callback, since if we're introducing a custom one,
                        // it may be re-used on reload
                        delete me.lastOptions.internalCallback;
                        callbackArgs = [node.childNodes];
                        if (args) {
                            callbackArgs.push.apply(callbackArgs, args);
                        }
                        Ext.callback(callback, scope || node, callbackArgs);
                    }
                });
            }
        }
    },

    /**
     * @method
     * @inheritdoc
     */
    getNewRecords: function() {
        return Ext.Array.filter(this.tree.flatten(), this.filterNew);
    },

    /**
     * @method
     * @inheritdoc
     */
    getUpdatedRecords: function() {
        return Ext.Array.filter(this.tree.flatten(), this.filterUpdated);
    },

    onNodeRemove: function(parent, node, isMove) {
        var me = this;

        node.unjoin(me);
        // Phantom nodes should never be included in the removed collection.
        // Also, if we're moving a node a remove will be fired, however we'll
        // be inserting it again, so don't push it into the removed collection
        if (!node.phantom && !isMove && !me.loading) {
            Ext.Array.include(me.removed, node);
        }

        if (me.autoSync && !me.autoSyncSuspended && !isMove) {
            me.sync();
        }
    },

    onNodeAdded: function(parent, node) {
        var me = this,
            reader = me.proxy.getReader(),
            data = node.raw || node[node.persistenceProperty],
            dataRoot,
            isVisible;

        if (me.filterFn) {
            isVisible = me.filterFn(node);
            node.set('visible', isVisible);

            // If a node which passes the filter is added to a parent node
            if (isVisible) {
                parent.set('visible', me.filterFn(parent));
            }
        }

        Ext.Array.remove(me.removed, node);
        node.join(me);

        if (!node.isLeaf() && !node.isLoaded() && !me.lazyFill) {
            dataRoot = reader.getRoot(data);
            if (dataRoot) {
                me.fillNode(node, reader.extractData(dataRoot));
            }
        }

        if (me.autoSync && !me.autoSyncSuspended && (node.phantom || node.dirty)) {
            me.sync();
        }
    },

    onNodeSort: function() {
        if (this.autoSync && !this.autoSyncSuspended) {
            this.sync();
        }
    },

    /**
     * Sets the root node for this store.  See also the {@link #root} config option.
     * @param {Ext.data.TreeModel/Ext.data.NodeInterface/Object} root
     * @return {Ext.data.NodeInterface} The new root
     */
    setRootNode: function(root, /* private */ preventLoad) {
        var me = this,
            model = me.model,
            idProperty = model.prototype.idProperty;

        // Ensure that the model has the required fields to function as a tree node.
        if (!model.prototype.isNode) {
            Ext.data.NodeInterface.decorate(model);
        }

        // Convert to a node. Even if they are passing a normal Model, the Model will not yet
        // have been decorated with the constructor which initializes properties, so we always
        // have to construct a new node if the passed root is not a Node.
        if (!root || !root.isNode) {
            // create a default rootNode and create internal data struct.
            root = Ext.apply({
                id: me.defaultRootId,
                text: me.defaultRootText,
                allowDrag: false
            }, root);
            if (root[idProperty] === undefined) {
                root[idProperty] = me.defaultRootId;
            }

            // Create the root.
            root = Ext.ModelManager.create(root, model);
        }

        // Because we have decorated the model with new fields,
        // we need to build new extactor functions on the reader.
        me.getProxy().getReader().buildExtractors(true);

        // Ensure the root node is filtered, registered and joined.
        me.onNodeAdded(null, root);

        // When we add the root to the tree, it will automaticaly get the NodeInterface
        me.tree.setRootNode(root);

        // If the user has set expanded: true on the root, we want to call the expand function to kick off
        // an expand process, so clear the expanded status and call expand.
        // Upon receipt, the expansion process is the most efficient way of processing the
        // returned nodes and putting them into the NodeStore in one block.
        // Appending a node to an expanded node is expensive - the NodeStore and UI are updated.
        if (preventLoad !== true && !root.isLoaded() && (me.autoLoad === true || root.isExpanded())) {
            root.data.expanded = false;
            root.expand();
        }

        return root;
    },

    /**
     * Returns the root node for this tree.
     * @return {Ext.data.NodeInterface}
     */
    getRootNode: function() {
        return this.tree.getRootNode();
    },

    /**
     * Returns the record node by id
     * @param {String} id The id of the node to get.
     * @return {Ext.data.NodeInterface}
     */
    getNodeById: function(id) {
        return this.tree.getNodeById(id);
    },

    getById: function(id) {
        return this.getNodeById(id);
    },

    /**
     * Loads the Store using its configured {@link #proxy}.
     * @param {Object} options (Optional) config object. This is passed into the {@link Ext.data.Operation Operation}
     * object that is created and then sent to the proxy's {@link Ext.data.proxy.Proxy#read} function.
     * The options can also contain a node, which indicates which node is to be loaded. If not specified, it will
     * default to the root node.
     */
    load: function(options) {
        options = options || {};
        options.params = options.params || {};

        var me = this,
            node = options.node || me.tree.getRootNode(),
            callback = options.callback,
            scope = options.scope,
            operation;

        // If there is not a node it means the user hasn't defined a root node yet. In this case let's just
        // create one for them.
        if (!node) {
            node = me.setRootNode({
                expanded: true
            }, true);
        }

        // If the node we are loading was expanded, we have to expand it after the load
        if (node.data.expanded) {
            node.data.loaded = false;

            // Must set expanded to false otherwise the onProxyLoad->fillNode->appendChild calls will update the view.
            // We ned to update the view in the callback below.
            if (me.clearOnLoad) {
                node.data.expanded = false;
            }
            options.callback = function() {

                // If newly loaded nodes are to be added to the existing child node set, then we have to collapse
                // first so that they get removed from the NodeStore, and the subsequent expand will reveal the
                // newly augmented child node set.
                if (!me.clearOnLoad) {
                    node.collapse();
                }
                node.expand();

                // Call the original callback (if any)
                Ext.callback(callback, scope, arguments);
            };
        }

        // Assign the ID of the Operation so that a ServerProxy can set its idParam parameter,
        // or a REST proxy can create the correct URL
        options.id = node.getId();

        options = Ext.apply({
            action: 'read',
            filters: me.filters.items,
            sorters: me.getSorters(),
            node: options.node || node
        }, options);

        me.lastOptions = options;

        operation = new Ext.data.Operation(options);

        if (me.fireEvent('beforeload', me, operation) !== false) {

            // Set the loading flag early
            // Used by onNodeRemove to NOT add the removed nodes to the removed collection
            me.loading = true;
            if (me.clearOnLoad) {
                if (me.clearRemovedOnLoad) {
                    // clear from the removed array any nodes that were descendants of the node being reloaded so that they do not get saved on next sync.
                    me.clearRemoved(node);
                }
                // remove all the nodes
                node.removeAll(false);
            }
            me.proxy.read(operation, me.onProxyLoad, me);
        }

        if (me.loading && node) {
            node.set('loading', true);
        }

        return me;
    },

    /**
     * Removes all records that used to be descendants of the passed node from the removed array
     * @private
     * @param {Ext.data.NodeInterface} node
     */
    clearRemoved: function(node) {
        var me = this,
            removed = me.removed,
            id = node.getId(),
            removedLength = removed.length,
            i = removedLength,
            recordsToClear = {},
            newRemoved = [],
            removedHash = {},
            removedNode,
            targetNode,
            targetId;

        if(node === me.getRootNode()) {
            // if the passed node is the root node, just reset the removed array
            me.removed = [];
            return;
        }

        // add removed records to a hash so they can be easily retrieved by id later
        for(; i--;) {
            removedNode = removed[i];
            removedHash[removedNode.getId()] = removedNode;
        }

        for(i = removedLength; i--;) {
            removedNode = removed[i];
            targetNode = removedNode;
            while(targetNode && targetNode.getId() !== id) {
                // walk up the parent hierarchy until we find the passed node or until we get to the root node
                targetId = targetNode.get('parentId');
                targetNode = targetNode.parentNode || me.getNodeById(targetId) || removedHash[targetId];
            }
            if(targetNode) {
                // removed node was previously a descendant of the passed node - add it to the records to clear from "removed" later
                recordsToClear[removedNode.getId()] = removedNode;
            }
        }

        // create a new removed array containing only the records that are not in recordsToClear
        for(i = 0; i < removedLength; i++) {
            removedNode = removed[i];
            if(!recordsToClear[removedNode.getId()]) {
                newRemoved.push(removedNode);
            }
        }

        me.removed = newRemoved;
    },

    /**
     * Fills a node with a series of child records.
     * @private
     * @param {Ext.data.NodeInterface} node The node to fill
     * @param {Ext.data.TreeModel[]} newNodes The records to add
     */
    fillNode: function(node, newNodes) {
        var me = this,
            newNodeCount = newNodes ? newNodes.length : 0,
            sorters = me.sorters,
            i, sortCollection,
            needsIndexSort = false,
            performLocalSort = me.sortOnLoad && !me.remoteSort && sorters && sorters.items && sorters.items.length,
            node1, node2, rootFill;

        if (newNodeCount) {

            // Apply any filter to the nodes as we fill
            if (me.filterFn) {
                newNodes[0].set('visible', me.filterFn(newNodes[0]));
            }

            // See if there are any differing index values in the new nodes. If not, then we do not have to sortByIndex
            for (i = 1; !needsIndexSort && i < newNodeCount; i++) {

                node1 = newNodes[i];
                node2 = newNodes[i - 1];

                // Apply any filter to the nodes as we fill
                if (me.filterFn) {
                    node1.set('visible', me.filterFn(node1));
                }
                needsIndexSort = node1[node1.persistenceProperty].index !== node2[node2.persistenceProperty].index;
            }

            // If there is a set of local sorters defined.
            if (performLocalSort) {
                // If sorting by index is needed, sort by index first
                if (needsIndexSort) {
                    me.sorters.insert(0, me.indexSorter);
                }
                sortCollection = new Ext.util.MixedCollection();
                sortCollection.addAll(newNodes);
                sortCollection.sort(me.sorters.items);
                newNodes = sortCollection.items;

                // Remove the index sorter
                me.sorters.remove(me.indexSorter);
            } else if (needsIndexSort) {
                Ext.Array.sort(newNodes, me.sortByIndex);
            }
        }

        node.set('loaded', true);

        // Fill node gets called recursively.
        // The appendChild calls through to onNodeAdded for each of these newNodes.
        // If this.lazyFill is false, that immediately loads locally available nodes from the raw data object captured for
        // the newNode. That in turn calls fillNode.
        // As such, when we hit the top most node, we fire an event to let any views know we'll be doing
        // a bulk operation so they can take appropriate action.
        rootFill = me.fillCount === 0;
        if (rootFill) {
            // internal event
            me.fireEvent('beforefill', me, node, newNodes);
        }
        ++me.fillCount;

        if (newNodes.length) {
            node.appendChild(newNodes, undefined, true);
        }

        if (rootFill) {
            // internal event
            me.fireEvent('fillcomplete', me, node, newNodes);
        }
        --me.fillCount;

        return newNodes;
    },

    /**
     * Sorter function for sorting records in index order
     * @private
     * @param {Ext.data.NodeInterface} node1
     * @param {Ext.data.NodeInterface} node2
     * @return {Number}
     */
    sortByIndex: function(node1, node2) {
        return node1[node1.persistenceProperty].index - node2[node2.persistenceProperty].index;
    },

    onIdChanged: function(model, oldId, newId, oldInternalId){
        this.tree.onNodeIdChanged(model, oldId, newId, oldInternalId);
        this.callParent(arguments);
    },

    onProxyLoad: function(operation) {
        var me = this,
            successful = operation.wasSuccessful(),
            records = operation.getRecords(),
            node = operation.node,
            scope = operation.scope || me,
            args = [records, operation, successful];

        me.loading = false;
        node.set('loading', false);
        if (successful) {
            if (!me.clearOnLoad) {
                records = me.cleanRecords(node, records);
            }
            records = me.fillNode(node, records);
        }
        // The load event has an extra node parameter
        // (differing from the load event described in AbstractStore)
        /**
         * @event load
         * Fires whenever the store reads data from a remote data source.
         * @param {Ext.data.TreeStore} this
         * @param {Ext.data.NodeInterface} node The node that was loaded.
         * @param {Ext.data.TreeModel[]} records An array of records.
         * @param {Boolean} successful True if the operation was successful.
         */
        // deprecate read?
        Ext.callback(operation.internalCallback, scope, args);
        me.fireEvent('read', me, operation.node, records, successful);
        me.fireEvent('load', me, operation.node, records, successful);
        //this is a callback that would have been passed to the 'read' function and is optional
        Ext.callback(operation.callback, scope, args);
    },

    cleanRecords: function(node, records){
        var nodeHash = {},
            childNodes = node.childNodes,
            i = 0,
            len  = childNodes.length,
            out = [],
            rec;

        // build a hash of all the childNodes under the current node for performance
        for (; i < len; ++i) {
            nodeHash[childNodes[i].getId()] = true;
        }

        for (i = 0, len = records.length; i < len; ++i) {
            rec = records[i];
            if (!nodeHash[rec.getId()]) {
                out.push(rec);
            }
        }

        return out;
    },

    /**
     * @method
     * @inheritdoc
     */
    removeAll: function() {
        var root = this.getRootNode();
        if (root) {
            root.destroy(true);
        }
        this.fireEvent('clear', this);
    },

    doSort: function(sorterFn) {
        var me = this;
        if (me.remoteSort) {
            //the load function will pick up the new sorters and request the sorted data from the proxy
            me.load();
        } else {
            me.tree.sort(sorterFn, true);
            me.fireEvent('datachanged', me);
            me.fireEvent('refresh', me);
        }
        me.fireEvent('sort', me, me.sorters.getRange());
    }
}, function() {
    var proto = this.prototype;
    proto.indexSorter = new Ext.util.Sorter({
        sorterFn: proto.sortByIndex
    });
});
