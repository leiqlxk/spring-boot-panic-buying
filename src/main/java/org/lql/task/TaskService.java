package org.lql.task;

/**
 * Title: TaskService <br>
 * ProjectName: spring-boot-panic-buying <br>
 * description: 定时任务执行redis到数据库操作 <br>
 *
 * @author: leiql <br>
 * @version: 1.0 <br>
 * @since: 2021/6/16 14:52 <br>
 */
public interface TaskService {

    void purchaseTask();
}
