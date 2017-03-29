/**
 * 用户密码重置
 */
export class UserPwdReset {

    mail = '';
    pwd = '';

    isReq = false;

    token = utils.urlQuery('id');

    resetPwdHandler() {

        if (!$(this.fm).form('is valid')) {
            toastr.error('邮件地址输入不合法!');
            return;
        }

        this.isReq = true;
        http.fetch('/free/user/pwd/reset', {
            method: 'post',
            body: json({
                mail: this.mail,
                baseUrl: utils.getBaseUrl(),
                path: wurl('path')
            })
        }).then((resp) => {
            if (resp.ok) {
                resp.json().then((data) => {
                    if (data.success) {
                        toastr.success('重置密码邮件链接发送成功!');
                        _.delay(() => {
                            window.location = "/admin/login";
                        }, 2000);
                    } else {
                        toastr.error(data.data, '重置密码邮件链接发送失败!');
                        this.isReq = false;
                    }
                });
            }

        });
    }

    newPwdHandler() {

        if (!$(this.fm2).form('is valid')) {
            toastr.error('新密码输入不合法!');
            return;
        }

        this.isReq = true;
        http.fetch('/free/user/pwd/new', {
            method: 'post',
            body: json({
                token: this.token,
                pwd: this.pwd
            })
        }).then((resp) => {
            if (resp.ok) {
                resp.json().then((data) => {
                    if (data.success) {
                        toastr.success('重置密码成功!');
                        _.delay(() => {
                            window.location = "/admin/login";
                        }, 2000);
                    } else {
                        toastr.error(data.data, '重置密码失败!');
                        this.isReq = false;
                    }
                });
            }
            
        });
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {

        $(this.fm).form({
            on: 'blur',
            inline: true,
            fields: {
                mail: ['empty', 'email']
            }
        });

        $(this.fm2).form({
            on: 'blur',
            inline: true,
            fields: {
                mail: ['empty', 'minLength[8]']
            }
        });
    }

}
