import { customAttribute, bindable } from 'aurelia-framework';
import { inject } from 'aurelia-dependency-injection';

@customAttribute('attr')
@inject(Element)
export class AttrAttr {

    @bindable name;
    @bindable value;

    constructor(element) {

        this.element = element;
    }

    nameChanged(value) {}

    valueChanged(value) {

        this.value = value;

        if (value) {
            $(this.element).attr(this.name, value);
        } else {
            $(this.element).removeAttr(this.name);
        }
    }

    bind(bindingContext) {
        this.valueChanged(this.value);
    }

    unbind() {}
}
