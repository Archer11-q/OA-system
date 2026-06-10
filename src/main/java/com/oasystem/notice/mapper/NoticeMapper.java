package com.oasystem.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}

