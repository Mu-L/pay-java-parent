package com.egzosn.pay.demo.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.egzosn.pay.common.api.BasePayService;
import com.egzosn.pay.common.api.PayConfigStorage;
import com.egzosn.pay.common.api.PayMessageHandler;
import com.egzosn.pay.common.api.PayMessageRouter;
import com.egzosn.pay.common.api.PayService;
import com.egzosn.pay.common.http.HttpConfigStorage;
import com.egzosn.pay.demo.entity.ApyAccount;
import com.egzosn.pay.demo.entity.PayType;
import com.egzosn.pay.demo.service.handler.AliPayMessageHandler;
import com.egzosn.pay.demo.service.handler.FuiouPayMessageHandler;
import com.egzosn.pay.demo.service.handler.PayoneerMessageHandler;
import com.egzosn.pay.demo.service.handler.UnionPayMessageHandler;
import com.egzosn.pay.demo.service.handler.WxPayMessageHandler;
import com.egzosn.pay.demo.service.handler.YouDianPayMessageHandler;
import com.egzosn.pay.demo.service.interceptor.AliPayMessageInterceptor;
import com.egzosn.pay.demo.service.interceptor.YoudianPayMessageInterceptor;

/**
 * 支付响应对象
 *
 * @author egan
 * email egzosn@gmail.com
 * date 2016/11/18 0:34
 */
public class PayResponse {

    @Resource
    private AutowireCapableBeanFactory spring;

    private PayConfigStorage storage;

    private PayService service;

    private PayMessageRouter router;

    public PayResponse() {

    }

    /**
     * 初始化支付配置
     *
     * @param apyAccount 账户信息
     * @see ApyAccount 对应表结构详情--》 /pay-java-demo/resources/apy_account.sql
     */
    public void init(ApyAccount apyAccount) {
        //根据不同的账户类型 初始化支付配置
        this.service = apyAccount.getPayType().getPayService(apyAccount);
        this.storage = service.getPayConfigStorage();


        //这里设置http请求配置
//        service.setRequestTemplateConfigStorage(getHttpConfigStorage());
        buildRouter(apyAccount.getPayId());


    }

    /**
     * 获取http配置，如果配置为null则为默认配置，无代理,无证书的请求方式。
     * 此处非必需
     *
     * @param apyAccount 账户信息
     * @return 请求配置
     */
    public HttpConfigStorage getHttpConfigStorage(ApyAccount apyAccount) {
        HttpConfigStorage httpConfigStorage = new HttpConfigStorage();
        /* 网路代理配置 根据需求进行设置*/
//        //http代理地址
//        httpConfigStorage.setHttpProxyHost("192.168.1.69");
//        //代理端口
//        httpConfigStorage.setHttpProxyPort(3308);
//        //代理用户名
//        httpConfigStorage.setHttpProxyUsername("user");
//        //代理密码
//        httpConfigStorage.setHttpProxyPassword("password");
        //设置ssl证书路径 https证书设置 方式二
        httpConfigStorage.setKeystore(apyAccount.getKeystorePath());
        //设置ssl证书对应的密码
        httpConfigStorage.setStorePassword(apyAccount.getStorePassword());
        return httpConfigStorage;
    }


    /**
     * 配置路由
     *
     * @param payId 指定账户id，用户多微信支付多支付宝支付
     * @deprecated 不再推荐使用路由方式，回调或拦截器，直接在payService中设置并获取使用,回调拦截器已提供对应的实现方式：{@link BasePayService#setPayMessageHandler(com.egzosn.pay.common.api.PayMessageHandler)} 与{@link BasePayService#addPayMessageInterceptor(com.egzosn.pay.common.api.PayMessageInterceptor)}
     */
    @Deprecated
    private void buildRouter(Integer payId) {
        router = new PayMessageRouter(this.service);
        router
                .rule()
                //消息类型
                //支付账户事件类型
                .payType(PayType.aliPay.name())
                //拦截器
                .interceptor(new AliPayMessageInterceptor())
                //处理器
                .handler(spring.getBean(AliPayMessageHandler.class))
                .end()
                .rule()
                .payType(PayType.wxPay.name())
                .handler(autowire(new WxPayMessageHandler(payId)))
                .end()
                .rule()
                .payType(PayType.youdianPay.name())
                .interceptor(new YoudianPayMessageInterceptor()) //拦截器
                .handler(autowire(new YouDianPayMessageHandler(payId)))
                .end()
                .rule()
                .payType(PayType.fuiou.name())
                .handler(autowire(new FuiouPayMessageHandler(payId)))
                .end()
                .rule()
                .payType(PayType.unionPay.name())
                .handler(autowire(new UnionPayMessageHandler(payId)))
                .end()
                .rule()
                .payType(PayType.payoneer.name())
                .handler(autowire(new PayoneerMessageHandler(payId)))
                .end()
                .rule()
                .payType(PayType.payPal.name())
                .handler(spring.getBean(AliPayMessageHandler.class))
                .end()
        ;
    }


    private PayMessageHandler autowire(PayMessageHandler handler) {
        spring.autowireBean(handler);
        return handler;
    }

    public PayConfigStorage getStorage() {
        return storage;
    }

    public PayService getService() {
        return service;
    }

    /**
     * 不建议使用， 回调拦截器已提供对应的实现方式：{@link BasePayService#setPayMessageHandler(com.egzosn.pay.common.api.PayMessageHandler)} 与{@link BasePayService#addPayMessageInterceptor(com.egzosn.pay.common.api.PayMessageInterceptor)}
     *
     * @return
     */
    @Deprecated
    public PayMessageRouter getRouter() {
        return router;
    }
}
