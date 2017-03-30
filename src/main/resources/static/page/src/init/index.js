/**
 * 程序初次加载启动,进行一些初始化操作:
 */
import 'jquery';
import 'jquery.scrollto'; // https://github.com/flesler/jquery.scrollTo
import 'timeago';
import 'lodash';
import 'hotkeys';

import config from './config';

export function configure(aurelia, params) {

    config.context(aurelia)
        .initGlobalVar()
        .initAjax()
        .initToastr()
        .initMarked()
        .initAnimateCss()
        .initEmoji()
        .initModaal();
}
