/**
 * FileTree Translation : Turkish tr_TR.UTF-8
 *
 * @author  Ing. Jozef Sakáloš
 * @translator Kemal Tunador
 *
 * @license FileTree Translation file is licensed under the terms of
 * the Open Source LGPL 3.0 license.  Commercial use is permitted to the extent
 * that the code/component(s) do NOT become part of another Open Source or Commercially
 * licensed development library or toolkit without explicit permission.
 * 
 * License details: http://www.gnu.org/licenses/lgpl.html
 */
if(Ext.ux.FileUploader){
    Ext.apply(Ext.ux.FileUploader.prototype, {
        jsonErrorText:'JSON nesnesı çözümlenemedi',
        unknownErrorText:'Bilinmeyen hata'
    });
}

if(Ext.ux.UploadPanel){
    Ext.apply(Ext.ux.UploadPanel.prototype, {
        addText:'Ekle',
        clickRemoveText:'Silmek için tıklayın',
        clickStopText:'Durdurmak için tıklayın',
        emptyText:'Dosya yok',
        errorText:'Hata',
        fileQueuedText:'<b>{0}</b> isimli dosya yüklenmek için sırada' ,
        fileDoneText:'<b>{0}</b> isimli dosya başarıyla yüklendi',
        fileFailedText:'<b>{0}</b> isimli dosya yüklenemedi',
        fileStoppedText:'<b>{0}</b> isimli dosya kullanıcı tarafından durduruldu',
        fileUploadingText:'Yükleniyor : <b>{0}</b>',
        removeAllText:'Hepsini Sil',
        removeText:'Sil',
        stopAllText:'Hepsini Durdur',
        uploadText:'Yükle'
    });
}

if(Ext.ux.FileTreeMenu){
    Ext.apply(Ext.ux.FileTreeMenu.prototype, {
        collapseText: 'Hepsini kapat',
        deleteKeyName:'Delete Tuşu',
        deleteText:'Sil',
        expandText: 'Hepsini aç',
        newdirText:'Yeni Klasör',
        openBlankText:'Yeni pencerede aç',
        openDwnldText:'İndir',
        openPopupText:'Pop-Up pencerede aç',
        openSelfText:'Bu pencerede aç',
        openText:'Open',
        reloadText:'Y<span style="text-decoration:underline">e</span>niden Yükle',
        renameText: 'Yeniden Adlandır',
        uploadFileText:'Dosya Yükle',
        uploadText:'Yükle'
    });
}

if(Ext.ux.FileTreePanel){
    Ext.apply(Ext.ux.FileTreePanel.prototype, {
        confirmText:'Onay',
        deleteText:'Sil',
        errorText:'Hata',
        existsText:'<b>{0}</b> isimli dosya zaten mevcut',
        fileText:'Dosya',
        newdirText:'Yeni Kalsör',
        overwriteText:'Üzerine yazmak istiyor musun?',
        reallyWantText:'Bu işlemi gerçekten istiyormusun : ',
        rootText:'Ana Klasör'
    });
}

// eof
