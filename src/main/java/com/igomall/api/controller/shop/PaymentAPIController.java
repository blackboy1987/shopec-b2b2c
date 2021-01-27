package com.igomall.api.controller.shop;


import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.service.WxPayService;
import com.igomall.entity.PaymentTransaction;
import com.igomall.service.PaymentTransactionService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 支付通知 - 接口类
 */
@RestController("shopPaymentAPIController")
@RequestMapping("/api/payment")
public class PaymentAPIController {

    private static final Logger _logger = LoggerFactory.getLogger(PaymentAPIController.class);

    @Inject
    private PaymentTransactionService paymentTransactionService;
    @Inject
    private WxPayService wxService;

    @ResponseBody
    @RequestMapping("/pay_notify")
    public String payNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            _logger.info("xmlResult = [{}]", xmlResult);

            WxPayOrderNotifyResult result = wxService.parseOrderNotifyResult(xmlResult);
            // 结果正确
            String paymentTransactionSn = result.getOutTradeNo();
            String transactionId = result.getTransactionId();

            //自己处理订单的业务逻辑，需要判断订单是否已经支付过，否则可能会重复调用
            PaymentTransaction paymentTransaction = paymentTransactionService.findBySn(paymentTransactionSn);

            if (paymentTransaction == null) {
                _logger.error("找不到该支付事务，sn:" + paymentTransactionSn);
                return WxPayNotifyResponse.fail("找不到该支付事务，sn:" + paymentTransactionSn);
            }

            if (!paymentTransaction.getIsSuccess()) {
                _logger.info("进入订单支付回调paymentTransactionSn = [{}]", paymentTransactionSn);
                paymentTransactionService.handle(paymentTransaction);
            }
            _logger.info("paymentTransactionSn = [{}], transactionId = [{}]", paymentTransactionSn, transactionId);

            return WxPayNotifyResponse.success("处理成功!");
        } catch (Exception e) {
            e.printStackTrace();
            _logger.error("微信回调结果异常, 异常原因{}", e.getMessage());
            return WxPayNotifyResponse.fail(e.getMessage());
        }
    }

}

