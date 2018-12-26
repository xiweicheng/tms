/*
 Copyright (c) 2012-2017 Open Lab
 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


function dateToRelative(localTime) {
    var diff = new Date().getTime() - localTime;
    var ret = "";

    var min = 60000;
    var hour = 3600000;
    var day = 86400000;
    var wee = 604800000;
    var mon = 2629800000;
    var yea = 31557600000;

    if (diff < -yea * 2)
        ret = "in ## years".replace("##", (-diff / yea).toFixed(0));

    else if (diff < -mon * 9)
        ret = "in ## months".replace("##", (-diff / mon).toFixed(0));

    else if (diff < -wee * 5)
        ret = "in ## weeks".replace("##", (-diff / wee).toFixed(0));

    else if (diff < -day * 2)
        ret = "in ## days".replace("##", (-diff / day).toFixed(0));

    else if (diff < -hour)
        ret = "in ## hours".replace("##", (-diff / hour).toFixed(0));

    else if (diff < -min * 35)
        ret = "in about one hour";

    else if (diff < -min * 25)
        ret = "in about half hour";

    else if (diff < -min * 10)
        ret = "in some minutes";

    else if (diff < -min * 2)
        ret = "in few minutes";

    else if (diff <= min)
        ret = "just now";

    else if (diff <= min * 5)
        ret = "few minutes ago";

    else if (diff <= min * 15)
        ret = "some minutes ago";

    else if (diff <= min * 35)
        ret = "about half hour ago";

    else if (diff <= min * 75)
        ret = "about an hour ago";

    else if (diff <= hour * 5)
        ret = "few hours ago";

    else if (diff <= hour * 24)
        ret = "## hours ago".replace("##", (diff / hour).toFixed(0));

    else if (diff <= day * 7)
        ret = "## days ago".replace("##", (diff / day).toFixed(0));

    else if (diff <= wee * 5)
        ret = "## weeks ago".replace("##", (diff / wee).toFixed(0));

    else if (diff <= mon * 12)
        ret = "## months ago".replace("##", (diff / mon).toFixed(0));

    else
        ret = "## years ago".replace("##", (diff / yea).toFixed(0));

    return ret;
}

//override date format i18n

Date.monthNames = ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"];
// Month abbreviations. Change this for local month names
Date.monthAbbreviations = ["一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"];
// Full day names. Change this for local month names
// Date.dayNames = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
Date.dayNames = ["日", "一", "二", "三", "四", "五", "六"];
// Day abbreviations. Change this for local month names
Date.dayAbbreviations = ["日", "一", "二", "三", "四", "五", "六"];
// Used for parsing ambiguous dates like 1/2/2000 - default to preferring 'American' format meaning Jan 2.
// Set to false to prefer 'European' format meaning Feb 1
Date.preferAmericanFormat = false;

Date.firstDayOfWeek = 0;
Date.defaultFormat = "M/d/yyyy";
Date.masks = {
    fullDate: "EEEE, MMMM d, yyyy",
    shortTime: "h:mm a"
};
Date.today = "Today";

Number.decimalSeparator = ".";
Number.groupingSeparator = ",";
Number.minusSign = "-";
Number.currencyFormat = "###,##0.00";



var millisInWorkingDay = 28800000;
var workingDaysPerWeek = 5;

function isHoliday(date) {
    var friIsHoly = false;
    var satIsHoly = true;
    var sunIsHoly = true;

    var pad = function(val) {
        val = "0" + val;
        return val.substr(val.length - 2);
    };

    var holidays = "##";

    var ymd = "#" + date.getFullYear() + "_" + pad(date.getMonth() + 1) + "_" + pad(date.getDate()) + "#";
    var md = "#" + pad(date.getMonth() + 1) + "_" + pad(date.getDate()) + "#";
    var day = date.getDay();

    return (day == 5 && friIsHoly) || (day == 6 && satIsHoly) || (day == 0 && sunIsHoly) || holidays.indexOf(ymd) > -1 || holidays.indexOf(md) > -1;
}



var i18n = {
    YES: "是",
    NO: "否",
    FLD_CONFIRM_DELETE: "确认删除吗?",
    INVALID_DATA: "插入数据格式不正确。",
    ERROR_ON_FIELD: "字段错误",
    OUT_OF_BOUDARIES: "字段外允许值:",
    CLOSE_ALL_CONTAINERS: "全部关闭吗?",
    DO_YOU_CONFIRM: "你确认吗?",
    ERR_FIELD_MAX_SIZE_EXCEEDED: "超过字段最大值",
    WEEK_SHORT: "W.",

    FILE_TYPE_NOT_ALLOWED: "文件类型不允许.",
    FILE_UPLOAD_COMPLETED: "文件上传完成.",
    UPLOAD_MAX_SIZE_EXCEEDED: "超过文件大小限制",
    ERROR_UPLOADING: "上传错误",
    UPLOAD_ABORTED: "上传终止",
    DROP_HERE: "拖拽文件到此",

    FORM_IS_CHANGED: "你有一些未保存的页面数据!",

    PIN_THIS_MENU: "固定菜单",
    UNPIN_THIS_MENU: "解除固定",
    OPEN_THIS_MENU: "打开菜单",
    CLOSE_THIS_MENU: "关闭菜单",
    PROCEED: "执行吗?",

    PREV: "上一个",
    NEXT: "下一个",
    HINT_SKIP: "知道了，关闭这个提示.",

    WANT_TO_SAVE_FILTER: "保存过滤器",
    NEW_FILTER_NAME: "新过滤器的名称",
    SAVE: "保存",
    DELETE: "删除",

    COMBO_NO_VALUES: "无可用数据...?",

    FILTER_UPDATED: "过滤器已更新.",
    FILTER_SAVED: "过滤器已保存."

};