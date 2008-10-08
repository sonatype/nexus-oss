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