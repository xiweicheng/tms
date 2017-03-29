import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogShare {

    shares = [];
    desc = '';

    @bindable blog;

    basePath = utils.getBasePath();

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.searchRef)
            .search({
                minCharacters: 2,
                cache: false,
                selectFirstResult: true,
                showNoResults: false,
                onSelect: (result, response) => {
                    result.item._id = _.uniqueId('share-item-');
                    result.item._type = result.item.username ? 'user' : 'channel';
                    this.shares.push(result.item);
                    _.defer(() => { $(this.inputSearchRef).val(''); });
                },
                apiSettings: {
                    onResponse: (resp) => {
                        var response = {
                            results: []
                        };
                        $.each(resp.data.users, (index, item) => {
                            if (!_.find(_.filter(this.shares, c => c._type == 'user'), { username: item.username })) {
                                response.results.push({
                                    item: item,
                                    title: `<i class="user icon"></i> ${item.name} (${item.username})`,
                                });
                            }
                        });
                        $.each(resp.data.channels, (index, item) => {
                            if (!_.find(_.filter(this.shares, c => c._type == 'channel'), { name: item.name })) {
                                response.results.push({
                                    item: item,
                                    title: `<i class="users icon"></i> ${item.title} (${item.name})`,
                                });
                            }
                        });
                        return response;
                    },
                    url: '/admin/blog/share/to/search?search={query}'
                }
            });
        $(this.shareRef).popup({
            on: 'click',
            inline: true,
            silent: true,
            position: 'bottom right',
            jitter: 300,
            delay: {
                show: 300,
                hide: 300
            },
            onVisible: () => {
                $(this.inputSearchRef).focus();
            }
        });
    }

    shareSearchKeyupHandler(event) {
        if (event.keyCode === 13 && !$(this.searchRef).search('is visible')) {
            let val = $(this.inputSearchRef).val();
            if (utils.isMail(val)) {
                if (!_.find(_.filter(this.shares, c => c._type == 'mail'), { mail: val })) {
                    this.shares.push({
                        _id: _.uniqueId('share-item-'),
                        _type: 'mail',
                        mail: val
                    });
                    $(this.inputSearchRef).val('');
                }
            }
        }
    }

    show() {
        $(this.shareRef).popup('show');
    }

    removeShareHandler(item) {
        this.shares = _.reject(this.shares, { _id: item._id });
    }

    cancelHandler() {
        this._reset();
    }

    _reset() {
        this.shares = [];
        this.desc = '';
        $(this.inputSearchRef).val('');
        $(this.shareRef).popup('hide');
    }

    shareHandler() {

        if (this.shares.length === 0) {
            toastr.error('请先指定博文分享用户或者频道!');
            return;
        }

        this.ajaxS = $.post('/admin/blog/share', {
            basePath: utils.getBasePath(),
            id: this.blog.id,
            desc: this.desc,
            title: this.blog.title,
            html: utils.md2html(this.blog.content),
            users: _.chain(this.shares).filter(item => item._type == 'user').map('username').join().value(),
            channels: _.chain(this.shares).filter(item => item._type == 'channel').map('name').join().value(),
            mails: _.chain(this.shares).filter(item => item._type == 'mail').map('mail').join().value()
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this._reset();
                toastr.success('博文分享成功!');
            } else {
                toastr.error(data.data, '博文分享失败!');
            }
        });
    }
}
