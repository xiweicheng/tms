import { inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';

@customAttribute('autosize')
@inject(Element)
export class AttrAutosize {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {
        autosize(this.element);
    }

    bind(bindingContext) {
        this.valueChanged(this.value);
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        autosize.destroy(this.elements)
    }
}
