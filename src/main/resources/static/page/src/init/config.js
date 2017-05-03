import { BindingSignaler } from 'aurelia-templating-resources';
import { EventAggregator } from 'aurelia-event-aggregator';
import 'isomorphic-fetch';
import { HttpClient, json } from 'aurelia-fetch-client';
import {
    default as toastr
} from 'toastr';
import {
    default as wurl
} from 'wurl';
import utils from 'common/common-utils';
import 'common/common-plugin'
import 'common/common-constant';
import 'common/common-ctx';
import 'common/common-imgs-loaded';
import {
    default as marked
} from 'marked'; // https://github.com/chjj/marked
import {
    default as hljs
} from 'highlight';
import {
    default as autosize
} from 'autosize';
import {
    default as NProgress
}
from 'nprogress';
import {
    default as push
} from 'push';
import {
    default as ColorHash
} from 'color-hash';
import 'modaal';

export class Config {

    initHttp() {
        window.json = (param) => {
            console.log(JSON.stringify(param));
            return json(param);
        };
        window.http = this.aurelia.container.root.get(HttpClient);
        http.configure(config => {
            config
            // .withBaseUrl(nsParam.baseUrl)
                .withDefaults({
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'fetch'
                    }
                })
                .withInterceptor({
                    request(req) {
                        NProgress && NProgress.start();
                        return req;
                    },
                    requestError(req) {
                        console.log(req);
                    },
                    response(resp) {
                        NProgress && NProgress.done();
                        if (!resp.ok) {
                            resp.json().then((data) => {
                                // toastr.error('PATH: ' + data.path + '<br/>STATUS: ' + data.status + '<br/>EXCEPTION:<br/>' + data.exception + '<br/>MESSAGE:<br/>' + data.message, data.error);
                                toastr.error(data.message);
                            });

                            if (resp.status == 401) {
                                toastr.error('用户未登录!');
                                utils.redirect2Login();
                                return;
                            }
                        }

                        return resp;
                    },
                    responseError(resp) {
                        toastr.error(resp.message, '网络请求错误!');
                        console.log(resp);
                    }
                });
        });

        return this;
    }

    initToastr() {

        // toastr弹出消息提示插件全局配置设置
        toastr.options.positionClass = 'toast-bottom-center';
        toastr.options.preventDuplicates = true;

        return this;
    }

    initMarked() {

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
        renderer.link = function(href, title, text) {
            if (this.options.sanitize) {
                try {
                    var prot = decodeURIComponent(unescape(href))
                        .replace(/[^\w:]/g, '')
                        .toLowerCase();
                } catch (e) {
                    return '';
                }
                if (prot.indexOf('javascript:') === 0 || prot.indexOf('vbscript:') === 0 || prot.indexOf('data:') === 0) {
                    return '';
                }
            }
            let out;
            let isChatLink = /\/chat\/.+\?id=.+/g.test(wurl('hash', href));
            let isCommentLink = /\/blog\/.+\?cid=.+/g.test(wurl('hash', href));
            if (isChatLink || isCommentLink || (utils.isAbsUrl(href) && (wurl('hostname', href) != wurl('hostname')))) {
                out = '<a target="_blank" href="' + href + '"';
            } else {
                out = '<a href="' + href + '"';
            }
            if (title) {
                out += ' title="' + title + '"';
            }
            out += '>' + text + '</a>';
            return out;
        };

        renderer.codespan = function(text) {
            return `<code data-code="${text}">${text}</code>`;
        };

        renderer.code = function(code, lang, escaped) {
            let codeBk = code;
            if (this.options.highlight) {
                var out = this.options.highlight(code, lang);
                if (out != null && out !== code) {
                    escaped = true;
                    code = out;
                }
            }

            if (!lang) {
                return `<div class="pre-code-wrapper"><i data-clipboard-text="${utils.escape(codeBk, true)}" title="复制(ctrl+click)" class="tms-clipboard copy icon"></i><pre><code>${escaped ? code : utils.escape(code, true)}\n</code></pre></div>`;
            }

            return `<div class="pre-code-wrapper"><i data-clipboard-text="${utils.escape(codeBk, true)}" title="复制(ctrl+click)" class="tms-clipboard copy icon"></i><pre><code class="${this.options.langPrefix + utils.escape(lang, true)}">${escaped ? code : utils.escape(code, true)}\n</code></pre><div>\n`;
        };

        // https://github.com/chjj/marked
        marked.setOptions({
            renderer: renderer,
            breaks: true,
            highlight: function(code) {
                return hljs.highlightAuto(code).value;
            }
        });

        return this;
    }

    initAjax() {
        // ajax全局配置选项设置
        $.ajaxSetup({
            // ajax请求不缓存
            cache: false,
        });

        let exceptUrls = [
            '/chat/channel/latest',
            '/chat/direct/latest',
            '/chat/channel/poll'
        ];

        $(document).ajaxSend(function(event, jqxhr, settings) {

            let isNotInExceptUrls = _.every(exceptUrls, (url) => {
                return (settings.url.lastIndexOf(url) == -1);
            });

            if (isNotInExceptUrls) {
                NProgress && NProgress.start();
            }
        });

        // $(document).on('ajaxStart', function() {
        //     NProgress && NProgress.start();
        // });
        $(document).on('ajaxStop', function() {
            NProgress && NProgress.done();
        });
        // $(document).ajaxComplete(function(event, request, settings) {
        //     console.log(request);
        // });
        $(document).ajaxError(function(event, xhr, settings) {
            if (xhr && xhr.status == 401) {
                utils.redirect2Login();
            }
        });

        return this;
    }

    initGlobalVar() {
        window.toastr = toastr;
        window.wurl = wurl;
        window.utils = utils;
        window.marked = marked;
        window.autosize = autosize;
        window.push = push;
        window.bs = this.aurelia.container.root.get(BindingSignaler);
        window.ea = this.aurelia.container.root.get(EventAggregator);
        window.colorHash = new ColorHash();
        return this;
    }

    initAnimateCss() {
        $.fn.extend({
            animateCss: function(animationName) {
                var animationEnd = 'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend';
                this.addClass('animated ' + animationName).one(animationEnd, function() {
                    $(this).removeClass('animated ' + animationName);
                });
            }
        });
        return this;
    }

    initEmoji() {
        emojify && emojify.setConfig({
            img_dir: utils.getResourceBase() + '/img/emoji'
        });
        return this;
    }

    initModaal() {
        _.extend($.fn.modaal.options, {
            close_text: '关闭',
            close_aria_label: '按[esc]关闭',
            confirm_button_text: '确认',
            confirm_cancel_button_text: '取消',
            confirm_title: '操作确认',
            accessible_title: '对话框窗口',
            confirm_content: '<p>默认确认对话框内容.</p>',
        });
        return this;
    }

    context(aurelia) {
        this.aurelia = aurelia;
        return this;
    }

}

export default new Config();
