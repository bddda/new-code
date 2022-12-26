package oj.service;

import com.github.pagehelper.PageInfo;
import oj.pojo.Problem;

import java.util.List;

public interface PageService {
    List<Problem> findSysMenus(Integer page, Integer size);
}
