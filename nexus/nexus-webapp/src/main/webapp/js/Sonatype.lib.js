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
(function(){

Sonatype.lib.Permissions = {
    READ : 1,   // 0001
    EDIT : 2,   // 0010
    DELETE : 4, // 0100
    CREATE : 8, // 1000
    ALL : 15,   // 1111
    NONE : 0,   // 0000
    
    // returns bool indicating if value has all perms
    // all values are base 10 representations of the n-bit representation
    // Example: for 4-bit permissions: 3 (base to) represents 0011 (base 2)
    checkPermission : function(value, perm /*, perm...*/) {
      var p = perm;
      
      if (Sonatype.user.curr.repoServer){      
        Ext.each(Sonatype.user.curr.repoServer, function(item, i, arr){
          if ( item.id == value ){
            value = item.value;
            return false;
          }
        });
      }
      
      if(arguments.length > 2){
        var perms = Array.slice(arguments, 2);
        Ext.each(perms, function(item, i, arr){
          p = p | item;
        });
      }
      
      return ((p & value) == p);
    }
};


/* Adapted from ExtJS v2.0.2 Ext.state.CookieProvider; removed inheritance from Ext.state.Provider
 *
 * @cfg {String} path The path for which the cookie is active (defaults to root '/' which makes it active for all pages in the site)
 * @cfg {Date} expires The cookie expiration date (defaults to 7 days from now)
 * @cfg {String} domain The domain to save the cookie for.  Note that you cannot specify a different domain than
 * your page is on, but you can specify a sub-domain, or simply the domain itself like 'extjs.com' to include
 * all sub-domains if you need to access cookies across different sub-domains (defaults to null which uses the same
 * domain the page is running on including the 'www' like 'www.extjs.com')
 * @cfg {Boolean} secure True if the site is using SSL (defaults to false)
 * @constructor
 * Create a new CookieProvider
 * @param {Object} config The configuration object
 */
Sonatype.lib.CookieProvider = function(config){
    this.namePrefix = 'st-'; //added alternate default prefix
    this.path = "/";
    this.expires = new Date(new Date().getTime()+(1000*60*60*24*7)); //7 days
    this.domain = null;
    this.secure = false;
    Ext.apply(this, config);
    this.state = this.readCookies();
};

Sonatype.lib.CookieProvider.prototype = {
  /**
  * Returns the current value for a key
  * @param {String} name The key name
  * @param {Mixed} defaultValue A default value to return if the key's value is not found
  * @return {Mixed} The state data
  */
  get : function(name, defaultValue){
    return typeof this.state[name] == "undefined" ?  defaultValue : this.state[name];
  },

  // private
  set : function(name, value){
    if(typeof value == "undefined" || value === null){
      this.clear(name);
      return;
    }
    this.setCookie(name, value);
    this.state[name] = value;
  },

  // private
  clear : function(name){
    this.clearCookie(name);
    delete this.state[name];
  },

  // private
  readCookies : function(){
    var cookies = {};
    var c = document.cookie + ";";
    var re = /\s?(.*?)=(.*?);/g;
    var matches;
    while((matches = re.exec(c)) != null){
      var name = matches[1];
      var value = matches[2];
      if(name && name.substring(0,3) == this.namePrefix){
        cookies[name.substr(3)] = this.decodeValue(value);
      }
    }
    return cookies;
  },

  // private
  setCookie : function(name, value){
    document.cookie = this.namePrefix+ name + "=" + this.encodeValue(value) +
    ((this.expires == null) ? "" : ("; expires=" + this.expires.toGMTString())) +
    ((this.path == null) ? "" : ("; path=" + this.path)) +
    ((this.domain == null) ? "" : ("; domain=" + this.domain)) +
    ((this.secure == true) ? "; secure" : "");
  },

  // private
  clearCookie : function(name){
    document.cookie = this.namePrefix + name + "=null; expires=Thu, 01-Jan-70 00:00:01 GMT" +
    ((this.path == null) ? "" : ("; path=" + this.path)) +
    ((this.domain == null) ? "" : ("; domain=" + this.domain)) +
    ((this.secure == true) ? "; secure" : "");
  },

  /**
  * Decodes a string previously encoded with {@link #encodeValue}.
  * @param {String} value The value to decode
  * @return {Mixed} The decoded value
  */
  decodeValue : function(cookie){
    var re = /^(a|n|d|b|s|o)\:(.*)$/;
    var matches = re.exec(unescape(cookie));
    if(!matches || !matches[1]) return; // non state cookie
    var type = matches[1];
    var v = matches[2];
    switch(type){
      case "n":
      return parseFloat(v);
      case "d":
      return new Date(Date.parse(v));
      case "b":
      return (v == "1");
      case "a":
      var all = [];
      var values = v.split("^");
      for(var i = 0, len = values.length; i < len; i++){
        all.push(this.decodeValue(values[i]));
      }
      return all;
      case "o":
      var all = {};
      var values = v.split("^");
      for(var i = 0, len = values.length; i < len; i++){
        var kv = values[i].split("=");
        all[kv[0]] = this.decodeValue(kv[1]);
      }
      return all;
      default:
      return v;
    }
  },

  /**
  * Encodes a value including type information.  Decode with {@link #decodeValue}.
  * @param {Mixed} value The value to encode
  * @return {String} The encoded value
  */
  encodeValue : function(v){
    var enc;
    if(typeof v == "number"){
      enc = "n:" + v;
    }else if(typeof v == "boolean"){
      enc = "b:" + (v ? "1" : "0");
    }else if(Ext.isDate(v)){
      enc = "d:" + v.toGMTString();
    }else if(Ext.isArray(v)){
      var flat = "";
      for(var i = 0, len = v.length; i < len; i++){
        flat += this.encodeValue(v[i]);
        if(i != len-1) flat += "^";
      }
      enc = "a:" + flat;
    }else if(typeof v == "object"){
      var flat = "";
      for(var key in v){
        if(typeof v[key] != "function" && v[key] !== undefined){
          flat += key + "=" + this.encodeValue(v[key]) + "^";
        }
      }
      enc = "o:" + flat.substring(0, flat.length-1);
    }else{
      enc = "s:" + v;
    }
    return escape(enc);        
  }
};

})();