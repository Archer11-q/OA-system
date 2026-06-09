package com.oasystem.notice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告/通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class Notice extends BaseEntity {

    /** 公告标题 */
    private String title;

    /** 公告内容（富文本HTML） */
    private String content;

    /** 公告类型（1=通知，2=公告，3=制度） */
    private Integer noticeType;

    /** 发布状态（0=草稿，1=已发布） */
    private Integer status;

    /** 是否置顶（0=否，1=是） */
    private Integer isTop;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 发布人ID */
    private Long publisherId;

    /** 查看次数 */
    private Integer viewCount;
}
