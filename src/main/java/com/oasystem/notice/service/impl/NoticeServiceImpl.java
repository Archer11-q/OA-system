package com.oasystem.notice.service.impl;

import com.oasystem.notice.entity.Notice;
import com.oasystem.notice.mapper.NoticeMapper;
import com.oasystem.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public List<Notice> listAll() {
        return noticeMapper.selectList(null);
    }

    @Override
    public Notice getById(Long id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public void createNotice(Notice notice) {
        noticeMapper.insert(notice);
    }

    @Override
    public void updateNotice(Notice notice) {
        noticeMapper.updateById(notice);
    }

    @Override
    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
    }
}

