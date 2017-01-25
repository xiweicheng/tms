jQuery(document).ready(function($) {

    var baseURL = Utils.getBaseURL();

    // 注册
    Vue.filter('timeago', function(value) {
        return jQuery.timeago(value);
    });

    Vue.filter('searchBy', function(array, filter) {
        var arr = [];
        $.each(array, function(index, item) {
            if (item.title.toLowerCase().indexOf(filter) > -1) {
                arr.push(item);
            } else {
                var lbls = item.labels;
                if (lbls && lbls.length > 0) {
                    $.each(lbls, function(index, lbl) {
                        if (lbl.name.toLowerCase().indexOf(filter) > -1) {
                            arr.push(item);
                            return false;
                        }
                    });
                }
            }
        });

        return arr;
    });

    var app = new Vue({
        el: '.tms-wiki-dir',
        data: {
            activeId: url('?id'),
            wikis: [],
            filter: '',
            username: $('input:hidden[name="user.username"]').val()
        },
        methods: {
            clearFilterHandler: function() {
                this.filter = '';
            },
            editHandler: function(item) {

                $('.ad-msg-as-wiki-edit').modal({
                    onShow: function() {
                        var $m = $(this);
                        $.get('admin/chat/get', {
                            id: item.id
                        }, function(data) {
                            if (data.success) {
                                $m.find('input[name="title"]').val(data.data.title);
                                var names = [];
                                $.each(data.data.labels, function(index, lbl) {
                                    names.push(lbl.name);
                                });
                                $m.find('.dd-labels').dropdown('clear').dropdown('set selected', names);
                                data.data.privated && ($m.find('.ui.checkbox').checkbox('set checked'));
                                !data.data.privated && ($m.find('.ui.checkbox').checkbox('set unchecked'));
                            } else {
                                toastr.error(data.data, "获取博文失败!");
                            }

                        });
                    },
                    onApprove: function() {
                        var $m = $(this);
                        var title = $m.find('input[name="title"]').val();
                        var privated = $m.find('.ui.checkbox').checkbox('is checked');
                        var labels = $m.find('.dd-labels').dropdown('get value');

                        $.post('admin/chat/updateWiki', {
                            id: item.id,
                            baseURL: baseURL,
                            title: title,
                            privated: privated,
                            labels: labels
                        }, function(data, textStatus, xhr) {
                            if (data.success) {
                                toastr.success('博文修改成功!');
                                item.title = title;
                                item.labels.splice(0, item.labels.length);
                                $.each(labels.split(','), function(index, lbl) {
                                    item.labels.push({
                                        name: lbl
                                    });
                                });
                            } else {
                                toastr.error(data.data, "获取博文失败!");
                            }
                        });
                    }
                }).modal('show');
            }
        },
        computed: {
            // a computed getter
            filterWikis: function() {
                // `this` points to the vm instance
                let _this = this;
                var arr = [];
                $.each(this.wikis, function(index, item) {
                    if (item.title.toLowerCase().indexOf(_this.filter) > -1) {
                        arr.push(item);
                    } else {
                        var lbls = item.labels;
                        if (lbls && lbls.length > 0) {
                            $.each(lbls, function(index, lbl) {
                                if (lbl.name.toLowerCase().indexOf(_this.filter) > -1) {
                                    arr.push(item);
                                    return false;
                                }
                            });
                        }
                    }
                });

                return arr;
            }
        },
        created: function() {
            var _this = this;
            $.get('/free/wiki/latest', {
                size: 500
            }, function(data) {
                if (data.success) {
                    _this.wikis = data.data.content;
                    // DOM 还没有更新
                    _this.$nextTick(function() {
                        // DOM 现在更新了
                        $('.tms-wiki-dir').scrollTo($(".tms-wiki-dir .active.item"), 300, {
                            offset: -80
                        });
                    })
                }
            });
        },
        mounted: function() {
            // $('.tms-wiki-dir').show();
        }
    });
});
