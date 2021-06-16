package org.lql.service;

import org.lql.domain.PurchaseRecordPo;

import java.util.List;

/**
 * Title: ProductService <br>
 * ProjectName: spring-boot-panic-buying <br>
 * description: TODO <br>
 *
 * @author: leiql <br>
 * @version: 1.0 <br>
 * @since: 2021/6/15 15:58 <br>
 */
public interface PurchaseService {

    public boolean purchase(Long userId, Long productId, Integer quantity);

    boolean purchaseRedis(Long userId, Long productId, Integer quantity);

    boolean dealRedisPurchase(List<PurchaseRecordPo> purchaseRecordPos);
}
