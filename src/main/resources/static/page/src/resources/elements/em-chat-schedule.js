import { bindable, containerless } from 'aurelia-framework';
import {
    default as moment
} from 'moment';
import 'fullcalendar';
import 'fullcalendar/dist/locale/zh-cn';

@containerless
export class EmChatSchedule {

    @bindable loginUser;

    offset = 100;

    show() {
        this.users = window.tmsUsers;

        _.defer(() => {
            $(this.scheduleRef).fullCalendar('today');
        });
        _.delay(() => {
            $(this.scheduleRef).fullCalendar('option', 'height', 'parent');
            $(this.scheduleRef).fullCalendar('refetchEvents');
        }, 500);
    }

    /**
     * 构造函数
     */
    constructor() {
        this.actorsOpts = {
            onAdd: (addedValue, addedText, $addedChoice) => {},
            onLabelRemove: (removedValue) => {
                if (this.loginUser.username == removedValue) {
                    return false;
                }
            }
        };

        this.subscribe = ea.subscribe(nsCons.EVENT_SCHEDULE_REFRESH, (payload) => {
            $(this.scheduleRef).fullCalendar('refetchEvents');
        });

        this._getEvents();
    }

    _getEvents(start, end, callback) {
        let data = {};
        if (start) {
            data.start = start.unix();
        }
        if (end) {
            data.start = end.unix();
        }
        $.get('/admin/schedule/listMy', data, (data) => {
            if (data.success) {
                this.events = _.map(data.data, (item) => {
                    let event = {
                        id: item.id,
                        title: item.title,
                        actors: item.actors,
                        creator: item.creator
                    };

                    if (item.startDate) {
                        event.start = item.startDate;
                    } else {
                        event.start = new Date().getTime();
                    }

                    if (item.endDate) {
                        event.end = item.endDate;
                    }

                    return event;
                });
                callback && callback(this.events);
            }
        });
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        this.subscribe.dispose();
    }

    attached() {

        $(this.scheduleRef).fullCalendar({
            header: {
                left: 'prev,next today',
                // center: '',
                center: 'title',
                right: 'month,agendaWeek,agendaDay,listWeek'
            },
            // height: $(window).height() - this.offset,
            height: 'parent',
            defaultDate: new Date(),
            defaultView: 'listWeek',
            editable: true,
            eventLimit: true, // allow "more" link when too many events
            navLinks: true,
            // timezone: 'Asia/Shanghai',
            // timezone: 'UTC',
            timezone: 'local',
            dayClick: (date, jsEvent, view) => {

                $(this.startRef).calendar('set date', date.toDate());
                this.isPopupShowForDayClick = true;
                $(this.addRef).popup('show');
            },
            eventClick: (calEvent, jsEvent, view) => {
                this.scheduleEditVm.show(calEvent);
            },
            eventMouseover: (event, jsEvent, view) => {},
            eventMouseout: (event, jsEvent, view) => {},
            events: (start, end, timezone, callback) => {

                this._getEvents(start, end, callback);
            },
            eventDrop: (event, delta, revertFunc) => {

                if (event.creator.username != this.loginUser.username) {
                    toastr.error('您没有权限修改!');
                    ea.publish(nsCons.EVENT_SCHEDULE_REFRESH, {});
                    return;
                }

                this._updateDate(event.id, event.start, event.end);
            },
            eventResize: (event, delta, revertFunc) => {

                if (event.creator.username != this.loginUser.username) {
                    toastr.error('您没有权限修改!');
                    ea.publish(nsCons.EVENT_SCHEDULE_REFRESH, {});
                    return;
                }

                this._updateDate(event.id, event.start, event.end);
            }
        });

        $(this.addRef)
            .popup({
                on: 'click',
                // closable: false,
                inline: true,
                // hoverable: true,
                silent: true,
                // movePopup: false,
                jitter: 300,
                position: 'bottom center',
                delay: {
                    show: 300,
                    hide: 300
                },
                onVisible: () => {
                    $(this.titleRef).focus();
                    autosize.update(this.titleRef);
                    if (!this.title && !this.isPopupShowForDayClick) {
                        $(this.startRef).calendar('set date', new Date());
                    }
                    this.isPopupShowForDayClick = false;
                }
            });

        $(this.startRef).calendar({
            today: true,
            endCalendar: $(this.endRef)
        });
        $(this.endRef).calendar({
            today: true,
            startCalendar: $(this.startRef)
        });

        this._reset();
    }

    titleKeyupHandler(event) {
        if (event.ctrlKey && event.keyCode === 13) {
            this.addHandler();
        }
    }

    _updateDate(id, start, end) {
        let data = {
            id: id,
            basePath: utils.getBasePath()
        };

        if (start) {
            data.startDate = start.toDate();
        } else {
            data.startDate = new Date();
        }

        if (end) {
            data.endDate = end.toDate();
        }

        $.post('/admin/schedule/updateStartEndDate', data, (data, textStatus, xhr) => {
            if (data.success) {
                toastr.success('更新日程成功!');
                ea.publish(nsCons.EVENT_SCHEDULE_REFRESH, {});
            } else {
                toastr.error(data.data);
            }
        });
    }

    initMembersUI(last) {
        if (last) {
            _.defer(() => {
                $(this.actorsRef).dropdown().dropdown('clear').dropdown(this.actorsOpts).dropdown('set selected', [this.loginUser.username]);
            });
        }
    }

    clearStartDateHandler() {
        $(this.startRef).calendar('clear');
    }

    clearEndDateHandler() {
        $(this.endRef).calendar('clear');
    }

    addHandler() {

        if (!this.title) {
            toastr.error('日程内容不能为空!');
            return;
        }

        let data = {
            title: this.title,
            basePath: utils.getBasePath(),
            actors: $(this.actorsRef).dropdown('get value')
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

        $.post('/admin/schedule/create', data, (data, textStatus, xhr) => {
            if (data.success) {
                $(this.scheduleRef).fullCalendar('refetchEvents');
                toastr.success('添加日程成功!');
                this._reset();
                $(this.addRef).popup('hide');
            } else {
                toastr.error(data.data);
            }
        });
    }

    _reset() {
        this.title = '';
        $(this.startRef).calendar('set date', new Date());
        $(this.endRef).calendar('clear');
        $(this.actorsRef).dropdown('clear');
        if (this.loginUser && this.loginUser.username) {
            $(this.actorsRef).dropdown('set selected', [this.loginUser.username]).dropdown('set value', this.loginUser.username);
        }
    }
}
