/**
 * FileTree Translation : Russian ru_RU.UTF-16
 *
 * @author     Ing. Jozef Sakáloš
 * @translator Max K.
 * @date       28. March 2008
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
        jsonErrorText:'Не могу раскодировать объект JSON',
        unknownErrorText:'Неизвестная ошибка'
    });
}

if(Ext.ux.UploadPanel){
    Ext.apply(Ext.ux.UploadPanel.prototype, {
        addText:'Добавить',
        clickRemoveText:'Щелкните для удаления',
        clickStopText:'Щелкните для остановки',
        emptyText:'Нет файлов',
        errorText:'Ошибка',
        fileQueuedText:'Файл <b>{0}</b> поставлен в очередь' ,
        fileDoneText:'Файл <b>{0}</b> успешно загружен',
        fileFailedText:'Не смог загрузить файл <b>{0}</b>',
        fileStoppedText:'Файл <b>{0}</b> остановлен пользователем',
        fileUploadingText:'Закачиваю файл <b>{0}</b>',
        removeAllText:'Убрать все',
        removeText:'Убрать',
        stopAllText:'Остановить все',
        uploadText:'Закачать'
    });
}

if(Ext.ux.FileTreeMenu){
    Ext.apply(Ext.ux.FileTreeMenu.prototype, {
    collapseText:'Свернуть все',
    deleteKeyName:'Кнопка удаления',
    deleteText:'Удалить',
    expandText:'Развернуть все',
    newdirText:'Новая папка',
    openBlankText:'Открыть в новом окне',
    openDwnldText:'Скачать',
    openPopupText:'Открыть во всплывающем окне',
    openSelfText:'Открыть в этом окне',
    openText:'Открыть',
    reloadText:'Загрузить заново',
    renameText:'Переименовать',
    uploadFileText:'Закачать файл',
    uploadText:'Закачать'
    });
}

if(Ext.ux.FileTreePanel){
    Ext.apply(Ext.ux.FileTreePanel.prototype, {
        confirmText:'Подтвердить',
        deleteText:'Удалить',
        errorText:'Ошибка',
        existsText:'Файл <b>{0}</b> уже существует',
        fileText:'Файл',
        newdirText:'Новая папка',
        overwriteText:'Хотите записать поверх существующего?',
        reallyWantText:'Действительно хотите',
        rootText:'Корень дерева'
    });
}

// eof
