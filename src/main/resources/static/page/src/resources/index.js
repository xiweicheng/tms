/* 
代码生成常用命令:
au generate element
au generate attribute
au generate value-converter
au generate binding-behavior
au generate task
au generate generator
*/
/* 加载全局资源 */
export function configure(aurelia) {

    aurelia.globalResources([
        'resources/value-converters/vc-common',
        'resources/binding-behaviors/bb-key',
        'resources/attributes/attr-task',
        'resources/attributes/attr-swipebox',
        'resources/attributes/attr-pastable',
        'resources/attributes/attr-autosize',
        'resources/attributes/attr-dropzone',
        'resources/attributes/attr-attr',
        'resources/attributes/attr-c2c',
        'resources/attributes/attr-dimmer',
        'resources/attributes/attr-ui-dropdown',
        'resources/attributes/attr-ui-tab',
        'resources/attributes/attr-tablesort',
        'resources/attributes/attr-textcomplete',
        'resources/attributes/attr-scrollbar',
        'resources/elements/em-modal',
        'resources/elements/em-dropdown',
        'resources/elements/em-confirm-modal',
        'resources/elements/em-hotkeys-modal',
        'resources/elements/em-chat-input',
        'resources/elements/em-chat-top-menu',
        'resources/elements/em-chat-sidebar-left',
        'resources/elements/em-chat-content-item',
        'resources/elements/em-chat-sidebar-right',
        'resources/elements/em-chat-channel-create',
        'resources/elements/em-chat-channel-join',
        'resources/elements/em-chat-channel-edit',
        'resources/elements/em-chat-channel-members-mgr',
        'resources/elements/em-chat-channel-members-show',
        'resources/elements/em-chat-msg-popup',
        'resources/elements/em-chat-member-popup',
        'resources/elements/em-user-avatar',
        'resources/elements/em-user-edit',
    ]);
}
