package org.lql.task.impl;

import org.lql.domain.PurchaseRecordPo;
import org.lql.service.PurchaseService;
import org.lql.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Title: TaskServiceImpl <br>
 * ProjectName: spring-boot-panic-buying <br>
 * description: TODO <br>
 *
 * @author: leiql <br>
 * @version: 1.0 <br>
 * @since: 2021/6/16 14:53 <br>
 */
@Service
public class TaskServiceImpl implements TaskService {

    // redis购买记录集合前缀
    private static final String PURCHASE_PRODUCT_LIST = "purchase_list_";

    // 抢购商品集合
    private static final String PRODUCT_SCHEDULE_SET = "product_schedule_set";

    // 每次取出1000条，避免一次取出消耗太多内存
    private static final int ONCE_TIME_SIZE = 1000;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PurchaseService purchaseService;

    @Override
    // 每天凌晨一点执行
//    @Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(fixedRate = 1000 * 60)
    public void purchaseTask() {
        System.out.println("定时任务开始.................");
        Set<String> productIdList = stringRedisTemplate.opsForSet().members(PRODUCT_SCHEDULE_SET);

        List<PurchaseRecordPo> purchaseRecordPoList = new ArrayList<>();

        for (String productIdStr : productIdList) {
            Long productId = Long.parseLong(productIdStr);
            String purchaseKey = PURCHASE_PRODUCT_LIST + productId;
            BoundListOperations<String, String> ops = stringRedisTemplate.boundListOps(purchaseKey);
            // 计算记录数
            long size = ops.size();
            Long times = size % ONCE_TIME_SIZE == 0 ? size / ONCE_TIME_SIZE : size / ONCE_TIME_SIZE + 1;
            for (int i = 0; i < times; i++) {
                // 获取至多ONCE_TIME_SIZE个抢购记录
                List<String> prList = null;

                if (i == 0) {
                    prList = ops.range(i * ONCE_TIME_SIZE, (i + 1) * ONCE_TIME_SIZE);
                }else {
                    prList = ops.range(i * ONCE_TIME_SIZE + 1, (i + 1) * ONCE_TIME_SIZE);
                }

                for (String prStr : prList) {
                    PurchaseRecordPo prp = this.createPurchaseRecord(productId, prStr);
                    purchaseRecordPoList.add(prp);
                }

                try {
                    // 该方法采用新建事务的方式，不会导致全局事务回滚
                    purchaseService.dealRedisPurchase(purchaseRecordPoList);
                }catch (Exception e) {
                    e.printStackTrace();
                }

                // 清除列表，等待重新写入数据
                purchaseRecordPoList.clear();
            }

            // 删除购买列表
            stringRedisTemplate.delete(purchaseKey);

            // 从商品集合中删除商品
            stringRedisTemplate.opsForSet().remove(PRODUCT_SCHEDULE_SET, productIdStr);
        }

        System.out.println("定时任务结束.............");
    }

    private PurchaseRecordPo createPurchaseRecord(Long productId, String prStr) {
        String[] arr = prStr.split(",");
        Long userId = Long.parseLong(arr[0]);
        Integer quantity = Integer.parseInt(arr[1]);
        Double sum = Double.parseDouble(arr[2]);
        Double price = Double.parseDouble(arr[3]);
        Long time = Long.parseLong(arr[4]);
        Timestamp purchaseTime = new Timestamp(time);

        PurchaseRecordPo purchaseRecordPo = new PurchaseRecordPo();
        purchaseRecordPo.setUserId(userId);
        purchaseRecordPo.setQuantity(quantity);
        purchaseRecordPo.setSum(sum);
        purchaseRecordPo.setPrice(price);
        purchaseRecordPo.setProductId(productId);
        purchaseRecordPo.setPurchaseTime(purchaseTime);
        purchaseRecordPo.setNote("购买日志，时间：" + purchaseTime.getTime());

        return purchaseRecordPo;
    }
}
