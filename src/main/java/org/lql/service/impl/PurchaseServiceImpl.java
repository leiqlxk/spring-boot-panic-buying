package org.lql.service.impl;

import org.lql.domain.ProductPo;
import org.lql.domain.PurchaseRecordPo;
import org.lql.mapper.ProductMapper;
import org.lql.mapper.PurchaseRecordMapper;
import org.lql.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Title: PurchaseServiceImpl <br>
 * ProjectName: spring-boot-panic-buying <br>
 * description: TODO <br>
 *
 * @author: leiql <br>
 * @version: 1.0 <br>
 * @since: 2021/6/15 16:00 <br>
 */
@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRecordMapper purchaseRecordMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean purchase(Long userId, Long productId, Integer quantity) {
        // 获取产品
        ProductPo productPo = productMapper.getProduct(productId);

        // 比较库存和购买数量
        if (productPo.getStock() < quantity) {
            // 库存不足
            return false;
        }

        // 扣减库存
        productMapper.decreaseProduct(productId, quantity);

        // 初始化购买记录
        PurchaseRecordPo purchaseRecordPo = this.initPurchaseRecord(userId, productPo, quantity);

        purchaseRecordMapper.insertPurchaseRecord(purchaseRecordPo);

        return true;
    }

    private PurchaseRecordPo initPurchaseRecord(Long userId, ProductPo productPo, Integer quantity) {
        PurchaseRecordPo purchaseRecordPo = new PurchaseRecordPo();
        purchaseRecordPo.setUserId(userId);
        purchaseRecordPo.setProductId(productPo.getId());
        purchaseRecordPo.setPrice(productPo.getPrice());
        purchaseRecordPo.setQuantity(quantity);
        purchaseRecordPo.setSum(productPo.getPrice() * quantity);
        purchaseRecordPo.setNote("购买日志，时间：" + System.currentTimeMillis());

        return purchaseRecordPo;
    }

}
