package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.MajorDict;
import java.util.List;

public interface MajorDictService {
    MajorDict getById(Long id);
    List<MajorDict> list(String majorName);
    boolean save(MajorDict dict);
    boolean update(MajorDict dict);
    boolean delete(Long id);

    /** 上下线 */
    boolean updateOnlineStatus(Long id, Integer status);
}
