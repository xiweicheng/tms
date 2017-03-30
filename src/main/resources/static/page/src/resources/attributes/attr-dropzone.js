import { bindable, inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';
import {
    EventAggregator
}
from 'aurelia-event-aggregator';

@customAttribute('dropzone')
@inject(Element, EventAggregator)
export class AttrDropzone {

    @bindable clickable;
    @bindable target;
    @bindable type;

    constructor(element, eventAggregator) {
        this.element = element;
        this.eventAggregator = eventAggregator;

        this.subscribe = this.eventAggregator.subscribe(nsCons.EVENT_CHAT_MSG_EDIT_UPLOAD, (payload) => {
            if (payload.target === this.target) {
                $(this.element).click();
            }
        });
    }

    valueChanged(newValue, oldValue) {

        let target = this.target ? this.target : this.element;
        let toType = this.type ? this.type : (nsCtx.isAt ? 'User' : 'Channel');

        $(this.element).parent().addClass('tms-dropzone-preview-hidden');

        $(this.element).children().andSelf().dropzone({
            url: "/admin/file/upload",
            paramName: 'file',
            clickable: !!this.clickable,
            dictDefaultMessage: '',
            maxFilesize: 10,
            addRemoveLinks: true,
            // previewsContainer: this.chatStatusBarRef,
            // previewTemplate: this.previewTemplateRef.innerHTML,
            dictCancelUpload: '取消上传',
            dictCancelUploadConfirmation: '确定要取消上传吗?',
            dictFileTooBig: '文件过大({{filesize}}M),最大限制:{{maxFilesize}}M',
            init: function() {
                this.on("sending", function(file, xhr, formData) {
                    formData.append('toType', toType);
                    if('Blog' !== toType) {
                        formData.append('toId', nsCtx.chatTo);
                    }
                });
                this.on("success", function(file, data) {
                    if (data.success) {

                        $.each(data.data, function(index, item) {
                            if (item.type == 'Image') {
                                $(target).insertAtCaret('![{name}]({baseURL}{path}{uuidName}) '
                                    .replace(/\{name\}/g, item.name)
                                    .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                                    .replace(/\{path\}/g, item.path)
                                    .replace(/\{uuidName\}/g, item.uuidName));
                            } else {
                                $(target).insertAtCaret('[{name}]({baseURL}{path}{uuidName}) '
                                    .replace(/\{name\}/g, item.name)
                                    .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                                    .replace(/\{path\}/g, "admin/file/download/")
                                    .replace(/\{uuidName\}/g, item.id));
                            }
                        });
                        toastr.success('上传成功!');
                    } else {
                        toastr.error(data.data, '上传失败!');
                    }

                });
                this.on("error", function(file, errorMessage, xhr) {
                    toastr.error(errorMessage, '上传失败!');
                });
                this.on("complete", function(file) {
                    this.removeFile(file);
                });
            }
        });
    }

    bind(bindingContext) {
        this.valueChanged(this.value);
    }
}
