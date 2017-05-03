import { bindable, containerless } from 'aurelia-framework';

@containerless
export class EmChatScheduleEdit {

    @bindable loginUser;

    /**
     * 构造函数
     */
    constructor() {
        this.actorsOpts = {
            onAdd: (addedValue, addedText, $addedChoice) => {
                $.post('/admin/schedule/addActors', {
                    id: this.event.id,
                    basePath: utils.getBasePath(),
                    actors: addedValue
                }, (data, textStatus, xhr) => {
                    if (data.success) {
                        toastr.success('添加参与者成功!');
                        ea.publish(nsCons.EVENT_SCHEDULE_REFRESH, {});
                    } else {
                        toastr.error(data.data);
                    }
                });
            },
            onLabelRemove: (removedValue) => {
                if (this.loginUser.username == removedValue) {
                    return false;
                }
                $.post('/admin/schedule/removeActors', {
                    id: this.event.id,
                    basePath: utils.getBasePath(),
                    actors: removedValue
                }, (data, textStatus, xhr) => {
                    if (data.success) {
                        toastr.success('移除参与者成功!');
                        ea.publish(nsCons.EVENT_SCHEDULE_REFRESH, {});
                    } else {
                        toastr.error(data.data);
                    }
                });
            }
        };
    }

    attached() {
        $(this.startRef).calendar({
            today: true,
            endCalendar: $(this.endRef)
        });
        $(this.endRef).calendar({
            today: true,
            startCalendar: $(this.startRef)
        });

    }

    initMembersUI(last) {
        if (last) {
            _.defer(() => {
                let actors = [this.loginUser.username];
                if (this.event) {
                    actors = _.map(this.event.actors, 'username');
                }

                $(this.actorsRef).dropdown().dropdown('clear').dropdown('set selected', actors).dropdown(this.actorsOpts);
            });
        }
    }

    titleKeyupHandler(event) {
        if (event.ctrlKey && event.keyCode === 13) {
            this.updateHandler();
        }
    }

    clearStartDateHandler() {
        $(this.startRef).calendar('clear');
    }

    clearEndDateHandler() {
        $(this.endRef).calendar('clear');
    }

    show(calEvent) {
        this.event = _.clone(calEvent);

        this.showHandler();

        $(this.scheduleEditRef).popup({
            on: 'click',
            // closable: true,
            inline: true,
            silent: true,
            // movePopup: false,
            position: 'bottom center',
            jitter: 300,
            prefer: 'opposite',
            delay: {
                show: 300,
                hide: 300
            }
        }).popup('show');
    }

    showHandler() {

        this.users = window.tmsUsers;
        $(this.actorsRef).dropdown().dropdown('clear');
        _.defer(() => {
            if (this.event.start) {
                $(this.startRef).calendar('set date', this.event.start.toDate());
            } else {
                $(this.startRef).calendar('clear');
            }

            if (this.event.end) {
                $(this.endRef).calendar('set date', this.event.end.toDate());
            } else {
                $(this.endRef).calendar('clear');
            }

            let actors = _.map(this.event.actors, 'username');

            $(this.actorsRef).dropdown('set selected', actors).dropdown(this.actorsOpts);

            if (this.event.creator.username == this.loginUser.username) {
                $(this.titleRef).focus();
            }

            autosize.update(this.titleRef);
        });

    }

    updateHandler() {
        if (!this.event.title) {
            toastr.error('日程内容不能为空!');
            return;
        }

        let data = {
            id: this.event.id,
            basePath: utils.getBasePath(),
            title: this.event.title
        };
        let start = $(this.startRef).calendar('get date');
        let end = $(this.endRef).calendar('get date');

        if (start) {
            data.startDate = start;
        } else {
            data.startDate = new Date();
        }

        if (end) {
            data.endDate = end;
        }

        $.post('/admin/schedule/update2', data, (data, textStatus, xhr) => {
            if (data.success) {
                toastr.success('更新日程成功!');
                $(this.scheduleEditRef).popup('hide');
                ea.publish(nsCons.EVENT_SCHEDULE_REFRESH, {});
            } else {
                toastr.error(data.data);
            }
        });
    }

    delHandler() {
        this.emConfirmModal.show({
            onapprove: () => {

                $.post('/admin/schedule/delete', {
                    id: this.event.id,
                    basePath: utils.getBasePath()
                }, (data, textStatus, xhr) => {
                    if (data.success) {
                        toastr.success('日程删除成功!');
                        ea.publish(nsCons.EVENT_SCHEDULE_REFRESH, {});
                    } else {
                        toastr.error(data.data);
                    }
                });

            }
        });
    }
}
