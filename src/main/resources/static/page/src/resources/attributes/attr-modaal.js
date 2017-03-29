import { inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';

@customAttribute('modaal')
@inject(Element)
export class AttrModaalCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {
        // https://github.com/humaan/Modaal
        _.defer(() => {
            $(this.element).modaal({
                fullscreen: true,
                overlay_close: false,
                // is_locked: true,
                // start_open: true,
                before_open: () => {
                    ea.publish(nsCons.EVENT_MODAAL_BEFORE_OPEN, {
                        id: newValue
                    });
                },
                after_open: () => {
                    ea.publish(nsCons.EVENT_MODAAL_AFTER_OPEN, {
                        id: newValue
                    });
                },
                before_close: () => {
                    ea.publish(nsCons.EVENT_MODAAL_BEFORE_CLOSE, {
                        id: newValue
                    });
                },
                after_close: () => {
                    ea.publish(nsCons.EVENT_MODAAL_AFTER_CLOSE, {
                        id: newValue
                    });
                }
            });
        });

    }
}
