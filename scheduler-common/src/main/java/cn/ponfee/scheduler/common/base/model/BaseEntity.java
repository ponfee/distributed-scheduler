/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.common.base.model;

import cn.ponfee.scheduler.common.base.ToJsonString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Base entity.
 * 
 * @author Ponfee
 */
@Getter
@Setter
public abstract class BaseEntity extends ToJsonString implements java.io.Serializable {

    private static final long serialVersionUID = -7150065349727498445L;

    /**
     * 自增主键ID
     */
    private Long         id;

    /**
     * 行记录版本号
     */
    private Integer version;

    /**
     * 更新时间
     */
    private Date  updatedAt;

    /**
     * 创建时间
     */
    private Date  createdAt;

}
