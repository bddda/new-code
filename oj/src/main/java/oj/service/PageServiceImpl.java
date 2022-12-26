package oj.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import oj.mapper.ProblemMapper;
import oj.pojo.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private ProblemMapper problemMapper;
    @Override
    public List<Problem> findSysMenus(Integer page, Integer size) {
        PageHelper.startPage(page,size);
        return problemMapper.selectAll();
    }
}
