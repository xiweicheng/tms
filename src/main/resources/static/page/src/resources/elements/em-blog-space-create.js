import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogSpaceCreate {

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.chk).checkbox();
    }

    createHandler() {
        this.ajax = $.post('/admin/space/create', {
            name: this.name,
            desc: this.desc,
            privated: $(this.chk).checkbox('is checked'),
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.name = '';
                this.desc = '';
                $(this.chk).checkbox('set unchecked');
                toastr.success('空间创建成功!');
                $(this.ppRef).popup('hide');
                ea.publish(nsCons.EVENT_SPACE_CHANGED, { action: 'created', space: data.data });
            } else {
                toastr.error(data.data, '空间创建失败!');
            }
        });
    }
}
