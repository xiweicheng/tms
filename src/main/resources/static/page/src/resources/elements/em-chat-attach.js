import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatAttach {

    type = 'Image'; //Image | Attachment
    search = '';

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.tabRef).find('.item').tab({
            onVisible: (tabPath) => {
                // this.type = tabPath;
                // this.fetch();
            }
        });
    }

    moreHandler() {
        this._listByPage(true);
    }

    _listByPage(nextPage = false) {
        let url = nsCtx.isAt ? '/admin/file/listByUser' : '/admin/file/listByChannel';
        this.ajax = $.get(url, {
            name: nsCtx.chatTo,
            type: this.type,
            page: this.page ? (nextPage ? this.page.number + 1 : this.page.number) : 0,
            size: 10,
            search: this.search
        }, (data) => {
            this.page = data.data;
            this.moreCnt = this.page.last ? 0 : this.page.totalElements - (this.page.number + 1) * this.page.size;
            if (!nextPage) {
                this.attachs = data.data.content;
            } else {
                this.attachs = _.concat(this.attachs, data.data.content);
            }

        });
    }

    fetch() {
        this.page = null;
        this.moreCnt = 0;
        this.attachs = null;
        if ($(window).width() > 991) {
            $(this.searchRef).focus();
        }
        this._listByPage();
    }

    tabClickHandler(tabPath) {
        this.type = tabPath;
        this.fetch();
    }

    searchHandler() {
        this.fetch();
    }

    keyupHandler(event) {
        if (event.keyCode == 13) {
            this.fetch();
        } else if (event.keyCode == 27) {
            this.search = '';
            this.fetch();
        }
        return true;
    }

}
