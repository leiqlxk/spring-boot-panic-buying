package org.lql.service.impl;

import org.apache.logging.log4j.util.Strings;
import org.lql.domain.ProductPo;
import org.lql.domain.PurchaseRecordPo;
import org.lql.mapper.ProductMapper;
import org.lql.mapper.PurchaseRecordMapper;
import org.lql.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 使用LUA语言操作redis
    String purchaseScript =
            // 先将产品编号保存到集合中
            " redis.call('sadd', KEYS[1], ARGV[2]) \n" +
            // 购买列表
            "local productPurchaseList = KEYS[2]..ARGV[2] \n" +
            // 用户编号
            "local userId = ARGV[1] \n" +
            // 产品键
            "local product = 'product_'..ARGV[2] \n" +
            // 购买数量
            "local quantity = tonumber(ARGV[3]) \n" +
            // 当前库存
            "local stock = tonumber(redis.call('hget', product, 'stock')) \n" +
            // 价格
            "local price = tonumber(redis.call('hget', product, 'price')) \n" +
            // 购买时间
            "local purchase_date = ARGV[4] \n" +
            // 库存不足，返回0
            "if stock < quantity then return 0 end \n" +
            // 减库存
            "stock = stock - quantity \n" +
            "redis.call('hset', product, 'stock', tostring(stock)) \n" +
            // 计算价格
            "local sum = price * quantity \n" +
            // 合并购买记录
            "local purchaseRecord = userId..','..quantity..','..sum..','..price..','..purchase_date \n" +
            // 将购买记录保存到list
            "redis.call('rpush', productPurchaseList, purchaseRecord) \n" +
            // 返回成功
            "return 1 \n";

    // redis购买记录集合前缀
    private static final String PURCHASE_PRODUCT_LIST = "purchase_list_";

    // 抢购商品集合
    private static final String PRODUCT_SCHEDULE_SET = "product_schedule_set";

    // 32位SHA1编码，第一次执行的时候先让redis进行缓存脚本返回
    private String sha1 = null;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean purchase(Long userId, Long productId, Integer quantity) {
        // 当前时间，用于乐观锁，其一般通过限制时间或重入次数的办法来压制过多的sql被执行，随着并发量增加用时间来限制的方法会使得重入次数大大下降，因此用限制重入次数的方式可能会更好
        Long start = System.currentTimeMillis();

        while (true) {

            // 循环时间
            Long end = System.currentTimeMillis();

            // 如果循环时间大于100ms返回终止循环
            if (end - start > 100) {
                return false;
            }

            // 获取产品
            ProductPo productPo = productMapper.getProduct(productId);

            // 比较库存和购买数量
            if (productPo.getStock() < quantity) {
                // 库存不足
                return false;
            }

            // 扣减库存
            int result = productMapper.decreaseProduct(productId, quantity, productPo.getVersion());

            // 使用乐观锁，当版本号发生改变则取消操作，但直接取消则会出现请求丢失的问题，可引入重入机制来解决，即一旦更新失败就重做一次，所以乐观锁也称为可重入的锁
            // 如果更新失败，说明数据被其他线程修改导致失败，则通过重入尝试购买商品
            if (result == 0) {
//                return false;
                continue;
            }

            // 初始化购买记录
            PurchaseRecordPo purchaseRecordPo = this.initPurchaseRecord(userId, productPo, quantity);

            purchaseRecordMapper.insertPurchaseRecord(purchaseRecordPo);

            return true;
        }


    }

    // 使用redis进行抢购处理提高性能
    @Override
    public boolean purchaseRedis(Long userId, Long productId, Integer quantity) {
        // 购买时间
        Long purchaseDate = System.currentTimeMillis();

        Jedis jedis = null;

        try {
            // 获取原始连接
            jedis = (Jedis) stringRedisTemplate.getConnectionFactory().getConnection().getNativeConnection();

            // 如果没有加载过，则先将脚本加载到Redis服务器，让其返回sha1
            if (sha1 == null) {
                sha1 = jedis.scriptLoad(purchaseScript);
            }

            // 执行脚本，返回结果
            Object res = jedis.evalsha(sha1, 2, PRODUCT_SCHEDULE_SET, PURCHASE_PRODUCT_LIST, userId + "", productId + "", quantity + "", purchaseDate + "");

            Long result = (Long) res;

            return result == 1;
        }finally {
            // 关闭jedis连接
            if (jedis != null && jedis.isConnected()) {
                jedis.close();
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public boolean dealRedisPurchase(List<PurchaseRecordPo> purchaseRecordPos) {
        for (PurchaseRecordPo purchaseRecordPo : purchaseRecordPos) {
            purchaseRecordMapper.insertPurchaseRecord(purchaseRecordPo);
            productMapper.decreaseProductRedis(purchaseRecordPo.getProductId(), purchaseRecordPo.getQuantity());
        }
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
