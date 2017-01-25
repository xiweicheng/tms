import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmUserAvatar {

    @bindable user;

    userChanged() {
        if (this.user) {
            if (this.user.name) {
                this.nameChar = _.last(this.user.name);
            } else {
                this.nameChar = _.last(this.user.username);
            }
        }
    }

}
