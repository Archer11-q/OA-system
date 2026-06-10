package com.oasystem.notice.service;

import com.oasystem.notice.entity.Notice;

import java.util.List;

public interface NoticeService {
    List<Notice> listAll();

    Notice getById(Long id);

    void createNotice(Notice notice);

    void updateNotice(Notice notice);

    void deleteNotice(Long id);
}

