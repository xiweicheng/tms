import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmBlogSpaceAuth {

    shares = [];

    type; // blog | space
    authO;

    _isBlog() {
        return this.type == 'blog';
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $(this.chk).checkbox({ // privated
            onChange: () => {
                if (this._isBlog()) {
                    $.post('/admin/blog/privated/update', {
                        id: this.authO.id,
                        privated: $(this.chk).checkbox('is checked')
                    }, (data, textStatus, xhr) => {
                        if (data.success) {
                            _.extend(this.authO, data.data);
                            ea.publish(nsCons.EVENT_BLOG_CHANGED, {
                                action: 'updated',
                                blog: data.data
                            });
                            toastr.success('更新博文可见性成功!');
                        } else {
                            toastr.error(data.data, '更新博文可见性失败!');
                        }
                    });
                } else {
                    $.post('/admin/space/update', {
                        id: this.authO.id,
                        privated: $(this.chk).checkbox('is checked')
                    }, (data, textStatus, xhr) => {
                        if (data.success) {
                            _.extend(this.authO, data.data);
                            ea.publish(nsCons.EVENT_SPACE_CHANGED, {
                                action: 'updated',
                                space: data.data
                            });
                            toastr.success('更新空间可见性成功!');
                        } else {
                            toastr.error(data.data, '更新空间可见性失败!');
                        }
                    });
                }
            }
        });
        $(this.chk2).checkbox({ // opened
            onChange: () => {
                if (this._isBlog()) {
                    $.post('/admin/blog/opened/update', {
                        id: this.authO.id,
                        opened: $(this.chk2).checkbox('is checked')
                    }, (data, textStatus, xhr) => {
                        if (data.success) {
                            _.extend(this.authO, data.data);
                            ea.publish(nsCons.EVENT_BLOG_CHANGED, {
                                action: 'updated',
                                blog: data.data
                            });
                            toastr.success('更新博文可见性成功!');
                        } else {
                            toastr.error(data.data, '更新博文可见性失败!');
                        }
                    });
                } else {
                    $.post('/admin/space/update', {
                        id: this.authO.id,
                        opened: $(this.chk2).checkbox('is checked')
                    }, (data, textStatus, xhr) => {
                        if (data.success) {
                            _.extend(this.authO, data.data);
                            ea.publish(nsCons.EVENT_SPACE_CHANGED, {
                                action: 'updated',
                                space: data.data
                            });
                            toastr.success('更新空间可见性成功!');
                        } else {
                            toastr.error(data.data, '更新空间可见性失败!');
                        }
                    });
                }
            }
        });

        $(this.searchRef)
            .search({
                minCharacters: 2,
                cache: false,
                selectFirstResult: true,
                onSelect: (result, response) => {
                    result.item._id = _.uniqueId('share-item-');
                    _.defer(() => { $(this.inputSearchRef).val(''); });

                    let param = { id: this.authO.id };
                    if (result.item.username) {
                        _.extend(param, { users: result.item.username });
                    } else {
                        _.extend(param, { channels: result.item.id });
                    }

                    if (this._isBlog()) {
                        $.post('/admin/blog/auth/add', param, (data, textStatus, xhr) => {
                            if (data.success) {
                                this.shares.push(result.item);
                                this.authO.blogAuthorities = data.data.blogAuthorities;
                            } else {
                                toastr.error(data.data);
                            }
                        });
                    } else {
                        $.post('/admin/space/auth/add', param, (data, textStatus, xhr) => {
                            if (data.success) {
                                this.shares.push(result.item);
                                this.authO.spaceAuthorities = data.data.spaceAuthorities;
                            } else {
                                toastr.error(data.data);
                            }
                        });
                    }
                },
                apiSettings: {
                    onResponse: (resp) => {
                        var response = {
                            results: []
                        };
                        $.each(resp.data.users, (index, item) => {
                            if (!_.find(this.shares, { username: item.username })) {
                                response.results.push({
                                    item: item,
                                    title: `<i class="user icon"></i> ${item.name} (${item.username})`,
                                });
                            }
                        });
                        $.each(resp.data.channels, (index, item) => {
                            if (!_.find(_.filter(this.shares, c => !c.username), { name: item.name })) {
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
    }

    removeShareHandler(item) {
        let param = { id: this.authO.id };
        if (item.username) {
            _.extend(param, { users: item.username });
        } else {
            _.extend(param, { channels: item.id });
        }

        if (this._isBlog()) {
            $.post('/admin/blog/auth/remove', param, (data, textStatus, xhr) => {
                if (data.success) {
                    this.shares = _.reject(this.shares, { _id: item._id });
                    this.authO.blogAuthorities = data.data.blogAuthorities;
                } else {
                    toastr.error(data.data);
                }
            });
        } else {
            $.post('/admin/space/auth/remove', param, (data, textStatus, xhr) => {
                if (data.success) {
                    this.shares = _.reject(this.shares, { _id: item._id });
                    this.authO.spaceAuthorities = data.data.spaceAuthorities;
                } else {
                    toastr.error(data.data);
                }
            });
        }
    }

    _reset() {
        this.shares = [];
        $(this.inputSearchRef).val('');
    }

    show(type, authO) {
        this.type = type;
        this.authO = authO;
        this.emModal.show({ hideOnApprove: true, autoDimmer: false });
    }

    showHandler() {
        this._reset();
        $(this.chk).checkbox(this.authO.privated ? 'set checked' : 'set unchecked');
        $(this.chk2).checkbox(this.authO.opened ? 'set checked' : 'set unchecked');
        let auths;
        if (this._isBlog()) {
            auths = this.authO.blogAuthorities;
        } else {
            auths = this.authO.spaceAuthorities;
        }
        _.forEach(auths, (item) => {
            let share = item.user ? item.user : item.channel;
            share._id = _.uniqueId('share-item-');
            this.shares.push(share);
        });
    }

    approveHandler() {

    }
}
