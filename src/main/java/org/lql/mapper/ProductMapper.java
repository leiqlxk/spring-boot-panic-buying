package org.lql.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lql.domain.ProductPo;

/**
 * Title: ProductMapper <br>
 * ProjectName: spring-boot-panic-buying <br>
 * description: TODO <br>
 *
 * @author: leiql <br>
 * @version: 1.0 <br>
 * @since: 2021/6/15 15:47 <br>
 */
@Mapper
public interface ProductMapper {

    ProductPo getProduct(Long id);

    int decreaseProduct(@Param("id") Long id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    int decreaseProductRedis(@Param("id") Long id, @Param("quantity") Integer quantity);
}
