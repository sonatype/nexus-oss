/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
(function(){

Sonatype.utils = {
  passwordPlaceholder : '|$|N|E|X|U|S|$|',
  version : '',
  lowercase : function(str){
    if (Ext.isEmpty(str)) {return str;}
    str = str.toString();
    return str.toLowerCase();
  },
  lowercaseFirstChar : function(str){
    if (Ext.isEmpty(str)) {return str;}
    str = str.toString();
    return str.charAt(0).toLowerCase() + str.slice(1);
  },
  capitalize : function(str){
    if (Ext.isEmpty(str)) {return str;}
    str = str.toString();
    return str.charAt(0).toUpperCase() + str.slice(1);
  },
  returnEmptyStr : function(){
    return '';
  },
  returnValidStr : function(str){
    if (str != null) {
      return str;
    }
    else {
      return Sonatype.utils.returnEmptyStr();
    }
  },
  convert : {
    stringContextToBool : function(str){
      return (str.toLowerCase() === 'true');
    },
    passwordToString : function(str){
      if (Sonatype.utils.passwordPlaceholder === str){
        return null;
      }
      else if (str){
        return str;
      }
      else{
        return Sonatype.utils.returnEmptyStr();
      }
    }
  },
  //deep copy of an object.  All references independent from object passed in.
  cloneObj : function(o){
    if(typeof(o) != 'object' || o === null) {
      return o;
    }
    
    var newObj = {};

    for(var i in o){
        newObj[i] = Sonatype.utils.cloneObj(o[i]);
    }

    return newObj;
  },
  
  // (Array : arr, string : child, [string seperator])
  // array to join, name of element of contained object, seperator (defaults to ", ")
  joinArrayObject : function (arr, child, seperator){
    var sOut = '';
    var sep = (seperator) ? seperator : ', ';
    
    for(var i=0; i<arr.length; i++){
      if((arr[i])[child]){
        sOut += (arr[i])[child] + sep;
      }
    }
    
    return sOut.substring(0, sOut.length - sep.length);
  },

  connectionError: function( response, message, offerRestart ) {
    if ( response.status == 403 ) {
      if ( Sonatype.repoServer.RepoServer.loginWindow.isVisible() ) {
        Sonatype.MessageBox.show( {
          title: 'Login Error',
          msg: 'Incorrect username or password.<br />Try again.',
          buttons: Sonatype.MessageBox.OK,
          icon: Sonatype.MessageBox.ERROR,
          animEl: 'mb3'
        } );
      }
      else {
        delete Ext.lib.Ajax.defaultHeaders.Authorization;
        Sonatype.state.CookieProvider.clear('authToken');
        Sonatype.state.CookieProvider.clear('username');
        Sonatype.MessageBox.show( {
          title: 'Authentication Error',
          msg: 'Your login is incorrect or your session has expired.<br />' +
            'Please login again.',
          buttons: Sonatype.MessageBox.OK,
          icon: Sonatype.MessageBox.ERROR,
          animEl: 'mb3',
          fn: function(button) {
            window.location.reload();
          }
        } );
      }
    }
    else {
      Sonatype.MessageBox.show( {
        title: "Connection Error",
        msg: (
          ( message ? message + '<br /><br />' : '' ) + 
          ( response.status ?
              'Nexus returned an error: ERROR ' + response.status + ': ' + response.statusText
              :
              'There was an error connecting to Nexus: ' + response.statusText + '<br />' +
              'Check your network connection, make sure Nexus is running.' ) +
          ( offerRestart ?
              '<br /><br />Click OK to reload the console or ' +
              'CANCEL if you wish to retry the same action in a little while.'
              : '' )
        ),
        buttons: offerRestart ? Sonatype.MessageBox.OKCANCEL : Sonatype.MessageBox.OK,
        icon: Sonatype.MessageBox.ERROR,
        animEl: 'mb3',
        fn: function(button) {
          if ( offerRestart && button == "ok" ) {
            window.location.reload();
          }
        }
      } );
    }
  },
  
  /**
  *  Base64 encode / decode
  *  http://www.webtoolkit.info/
  **/
  base64 : function(){
    // private property
    var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    
    // private method for UTF-8 encoding
    var _utf8_encode = function (string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }
        }

        return utftext;
    };

    // private method for UTF-8 decoding
    var _utf8_decode = function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;

        while ( i < utftext.length ) {
            c = utftext.charCodeAt(i);

            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            }
            else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            }
            else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }

        return string;
    };
    
    return {
        // public method for encoding
        encode : function (input) {
            var output = "";
            var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
            var i = 0;

            input = _utf8_encode(input);

            while (i < input.length) {
                chr1 = input.charCodeAt(i++);
                chr2 = input.charCodeAt(i++);
                chr3 = input.charCodeAt(i++);

                enc1 = chr1 >> 2;
                enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                enc4 = chr3 & 63;

                if (isNaN(chr2)) {
                    enc3 = enc4 = 64;
                } else if (isNaN(chr3)) {
                    enc4 = 64;
                }

                output = output +
                _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
                _keyStr.charAt(enc3) + _keyStr.charAt(enc4);
            }

            return output;
        },

        // public method for decoding
        decode : function (input) {
            var output = "";
            var chr1, chr2, chr3;
            var enc1, enc2, enc3, enc4;
            var i = 0;

            input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

            while (i < input.length) {
                enc1 = _keyStr.indexOf(input.charAt(i++));
                enc2 = _keyStr.indexOf(input.charAt(i++));
                enc3 = _keyStr.indexOf(input.charAt(i++));
                enc4 = _keyStr.indexOf(input.charAt(i++));

                chr1 = (enc1 << 2) | (enc2 >> 4);
                chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                chr3 = ((enc3 & 3) << 6) | enc4;

                output = output + String.fromCharCode(chr1);

                if (enc3 != 64) {
                    output = output + String.fromCharCode(chr2);
                }
                if (enc4 != 64) {
                    output = output + String.fromCharCode(chr3);
                }
            }

            output = _utf8_decode(output);

            return output;
        }
    }
  }(),
  
  defaultToNo: function() {
    //@note: this handler selects the "No" button as the default
    //@todo: could extend Sonatype.MessageBox to take the button to select as a param
    Sonatype.MessageBox.getDialog().on('show', function(){
        this.focusEl = this.buttons[2]; //ack! we're offset dependent here
        this.focus();
      },
      Sonatype.MessageBox.getDialog(),
      {single:true}
    );
  }
};

})();


Sonatype.utils.Observable = function(){
  this.addEvents({
    'repositoryChanged': true,
    'groupChanged': true 
  });
};
Ext.extend( Sonatype.utils.Observable, Ext.util.Observable );
Sonatype.Events = new Sonatype.utils.Observable();

Ext.apply(Ext.form.VTypes, {
  password: function(val, field) {
    if (field.initialPasswordField != null && field.initialPasswordField != 'undefined') {
      var pwd = field.ownerCt.find('name', field.initialPasswordField)[0].getValue();
      return (val == pwd);
    }
    return true;
  },
  
  passwordText: 'Passwords do not match.'
});
