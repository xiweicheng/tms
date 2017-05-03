/**
 * 轮询插件
 * 原理:轮询最小间隔 6s, 最大间隔5min, 轮询节能模式, 当连续1min(10次)获取不到新数据, 轮询间隔 时间 +6s, 
 * 接着递增 +6/次, 直到最大间隔, 不再递增轮询间隔. 一旦有一次获得新数据, 轮询间隔恢复到最小间隔6s.
 * @return {[type]} [description]
 */
var minInterval = 6000; // 轮询最小间隔 6s
var maxInterval = 300000; // 轮询最大间隔5min
var incInterval = 6000; // 递增轮询间隔时间 6s

var tolerate = 10; // 容忍连续获取不到新数据的(次数), 超过, 就会开始递增轮询间隔时间.

var timer = null; // 轮询对象引用

var inc = 0; // 轮询次数计数器

var interval = minInterval; // 轮询实际轮询间隔

var _pollCb = null;
var _errCb = null;
var _isPause = false; // 是否暂停

function oneHandler() {

    if (_isPause) {
        return;
    }

    try { // 捕获轮询执行方法体中的异常, 防止破坏轮询的持续性.
        _pollCb && _pollCb(_reset, _stop);
    } catch (e) {
        _errCb && _errCb(_reset, _stop, e);

        // TODO for debugging
        console.log('轮询异常: ' + e);
    }
}

/**
 * 轮询处理递归逻辑
 * @param  {[Function]} pollCb 轮询业务回调
 * @param  {[Function]} errCb  轮询业务处理异常回到
 */
function _start() {
    // TODO for debugging
    // console.log('poll start...');

    _isPause = false;

    oneHandler();
    timer = setInterval(function() {
        inc++;
        oneHandler();
        // TODO for debugging
        // console.log(interval);

        if (inc > tolerate) { // 超过轮询容忍次数内

            interval = minInterval + (incInterval * (inc - tolerate));

            if (interval <= maxInterval) { // 最大轮询间隔范围内, 逐次递增轮询间隔
                clearInterval(timer);
                _start();
            }
        }
    }, interval);
}

function _stop() {
    // TODO for debugging
    // console.log("poll stop...");

    inc = 0;
    interval = minInterval;
    _isPause = false;
    clearInterval(timer);
    timer = null;
}

function _reset() {
    // TODO for debugging
    // console.log("poll reset...");

    _stop();
    _start();
}

function _pause() {
    // TODO for debugging
    // console.log("pause reset...");
    _isPause = true;
}

export default {
    start: function(pollCb, errCb) {
        if (timer) {
            _stop();
        }
        _pollCb = pollCb;
        _errCb = errCb;
        _start();
    },
    reset: function() {
        _reset();
    },
    stop: function() {
        _stop();
    },
    pause: function() {
        _pause();
    }
};
