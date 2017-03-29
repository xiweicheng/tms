import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatChannelMembersShow {

    @bindable channel;

    showHandler() {

    }

    approveHandler(modal) {

    }

    show() {
        this.emModal.show({
            hideOnApprove: true,
            autoDimmer: false
        });
    }
}
