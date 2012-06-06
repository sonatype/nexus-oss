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

// the following is a global, singleton class
Nexus.error.ErrorHandler = function() {
  return {
    init: function() {
      window.onerror = !window.onerror ? Nexus.error.handle : window.onerror.createSequence(Nexus.error.handle);
    },
    getFormattedMessage: function(args) {
      var lines = ["The following error has occurred:"];
      if (args[0] instanceof Error) { // Error object thrown in try...catch
        var err = args[0];
        lines[lines.length] = "Message: (" + err.name + ") " + err.message;
        lines[lines.length] = "Error number: " + (err.number & 0xFFFF); //Apply binary arithmetic for IE number, firefox returns message string in element array element 0
        lines[lines.length] = "Description: " + err.description;
      } else if ((args.length == 3) && (typeof(args[2]) == "number")) { // Check the signature for a match with an unhandled exception
        lines[lines.length] = "Message: " + args[0];
        lines[lines.length] = "URL: " + args[1];
        lines[lines.length] = "Line Number: " + args[2];
      } else {
        lines = ["An unknown error has occurred."]; // purposely rebuild lines
        lines[lines.length] = "The following information may be useful:"
        for (var x = 0; x < args.length; x++) {
          lines[lines.length] = Ext.encode(args[x]);
        }
      }
      return lines.join("\n");
    },
    displayError: function(args) {
      // purposely creating a new window for each exception (to handle concurrent exceptions)
      var errWindow = new Ext.Window({
        autoScroll: true,
        bodyStyle: {padding: 5},
        height: 150,
        html: this.getFormattedMessage(args).replace(/\n/g, "<br />").replace(/\t/g, " &nbsp; &nbsp;"),
        modal: true,
        title: "An error has occurred",
        width: 400
      });
      errWindow.show();
    },
    handleError:  function() {
      var args = [];
      for (var x = 0; x < arguments.length; x++) {
        args[x] = arguments[x];
      }
      try {
        if ( '?debug' === windows.location.search )  {
          this.displayError(args);
        }
      } catch(e) {
        // don't introduce even more errors when displaying errors
      }
      // let handling continue to bubble up to firebug console etc.
      return false;
    }
  };
}();

// the following line ensures that the handleError method always executes in the scope of ErrorHandler
Nexus.error.handle = Nexus.error.ErrorHandler.handleError.createDelegate(Nexus.error.ErrorHandler);

Nexus.error.ErrorHandler.init();