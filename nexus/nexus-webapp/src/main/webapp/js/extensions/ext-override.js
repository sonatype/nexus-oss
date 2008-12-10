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
/*
 * ExtJS library fix-ups and overrides
 */

/*
 * ext-base-debug
 */
Ext.BLANK_IMAGE_URL="ext-2.2/resources/images/default/s.gif";
/*
Ext.lib.Ajax.request = function(method, uri, cb, data, options) {
  if(options){
    var hs = options.headers;
    if(hs){
      for(var h in hs){
        if(hs.hasOwnProperty(h)){
          this.initHeader(h, hs[h], false);
        }
      }
    }
    if(options.xmlData){
      this.initHeader('Content-Type', 'text/xml', false);
      //Sonatype: take method from config to accept PUT or POST
      //method = 'POST'; 
      data = options.xmlData;
    }else if(options.jsonData){
      this.initHeader('Content-Type', 'text/javascript', false);
      //Sonatype: take method from config to accept PUT or POST
      //method = 'POST'; 
      data = typeof options.jsonData == 'object' ? Ext.encode(options.jsonData) : options.jsonData;
    }
  }

  return this.asyncRequest(method, uri, cb, data);
};
*/
Ext.lib.Ajax.setHeader = function(o) {
  //Sonatype: Safari and IE don't always overwrite headers correctly, so need to merge default and provide headers before writing
  if (this.hasDefaultHeaders && this.hasHeaders) {
    var combinedHeaders = Ext.applyIf(this.headers, this.defaultHeaders);
    
    for (var prop in combinedHeaders) {
      if (combinedHeaders.hasOwnProperty(prop)) {
        o.conn.setRequestHeader(prop, combinedHeaders[prop]);
      }
    }
    
    this.headers = {};
    this.hasHeaders = false;
  }
  else if (this.hasDefaultHeaders) {
    for (var prop in this.defaultHeaders) {
      if (this.defaultHeaders.hasOwnProperty(prop)) {
        o.conn.setRequestHeader(prop, this.defaultHeaders[prop]);
      }
    }
  }
  else if (this.hasHeaders) {
    for (var prop in this.headers) {
      if (this.headers.hasOwnProperty(prop)) {
        o.conn.setRequestHeader(prop, this.headers[prop]);
      }
    }
    this.headers = {};
    this.hasHeaders = false;
  }
};


/*
 * ext-all-debug
 */
Ext.override(Ext.data.Connection, {
  request : function(o){
    if (this.fireEvent("beforerequest", this, o) !== false){
      var p = o.params;

      if(typeof p == "function"){
        p = p.call(o.scope||window, o);
      }
      if(typeof p == "object"){
        p = Ext.urlEncode(p);
      }
      if(this.extraParams){
        var extras = Ext.urlEncode(this.extraParams);
        p = p ? (p + '&' + extras) : extras;
      }

      var url = o.url || this.url;
      if(typeof url == 'function'){
        url = url.call(o.scope||window, o);
      }

      if(o.form){
        var form = Ext.getDom(o.form);
        url = url || form.action;

        var enctype = form.getAttribute("enctype");
        if(o.isUpload || (enctype && enctype.toLowerCase() == 'multipart/form-data')){
          return this.doFormUpload(o, p, url);
        }
        var f = Ext.lib.Ajax.serializeForm(form);
        p = p ? (p + '&' + f) : f;
      }

      var hs = o.headers;
      if(this.defaultHeaders){
        // Sonatype: default header fix
        hs = Ext.applyIf(hs || {}, this.defaultHeaders);
        if(!o.headers){
          o.headers = hs;
        }
      }

      var cb = {
        success: this.handleResponse,
        failure: this.handleFailure,
        scope: this,
        argument: {options: o},
        timeout : o.timeout || this.timeout
      };

      var method = o.method||this.method||(p ? "POST" : "GET");

      if(method == 'GET' && (this.disableCaching && o.disableCaching !== false) || o.disableCaching === true){
        url += (url.indexOf('?') != -1 ? '&' : '?') + '_dc=' + (new Date().getTime());
      }

      if(typeof o.autoAbort == 'boolean'){ 
        if(o.autoAbort){
          this.abort();
        }
      }else if(this.autoAbort !== false){
        this.abort();
      }
      if((method == 'GET' && p) || o.xmlData || o.jsonData){
        url += (url.indexOf('?') != -1 ? '&' : '?') + p;
        p = '';
      }
      this.transId = Ext.lib.Ajax.request(method, url, cb, p, o);
      return this.transId;
    }else{
      Ext.callback(o.callback, o.scope, [o, null, null]);
      return null;
    }
  }
});

Ext.override(Ext.data.Node, {
  hasChildNodes : function(){
    // Sonatype [NEXUS-77]: null check added
    return !this.isLeaf() && this.childNodes && this.childNodes.length > 0;
  }
});

/*
Date.formatCodeToRegex = function(character, currentGroup) {
  switch (character) {
    case "d":
      return {g:1,
        c:"d = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(\\d{2})"};
    case "D":
      for (var a = [], i = 0; i < 7; a.push(Date.getShortDayName(i)), ++i);
      return {g:0,
        c:null,
        s:"(?:" + a.join("|") +")"};
    case "j":
      return {g:1,
        c:"d = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(\\d{1,2})"};
    case "l":
      return {g:0,
        c:null,
        s:"(?:" + Date.dayNames.join("|") + ")"};
    case "N":
      return {g:0,
        c:null,
        s:"[1-7]"};     
    case "S":
      return {g:0,
        c:null,
        s:"(?:st|nd|rd|th)"};
    case "w":
      return {g:0,
        c:null,
        s:"[0-6]"};     
    case "z":
      return {g:0,
        c:null,
        s:"(?:\\d{1,3}"};     
    case "W":
      return {g:0,
        c:null,
        s:"(?:\\d{2})"};     
    case "F":
      return {g:1,
        c:"m = parseInt(Date.getMonthNumber(results[" + currentGroup + "]), 10);\n",             
        s:"(" + Date.monthNames.join("|") + ")"};
    case "m":
      return {g:1,
        c:"m = parseInt(results[" + currentGroup + "], 10) - 1;\n",
        s:"(\\d{2})"};     
    case "M":
      for (var a = [], i = 0; i < 12; a.push(Date.getShortMonthName(i)), ++i);         
      return {g:1,
        c:"m = parseInt(Date.getMonthNumber(results[" + currentGroup + "]), 10);\n",             
        s:"(" + a.join("|") + ")"};
    case "n":
      return {g:1,
        c:"m = parseInt(results[" + currentGroup + "], 10) - 1;\n",
        s:"(\\d{1,2})"};     
    case "t":
      return {g:0,
        c:null,
        s:"(?:\\d{2})"};     
    case "L":
      return {g:0,
        c:null,
        s:"(?:1|0)"};
    case "o":
    case "Y":
      return {g:1,
        c:"y = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(\\d{4})"};     
    case "y":
      return {g:1,
        c:"var ty = parseInt(results[" + currentGroup + "], 10);\n"
            + "y = ty > Date.y2kYear ? 1900 + ty : 2000 + ty;\n",
        s:"(\\d{1,2})"};     
    case "a":
      return {g:1,
        c:"if (results[" + currentGroup + "] == 'am') {\n"
            + "if (h == 12) { h = 0; }\n"
            + "} else { if (h < 12) { h += 12; }}",
        s:"(am|pm)"};
    case "A":
      return {g:1,
        c:"if (results[" + currentGroup + "] == 'AM') {\n"
            + "if (h == 12) { h = 0; }\n"
            + "} else { if (h < 12) { h += 12; }}",
        s:"(AM|PM)"};
    case "g":
    case "G":
      return {g:1,
        c:"h = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(\\d{1,2})"};     
    case "h":
    case "H":
      return {g:1,
        c:"h = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(\\d{2})"};     
    case "i":
      return {g:1,
        c:"i = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(\\d{2})"};     
    case "s":
      return {g:1,
        c:"s = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(\\d{2})"};     
    case "u":
      return {g:1,
        c:"ms = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(\\d{3})"};     
    case "O":
      return {g:1,
        c:[
          "o = results[", currentGroup, "];\n",
          "var sn = o.substring(0,1);\n",                 
          "var hr = o.substring(1,3)*1 + Math.floor(o.substring(3,5) / 60);\n",                 "var mn = o.substring(3,5) % 60;\n",                 "o = ((-12 <= (hr*60 + mn)/60) && ((hr*60 + mn)/60 <= 14))?\n",                 "    (sn + String.leftPad(hr, 2, '0') + String.leftPad(mn, 2, '0')) : null;\n"
        ].join(""),
        s: "([+\-]\\d{4})"};     
    case "P":
      return {g:1,
        c:[
          "o = results[", currentGroup, "];\n",
          // Sonatype: case "P" - for date parsing with Z UTC
          "if(!o) {o='+00:00';}\n",
          "var sn = o.substring(0,1);\n",                 
          "var hr = o.substring(1,3)*1 + Math.floor(o.substring(4,6) / 60);\n",                 "var mn = o.substring(4,6) % 60;\n",                 "o = ((-12 <= (hr*60 + mn)/60) && ((hr*60 + mn)/60 <= 14))?\n",                 "    (sn + String.leftPad(hr, 2, '0') + String.leftPad(mn, 2, '0')) : null;\n"
        ].join(""),
        s: "Z|([+\-]\\d{2}:\\d{2})"};     
    case "T":
      return {g:0,
        c:null,
        s:"[A-Z]{1,4}"};     
    case "Z":
      return {g:1,
        c:"z = results[" + currentGroup + "] * 1;\n" +                   
          "z = (-43200 <= z && z <= 50400)? z : null;\n",
        s:"([+\-]?\\d{1,5})"};
    case "c":
      var df = Date.formatCodeToRegex, calc = [];
      var arr = [df("Y", 1), df("m", 2), df("d", 3), df("h", 4), df("i", 5), df("s", 6), df("P", 7)];
      for (var i = 0, l = arr.length; i < l; ++i) {
        calc.push(arr[i].c);
      }
      return {g:1,
        c:calc.join(""),
        s:arr[0].s + "-" + arr[1].s + "-" + arr[2].s + "T" + arr[3].s + ":" + arr[4].s + ":" + arr[5].s + arr[6].s};
    case "U":
      return {g:1,
        c:"u = parseInt(results[" + currentGroup + "], 10);\n",
        s:"(-?\\d+)"};
    default:
      return {g:0,
        c:null,
        s:Ext.escapeRe(character)};
  }
};
*/

Ext.override(Ext.Component,{ stateful:false });

Ext.dd.DropTarget = function(el, config){
  this.el = Ext.get(el);
  
  Ext.apply(this, config);
  
  if(this.containerScroll){
    Ext.dd.ScrollManager.register(this.el);
  }
  
  Ext.dd.DropTarget.superclass.constructor.call(this, this.el.dom, this.ddGroup || this.group,
    // Sonatype: allow config to pass thru to Ext.dd.DDTarget constructor so padding may
    //   be passed from Ext.tree.TreePanel config when it need to have the whole drop panel
    //   set as the drop zone because this is not done by the library      
    config || {isTarget: true});
};

Ext.extend(Ext.dd.DropTarget, Ext.dd.DDTarget, {
  dropAllowed : "x-dd-drop-ok",
  dropNotAllowed : "x-dd-drop-nodrop",
  isTarget : true,
  isNotifyTarget : true,
  notifyEnter : function(dd, e, data){
    if(this.overClass){
      this.el.addClass(this.overClass);
    }
    return this.dropAllowed;
  },
  notifyOver : function(dd, e, data){
    return this.dropAllowed;
  },
  notifyOut : function(dd, e, data){
    if(this.overClass){
      this.el.removeClass(this.overClass);
    }
  },
  notifyDrop : function(dd, e, data){
    return false;
  }
});

Ext.dd.DropZone = function(el, config){
  Ext.dd.DropZone.superclass.constructor.call(this, el, config);
};

Ext.extend(Ext.dd.DropZone, Ext.dd.DropTarget, {
  getTargetFromEvent : function(e){
    return Ext.dd.Registry.getTargetFromEvent(e);
  },
  onNodeEnter : function(n, dd, e, data){
  },
  onNodeOver : function(n, dd, e, data){
    return this.dropAllowed;
  },
  onNodeOut : function(n, dd, e, data){
  },
  onNodeDrop : function(n, dd, e, data){
    return false;
  },
  onContainerOver : function(dd, e, data){
    return this.dropNotAllowed;
  },
  onContainerDrop : function(dd, e, data){
    return false;
  },
  notifyEnter : function(dd, e, data){
    return this.dropNotAllowed;
  },
  notifyOver : function(dd, e, data){
    var n = this.getTargetFromEvent(e);
    if(!n){ 
      if(this.lastOverNode){
        this.onNodeOut(this.lastOverNode, dd, e, data);
        this.lastOverNode = null;
      }
      return this.onContainerOver(dd, e, data);
    }
    if(this.lastOverNode != n){
      if(this.lastOverNode){
        this.onNodeOut(this.lastOverNode, dd, e, data);
      }
      this.onNodeEnter(n, dd, e, data);
      this.lastOverNode = n;
    }
    return this.onNodeOver(n, dd, e, data);
  },
  notifyOut : function(dd, e, data){
    if(this.lastOverNode){
      this.onNodeOut(this.lastOverNode, dd, e, data);
      this.lastOverNode = null;
    }
  },
  notifyDrop : function(dd, e, data){
    if(this.lastOverNode){
      this.onNodeOut(this.lastOverNode, dd, e, data);
      this.lastOverNode = null;
    }
    var n = this.getTargetFromEvent(e);
    return n ?
      this.onNodeDrop(n, dd, e, data) :
      this.onContainerDrop(dd, e, data);
  },
  triggerCacheRefresh : function(){
    Ext.dd.DDM.refreshCache(this.groups);
  }  
});

Ext.override( Ext.form.Field, {
  adjustWidth : function(tag, w){
    tag = tag.toLowerCase();
    // Sonatype: modified input text sizing for Safari3 in strict mode bug.
    if(typeof w == 'number') { // && !Ext.isSafari){ 
      if(Ext.isIE && (tag == 'input' || tag == 'textarea')){
        if(tag == 'input' && !Ext.isStrict){
          return this.inEditor ? w : w - 3;
        }
        if(tag == 'input' && Ext.isStrict){
          return w - (Ext.isIE6 ? 4 : 1);
        }
        if(tag = 'textarea' && Ext.isStrict){
          return w-2;
        }
      }
      else if(Ext.isOpera && Ext.isStrict){
        if(tag == 'input'){
          return w + 2;
        }
        if(tag = 'textarea'){
          return w-2;
        }
      }
      else if(Ext.isSafari3){
        // Sonatype: assumes we are serving xhtml transitional doctype
        if(tag == 'input'){
          return w - 8;
        }
      }
    }
    return w;
  }
});

Ext.override( Ext.form.TextField, {
  onEnable : function(){
    this.getActionEl().removeClass(this.disabledClass);
    this.el.dom.readOnly = false;
  },
  onDisable : function(){
    this.getActionEl().addClass(this.disabledClass);
    this.el.dom.readOnly = true;
  }
});

Ext.override( Ext.grid.GridView, {
  initTemplates : function() {
    var ts = this.templates || {};
    if (!ts.master) {
      ts.master = new Ext.Template(
          '<div class="x-grid3" hidefocus="true">',
          '<div class="x-grid3-viewport">',
          '<div class="x-grid3-header"><div class="x-grid3-header-inner"><div class="x-grid3-header-offset">{header}</div></div><div class="x-clear"></div></div>',
          '<div class="x-grid3-scroller"><div class="x-grid3-body">{body}</div><a href="#" class="x-grid3-focus" tabIndex="-1"></a></div>',
          "</div>", '<div class="x-grid3-resize-marker">&#160;</div>',
          '<div class="x-grid3-resize-proxy">&#160;</div>', "</div>");
    }

    if (!ts.header) {
      ts.header = new Ext.Template(
          '<table border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
          '<thead><tr class="x-grid3-hd-row">{cells}</tr></thead>',
          "</table>");
    }

    if (!ts.hcell) {
      ts.hcell = new Ext.Template(
          '<td class="x-grid3-hd x-grid3-cell x-grid3-td-{id}" style="{style}"><div {tooltip} {attr} class="x-grid3-hd-inner x-grid3-hd-{id}" unselectable="on" style="{istyle}">',
          this.grid.enableHdMenu ? '<a class="x-grid3-hd-btn" href="#"></a>'
              : '', '{value}<img class="x-grid3-sort-icon" src="',
          Ext.BLANK_IMAGE_URL, '" />', "</div></td>");
    }

    if (!ts.body) {
      ts.body = new Ext.Template('{rows}');
    }

    if (!ts.row) {
      ts.row = new Ext.Template(
          '<div class="x-grid3-row {alt}" style="{tstyle}"><table class="x-grid3-row-table" border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
          '<tbody><tr>{cells}</tr>',
          (this.enableRowBody ? '<tr class="x-grid3-row-body-tr" style="{bodyStyle}"><td colspan="{cols}" class="x-grid3-body-cell" tabIndex="0" hidefocus="on"><div class="x-grid3-row-body">{body}</div></td></tr>'
              : ''), '</tbody></table></div>');
    }

    if (!ts.cell) {
      ts.cell = new Ext.Template(
          '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}" tabIndex="0" {cellAttr}>',
          // Sonatype [NEXUS-284]: added a column id as a hook for Selenium
          '<div id="{colid}" class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>{value}</div>',
          "</td>");
    }

    for ( var k in ts) {
      var t = ts[k];
      if (t && typeof t.compile == 'function' && !t.compiled) {
        t.disableFormats = true;
        t.compile();
      }
    }

    this.templates = ts;

    this.tdClass = 'x-grid3-cell';
    this.cellSelector = 'td.x-grid3-cell';
    this.hdCls = 'x-grid3-hd';
    this.rowSelector = 'div.x-grid3-row';
    this.colRe = new RegExp("x-grid3-td-([^\\s]+)", "");
  },

  doRender : function(cs, rs, ds, startRow, colCount, stripe) {
    var ts = this.templates, ct = ts.cell, rt = ts.row, last = colCount - 1;
    var tstyle = 'width:' + this.getTotalWidth() + ';';
    var buf = [], cb, c, p = {}, rp = {
      tstyle :tstyle
    }, r;
    for ( var j = 0, len = rs.length; j < len; j++) {
      r = rs[j];
      cb = [];
      var rowIndex = (j + startRow);
      for ( var i = 0; i < colCount; i++) {
        c = cs[i];
        p.id = c.id;
        p.css = i == 0 ? 'x-grid3-cell-first '
            : (i == last ? 'x-grid3-cell-last ' : '');
        p.attr = p.cellAttr = "";
        p.value = c.renderer(r.data[c.name], p, r, rowIndex, i, ds);
        p.style = c.style;
        if (p.value == undefined || p.value === "")
          p.value = "&#160;";
        if (r.dirty && typeof r.modified[c.name] !== 'undefined') {
          p.css += ' x-grid3-dirty-cell';
        }
        // Sonatype [NEXUS-284]: added a column id as a hook for Selenium
        p.colid = this.grid.id + '_' + rs[j].id + '_col' + i;
        cb[cb.length] = ct.apply(p);
      }
      var alt = [];
      if (stripe && ((rowIndex + 1) % 2 == 0)) {
        alt[0] = "x-grid3-row-alt";
      }
      if (r.dirty) {
        alt[1] = " x-grid3-dirty-row";
      }
      rp.cols = colCount;
      if (this.getRowClass) {
        alt[2] = this.getRowClass(r, rowIndex, rp, ds);
      }
      rp.alt = alt.join(" ");
      rp.cells = cb.join("");
      buf[buf.length] = rt.apply(rp);
    }
    return buf.join("");
  }
});

Ext.override(Ext.tree.TreeNode, {
  renderChildren : function(suppressEvent){
    if (suppressEvent !== false) {
      this.fireEvent("beforechildrenrendered", this);
    }
    var cs = this.childNodes;
    // Sonatype [NEXUS-77]: null checks added
    if (cs) for (var i = 0, len = cs.length; i < len; i++) {
      cs[i].render(true);
    }
    this.childrenRendered = true;
  }
});

Ext.override(Ext.tree.TreeNodeUI, {
  expand : function(){
    this.updateExpandIcon();
    // Sonatype [NEXUS-77]: null checks added
    if (this.ctNode) this.ctNode.style.display = "";
  },
  
  updateExpandIcon : function(){
    if (this.rendered) {
      var n = this.node, c1, c2;
      var cls = n.isLast() ? "x-tree-elbow-end" : "x-tree-elbow";
      var hasChild = n.hasChildNodes();
      if (hasChild || n.attributes.expandable) {
        if (n.expanded) {
          cls += "-minus";
          c1 = "x-tree-node-collapsed";
          c2 = "x-tree-node-expanded";
        }
        else{
          cls += "-plus";
          c1 = "x-tree-node-expanded";
          c2 = "x-tree-node-collapsed";
        }
        if (this.wasLeaf) {
          this.removeClass("x-tree-node-leaf");
          this.wasLeaf = false;
        }
        if (this.c1 != c1 || this.c2 != c2) {
          Ext.fly(this.elNode).replaceClass(c1, c2);
          this.c1 = c1; this.c2 = c2;
        }
      }
      else {
        if (!this.wasLeaf) {
          Ext.fly(this.elNode).replaceClass("x-tree-node-expanded", "x-tree-node-leaf");
          delete this.c1;
          delete this.c2;
          this.wasLeaf = true;
        }
      }
      var ecc = "x-tree-ec-icon "+cls;
      // Sonatype [NEXUS-77]: null checks added
      if (this.ecc != ecc && this.ecNode) {
        this.ecNode.className = ecc;
        this.ecc = ecc;
      }
    }
  }
});

Ext.override(Ext.layout.FormLayout, {
    renderItem : function(c, position, target){
        if(c && !c.rendered && c.isFormField && c.inputType != 'hidden'){
            var args = [
                   c.id, c.fieldLabel,
                   c.labelStyle||this.labelStyle||'',
                   this.elementStyle||'',
                   typeof c.labelSeparator == 'undefined' ? this.labelSeparator : c.labelSeparator,
                   (c.itemCls||this.container.itemCls||'') + (c.hideLabel ? ' x-hide-label' : ''),
                   c.clearCls || 'x-form-clear-left' 
            ];
            if(typeof position == 'number'){
                position = target.dom.childNodes[position] || null;
            }
            if(position){
                c.formItem = this.fieldTpl.insertBefore(position, args, true);
            }else{
                c.formItem = this.fieldTpl.append(target, args, true);
            }
            c.render('x-form-el-'+c.id);
            c.container = c.formItem; // must set after render, because render sets it.
            c.actionMode = 'container';
        }else {
            Ext.layout.FormLayout.superclass.renderItem.apply(this, arguments);
        }
    }
});

Ext.lib.Ajax.handleTransactionResponse = function(o, callback, isAbort)
{
    if (!callback) {
        this.releaseObject(o);
        return;
    }

    var httpStatus, responseObject;

    try
    {
        if (o.conn.status !== undefined && o.conn.status != 0) {
            httpStatus = o.conn.status;
        }
        else {
            httpStatus = 13030;
        }
    }
    catch(e) {


        httpStatus = 13030;
    }

    if ( (httpStatus >= 200 && httpStatus < 300) || httpStatus == 1223) {
        responseObject = this.createResponseObject(o, callback.argument);
        if (callback.success) {
            if (!callback.scope) {
                callback.success(responseObject);
            }
            else {


                callback.success.apply(callback.scope, [responseObject]);
            }
        }
    }
    else {
        switch (httpStatus) {

            case 12002:
            case 12029:
            case 12030:
            case 12031:
            case 12152:
            case 13030:
                responseObject = this.createExceptionObject(o.tId, callback.argument, (isAbort ? isAbort : false));
                if (callback.failure) {
                    if (!callback.scope) {
                        callback.failure(responseObject);
                    }
                    else {
                        callback.failure.apply(callback.scope, [responseObject]);
                    }
                }
                break;
            default:
                responseObject = this.createResponseObject(o, callback.argument);
                if (callback.failure) {
                    if (!callback.scope) {
                        callback.failure(responseObject);
                    }
                    else {
                        callback.failure.apply(callback.scope, [responseObject]);
                    }
                }
        }
    }

    this.releaseObject(o);
    responseObject = null;
};