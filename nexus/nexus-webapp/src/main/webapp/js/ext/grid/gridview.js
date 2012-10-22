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
/*global Ext*/
Ext.override(Ext.grid.GridView, {
  initTemplates : function() {
    var
          ts = this.templates || {},
          k, t;

    if (!ts.master)
    {
      ts.master = new Ext.Template('<div class="x-grid3" hidefocus="true">', '<div class="x-grid3-viewport">',
            '<div class="x-grid3-header"><div class="x-grid3-header-inner"><div class="x-grid3-header-offset">{header}</div></div><div class="x-clear"></div></div>',
            '<div class="x-grid3-scroller"><div class="x-grid3-body">{body}</div><a href="#" class="x-grid3-focus" tabIndex="-1"></a></div>', "</div>", '<div class="x-grid3-resize-marker">&#160;</div>', '<div class="x-grid3-resize-proxy">&#160;</div>',
            "</div>");
    }

    if (!ts.header)
    {
      ts.header = new Ext.Template('<table border="0" cellspacing="0" cellpadding="0" style="{tstyle}">', '<thead><tr class="x-grid3-hd-row">{cells}</tr></thead>', "</table>");
    }

    if (!ts.hcell)
    {
      ts.hcell = new Ext.Template('<td class="x-grid3-hd x-grid3-cell x-grid3-td-{id}" style="{style}"><div {tooltip} {attr} class="x-grid3-hd-inner x-grid3-hd-{id}" unselectable="on" style="{istyle}">', this.grid.enableHdMenu ?
            '<a class="x-grid3-hd-btn" href="#"></a>' :
            '', '{value}<img class="x-grid3-sort-icon" src="', Ext.BLANK_IMAGE_URL, '" />', "</div></td>");
    }

    if (!ts.body)
    {
      ts.body = new Ext.Template('{rows}');
    }

    if (!ts.row)
    {
      ts.row = new Ext.Template('<div class="x-grid3-row {alt}" style="{tstyle}"><table class="x-grid3-row-table" border="0" cellspacing="0" cellpadding="0" style="{tstyle}">', '<tbody><tr>{cells}</tr>', (this.enableRowBody ?
            '<tr class="x-grid3-row-body-tr" style="{bodyStyle}"><td colspan="{cols}" class="x-grid3-body-cell" tabIndex="0" hidefocus="on"><div class="x-grid3-row-body">{body}</div></td></tr>' :
            ''), '</tbody></table></div>');
    }

    if (!ts.cell)
    {
      ts.cell = new Ext.Template('<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}" tabIndex="0" {cellAttr}>',
            // Sonatype [NEXUS-284]: added a column id as a hook for Selenium
            '<div id="{colid}" class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>{value}</div>', "</td>");
    }

    for (k in ts)
    {
      if (ts.hasOwnProperty(k)) {
        t = ts[k];
        if (t && typeof t.compile === 'function' && !t.compiled)
        {
          t.disableFormats = true;
          t.compile();
        }
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
    var
          ts = this.templates, ct = ts.cell, rt = ts.row, last = colCount - 1,
          tstyle = 'width:' + this.getTotalWidth() + ';',
          buf = [], cb, c, p = {}, rp = {
            tstyle : tstyle
          },
          alt = [],
          r, j, len, rowIndex, i;

    for (j = 0, len = rs.length; j < len; j=j+1)
    {
      r = rs[j];
      cb = [];
      rowIndex = (j + startRow);
      for (i = 0; i < colCount; i=i+1)
      {
        c = cs[i];
        p.id = c.id;
        p.css = i === 0 ? 'x-grid3-cell-first ' : (i === last ? 'x-grid3-cell-last ' : '');
        p.attr = p.cellAttr = "";
        p.value = c.renderer(r.data[c.name], p, r, rowIndex, i, ds);
        p.style = c.style;

        if (p.value === undefined || p.value === null || p.value === "") {
          p.value = "&#160;";
        }

        if (r.dirty && typeof r.modified[c.name] !== 'undefined')
        {
          p.css += ' x-grid3-dirty-cell';
        }
        // Sonatype [NEXUS-284]: added a column id as a hook for Selenium
        p.colid = this.grid.id + '_' + rs[j].id + '_col' + i;
        cb[cb.length] = ct.apply(p);
      }
      if (stripe && ((rowIndex + 1) % 2 === 0))
      {
        alt[0] = "x-grid3-row-alt";
      }
      if (r.dirty)
      {
        alt[1] = " x-grid3-dirty-row";
      }
      rp.cols = colCount;
      if (this.getRowClass)
      {
        alt[2] = this.getRowClass(r, rowIndex, rp, ds);
      }
      rp.alt = alt.join(" ");
      rp.cells = cb.join("");
      buf[buf.length] = rt.apply(rp);
    }
    return buf.join("");
  }
});
