import { inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';

@customAttribute('ui-popup')
@inject(Element)
export class AttrUiPopupCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {
        _.defer(() => {
            $(this.element).popup({
            	on: 'click',
                inline: true,
                silent: true,
                // hoverable: true,
                position: newValue ? newValue : 'bottom right',
                jitter: 300,
                delay: {
                    show: 300,
                    hide: 300
                },
                onShow: () => {
                	
                },
                onVisible: () => {

                }
            });
        });

    }
}
