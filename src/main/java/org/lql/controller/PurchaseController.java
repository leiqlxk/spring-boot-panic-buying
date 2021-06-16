package org.lql.controller;

import org.lql.domain.Result;
import org.lql.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Title: PurchaseController <br>
 * ProjectName: spring-boot-panic-buying <br>
 * description: TODO <br>
 *
 * @author: leiql <br>
 * @version: 1.0 <br>
 * @since: 2021/6/15 16:07 <br>
 */
@RestController
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @GetMapping("/test")
    public ModelAndView testPage() {
        return new ModelAndView("test");
    }

    @PostMapping("/purchase")
    public Result purchase(Long userId, Long productId, Integer quantity) {
//        boolean success = purchaseService.purchase(userId, productId, quantity);
        boolean success = purchaseService.purchaseRedis(userId, productId, quantity);

        String message = success ? "抢购成功" : "抢购失败";

        return new Result(success, message);
    }
}
