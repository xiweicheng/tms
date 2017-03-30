import { customAttribute, inject } from 'aurelia-framework';

@customAttribute('tablesort')
@inject(Element)
export class AttrTablesortCustomAttribute {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {

    }

    _init() {
        if ($(this.element).is('table')) {
            $(this.element).addClass('sortable').tablesort();
        } else {
        	console.warn('tablesort element is not table tag!');
        }
    }

    bind() {
    	this._init();
    }
}
