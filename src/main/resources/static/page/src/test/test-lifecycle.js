import {
    bindable
}
from 'aurelia-framework';
import {
    EventAggregator
}
from 'aurelia-event-aggregator';

/**
 * description
 */
export class TestLifeCycle {

    @
    bindable
    prop = null;

    static inject = [EventAggregator];

    /**
     * 构造函数
     */
    constructor(eventAggregator) {
        this.eventAggregator = eventAggregator;

        console.log('constructor');

        // this.subscribe1 = this.eventAggregator.subscribe('', (payload) => {

        // });

    }

    /**
     * 在视图和视图模型都被创建后调用, 允许访问视图实例对象.
     * @param  {[object]} view 视图实例对象
     */
    created(view) {
        console.log('created');
    }

    /**
     * 当数据绑定引擎绑定到视图时被调用
     * @param  {[object]} ctx 视图绑定上下文环境对象
     */
    bind(ctx) {
        console.log('bind');
    }

    /**
     * 当数据绑定引擎从视图解除绑定时被调用
     */
    unbind() {
        console.log('unbind');
        // this.subscribe1.dispose();
    }

    /**
     * 当视图被附加到DOM中时被调用
     */
    attached() {
        console.log('attached');
    }

    /**
     * 当视图从DOM中分离时被调用
     */
    detached() {
        console.log('detached');
    }

    /**
     * 控制是否可以导航到当前路由视图模型(ViewModel)
     * @param  {[object]} params                参数
     * @param  {[object]} routeConfig           路由配置
     * @param  {[object]} navigationInstruction 导航指令
     * @return {[boolean | promise]}            返回一个boolean类型, 一个boolean类型延迟许诺(promise)或者一个导航(navigation)命令.
     */
    canActivate(params, routeConfig, navigationInstruction) {
        console.log('canActivate');
    }

    /**
     * 在视图模型(ViewModel)展示前执行一些自定义代码逻辑
     * @param  {[object]} params                参数
     * @param  {[object]} routeConfig           路由配置
     * @param  {[object]} navigationInstruction 导航指令
     * @return {[promise]}                      你可以可选的返回一个延迟许诺(promise), 告诉路由等待执行bind和attach视图(view), 直到你完成你的处理工作.
     */
    activate(params, routeConfig, navigationInstruction) {
        console.log('activate');
    }

    /**
     * 控制是否可以导航离开当前路由视图模型(ViewModel)到其它路由视图模型(ViewModel)
     * @return {[boolean | promise]}            返回一个boolean类型, 一个boolean类型延迟许诺(promise)或者一个导航(navigation)命令.
     */
    canDeactivate() {
        console.log('canDeactivate');
    }

    /**
     * 在当前视图模型(ViewModel)切换离开后执行一些自定义代码逻辑
     * @return {[promise]}                      你可以可选的返回一个延迟许诺(promise), 告诉路由等待, 直到你完成你的处理工作.
     */
    deactivate() {
        console.log('deactivate');
    }
}
