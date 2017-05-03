import { inject } from 'aurelia-framework';
import { customAttribute } from 'aurelia-templating';
import 'swipebox';

@customAttribute('swipebox')
@inject(Element)
export class AttrSwipebox {

    constructor(element) {
        this.element = element;
    }

    valueChanged(newValue, oldValue) {

        $(this.element).on('click', 'img', (event) => {
            event.preventDefault();
            let $img = $(event.target);
            var imgs = [];
            var initialIndexOnArray = 0;
            $(this.element).find('img').each(function(index, img) {
                imgs.push({ href: $(img).attr('src'), title: $(img).attr('alt') });
                if (event.target == img) {
                    initialIndexOnArray = index;
                }
            });
            $.swipebox(imgs, {
                useCSS: true, // false will force the use of jQuery for animations
                useSVG: true, // false to force the use of png for buttons
                initialIndexOnArray: initialIndexOnArray, // which image index to init when a array is passed
                hideCloseButtonOnMobile: false, // true will hide the close button on mobile devices
                removeBarsOnMobile: true, // false will show top bar on mobile devices
                hideBarsDelay: 3000, // delay before hiding bars on desktop
                videoMaxWidth: 1140, // videos max width
                beforeOpen: function() {}, // called before opening
                afterOpen: null, // called after opening
                afterClose: function() {}, // called after closing
                loopAtEnd: !!newValue // true will return to the first image after the last image is reached
            });
        });
    }

    bind(bindingContext) {
        this.valueChanged(this.value);
    }
}
