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
/*global define*/
define('ext/tree/treenode',['extjs'], function(Ext){
Ext.override(Ext.tree.TreeNode, {
  renderChildren : function(suppressEvent) {
    var i, len, cs = this.childNodes;
    if (suppressEvent !== false)
    {
      this.fireEvent("beforechildrenrendered", this);
    }
    // Sonatype [NEXUS-77]: null checks added
    if (cs) {
      for (i = 0, len = cs.length; i < len; i=i+1)
      {
        cs[i].render(true);
      }
    }
    this.childrenRendered = true;
  }
});

Ext.override(Ext.tree.TreeNodeUI, {
  expand : function() {
    this.updateExpandIcon();
    // Sonatype [NEXUS-77]: null checks added
    if (this.ctNode) {
      this.ctNode.style.display = "";
    }
  },

  updateExpandIcon : function() {
    if (this.rendered)
    {
      var
            c1, c2, ecc,
            n = this.node,
            cls = n.isLast() ? "x-tree-elbow-end" : "x-tree-elbow";

      if (n.hasChildNodes() || n.attributes.expandable)
      {
        if (n.expanded)
        {
          cls += "-minus";
          c1 = "x-tree-node-collapsed";
          c2 = "x-tree-node-expanded";
        }
        else
        {
          cls += "-plus";
          c1 = "x-tree-node-expanded";
          c2 = "x-tree-node-collapsed";
        }
        if (this.wasLeaf)
        {
          this.removeClass("x-tree-node-leaf");
          this.wasLeaf = false;
        }
        if (this.c1 !== c1 || this.c2 !== c2)
        {
          Ext.fly(this.elNode).replaceClass(c1, c2);
          this.c1 = c1;
          this.c2 = c2;
        }
      }
      else
      {
        if (!this.wasLeaf)
        {
          Ext.fly(this.elNode).replaceClass("x-tree-node-expanded", "x-tree-node-leaf");
          delete this.c1;
          delete this.c2;
          this.wasLeaf = true;
        }
      }
      ecc = "x-tree-ec-icon " + cls;
      // Sonatype [NEXUS-77]: null checks added
      if (this.ecc !== ecc && this.ecNode)
      {
        this.ecNode.className = ecc;
        this.ecc = ecc;
      }
    }
  }
});
});
