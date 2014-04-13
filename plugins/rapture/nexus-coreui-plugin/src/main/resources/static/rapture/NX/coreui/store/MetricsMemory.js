/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/**
 * Virtual machine "memory" metrics store.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.store.MetricsMemory', {
  extend: 'Ext.data.JsonStore',
  fields: [ 'name', 'data' ],
  data: [
    { 'name': 'metric one',   'data': 10 },
    { 'name': 'metric two',   'data':  7 },
    { 'name': 'metric three', 'data':  5 },
    { 'name': 'metric four',  'data':  2 },
    { 'name': 'metric five',  'data': 27 }
  ]
});


/*
 Ext.define('NX.coreui.store.MetricsMemory', {
 extend: 'Ext.data.Store',
 model: 'NX.coreui.model.MetricsMemory',

 proxy: {
 type: 'direct',
 paramsAsHash: false,
 api: {
 read: 'NX.direct.coreui_Metrics.memory'
 },

 reader: {
 type: 'json',
 root: 'data',
 successProperty: 'success'
 }
 }

 });

 */