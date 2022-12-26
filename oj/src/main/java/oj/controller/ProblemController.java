package oj.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import oj.exception.ProblemNotFountException;
import oj.mapper.ProblemMapper;
import oj.pojo.Problem;
import oj.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/oj")
public class ProblemController extends BaseController{
    @Autowired
    public ProblemMapper problemMapper;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public PageService pageService;

    @RequestMapping(value = "/problems",produces = "application/json;charset=utf-8")
    //value ：参数名 required ：是否包含该参数，默认为true，表示该请求路径中必须包含该参数，如果不包含就报错。
    public String getProblems(@RequestParam(value = "id",required = false) String id) throws JsonProcessingException, ProblemNotFountException {
        //如果没有参数则直接查询全部题目
        if (id == null || "".equals(id)){
            List<Problem> list = problemMapper.selectAll();
            //前端接受的是json格式的数据,所以要把list转化为json格式的字符串
            return objectMapper.writeValueAsString(list);
        }
        //如果有参数则查询指定id的题目详情
        int idString = Integer.parseInt(id);
        Problem problem = problemMapper.selectOne(idString);
        if (problem == null){
            throw new ProblemNotFountException();
        }
        return objectMapper.writeValueAsString(problem);
    }


    //查询操作
    @RequestMapping("/listMenu")
    public ModelAndView listMenu(@RequestParam(required = true,name = "page",defaultValue = "1")Integer page,
                                 @RequestParam(name = "size",defaultValue = "10") Integer size) {
        ModelAndView model = new ModelAndView();
        List<Problem> sysMenus = pageService.findSysMenus(page,size);

        PageInfo pageInfo = new PageInfo(sysMenus);
        pageInfo.setPageSize(size);
        model.addObject("pageInfo", pageInfo);
        model.setViewName("/sys/menu");
        return model;
    }

}
