import {
    inject
}
from 'aurelia-dependency-injection';
import {
    customAttribute
}
from 'aurelia-templating';


@customAttribute('task')
@inject(Element)
export class AttrTask {

    task = null;
    bindingCtx = null;

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue) {
        this.task = newValue;
        if (_.isFunction(this.task)) {
            _.bind(this.task, this.bindingCtx, this.element)();
        }
    }

    bind(bindingContext) {
        this.bindingCtx = bindingContext;
        this.valueChanged(this.value);
    }

    unbind() {
        this.element = null;
        this.task = null;
        this.bindingCtx = null;
    }

}
