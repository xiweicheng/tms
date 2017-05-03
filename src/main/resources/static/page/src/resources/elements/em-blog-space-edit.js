import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogSpaceEdit {

    space;

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.chk).checkbox();
    }

    show(space) {
        this.space = space;
        this.emModal.show({ hideOnApprove: false, autoDimmer: true });
    }

    showHandler() {
        $.get('/admin/space/get', {
            id: this.space.id
        }, (data) => {
            if (data.success) {
                this.space = data.data;
                $(this.chk).checkbox(this.space.privated ? 'set checked' : 'set unchecked');
            }
        });
    }

    approveHandler(modal) {
        $.post('/admin/space/update', {
            id: this.space.id,
            name: this.space.name,
            desc: this.space.description,
            privated: $(this.chk).checkbox('is checked')
        }, (data, textStatus, xhr) => {
            if (data.success) {
                toastr.success('空间更新成功!');
                ea.publish(nsCons.EVENT_SPACE_CHANGED, {
                    action: 'updated',
                    space: data.data
                });
                modal.hide();
            } else {
                toastr.error(data.data, '空间更新失败!');
            }
        });
    }
}
