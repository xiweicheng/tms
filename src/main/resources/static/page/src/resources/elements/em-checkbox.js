import {
    bindable,
    bindingMode,
    containerless
}
from 'aurelia-framework';

@containerless
export class EmCheckbox {

    @bindable label;
    @bindable title;
    @bindable classes = 'fitted';
    @bindable onchange;
    @bindable onchecked;
    @bindable onunchecked;
    @bindable emCheckboxAll;
    @bindable({ defaultBindingMode: bindingMode.twoWay }) checked;

    @bindable signal;

    checkedChanged(news, old) {
        if (news) {
            $(this.checkbox).checkbox('set checked');
        } else {
            $(this.checkbox).checkbox('set unchecked');
        }

        this.signal && bs.signal(this.signal);
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.checkbox).checkbox({
            onChecked: () => {
                this.checked = true;
                _.defer(() => {
                    this.emCheckboxAll && this.emCheckboxAll.refreshCheckedStatus();
                    this.onchecked && this.onchecked(this);
                    this.signal && bs.signal(this.signal);
                });

            },
            onUnchecked: () => {
                this.checked = false;
                _.defer(() => {
                    this.emCheckboxAll && this.emCheckboxAll.refreshCheckedStatus();
                    this.onunchecked && this.onunchecked(this);
                    this.signal && bs.signal(this.signal);
                });

            },
            onChange: () => {
                _.defer(() => {
                    this.onchange && this.onchange(this);
                });

            },
        });
        this.checkedChanged(this.checked);
    }
}
