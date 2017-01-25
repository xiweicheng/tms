/**
 * 账户注册
 */
export class ViewModel {

    header = '账户激活页面';

    /**
     * 在视图模型(ViewModel)展示前执行一些自定义代码逻辑
     * @param  {[object]} params                参数
     * @param  {[object]} routeConfig           路由配置
     * @param  {[object]} navigationInstruction 导航指令
     * @return {[promise]}                      你可以可选的返回一个延迟许诺(promise), 告诉路由等待执行bind和attach视图(view), 直到你完成你的处理工作.
     */
    activate(params, routeConfig, navigationInstruction) {

        if (params.id) {
            this.token = params.id;

            this.isReq = true;
            this.header = '账户激活中,请稍后...!';
            http.fetch('/free/user/register/activate', {
                method: 'post',
                body: json({
                    token: this.token
                })
            }).then((resp) => {
                if (resp.ok) {
                    resp.json().then((data) => {
                        if (data.success) {
                            this.header = '账户激活成功,请返回登录页面登录!';
                        } else {
                            this.header = '账户激活失败!';
                            toastr.error(data.data, '账户激活失败!');
                        }
                    });
                    this.isReq = false;
                }
            });
        }
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {

        $(this.fm).form({
            on: 'blur',
            inline: true,
            fields: {
                username: {
                    identifier: 'username',
                    rules: [{
                        type: 'empty'
                    }, {
                        type: 'minLength[3]'
                    }, {
                        type: 'regExp',
                        value: /^[a-z]+[a-z0-9\.\-_]*[a-z0-9]+$/,
                        prompt: '小写字母数字.-_组合,字母开头,字母数字结尾'
                    }]
                },
                pwd: {
                    identifier: 'pwd',
                    rules: [{
                        type: 'empty'
                    }, {
                        type: 'minLength[8]'
                    }]
                },
                name: {
                    identifier: 'name',
                    rules: [{
                        type: 'empty'
                    }, {
                        type: 'maxLength[20]'
                    }]
                },
                mail: {
                    identifier: 'mail',
                    rules: [{
                        type: 'empty'
                    }, {
                        type: 'email'
                    }]
                }
            }
        });

    }

    okHandler() {

        if (!$(this.fm).form('is valid')) {
            toastr.error('账户注册信息输入不合法!');
            return;
        }

        this.isReq = true;
        http.fetch('/free/user/register', {
            method: 'post',
            body: json({
                username: this.username,
                pwd: this.pwd,
                name: this.name,
                mail: this.mail,
                baseUrl: utils.getBaseUrl(),
                path: wurl('path')
            })
        }).then((resp) => {
            if (resp.ok) {
                resp.json().then((data) => {
                    if (data.success) {
                        toastr.success('注册成功,请通过接收到的激活邮件激活账户!');
                        _.delay(() => {
                            window.location = "/admin/login";
                        }, 2000);
                    } else {
                        toastr.error(data.data, '注册失败!');
                        this.isReq = false;
                    }
                });

            }
        });

    }

}
