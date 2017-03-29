import { inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';

@customAttribute('ui-checkbox')
@inject(Element)
export class AttrUiCheckboxCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {
        $(this.element).checkbox();
    }
}
