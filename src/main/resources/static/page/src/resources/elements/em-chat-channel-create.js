import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatChannelCreate {

    @bindable loginUser;
    @bindable trigger;
    @bindable name;
    activeTab = 'channel-create';

    nameRegex = /^[a-z][a-z0-9_\-]{0,49}$/;

    nameChanged(news, old) {
        this.oldName = old;
        if (news && !this.nameRegex.test(news)) {
            this.name = this._getOldName();
        }
    }

    _getOldName() {
        if (!this.nameRegex.test(this.oldName)) {
            this.oldName = '';
        }

        return this.oldName;
    }

    triggerChanged(newValue, oldValue) {
        $(this.trigger).click(() => {
            this.emModal.show({
                hideOnApprove: false,
                autoDimmer: true
            });
        });
    }

    showHandler() {
        this._reset();
    }

    _reset() {
        this.name = '';
        this.title = '';
        this.desc = '';
        $(this.chk).checkbox('set checked');
        this.channelJoinVm.refresh();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.chk).checkbox();
        $(this.tabRef).find('.item').tab({
            onVisible: (tabPath) => {
                this.activeTab = tabPath;
            }
        });
    }

    approveHandler(modal) {

        $.post('/admin/channel/create', {
            name: this.name,
            title: this.title,
            desc: this.desc,
            privated: $(this.chk).checkbox('is checked')
        }, (data) => {
            if (data.success) {
                modal.hide();
                toastr.success('创建频道成功!');
                ea.publish(nsCons.EVENT_CHAT_CHANNEL_CREATED, {
                    channel: data.data
                });
            } else {
                modal.hideDimmer();
                toastr.error(data.data, '创建频道失败!');
            }
        });

    }
}
