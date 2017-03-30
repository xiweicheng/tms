import { bindable, containerless } from 'aurelia-framework';
import {
    default as ColorHash
} from 'color-hash';

@containerless
export class EmUserAvatar {

    @bindable user;

    userChanged() {
        if (this.user) {
            this._calcNameChar();
            // let hsl = colorHash.hsl(this.user.username);
            let cs = colorHash.rgb(this.user.username);
            this.bgColor = `rgba(${cs[0]}, ${cs[1]}, ${cs[2]}, 0.6)`;
            // this.bgColor = `hsl(${hsl[0]}, ${hsl[1] * 100}%, ${hsl[2] * 100}%)`;
            this.color = `rgba(${255 - cs[0]}, ${255 - cs[1]}, ${255 - cs[2]}, 1)`;
        }
    }

    _calcNameChar(lastChar = true) {
        if (this.user.name) {
            this.nameChar = lastChar ? _.last(this.user.name) : _.first(this.user.name);
        } else {
            this.nameChar = lastChar ? _.last(this.user.username) : _.first(this.user.username);
        }
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.avatarRef).hover(() => {
            this._calcNameChar(false);
        }, () => {
            this._calcNameChar();
        });
    }

}
