/**
 * FileTree Translation : Czech cs_CZ
 *
 * @author     Ing. Jozef Sakáloš
 * @translator Ing. Jozef Sakáloš
 * @date       21. March 2008
 *
 * @license FileTree Translation file is licensed under the terms of
 * the Open Source LGPL 3.0 license.  Commercial use is permitted to the extent
 * that the code/component(s) do NOT become part of another Open Source or Commercially
 * licensed development library or toolkit without explicit permission.
 * 
 * License details: http://www.gnu.org/licenses/lgpl.html
 */
if(Ext.ux.FileUploader){
    Ext.override(Ext.ux.FileUploader, {
        jsonErrorText:'JSON objekt sy nedá dekódovat',
        unknownErrorText:'Neznámá chyba'
    });
}

if(Ext.ux.UploadPanel){
    Ext.override(Ext.ux.UploadPanel, {
        addText:'Přidat',
        clickRemoveText:'Klikni pro odebraní',
        clickStopText:'Klikni pro zastavení',
        emptyText:'Žádné soubory',
        errorText:'Chyba',
        fileQueuedText:'Soubor <b>{0}</b> je připraven k odeslání' ,
        fileDoneText:'Soubor <b>{0}</b> byl úspěšně odeslán',
        fileFailedText:'Odesílání souboru <b>{0}</b> selhalo',
        fileStoppedText:'Odesílání souboru <b>{0}</b> zastaveno uživatelem',
        fileUploadingText:'Odesílání souboru <b>{0}</b>',
        removeAllText:'Odebrat všechny',
        removeText:'Odebrat',
        stopAllText:'Zastavit všechny',
        uploadText:'Odeslat'
    });
}

if(Ext.ux.FileTreeMenu){
    Ext.override(Ext.ux.FileTreeMenu, {
    collapseText: 'Sbalit všechny',
    deleteKeyName:'Klávesa Delete',
    deleteText:'Vymazat',
    expandText: 'Rozbalit všechny',
    newdirText:'<span style="text-decoration:underline">N</span>ová složka',
    openBlankText:'Otevřít v novém okně',
    openDwnldText:'Stáhnout',
    openPopupText:'Otevřít v dialogu',
    openSelfText:'Otevřít v tomto okně',
    openText:'Otevřít',
    reloadText:'Aktualizovat',
    renameText: 'Přejemenovat',
    uploadFileText:'Odeslat so<span style="text-decoration:underline">u</span>bor',
    uploadText:'Odeslat'
    });
}

if(Ext.ux.FileTreePanel){
    Ext.override(Ext.ux.FileTreePanel, {
        confirmText:'Potvrdit',
        deleteText:'Vymazat',
        errorText:'Chyba',
        existsText:'Soubor <b>{0}</b> už existuje',
        fileText:'Soubor',
        newdirText:'Nová složka',
        overwriteText:'Chceš jej přepsat?',
        reallyWantText:'Opravdu chceš',
        rootText:'Kořen stromu'
    });
}

// eof
