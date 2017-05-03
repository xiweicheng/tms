import { bindable, containerless } from 'aurelia-framework';
import {
    default as SimpleMDE
} from 'simplemde';
import {
    default as Dropzone
} from 'dropzone';
import emojis from 'common/common-emoji';

@containerless
export class EmBlogWrite {

    @bindable members;

    static NAME = 'blog-create';

    /**
     * 构造函数
     */
    constructor() {

        this.subscribe = ea.subscribe(nsCons.EVENT_MODAAL_AFTER_OPEN, (payload) => {
            if (payload.id == EmBlogWrite.NAME) {
                nsCtx.isModaalOpening = true;
                this.init();
            }
        });
        this.subscribe2 = ea.subscribe(nsCons.EVENT_MODAAL_BEFORE_CLOSE, (payload) => {
            if (payload.id == EmBlogWrite.NAME) {
                this.destroy();
                nsCtx.isModaalOpening = false;
            }
        });
        this.subscribe3 = ea.subscribe(nsCons.EVENT_BLOG_ACTION, (payload) => {
            this.action = payload.action;
            $.get('/admin/blog/get', { id: payload.id }, (data) => {
                if (data.success) {
                    this.blog = data.data;
                    $('a[href="#modaal-blog-write"]').click();
                }
            });

        });
        this.subscribe4 = ea.subscribe(nsCons.EVENT_BLOG_CHANGED, (payload) => {
            this.action = payload.action;
            if (payload.action === 'created') {
                this.blog = payload.blog;
                $('#blog-save-btn span').text('更新');
                $('#blog-save-btn').attr('title', 'ctrl+click更新后关闭窗口');
            }

        });

        this.blogTitleInputKeyupInit = _.once(() => {
            $('#blog-title-input').keyup((e) => {
                let $t = $(e.currentTarget);

                if (!e.shiftKey && e.keyCode == 13) { // Enter
                    if (this.simplemde.value()) {
                        this.save(e, true);
                    } else {
                        this.simplemde.codemirror.focus();
                    }

                } else if (e.shiftKey && e.keyCode == 13) { // Esc
                    this.simplemde.codemirror.focus();
                } else if (e.keyCode == 27) { // Esc
                    $t.val('');
                }
            });
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
        this.subscribe2.dispose();
        this.subscribe3.dispose();
        this.subscribe4.dispose();
    }

    _reset() {
        this.action = null;
        this.blog = null;
        $('#blog-save-btn span').text('保存');
        $('#blog-save-btn').attr('title', 'ctrl+click快速保存');
        $('#blog-title-input').val('');
        this.simplemde.value('');
        this.simplemde.toTextArea();
        this.simplemde = null;
    }

    _editInit() {
        $('#blog-title-input').val(this.blog.title);
        this.simplemde.value(this.blog.content);
        $('#blog-save-btn span').text('更新');
        $('#blog-save-btn').attr('title', 'ctrl+click更新后关闭窗口');
    }

    _writeInit() {
        let ccid = utils.urlQuery('ccid'); // chat channel id
        let cdid = utils.urlQuery('cdid'); // chat direct id
        let url = null;
        let id = null;
        if (ccid) {
            url = `/admin/chat/channel/get`;
            id = ccid;
        } else if (cdid) {
            url = `/admin/chat/direct/get`;
            id = cdid;
        }

        if (url) {
            $.get(url, { id: +id }, (data) => {
                if (data.success) {
                    this.simplemde.value(data.data.content);
                    let val = $('#blog-title-input').val();
                    if (!val) {
                        let ms = /#{1,6}[\s]+(.+)\n?/g.exec(data.data.content);
                        if (ms && ms.length > 1) {
                            $('#blog-title-input').val(ms[1]);
                        }
                    }
                } else {
                    toastr.error(data.data, '获取沟通消息失败!');
                }
            });
        }
    }

    _copyInit() {
        $('#blog-title-input').val(this.blog.title + ' (副本)');
        this.simplemde.value(this.blog.content);
        this.blog = null;
    }

    init() {

        this.simplemde = new SimpleMDE({
            element: $('#txt-blog-write')[0],
            spellChecker: false,
            // status: false,
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
                }, {
                    name: "side-by-side",
                    action: SimpleMDE.toggleSideBySide,
                    className: "fa fa-columns no-disable no-mobile",
                    title: "实时预览",
                }, {
                    name: "fullscreen",
                    action: SimpleMDE.toggleFullScreen,
                    className: "fa fa-arrows-alt no-disable no-mobile",
                    title: "全屏",
                }, {
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
                this.save(e, true);
            } else if (e.keyCode == 27) { // Esc
                this.simplemde.value('');
            } else if (e.keyCode == 13) { // Enter
                let val = $('#blog-title-input').val();
                if (!val) {
                    let ms = /#{1,6}[\s]+(.+)\n?/g.exec(this.simplemde.value());
                    if (ms && ms.length > 1) {
                        $('#blog-title-input').val(ms[1]);
                    }
                }
            }
        });

        this.$chatMsgInputRef = $('#txt-blog-write-wrapper').find('.CodeMirror textarea');
        if (this.$chatMsgInputRef.size() === 0) {
            this.$chatMsgInputRef = $('#txt-blog-write-wrapper').find('.CodeMirror [contenteditable="true"]');
        }

        if (this.action == 'edit') { // edit
            this._editInit();
        } else if (this.action == 'copy') {
            this._copyInit();
        } else {
            this._writeInit();
        }

        $('#blog-title-input').focus();

        this.initPaste();
        this.initTextcomplete();

        this.initUploadDropzone($('.CodeMirror-wrap', '#txt-blog-write-wrapper'), () => {
            return this.$chatMsgInputRef
        }, false);

        this.initUploadDropzone($('.editor-toolbar .fa.fa-upload', '#txt-blog-write-wrapper'), () => {
            return this.$chatMsgInputRef
        }, true);

        this.blogTitleInputKeyupInit();

    }

    close() {
        $('a[href="#modaal-blog-write"]').modaal('close');
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
            appendTo: '.tms-blog-write-status-bar',
            maxCount: 5
        });

        this.simplemde.codemirror.on('keydown', (cm, e) => {
            if (_.includes([13, 38, 40], e.keyCode) && this.isTipsShow()) { // enter | up | down
                e.preventDefault();
            }
        });
    }

    isTipsShow() {
        return $('.tms-blog-write-status-bar').find('.textcomplete-dropdown:visible').size() === 1;
    }

    tipsActionHandler(value) {

        if (value == 'search') {
            _.delay(() => { utils.openNewWin(nsCons.STR_EMOJI_SEARCH_URL); }, 200);
        } else {
            return true;
        }

        return false;
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
            previewsContainer: '.em-blog-write .dropzone-previews',
            previewTemplate: $('.em-blog-write .preview-template')[0].innerHTML,
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

    destroy() {
        this._reset();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        $('#blog-save-btn').click((event) => {
            this.save(event);
        });
    }

    save(event, isKey) {

        let title = $('#blog-title-input').val();
        let content = this.simplemde.value();

        if (!$.trim(title)) {
            $('#blog-title-input').val('');
            toastr.error('标题不能为空!');
            return;
        }

        if (!$.trim(content)) {
            this.simplemde.value('');
            toastr.error('内容不能为空!');
            return;
        }

        if (!this.blog) {
            if (event.ctrlKey) {
                $.post(`/admin/blog/create`, {
                    url: utils.getBasePath(),
                    usernames: utils.parseUsernames(content, [nsCtx.memberAll, ...(window.tmsUsers ? tmsUsers : [])]).join(','),
                    title: title,
                    content: content,
                    spaceId: '',
                    privated: false,
                    contentHtml: utils.md2html(content)
                }, (data, textStatus, xhr) => {
                    if (data.success) {
                        this.blog = data.data;
                        toastr.success('博文保存成功!');
                        ea.publish(nsCons.EVENT_BLOG_CHANGED, {
                            action: 'created',
                            blog: this.blog
                        });
                        $('a[href="#modaal-blog-write"]').modaal('close');
                    } else {
                        toastr.error(data.data, '博文保存失败!');
                    }
                });
            } else {
                ea.publish(nsCons.EVENT_BLOG_SAVE, {
                    title: title,
                    content: content,
                });
            }
        } else {

            if (this.sending) {
                return;
            }

            this.sending = true;
            $('#blog-save-btn i').show();

            var html = utils.md2html(content);
            let users = [nsCtx.memberAll, ...(window.tmsUsers ? tmsUsers : [])];

            $.post('/admin/blog/update', {
                url: utils.getBasePath(),
                id: this.blog.id,
                version: this.blog.version,
                usernames: utils.parseUsernames(content, users).join(','),
                title: title,
                content: content,
                diff: utils.diffS(this.blog.content, content),
                // contentHtml: html,
                // contentHtmlOld: htmlOld
            }, (data, textStatus, xhr) => {
                if (data.success) {
                    this.blog = data.data;
                    toastr.success('博文更新成功!');
                    ea.publish(nsCons.EVENT_BLOG_CHANGED, {
                        action: 'updated',
                        blog: this.blog
                    });
                    if (!isKey) {
                        (event && event.ctrlKey) && this.close();
                    } else {
                        (event && event.ctrlKey && event.shiftKey) && this.close();
                    }
                } else {
                    toastr.error(data.data, '博文更新失败!');
                }
            }).always(() => {
                this.sending = false;
                $('#blog-save-btn i').hide();
            });
        }

    }

}
