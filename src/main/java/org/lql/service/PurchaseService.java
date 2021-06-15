package org.lql.service;

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
}
