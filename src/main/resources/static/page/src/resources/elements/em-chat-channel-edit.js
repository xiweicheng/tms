import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatChannelEdit {

    @bindable channel;

    channelChanged() {

        if (this.channel) {
            let chkSet = this.channel.privated ? 'set checked' : 'set unchecked';
            $(this.chk).checkbox(chkSet);
        }

    }

    show() {
        this.emModal.show({
            hideOnApprove: false,
            autoDimmer: true
        });
    }

    showHandler() {}

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.chk).checkbox();
    }

    approveHandler(modal) {

        $.post('/admin/channel/update', {
            id: this.channel.id,
            title: this.channel.title,
            desc: this.channel.description,
            privated: $(this.chk).checkbox('is checked')
        }, (data) => {
            modal.hide();
            if (data.success) {
                toastr.success('更新频道成功!');
            } else {
                toastr.error(data.data, '编辑频道失败!');
            }
        });

    }
}
