/**
 * The Preview enables you to show a configurable preview of a record.
 *
 * This plugin assumes that it has control over the features used for this
 * particular grid section and may conflict with other plugins.
 */
Ext.define('Ext.ux.PreviewPlugin', {
    extend: 'Ext.AbstractPlugin',
    alias: 'plugin.preview',
    requires: ['Ext.grid.feature.RowBody'],
    
    // private, css class to use to hide the body
    hideBodyCls: 'x-grid-row-body-hidden',
    
    /**
     * @cfg {String} bodyField
     * Field to display in the preview. Must be a field within the Model definition
     * that the store is using.
     */
    bodyField: '',
    
    /**
     * @cfg {Boolean} previewExpanded
     */
    previewExpanded: true,
    
    setCmp: function (target) {
        this.callParent(arguments);

        // Resolve grid from view as necessary
        var me = this,
            grid = me.cmp = target.isXType('gridview') ? target.panel : target,
            bodyField = me.bodyField,
            hideBodyCls = me.hideBodyCls,
            feature = Ext.create('Ext.grid.feature.RowBody', {
                grid: grid,
                getAdditionalData: function (data, idx, model, rowValues) {

                    var getAdditionalData = Ext.grid.feature.RowBody.prototype.getAdditionalData,
                        additionalData = {
                            rowBody: data[bodyField],
                            rowBodyCls: grid.getView().previewExpanded ? '' : hideBodyCls
                        };

                    if (Ext.isFunction(getAdditionalData)) {
                        // "this" is the RowBody object hjere. Do not change to "me"
                        Ext.apply(additionalData, getAdditionalData.apply(this, arguments));
                    }
                    return additionalData;
                }
            }),
            initFeature = function (grid, view) {
                view.previewExpanded = me.previewExpanded;

                // By this point, existing features are already in place, so this must be initialized and added
                view.featuresMC.add(feature);
                feature.init(grid);
            };

        // The grid has already created its view
        if (grid.view) {
            initFeature(grid, grid.view);
        }

        // At the time a grid creates its plugins, it has not created all the things
        // it needs to create its view correctly.
        // Process the view and init the RowBody Feature as soon as the view is created.
        else {
            grid.on({
                viewcreated: initFeature,
                single: true
            });
        }
    },
    
    /**
     * Toggle between the preview being expanded/hidden
     * @param {Boolean} expanded Pass true to expand the record and false to not show the preview.
     */
    toggleExpanded: function (expanded) {
        var grid = this.getCmp(),
            view = grid && grid.getView(),
            bufferedRenderer = view.bufferedRenderer;

        if (grid && view && expanded !== view.previewExpanded ) {
            this.previewExpanded = view.previewExpanded = !!expanded;
            view.refreshView();
        }
    }
});
