import { inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';
import tips from 'common/common-tips';
import emojis from 'common/common-emoji';

@customAttribute('textcomplete')
@inject(Element)
export class AttrTextcompleteCustomAttribute {

    constructor(element) {
        this.element = element;
        this.initHotkeys();
    }

    tipsActionHandler(value) {
        if (value == '/upload') {
            $(this.element).next('.tms-edit-actions').find('button > .upload.icon').click();
        } else if (value == '/shortcuts') {
            ea.publish(nsCons.EVENT_SHOW_HOTKEYS_MODAL, {});
        } else if (value == 'search') {
            _.delay(() => { utils.openNewWin(nsCons.STR_EMOJI_SEARCH_URL); }, 200);
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
                        _.defer(() => {
                            autosize.update(this.element);
                        });
                        this.setCaretPosition(tips[value].ch2 ? tips[value].ch2 : tips[value].ch);
                        return `$1${tips[value].value}`;
                    } else {
                        return '';
                    }
                }
            }, { // @user
                match: /(^|\s)@(\w*)$/,
                search: (term, callback) => {
                    callback($.map(this.members, (member) => {
                        return (member.enabled && member.username.indexOf(term) >= 0) ? member.username : null;
                    }));
                },
                template: (value, term) => {
                    let user = _.find(this.members, { username: value });
                    return `${user.name} - ${user.mails} (${user.username})`;
                },
                replace: (value) => {
                    return `$1{~${value}}`;
                }
            }, { // emoji
                match: /(^|\s):([\+\-\w]*)$/,
                search: function(term, callback) {
                    callback($.map(emojis, (emoji) => {
                        return _.some(emoji.split('_'), (item) => {
                            return item.indexOf(term) === 0;
                        }) ? emoji : null;
                    }));
                },
                template: (value, term) => {
                    if (value == 'search') {
                        return `表情查找 - :search`;
                    }
                    let emojiKey = `:${value}:`;
                    return `${emojify.replace(emojiKey)} - ${emojiKey}`;
                },
                replace: (value) => {
                    if (this.tipsActionHandler(value)) {
                        return '$1:' + value + ': ';
                    } else {
                        return '';
                    }
                }
            }], {
                appendTo: $(this.element).prev('.textcomplete-container').find('.append-to'),
                maxCount: nsCons.NUM_TEXT_COMPLETE_MAX_COUNT
            });
        } else {
            this.unbind();
        }
    }

    setCaretPosition(ch) {
        (ch) && (_.delay(() => {
            let cr = utils.getCursortPosition(this.element);
            utils.setCaretPosition(this.element, cr - ch);
        }, 100));
    }

    initHotkeys() {

        _.each(_.filter(_.values(tips), 'key'), (value) => {
            $(this.element).bind('keydown', value.key, (evt) => {
                evt.preventDefault();
                $(this.element).insertAtCaret(value.value);
                let cr = utils.getCursortPosition(this.element);
                let ch = value.ch2 ? value.ch2 : value.ch;
                ch && (utils.setCaretPosition(this.element, cr - ch));
                _.defer(() => {
                    autosize.update(this.element);
                });
            });
        });

    }

    unbind() {
        try {
            $(this.element).textcomplete('destroy');
        } catch (err) {}
    }
}
