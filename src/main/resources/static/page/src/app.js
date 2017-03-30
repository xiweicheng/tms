import 'tms-semantic-ui';
import 'semantic-ui-calendar';
import 'jquery-format';

export class App {

    constructor() {
        this.init();
        this.initCalendar();
    }

    init() {

        $.fn.dropdown.settings.forceSelection = false;

        // ui form 验证提示信息国际化
        _.extend($.fn.form.settings.prompt, {
            empty: '{name}不能为空',
            checked: '{name}必须被勾选',
            email: '{name}必须是正确的邮件格式',
            url: '{name}必须是正确的URL格式',
            regExp: '{name}验证格式不正确',
            integer: '{name}必须为一个整数',
            decimal: '{name}必须为一个小数',
            number: '{name}必须设置为一个数字',
            is: '{name}必须符合规则"{ruleValue}"',
            isExactly: '{name}必须精确匹配"{ruleValue}"',
            not: '{name}不能设置为"{ruleValue}"',
            notExactly: '{name}不能准确设置为"{ruleValue}"',
            contain: '{name}需要包含"{ruleValue}"',
            containExactly: '{name}需要精确包含"{ruleValue}"',
            doesntContain: '{name}不能包含"{ruleValue}"',
            doesntContainExactly: '{name}不能精确包含"{ruleValue}"',
            minLength: '{name}必须至少包含{ruleValue}个字符',
            length: '{name}必须为{ruleValue}个字符',
            exactLength: '{name}必须为{ruleValue}个字符',
            maxLength: '{name}必须不能超过{ruleValue}个字符',
            match: '{name}必须匹配{ruleValue}字段',
            different: '{name}必须不同于{ruleValue}字段',
            creditCard: '{name}必须是一个正确的信用卡数字格式',
            minCount: '{name}必须至少包含{ruleValue}个选择项',
            exactCount: '{name}必须准确包含{ruleValue}个选择项',
            maxCount: '{name} 必须有{ruleValue}或者更少个选择项'
        });
    }

    initCalendar() {
        $.fn.calendar.settings.text = {
            days: ['日', '一', '二', '三', '四', '五', '六'],
            months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthsShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            today: '今天',
            now: '现在',
            am: '上午',
            pm: '下午'
        };

        $.fn.calendar.settings.formatter.date = function(date, settings) {
            if (!date) return '';
            var day = date.getDate();
            var month = date.getMonth() + 1;
            var year = date.getFullYear();
            // return year + '/' + month + '/' + day;
            return $.format.date(date, 'yyyy-MM-dd');
        };

        return this;
    }

    /**
     * 配置路由
     * @param  {[object]} config 路由配置
     * @param  {[object]} router 路由
     */
    configureRouter(config, router) {

        let chatTo = null;
        if (localStorage) {
            chatTo = localStorage.getItem(nsCons.KEY_REMEMBER_LAST_CHAT_TO);
        }

        config.map([{
            route: ['pwd-reset'],
            name: 'reset',
            moduleId: 'user/user-pwd-reset',
            nav: false,
            title: '密码重置 | TMS'
        }, {
            route: ['register'],
            name: 'register',
            moduleId: 'user/user-register',
            nav: false,
            title: '用户注册 | TMS'
        }, {
            route: ['chat/:username'],
            name: 'chat',
            moduleId: 'chat/chat-direct',
            nav: false,
            title: '私聊 | TMS'
        }, {
            route: ['blog'],
            name: 'chat',
            moduleId: 'blog/blog',
            nav: false,
            title: '博文 | TMS'
        }, {
            route: ['blog/:id'],
            name: 'chat',
            moduleId: 'blog/blog',
            nav: false,
            title: '博文 | TMS'
        }, {
            route: ['login'],
            name: 'login',
            moduleId: 'user/user-login',
            nav: false,
            title: '登录 | TMS'
        }, {
            route: ['test'],
            name: 'test',
            moduleId: 'test/test-lifecycle',
            nav: false,
            title: '测试 | TMS'
        }, {
            route: '',
            redirect: `chat/${chatTo ? chatTo : '@admin'}`
        }]);

        this.router = router;

    }

    /**
     * 在视图模型(ViewModel)展示前执行一些自定义代码逻辑
     * @param  {[object]} params                参数
     * @param  {[object]} routeConfig           路由配置
     * @param  {[object]} navigationInstruction 导航指令
     * @return {[promise]}                      你可以可选的返回一个延迟许诺(promise), 告诉路由等待执行bind和attach视图(view), 直到你完成你的处理工作.
     */
    activate(params, routeConfig, navigationInstruction) {

    }
}
