/**
 * FileTree Translation : Spanish es_ES
 *
 * @author  Ing. Jozef Sakáloš
 * @translator   cmendez21
 * @license FileTree Translation file is licensed under the terms of
 * the Open Source LGPL 3.0 license.  Commercial use is permitted to the extent
 * that the code/component(s) do NOT become part of another Open Source or Commercially
 * licensed development library or toolkit without explicit permission.
 * 
 * License details: http://www.gnu.org/licenses/lgpl.html
 */
if(Ext.ux.FileUploader){
    Ext.apply(Ext.ux.FileUploader.prototype, {
        jsonErrorText:'No se puede decodificar objeto JSON',
        unknownErrorText:'Error desconocido'
    });
}

if(Ext.ux.UploadPanel){
    Ext.apply(Ext.ux.UploadPanel.prototype, {
        addText:'Agregar',
        clickRemoveText:'Clic para eliminar',
        clickStopText:'Clic para detener',
        emptyText:'No hay archivos',
        errorText:'Error',
        fileQueuedText:'El archivo <b>{0}</b> is queued for upload' ,
        fileDoneText:'El archivo <b>{0}</b> ha subido exitosamente',
        fileFailedText:'El archivo <b>{0}</b> tuvo errores al subir',
        fileStoppedText:'El archivo <b>{0}</b> fue detenido por el usuario',
        fileUploadingText:'Subiendo Archivo <b>{0}</b>',
        removeAllText:'Eliminar todo',
        removeText:'Eliminar',
        stopAllText:'Detener todo',
        uploadText:'Subir'
    });
}

if(Ext.ux.FileTreeMenu){
    Ext.apply(Ext.ux.FileTreeMenu.prototype, {
    collapseText: 'Colapsar todo',
    deleteKeyName:'Eliminar llave',
    deleteText:'Borrar',
    expandText: 'Expandir todo',
    newdirText:'Nueva Carpeta',
    openBlankText:'Abrir en nueva ventana',
    openDwnldText:'Descargar',
    openPopupText:'Abrir en Ventana emergente',
    openSelfText:'Abrir en esta ventana',
    openText:'Abrir',
    reloadText:'R<span style="text-decoration:underline">e</span>cargar',
    renameText: 'Cambiar Nombre',
    uploadFileText:'<span style="text-decoration:underline">S</span>ubir archivo',
    uploadText:'Subir'
    });
}

if(Ext.ux.FileTreePanel){
    Ext.apply(Ext.ux.FileTreePanel.prototype, {
        confirmText:'Confirmar',
        deleteText:'Eliminar',
        errorText:'Error',
        existsText:'El archivo <b>{0}</b> ya existe',
        fileText:'Archivo',
        newdirText:'Nueva Carpeta',
        overwriteText:'Deseas sobre-escribirlo?',
        reallyWantText:'Realmente lo deseas',
        rootText:'Raiz de directorio'
    });
}
