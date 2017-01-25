import { inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';
import tips from 'common/common-tips';

@customAttribute('textcomplete')
@inject(Element)
export class AttrTextcompleteCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    tipsActionHandler(value) {
        if (value == '/upload') {
            $(this.element).next('.tms-edit-actions').find('button > .upload.icon').click();
        } else if (value == '/shortcuts') {
            ea.publish(nsCons.EVENT_SHOW_HOTKEYS_MODAL, {});
        } else {
            return true;
        }
        return false;
    }

    valueChanged() {
        if (this.value) {
            this.members = this.value;
            $(this.element).textcomplete([{ // chat msg help
                match: /(|\b)(\/.*)$/,
                search: (term, callback) => {
                    var keys = _.keys(tips);
                    callback($.map(keys, (key) => {
                        return key.indexOf(term) === 0 ? key : null;
                    }));
                },
                template: (value, term) => {
                    return tips[value].label;
                },
                replace: (value) => {
                    if (this.tipsActionHandler(value)) {
                        _.delay(() => {
                            autosize.update(this.element);
                        });
                        return `$1${tips[value].value}`;
                    } else {
                        return '';
                    }
                }
            }, { // @user
                match: /(^|\s)@(\w*)$/,
                search: (term, callback) => {
                    callback($.map(this.members, (member) => {
                        return member.username.indexOf(term) === 0 ? member.username : null;
                    }));
                },
                template: (value, term) => {
                    let user = _.find(this.members, { username: value });
                    return `${user.name} - ${user.mails} (${user.username})`;
                },
                replace: (value) => {
                    return `$1{~${value}}`;
                }
            }], {
                appendTo: $(this.element).prev('.textcomplete-container').find('.append-to'),
            });
        } else {
            this.unbind();
        }
    }

    unbind() {
        try {
            $(this.element).textcomplete('destroy');
        } catch (err) {}
    }
}
