import { customAttribute, inject } from 'aurelia-framework';
import Clipboard from 'clipboard';

@customAttribute('c2c')
@inject(Element)
export class AttrC2cCustomAttribute {

    constructor(element) {
        this.element = element;
        this._init();
    }

    _init() {

        $(this.element).append(`<span style="margin-left: 5px; display: none;" data-tooltip="复制到剪贴板" data-position="right center" data-inverted=""><i class="copy link icon"></i></span>`);
        this.clipboard = new Clipboard($(this.element).find('i.copy.icon')[0], {
            text: (trigger) => {
                return this.value ? this.value : $(this.element).text();
            }
        });
        let $tp = $(this.element).find('[data-tooltip]').hover(function() {}, function() {
            $(this).attr('data-tooltip', '复制到剪贴板!');
        });
        this.clipboard.on('success', (e) => {
            $tp.attr('data-tooltip', '复制成功!');
        }).on('error', (e) => {
            $tp.attr('data-tooltip', '复制失败!');
        });

        $(this.element).hover(() => {
            if (this.value || $(this.element).text()) {
                $tp.show();
            }
        }, () => {
            $tp.hide();
        });
    }

    unbind() {
        this.clipboard && this.clipboard.destroy();
    }
}
