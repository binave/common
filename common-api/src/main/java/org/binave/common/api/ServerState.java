package org.binave.common.api;

import java.util.List;

/**
 * @author by bin jin on 2019/08/30 15:23.
 */
public interface ServerState {

    String name();

    double version();

    String description();

    String type(); // 服务类型

    String location(); // 位置：区域.机房.[host:port]

    String runStage(); // 运行阶段

    String lastRunStage(); // 上一个运行阶段

    int concurrent();

    int maxConcurrent(); // 历史最大并发数

    double costMillis();

    double maxCostMillis(); // 最大开销（毫秒）

    List<String> interfaces(); // 接口列表

}
