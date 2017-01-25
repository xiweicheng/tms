export default {

    '/h1': {
        label: '/h1 [标题1] (ctrl+h)',
        value: '# ',
    },
    '/h2': {
        label: '/h2 [标题2]',
        value: '## ',
    },
    '/h3': {
        label: '/h3 [标题3]',
        value: '### ',
    },
    '/h4': {
        label: '/h4 [标题4]',
        value: '#### ',
    },
    '/h5': {
        label: '/h5 [标题5]',
        value: '##### ',
    },
    '/h6': {
        label: '/h6 [标题6] (ctrl+shift+h)',
        value: '###### ',
    },
    '/b': {
        label: '/b [粗体] (ctrl+b)',
        value: '****',
        ch: 2,
    },
    '/i': {
        label: '/i [斜体] (ctrl+i)',
        value: '**',
        ch: 1,
    },
    '/s': {
        label: '/s [删除线]',
        value: '~~~~',
        ch: 2,
    },
    '/code': {
        label: '/code [代码] (ctrl+alt+c)',
        value: '```\n\n```\n',
        line: 1
    },
    '/quote': {
        label: '/quote [引用] (ctrl+\')',
        value: '> ',
    },
    '/list': {
        label: '/list [列表] (ctrl+l)',
        value: '* ',
    },
    '/href': {
        label: '/href [链接] (ctrl+k)',
        value: '[](http://)',
        ch: 1,
    },
    '/img': {
        label: '/img [图片] (ctrl+alt+i)',
        value: '![](http://)',
        ch: 2,
    },
    '/table': {
        label: '/table [表格]',
        value: '| 列1 | 列2 | 列3 |\n| ------ | ------ | ------ |\n| 文本 | 文本 | 文本 |\n',
    },
    '/hr': {
        label: '/hr [分隔线]',
        value: '\n-----\n',
    },
    '/task': {
        label: '/task [任务列表]',
        value: '- [ ] 未完成任务\n- [x] 已完成任务',
    },
    '/details': {
        label: '/details [折叠详情]',
        value: '<details>\n<summary>标题</summary>详情内容\n</details>',
    },
    '/upload': {
        label: '/upload [上传文件] (ctrl+u)',
        value: '',
    },
    '/shortcuts': {
        label: '/shortcuts [热键] (ctrl+/)',
        value: '',
    },
}
