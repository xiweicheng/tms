import { bindable, containerless } from 'aurelia-framework';
import {
    default as SimpleMDE
} from 'simplemde';
import {
    default as Dropzone
} from 'dropzone';
import emojis from 'common/common-emoji';

@containerless
export class EmBlogComment {

    comments = [];

    baseUrl = utils.getUrl();
    basePath = utils.getBasePath();
    offset = 0;
    isSuper = nsCtx.isSuper;
    loginUser = nsCtx.loginUser;
    users = nsCtx.users;

    @bindable blog;

    blogChanged(newValue, oldValue) {
        this._refresh();
    }

    /**
     * 构造函数
     */
    constructor() {
        this.subscribe = ea.subscribe(nsCons.EVENT_BLOG_COMMENT_MSG_INSERT, (payload) => {
            this.insertContent(`${payload.content}`);
            this._scrollTo('b');
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }

    _refresh() {
        if (!this.blog) {
            return;
        }
        $.get('/admin/blog/comment/query', {
            id: this.blog.id,
            page: 0,
            size: 1000
        }, (data) => {
            if (data.success) {
                this.comments = data.data.content;
                let cid = utils.urlQuery('cid');
                if (cid) {
                    _.defer(() => {
                        this.scrollToAfterImgLoaded(cid);
                    });
                }
                ea.publish(nsCons.EVENT_BLOG_COMMENT_CHANGED, {
                    action: 'query',
                    comments: this.comments
                });
            } else {
                toastr.error(data.data);
            }
        });
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        this._init();

        // 消息popup
        $('.em-blog-comment .comments').on('mouseenter', '.markdown-body a[href*="#/blog/"]:not(.pp-not)', (event) => {
            event.preventDefault();
            var $a = $(event.currentTarget);
            let cid = utils.urlQuery('cid', $a.attr('href'));
            cid && ea.publish(nsCons.EVENT_BLOG_COMMENT_POPUP_SHOW, {
                id: cid,
                target: event.currentTarget
            });
        });

        $('.em-blog-comment .comments').on('dblclick', '.comment', (event) => {
            if (event.ctrlKey) {
                let cid = $(event.currentTarget).attr('data-id');
                let $t = $(event.currentTarget).find('.content > textarea');
                let item = _.find(this.comments, { id: +cid });
                if (this.isSuper || item.creator.username == this.loginUser.username) {
                    this.editHandler(item, $t);
                }
            }
        });

        $('.em-blog-comment .comments').on('click', '.comment', (event) => {
            this.focusedComment = $(event.currentTarget);
        });

        this.initHotkeys();
    }

    initHotkeys() {
        $(document).bind('keydown', 'r', (evt) => { // reply
            evt.preventDefault();
            $('.em-blog-content').scrollTo(`max`, 120, {
                offset: 0
            });
            this.simplemde.codemirror.focus();
        }).bind('keydown', 'alt+up', (evt) => { // comment pre
            evt.preventDefault();
            $('.em-blog-content').scrollTo(this.getScrollTargetComment(true), 120, {
                offset: 0
            });
        }).bind('keydown', 'alt+down', (evt) => { // comment next
            evt.preventDefault();
            $('.em-blog-content').scrollTo(this.getScrollTargetComment(), 120, {
                offset: 0
            });
        });

    }

    getScrollTargetComment(isPrev) {
        if (isPrev) {
            if (this.focusedComment && this.focusedComment.size() === 1) {
                let $avatar = this.focusedComment.find('> a.em-user-avatar');
                if (utils.isElementInViewport($avatar)) {
                    let prev = this.focusedComment.prev('.comment');
                    (prev.size() === 1) && (this.focusedComment = prev);
                }
            } else {
                this.focusedComment = $(this.blogCommentsRef).children('.comment:first');
            }
        } else {
            if (this.focusedComment && this.focusedComment.size() === 1) {
                let next = this.focusedComment.next('.comment');
                (next.size() === 1) && (this.focusedComment = next);
            } else {
                this.focusedComment = $(this.blogCommentsRef).children('.comment:last');
            }
        }
        return this.focusedComment;
    }

    _init() {
        this.simplemde = new SimpleMDE({
            element: this.commentRef,
            spellChecker: false,
            status: false,
            // autofocus: true,
            // toolbar: false,
            // forceSync: true,
            // autoDownloadFontAwesome: false,
            toolbar: [{
                    name: "bold",
                    action: SimpleMDE.toggleBold,
                    className: "fa fa-bold",
                    title: "粗体",
                }, {
                    name: "italic",
                    action: SimpleMDE.toggleItalic,
                    className: "fa fa-italic",
                    title: "斜体",
                }, {
                    name: "strikethrough",
                    action: SimpleMDE.toggleStrikethrough,
                    className: "fa fa-strikethrough",
                    title: "删除线",
                }, {
                    name: "heading",
                    action: SimpleMDE.toggleHeadingSmaller,
                    className: "fa fa-header",
                    title: "标题",
                }, {
                    name: "heading-smaller",
                    action: SimpleMDE.toggleHeadingSmaller,
                    className: "fa fa-header fa-header-x fa-header-smaller",
                    title: "变小标题",
                }, {
                    name: "heading-bigger",
                    action: SimpleMDE.toggleHeadingBigger,
                    className: "fa fa-header fa-header-x fa-header-bigger",
                    title: "变大标题",
                }, "|", {
                    name: "code",
                    action: SimpleMDE.toggleCodeBlock,
                    className: "fa fa-code",
                    title: "代码",
                }, {
                    name: "quote",
                    action: SimpleMDE.toggleBlockquote,
                    className: "fa fa-quote-left",
                    title: "引用",
                }, {
                    name: "unordered-list",
                    action: SimpleMDE.toggleUnorderedList,
                    className: "fa fa-list-ul",
                    title: "无序列表",
                }, {
                    name: "ordered-list",
                    action: SimpleMDE.toggleOrderedList,
                    className: "fa fa-list-ol",
                    title: "有序列表",
                }, {
                    name: "tasks",
                    action: (editor) => {
                        this.insertContent('- [ ] 未完成任务\n- [x] 已完成任务');
                    },
                    className: "fa fa-check-square-o ",
                    title: "任务列表",
                }, {
                    name: "details",
                    action: (editor) => {
                        this.insertContent('<details>\n<summary>标题</summary>\n<p>详情内容</p>\n</details>');
                    },
                    className: "fa fa-play ",
                    title: "折叠详情",
                }, "|", {
                    name: "link",
                    action: SimpleMDE.drawLink,
                    className: "fa fa-link",
                    title: "创建链接",
                }, {
                    name: "image",
                    action: SimpleMDE.drawImage,
                    className: "fa fa-picture-o",
                    title: "插入图片",
                }, {
                    name: "table",
                    action: SimpleMDE.drawTable,
                    className: "fa fa-table",
                    title: "插入表格",
                }, {
                    name: "horizontal-rule",
                    action: SimpleMDE.drawHorizontalRule,
                    className: "fa fa-minus",
                    title: "插入水平分割线",
                }, "|", {
                    name: "upload",
                    action: function(editor) {},
                    className: "fa fa-upload",
                    title: "上传文件",
                }, "|", {
                    name: "preview",
                    action: SimpleMDE.togglePreview,
                    className: "fa fa-eye no-disable",
                    title: "切换预览",
                },
                // {
                //     name: "side-by-side",
                //     action: SimpleMDE.toggleSideBySide,
                //     className: "fa fa-columns no-disable no-mobile",
                //     title: "实时预览",
                // }, {
                //     name: "fullscreen",
                //     action: SimpleMDE.toggleFullScreen,
                //     className: "fa fa-arrows-alt no-disable no-mobile",
                //     title: "全屏",
                // }, 
                {
                    name: "guide",
                    action: 'https://simplemde.com/markdown-guide',
                    className: "fa fa-question-circle",
                    title: "Markdown指南",
                }

            ],
            insertTexts: {
                table: ["", "\n\n| 列1 | 列2 | 列3 |\n| ------ | ------ | ------ |\n| 文本 | 文本 | 文本 |\n\n"],
            },
            previewRender: (plainText, preview) => { // Async method
                if (emojify) {
                    plainText = emojify.replace(plainText);
                }
                return marked(utils.preParse(plainText));
            },
        });

        this.simplemde.codemirror.on('keyup', (cm, e) => {
            if (e.ctrlKey && e.keyCode == 13) { // Ctrl+Enter
                this.addHandler();
            } else if (e.keyCode == 27) { // Esc
                this.simplemde.value('');
            }
        });

        this.$chatMsgInputRef = $(this.markdownRef).find('.CodeMirror textarea');
        if (this.$chatMsgInputRef.size() === 0) {
            this.$chatMsgInputRef = $(this.markdownRef).find('.CodeMirror [contenteditable="true"]');
        }

        this.initPaste();

        this.initTextcomplete();

        this.initUploadDropzone($('.CodeMirror-wrap', this.markdownRef), () => {
            return this.$chatMsgInputRef
        }, false);

        this.initUploadDropzone($('.editor-toolbar .fa.fa-upload', this.markdownRef), () => {
            return this.$chatMsgInputRef
        }, true);

    }

    initTextcomplete() {

        $(this.$chatMsgInputRef).textcomplete([{ // @user
            match: /(^|\s)@(\w*)$/,
            search: (term, callback) => {
                callback($.map(nsCtx.users, (member) => {
                    return (member.enabled && member.username.indexOf(term) >= 0) ? member.username : null;
                }));
            },
            template: (value, term) => {
                let user = _.find(nsCtx.users, { username: value });
                return `${user.name} - ${user.mails} (${user.username})`;
            },
            replace: (value) => {
                return `$1{~${value}}`;
            }
        }, { // emoji
            match: /(^|\s):([\+\-\w]*)$/,
            search: function(term, callback) {
                callback($.map(emojis, (emoji) => {
                    return _.some(emoji.split('_'), (item) => {
                        return item.indexOf(term) === 0;
                    }) ? emoji : null;
                }));
            },
            template: (value, term) => {
                if (value == 'search') {
                    return `表情查找 - :search`;
                }
                let emojiKey = `:${value}:`;
                return `${emojify.replace(emojiKey)} - ${emojiKey}`;
            },
            replace: (value) => {
                if (this.tipsActionHandler(value)) {
                    return '$1:' + value + ': ';
                } else {
                    return '';
                }
            }
        }], {
            appendTo: '.tms-blog-comment-status-bar',
            // maxCount: nsCons.NUM_TEXT_COMPLETE_MAX_COUNT
        });

        this.simplemde.codemirror.on('keydown', (cm, e) => {
            if (_.includes([13, 38, 40], e.keyCode) && this.isTipsShow()) { // enter | up | down
                e.preventDefault();
            }
        });
    }

    isTipsShow() {
        return $('.tms-blog-comment-status-bar').find('.textcomplete-dropdown:visible').size() === 1;
    }

    tipsActionHandler(value) {

        if (value == 'search') {
            _.delay(() => { utils.openNewWin(nsCons.STR_EMOJI_SEARCH_URL); }, 200);
        } else {
            return true;
        }

        return false;
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this._reset();
    }

    _reset() {
        this.blog = null;
        this.simplemde.value('');
        this.simplemde.toTextArea();
        this.simplemde = null;
    }

    /**
     * 编辑器插入自定义沟通内容
     * @param  {[type]} cm      [description]
     * @param  {[type]} comment [description]
     * @return {[type]}         [description]
     */
    insertContent(content, mde) {
        try {
            let cm = mde ? mde.codemirror : this.simplemde.codemirror;
            var cursor = cm.getCursor();
            if (cursor) {
                cm.replaceRange(content, cursor, cursor);
                cm.focus();
            }
        } catch (err) { console.log(err); }
    }

    replyHandler(item) {
        this.insertContent(`[[回复评论#${item.id}](${this.baseUrl}?cid=${item.id}){~${item.creator.username}}]\n\n`);
        this._scrollTo('b');
    }

    removeHandler(item) {
        $.post('/admin/blog/comment/remove', {
            cid: item.id
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.comments = _.reject(this.comments, { id: item.id });
                toastr.success('博文评论移除成功!');
                ea.publish(nsCons.EVENT_BLOG_COMMENT_CHANGED, {
                    action: 'removed',
                    comments: this.comments
                });
            } else {
                toastr.error(data.data, '博文评论移除失败!');
            }
        });
    }

    addHandler() {
        let content = this.simplemde.value();

        if (!$.trim(content)) {
            this.simplemde.value('');
            toastr.error('评论内容不能为空!');
            return;
        }

        if (this.sending) {
            return;
        }

        this.sending = true;

        var html = utils.md2html(content);
        let users = [nsCtx.memberAll, ...(window.tmsUsers ? tmsUsers : [])];

        $.post(`/admin/blog/comment/create`, {
            basePath: utils.getBasePath(),
            id: this.blog.id,
            users: utils.parseUsernames(content, users).join(','),
            content: content,
            contentHtml: html
        }, (data, textStatus, xhr) => {
            if (data.success) {
                this.comments = [...this.comments, data.data];
                this.simplemde.value('');
                toastr.success('博文评论提交成功!');
                this.scrollToAfterImgLoaded('b');
                ea.publish(nsCons.EVENT_BLOG_COMMENT_ADDED, {});
                ea.publish(nsCons.EVENT_BLOG_COMMENT_CHANGED, {
                    action: 'created',
                    comments: this.comments
                });
            } else {
                toastr.error(data.data, '博文评论提交失败!');
            }
        }).always(() => {
            this.sending = false;
        });
    }

    initPaste() {

        let $paste;
        if (this.$chatMsgInputRef.is('textarea')) {
            $paste = $(this.$chatMsgInputRef).pastableTextarea();
        } else {
            $paste = $(this.$chatMsgInputRef).pastableContenteditable();
        }

        $paste && ($paste.on('pasteImage', (ev, data) => {

            $.post('/admin/file/base64', {
                dataURL: data.dataURL,
                type: data.blob.type,
                toType: 'Blog'
            }, (data, textStatus, xhr) => {
                if (data.success) {
                    this.insertContent('![{name}]({baseURL}{path}{uuidName})'
                        .replace(/\{name\}/g, data.data.name)
                        .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                        .replace(/\{path\}/g, data.data.path)
                        .replace(/\{uuidName\}/g, data.data.uuidName));
                }
            });
        }).on('pasteImageError', (ev, data) => {
            toastr.error(data.message, '剪贴板粘贴图片错误!');
        }));
    }

    initUploadDropzone(domRef, getInputTargetCb, clickable) {

        let _this = this;

        $(domRef).dropzone({
            url: "/admin/file/upload",
            paramName: 'file',
            clickable: !!clickable,
            dictDefaultMessage: '',
            maxFilesize: 10,
            addRemoveLinks: true,
            previewsContainer: '.em-blog-comment .dropzone-previews',
            previewTemplate: $('.em-blog-comment .preview-template')[0].innerHTML,
            dictCancelUpload: '取消上传',
            dictCancelUploadConfirmation: '确定要取消上传吗?',
            dictFileTooBig: '文件过大({{filesize}}M),最大限制:{{maxFilesize}}M',
            init: function() {
                this.on("sending", function(file, xhr, formData) {
                    if (!getInputTargetCb()) {
                        this.removeAllFiles(true);
                    } else {
                        formData.append('toType', 'Blog');
                    }
                });
                this.on("success", function(file, data) {
                    if (data.success) {

                        $.each(data.data, function(index, item) {
                            if (item.type == 'Image') {
                                _this.insertContent('![{name}]({baseURL}{path}{uuidName}) '
                                    .replace(/\{name\}/g, item.name)
                                    .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                                    .replace(/\{path\}/g, item.path)
                                    .replace(/\{uuidName\}/g, item.uuidName));
                            } else {
                                _this.insertContent('[{name}]({baseURL}{path}{uuidName}) '
                                    .replace(/\{name\}/g, item.name)
                                    .replace(/\{baseURL\}/g, utils.getBaseUrl() + '/')
                                    .replace(/\{path\}/g, "admin/file/download/")
                                    .replace(/\{uuidName\}/g, item.id));
                            }
                        });
                        toastr.success('上传成功!');
                    } else {
                        toastr.error(data.data, '上传失败!');
                    }

                });
                this.on("error", function(file, errorMessage, xhr) {
                    toastr.error(errorMessage, '上传失败!');
                });
                this.on("complete", function(file) {
                    this.removeFile(file);
                });
            }
        });
    }

    scrollToAfterImgLoaded(to) {
        _.defer(() => {
            new ImagesLoaded($('.em-blog-content')[0]).always(() => {
                this._scrollTo(to);
            });

            this._scrollTo(to);
        });

    }

    _scrollTo(to) {
        if (to == 'b') {
            $('.em-blog-content').scrollTo('max');
        } else if (to == 't') {
            $('.em-blog-content').scrollTo(0);
        } else {
            if (_.some(this.comments, { id: +to })) {
                $('.em-blog-content').scrollTo(`.comment[data-id="${to}"]`, {
                    offset: this.offset
                });
                $('.em-blog-content').find(`.comment[data-id]`).removeClass('active');
                $('.em-blog-content').find(`.comment[data-id=${to}]`).addClass('active');
            } else {
                $('.em-blog-content').scrollTo('max');
                toastr.warning(`博文评论[${to}]不存在,可能已经被删除!`);
            }
        }
    }

    editHandler(item, editTxtRef) {
        $.get(`/admin/blog/comment/get`, {
            cid: item.id
        }, (data) => {
            if (data.success) {
                if (item.version != data.data.version) {
                    _.extend(item, data.data);
                }
                item.isEditing = true;
                item.contentOld = item.content;
                _.defer(() => {
                    $(editTxtRef).focus().select();
                    autosize.update(editTxtRef);
                });
            } else {
                toastr.error(data.data);
            }

        });
    }

    refreshHandler(item) {
        $.get('/admin/blog/comment/get', {
            cid: item.id
        }, (data) => {
            if (item.version != data.data.version) {
                _.extend(item, data.data);
                toastr.success('刷新同步成功!');
            } else {
                toastr.info('博文评论内容暂无变更!');
            }
        });
    }

    eidtKeydownHandler(evt, item, txtRef) {

        if (this.sending) {
            return false;
        }

        if (evt.ctrlKey && evt.keyCode === 13) {

            this.editSave(item, txtRef);

            return false;
        } else if (evt.ctrlKey && evt.keyCode === 85) {
            $(txtRef).next('.tms-blog-comment-edit-actions').find('.upload').click();
            return false;
        } else if (evt.keyCode === 27) {
            this.editCancelHandler(evt, item, txtRef);
        }

        return true;
    }

    editOkHandler(evt, item, txtRef) {
        this.editSave(item, txtRef);
        item.isEditing = false;
    }

    editCancelHandler(evt, item, txtRef) {
        item.content = item.contentOld;
        $(txtRef).val(item.content);
        item.isEditing = false;
    }

    editSave(item, txtRef) {

        this.sending = true;

        item.content = $(txtRef).val();

        var html = utils.md2html(item.content);
        var htmlOld = utils.md2html(item.contentOld);

        let users = [nsCtx.memberAll, ...(window.tmsUsers ? tmsUsers : [])];
        $.post(`/admin/blog/comment/update`, {
            basePath: utils.getBasePath(),
            id: this.blog.id,
            cid: item.id,
            version: item.version,
            users: utils.parseUsernames(item.content, users).join(','),
            content: item.content,
            contentHtml: html,
            diff: utils.diffS(item.contentOld, item.content),
        }, (data, textStatus, xhr) => {
            if (data.success) {
                toastr.success('博文评论更新成功!');
                item.isEditing = false;
                item.version = data.data.version;
            } else {
                toastr.error(data.data, '博文评论更新失败!');
            }
        }).always(() => {
            this.sending = false;
        });
    }

    isZanDone(comment) {
        let voteZan = comment.voteZan;
        if (!voteZan) {
            return false;
        }

        return voteZan.split(',').includes(this.loginUser.username);
    }

    rateHandler(item) {
        $.post('/admin/blog/comment/vote', {
            cid: item.id,
            url: utils.getBasePath(),
            contentHtml: utils.md2html(item.content),
            type: this.isZanDone(item) ? 'Cai' : 'Zan'
        }, (data, textStatus, xhr) => {
            if (data.success) {
                _.extend(item, data.data);
            } else {
                toastr.error(data.data, '博文投票失败!');
            }
        });

    }

    gotoTopHandler() {
        $('.em-blog-content').scrollTo(0, 120);
    }

}
