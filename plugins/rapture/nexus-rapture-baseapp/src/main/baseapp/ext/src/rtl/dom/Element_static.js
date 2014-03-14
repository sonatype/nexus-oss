/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
Ext.define('Ext.rtl.dom.Element_static', {
    override: 'Ext.dom.Element',
   
    statics: { 
        rtlUnitizeBox: function(box, units){
            var a = this.addUnits,
                b = this.parseBox(box);

            // Usual order is trbl, so reverse it
            // to return tlbr
            return a(b.top, units) + ' ' +
                   a(b.left, units) + ' ' +
                   a(b.bottom, units) + ' ' +
                   a(b.right, units);
        },
        
        rtlParseBox: function(box){
            var box = Ext.dom.Element.parseBox(box),
                temp;
               
            temp = box.left;
            box.left = box.right;
            box.right = temp;
            
            return box;
        }
    }
});
