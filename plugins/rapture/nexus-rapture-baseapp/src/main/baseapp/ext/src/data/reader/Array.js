/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2014 Sencha Inc

Contact:  http://www.sencha.com/contact

Pre-release code in the Ext repository is intended for development purposes only and will
not always be stable. 

Use of pre-release code is permitted with your application at your own risk under standard
Ext license terms. Public redistribution is prohibited.

For early licensing, please contact us at licensing@sencha.com

Build date: 2014-09-02 11:12:40 (ef1fa70924f51a26dacbe29644ca3f31501a5fce)
*/
/**
 * @author Ed Spencer
 * @class Ext.data.reader.Array
 * 
 * <p>Data reader class to create an Array of {@link Ext.data.Model} objects from an Array.
 * Each element of that Array represents a row of data fields. The
 * fields are pulled into a Record object using as a subscript, the <code>mapping</code> property
 * of the field definition if it exists, or the field's ordinal position in the definition.</p>
 * 
 * <p><u>Example code:</u></p>
 * 
<pre><code>
Employee = Ext.define('Employee', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        {name: 'name', mapping: 1},         // "mapping" only needed if an "id" field is present which
        {name: 'occupation', mapping: 2}    // precludes using the ordinal position as the index.        
    ]
});

var myReader = new Ext.data.reader.Array({
    model: 'Employee'
}, Employee);
</code></pre>
 * 
 * <p>This would consume an Array like this:</p>
 * 
<pre><code>
[ [1, 'Bill', 'Gardener'], [2, 'Ben', 'Horticulturalist'] ]
</code></pre>
 * 
 * @constructor
 * Create a new ArrayReader
 * @param {Object} meta Metadata configuration options.
 */
Ext.define('Ext.data.reader.Array', {
    extend: 'Ext.data.reader.Json',
    alternateClassName: 'Ext.data.ArrayReader',
    alias : 'reader.array',

    // For Array Reader, methods in the base which use these properties must not see the defaults
    totalProperty: undefined,
    successProperty: undefined,

    /**
     * @private
     * Returns an accessor expression for the passed Field from an Array using either the Field's mapping, or
     * its ordinal position in the fields collsction as the index.
     * This is used by buildExtractors to create optimized on extractor function which converts raw data into model instances.
     */
    createFieldAccessExpression: function(field, fieldVarName, dataName) {
            // In the absence of a mapping property, use the original ordinal position
            // at which the Model inserted the field into its collection.
        var index  = (field.mapping == null) ? field.originalIndex : field.mapping,
            result;

        if (typeof index === 'function') {
            result = fieldVarName + '.mapping(' + dataName + ', this)';
        } else {
            if (isNaN(index)) {
                index = '"' + index + '"';
            }
            result = dataName + "[" + index + "]";
        }
        return result;
    }
});
