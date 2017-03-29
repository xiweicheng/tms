import { inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';
import 'common/common-plugin';
import 'common/common-paste';

@customAttribute('pastable')
@inject(Element)
export class AttrPastable {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {
        // clipboard paste image
        $(this.element).pastableTextarea().on('pasteImage', (ev, data) => {

            $.post('/admin/file/base64', {
                dataURL: data.dataURL,
                type: data.blob.type,
                toType: nsCtx.isAt ? 'User' : 'Channel',
                toId: nsCtx.chatTo
            }, (data, textStatus, xhr) => {
                if (data.success) {
                    $(this.element).insertAtCaret('![{name}]({baseURL}{path}{uuidName})'
                        .replace(/\{name\}/g, data.data.name)
                        .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                        .replace(/\{path\}/g, data.data.path)
                        .replace(/\{uuidName\}/g, data.data.uuidName));
                }
            });
        }).on('pasteImageError', (ev, data) => {
            toastr.error(data.message, '剪贴板粘贴图片错误!');
        });
    }

    bind(bindingContext) {
        this.valueChanged(this.value);
    }
}
