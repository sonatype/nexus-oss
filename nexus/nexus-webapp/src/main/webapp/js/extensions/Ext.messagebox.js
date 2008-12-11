/*
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
//Extned message box, so that we can get ids on the buttons for testing
Sonatype.MessageBox = function() {
  var F = function(){};
  F.prototype = Ext.MessageBox;
  var o = function(){};
  o.prototype = new F();
  o.superclass = F.prototype;

  Ext.override(o, function(){
    return {
      show : function(options) {
        o.superclass.show.call(this, options);
        this.getDialog().getEl().select('button').each(function(el) {
          el.dom.id = el.dom.innerHTML;
        });
      }
    };
  }());
  return new o();
}();