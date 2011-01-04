/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
Sonatype.Events.addListener('initializeCustomTypes', function(customTypes) {
      customTypes['textarea'] = {
        createItem : function(curRec, prefix, width) {
          return {
            xtype : 'textarea',
            fieldLabel : curRec.label,
            itemCls : curRec.required ? 'required-field' : '',
            helpText : curRec.helpText,
            name : prefix + curRec.id,
            allowBlank : curRec.required ? false : true,
            disabled : true,
            width : width
          }
        },
        retrieveValue : function(item) {
          return item.getValue();
        },
        setValue : function(item, value) {
          item.setValue(value);
        }
      };
    });
