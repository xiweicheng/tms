jQuery(function($) {

    $.fn.dropdown.settings.forceSelection = false;

    $('.ad-index-btn-menu').click(function() {
        $('.ad-index-menu').sidebar('toggle');
    });

    $('.ad-index-logout').click(function() {
        $(this).children('form').submit();
    });

    $('.ad-index-user-edit').click(function() {
        var $btn = $(this);

        $.get('admin/user/get', { username: $btn.attr('data-id') },
            function(data) {
                if (data.success) {
                    $('.ad-user-own-edit').find('.user-username').text($btn.attr('data-id'));
                    $('.ad-user-own-edit').find('input[name="password"]').val('');
                    $('.ad-user-own-edit').find('input[name="mail"]').val(data.data.mails);
                    $('.ad-user-own-edit').find('input[name="name"]').val(data.data.name);

                    $('.ad-user-own-edit').modal({
                        onApprove: function() {
                            $.post('admin/user/update2', {
                                username: $btn.attr('data-id'),
                                password: $('.ad-user-own-edit').find('input[name="password"]').val(),
                                name: $('.ad-user-own-edit').find('input[name="name"]').val(),
                                mail: $('.ad-user-own-edit').find('input[name="mail"]').val()
                            }, function(data, textStatus, xhr) {
                                if (data.success) {
                                    toastr.success('个人信息修改成功!');
                                } else {
                                    toastr.error(data.data, '个人信息修改失败!');
                                }
                            });
                        }
                    }).modal('show');
                } else {
                    toastr.error('获取用户信息失败!');
                }
            });

    });

    $('.ui.accordion').accordion();
    $('.ui.checkbox').checkbox();
    $('.popup-login-user').popup({
        position: 'bottom right',
    });
    $('.ui.dropdown.dd-top-menu-user').dropdown({
        action: 'hide'
    });

    // load markdown help content
    if ($('.markdown-content').size() > 0) {
        var converter = new showdown.Converter();
        $('.markdown-content').each(function(index, el) {
            $.get($(el).attr('data-url'), function(data) {
                $('<div class="markdown-body"/>').html(converter.makeHtml(data)).appendTo(el);
            });
        });
    }

    // semantic-ui ajax api
    $.fn.api.settings.api = {
        'deleteFileById': 'admin/file/delete?id={id}',
        'updateFileName': 'admin/file/update?id={id}&name={name}',
        'saveArticle': 'admin/article/save',
        'updateArticle': 'admin/article/update?id={id}',
        'deleteArticleById': 'admin/article/delete?id={id}',
        'saveFeedback': 'admin/feedback/save'
    };

    $.fn.api.settings.successTest = function(resp) {
        if (resp && resp.success) {
            return resp.success;
        }
        return false;
    };

    // toastr notification options
    if ("undefined" != typeof toastr) {
        toastr.options = {
            "closeButton": true,
            "debug": false,
            "newestOnTop": true,
            "progressBar": false,
            "positionClass": $(window).width() < 768 ? "toast-bottom-center" : "toast-top-center",
            "preventDuplicates": true,
            "onclick": null,
            "showDuration": "300",
            "hideDuration": "1000",
            "timeOut": "5000",
            "extendedTimeOut": "1000",
            "showEasing": "swing",
            "hideEasing": "linear",
            "showMethod": "fadeIn",
            "hideMethod": "fadeOut"
        };
    }

    $(document).ajaxSend(function(event, jqxhr, settings) {

        if (settings.url.lastIndexOf('/poll/unmask') == -1) {
            // $('.ad-page-dimmer').addClass('active');
            NProgress && NProgress.start();
        }

        var csrf = {};
        csrf[$('.ad-csrf input:hidden').attr('name')] = $('.ad-csrf input:hidden').attr('value');

        if (!!settings.data) {
            settings.data = settings.data + "&" + $.param(csrf);
        } else {
            settings.data = $.param(csrf);
        }
    });

    // $(document).on('ajaxStart', function() {
    //     NProgress && NProgress.start();
    // });
    $(document).on('ajaxStop', function() {
        // $('.ad-page-dimmer').removeClass('active');
        NProgress && NProgress.done();
    });

    $(document).ajaxError(function(event, xhr, settings) {
        if (xhr && xhr.status == 401) {
            window.location = '/admin/login';
        }
    });


    $('.ad-item-feedback').click(function(event) {
        event.stopImmediatePropagation();
        $(this).find('form').find(':hidden[name="name"]').val($('title').text()).end().submit();
    });

    // 初始化系统外链
    if ($('.tms-sys-links').size() === 1) {
        $.get('/admin/link/listByApp', (data) => {
            if (data.success) {
                $.each(data.data, function(index, item) {
                    var $item = $('<a target="_blank" class="item"></a>')
                        .attr('href', item.href)
                        .html(item.title);

                    $('.tms-sys-links').append($item);
                });

                $('.tms-dd-sys-links').dropdown({
                    on: 'hover',
                    action: 'hide'
                });
            }
        });
    }

});

jQuery(function($) {
    // custom helper utils
    window.Utils = window.Utils || {};
    var _startId = 0;

    $.extend(window.Utils, {
        removeFileType: function(name) {
            var i = name.lastIndexOf('.');
            return (i != -1) ? name.substring(0, i) : name;
        },
        getFileType: function(name) {
            var i = name.lastIndexOf('.');
            return (i != -1) ? name.substring(i) : '';
        },
        abbreviate: function(str, len) {
            if (!!str && str.length > len) {
                return str.substring(0, len - 3) + "...";
            }

            return str;
        },
        formData: function(selector) {

            var data = {};
            $(selector).find('input[name],textarea[name]').each(function(index, el) {
                var name = $(el).attr('name');
                var val = $(el).val();
                data[name] = val;
            });

            return data;
        },
        remember: function() {
            localStorage && (typeof url == 'function') && localStorage.setItem(url('path'), url('path') + (url('query') ? ('?' + url('query')) : ''));
        },
        getRemember: function(name) {
            return localStorage && localStorage.getItem(name);
        },
        getBaseURL: function() {
            if (typeof url == 'function') {
                if (url('port') == 80 || url('port') == 443) {
                    return (url('protocol') + '://' + url('hostname') + '/');
                } else {
                    return (url('protocol') + '://' + url('hostname') + ':' + url('port') + '/');
                }
            }
            return '';
        },
        md2html: function(markdown) {
            if (showdown) {
                var converter = new showdown.Converter();
                return converter.makeHtml(markdown);
            }
            return markdown;
        },
        imgLoaded: function($imgs, callback) {
            var imgdefereds = [];
            $imgs.each(function() {
                var dfd = $.Deferred();
                $(this).bind('load', function() {
                    dfd.resolve();
                }).bind('error', function() {
                    //图片加载错误，加入错误处理
                    dfd.resolve();
                })
                if (this.complete) {
                    // setTimeout(function() {
                    //     dfd.resolve();
                    // }, 1000);
                    dfd.resolve();
                }

                imgdefereds.push(dfd);
            })
            $.when.apply(null, imgdefereds).done(function() {
                callback && callback.call(null);
            });
        },
        showMappedTxt: function(filterCls) {
            var userMap = {};
            $('input:hidden[data-group="users"]').each(function(index, el) {
                userMap[$(this).attr('name')] = $(this).attr('value');
            });

            $(filterCls).each(function(index, el) {
                var username = $(this).text();
                var name = userMap[username];
                if (name) {
                    $(this).text(name).removeClass(filterCls.replace('.', ''));
                }
            });
        },
        getAllUserInfo: function() {
            var userMap = {};
            $('input:hidden[data-group="users"]').each(function(index, el) {
                userMap[$(this).attr('name')] = {
                    name: $(this).attr('value'),
                    status: $(this).attr('data-status')
                };
            });
            return userMap;
        },
        isElementInViewport: function(el) {

            //special bonus for those using jQuery
            if (typeof jQuery === "function" && el instanceof jQuery) {
                el = el[0];
            }

            var rect = el.getBoundingClientRect();

            return (
                rect.top >= 0 &&
                rect.left >= 0 &&
                rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */
                rect.right <= (window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */
            );
        },
        catalog: function($e) {
            var $headers = $(":header", $e);
            var pre = null;

            var link = {
                pre: null,
                arr: []
            };
            var current = link;
            $headers.each(function(index, h) {
                var name = h.nodeName;
                if (!pre) {
                    current.arr.push(h);
                    pre = name;
                } else {
                    if (pre < name) {
                        var last = current;
                        current = {
                            pre: last,
                            arr: [h]
                        };
                        last.arr.push(current);
                        pre = name;
                    } else if (pre == name) {
                        current.arr.push(h);
                    } else {
                        current = current.pre ? current.pre : current;
                        current.arr.push(h);
                        pre = name;
                    }
                }
            });

            return link;
        },
        generateDir: function(link) {
            var $list = $('<div class="ui bulleted list"></div>');
            prodDir($list, link);
            return $list;
        },
        id: function(prefix) {
            return (prefix ? prefix : "tms") + '-' + (++_startId);
        },
        dir: function($e) {
            return this.generateDir(this.catalog($e));
        },
        openLink: function(url) {
            if (url) {
                var $a = $('<a target="_blank" style="display:none;"></a>').attr('href', url).appendTo('body');
                $('<input type="button">').appendTo($a).click();

                setTimeout(function() {
                    $a.remove();
                }, 200);
            }
        },
        errorAutoTry: function(callback, time) {

            var _this = this;

            if (_this.isRunning) {
                return;
            }

            var cnt = time ? time : 10;
            var $t = toastr.error("网络连接错误," + cnt + "秒后自动重试!", null, {
                "closeButton": false,
                "timeOut": "0",
                "preventDuplicates": false,
                "onclick": function() {
                    clearInterval(timer);
                    toastr.remove();
                    callback && callback();
                }
            });

            _this.isRunning = true;
            var timer = setInterval(function() {
                if (cnt === 0) {
                    clearInterval(timer);
                    _this.isRunning = false;
                    toastr.remove();
                    callback && callback();
                    return;
                }
                $t && $t.find('.toast-message').text("网络连接错误," + cnt + "秒后自动重试!");
                cnt--;
            }, 1000);
        },
        // 文本比较
        diffS: function(oldS, newS, way) {
            var ways = ['diffChars', 'diffWords', 'diffWordsWithSpace', 'diffLines'];
            if (!ways.includes(way)) {
                way = 'diffWords';
            }
            var delStyle = 'style="background-color: #e6cf56; text-decoration: line-through;"';
            var insStyle = 'style="background-color: #98e287; text-decoration: none;"';
            var diff = JsDiff ? JsDiff[way](oldS, newS) : [];
            var nodeArr = [];
            for (var i = 0; i < diff.length; i++) {

                if (diff[i].added && diff[i + 1] && diff[i + 1].removed) {
                    var swap = diff[i];
                    diff[i] = diff[i + 1];
                    diff[i + 1] = swap;
                }

                var node;
                if (diff[i].removed) {
                    node = '<del ' + delStyle + '>' + diff[i].value + '</del>';
                } else if (diff[i].added) {
                    node = '<del ' + insStyle + '>' + diff[i].value + '</del>';
                } else {
                    node = diff[i].value;
                }
                nodeArr.push(node);
            }

            return '<pre>' + nodeArr.join('') + '</pre>';
        }
    });

    function prodDir($list, link) {
        $.each(link.arr, function(index, item) {
            if (item.hasOwnProperty('arr')) {
                var $l = $('<div class="list"></div>');
                $list.append($l)
                prodDir($l, item);
            } else {
                var id = Utils.id();
                var $item = $('<a class="item wiki-dir-item" style="word-break: keep-all; white-space: nowrap;"></a>').text($(item).attr('id', id).text()).attr('data-id', id);
                $list.append($item);
            }
        });
    }

    // remember the url
    Utils.remember();

    // set remember url
    var translateUrl = Utils.getRemember('/admin/translate');
    translateUrl && $('a.item.mi-translate').attr('href', translateUrl);

    var importUrl = Utils.getRemember('/admin/import');
    importUrl && $('a.item.mi-import').attr('href', importUrl);

    var dynamicUrl = Utils.getRemember('/admin/dynamic');
    dynamicUrl && $('a.item.mi-dynamic').attr('href', dynamicUrl);

});

/**
 * 轮询插件
 * 原理:轮询最小间隔 6s, 最大间隔5min, 轮询节能模式, 当连续1min(10次)获取不到新数据, 轮询间隔 时间 +6s, 
 * 接着递增 +6/次, 直到最大间隔, 不再递增轮询间隔. 一旦有一次获得新数据, 轮询间隔恢复到最小间隔6s.
 * @return {[type]} [description]
 */
+ function() {

    var minInterval = 6000; // 轮询最小间隔 6s
    var maxInterval = 300000; // 轮询最大间隔5min
    var incInterval = 6000; // 递增轮询间隔时间 6s

    var tolerate = 10; // 容忍连续获取不到新数据的(次数), 超过, 就会开始递增轮询间隔时间.

    var timer = null; // 轮询对象引用

    var inc = 0; // 轮询次数计数器

    var interval = minInterval; // 轮询实际轮询间隔

    var _pollCb = null;
    var _errCb = null;
    var _isPause = false; // 是否暂停

    function oneHandler() {

        if (_isPause) {
            return;
        }

        try { // 捕获轮询执行方法体中的异常, 防止破坏轮询的持续性.
            _pollCb && _pollCb(_reset, _stop);
        } catch (e) {
            _errCb && _errCb(_reset, _stop, e);

            // TODO for debugging
            console.log('轮询异常: ' + e);
        }
    }

    /**
     * 轮询处理递归逻辑
     * @param  {[Function]} pollCb 轮询业务回调
     * @param  {[Function]} errCb  轮询业务处理异常回到
     */
    function _start() {
        // TODO for debugging
        console.log('poll start...');

        _isPause = false;

        oneHandler();

        timer = setInterval(function() {
            inc++;
            oneHandler();
            // TODO for debugging
            // console.log(interval);

            if (inc > tolerate) { // 超过轮询容忍次数内

                interval = minInterval + (incInterval * (inc - tolerate));

                if (interval <= maxInterval) { // 最大轮询间隔范围内, 逐次递增轮询间隔
                    clearInterval(timer);
                    _start();
                }
            }
        }, interval);
    }

    function _stop() {
        // TODO for debugging
        console.log("poll stop...");

        inc = 0;
        interval = minInterval;
        _isPause = false;
        clearInterval(timer);
    }

    function _reset() {
        // TODO for debugging
        console.log("poll reset...");

        _stop();
        _start();
    }

    function _pause() {
        // TODO for debugging
        console.log("pause reset...");
        _isPause = true;
    }

    window.poll = {
        start: function(pollCb, errCb) {
            _pollCb = pollCb;
            _errCb = errCb;
            _start();
        },
        reset: function() {
            _reset();
        },
        stop: function() {
            _stop();
        },
        pause: function() {
            _pause();
        }
    };

    /**
     * Default markdown render.
     */
    !!window.SimpleMDE && (SimpleMDE.prototype.markdown = function(text) {
        if (marked && !this.markedInited) {

            let renderer = new marked.Renderer();
            renderer.listitem = function(text) {
                if (/^\s*\[[x ]\]\s*/.test(text)) {
                    text = text
                        .replace(/^\s*\[ \]\s*/, '<input style="position: relative; top: 2px;" type="checkbox" disabled> ')
                        .replace(/^\s*\[x\]\s*/, '<input style="position: relative; top: 2px;" type="checkbox" checked disabled> ');
                    return '<li class="task-item" style="list-style: none; margin-left: -30px;">' + text + '</li>';
                } else {
                    return '<li>' + text + '</li>';
                }
            };

            // Initialize
            var markedOptions = {
                renderer: renderer,
            };

            if (this.options && this.options.renderingConfig) {
                markedOptions.breaks = !!this.options.renderingConfig.singleLineBreaks;
                if (this.options.renderingConfig.codeSyntaxHighlighting === true && window.hljs) {
                    markedOptions.highlight = function(code) {
                        return window.hljs.highlightAuto(code).value;
                    };
                }
            } else {
                markedOptions.breaks = true;
            }

            // Set options
            marked.setOptions(markedOptions);

            this.markedInited = true;
        }

        // Return
        return marked(text);
    });

}();
