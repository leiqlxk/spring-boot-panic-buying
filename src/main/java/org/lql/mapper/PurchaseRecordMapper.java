package org.lql.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.lql.domain.PurchaseRecordPo;

/**
 * Title: PurchaseRecordMapper <br>
 * ProjectName: spring-boot-panic-buying <br>
 * description: TODO <br>
 *
 * @author: leiql <br>
 * @version: 1.0 <br>
 * @since: 2021/6/15 15:54 <br>
 */
@Mapper
public interface PurchaseRecordMapper {

    int insertPurchaseRecord(PurchaseRecordPo purchaseRecordPo);
}
