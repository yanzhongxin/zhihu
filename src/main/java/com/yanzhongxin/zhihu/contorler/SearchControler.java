package com.yanzhongxin.zhihu.contorler;

import com.yanzhongxin.zhihu.model.EntityType;
import com.yanzhongxin.zhihu.model.Question;
import com.yanzhongxin.zhihu.model.ViewObject;
import com.yanzhongxin.zhihu.service.FollowService;
import com.yanzhongxin.zhihu.service.QuestionService;
import com.yanzhongxin.zhihu.service.SearchService;
import com.yanzhongxin.zhihu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/2/2 20:24
 */
@Controller
public class SearchControler {

    @Autowired
    SearchService searchService;
    @Autowired
    QuestionService questionService;
    @Autowired
    FollowService followService;
    @Autowired
    UserService userService;
    @RequestMapping(value = "search",method = RequestMethod.GET)
    public String search(Model model, @RequestParam("q")String keyword,
                         @RequestParam(value = "offset",defaultValue = "0") int offset,
                         @RequestParam(value = "count",defaultValue = "10") int count){
        try {
            List<Question> list = searchService.searchQuestion(keyword, offset, count, "<em>", "</em>");
            //这个question list中只有id title content没有其他的内容，需要根据id查询
            //剩余的Question属性
            List<ViewObject> vos=new ArrayList<>();
            for (Question question:list){
                Question querstionByid = questionService.getQuerstionByid(question.getId());
                ViewObject vo=new ViewObject();
                if (question.getContent()!=null){
                    querstionByid.setContent(question.getContent());
                }
                if (question.getTitle()!=null){
                    querstionByid.setTitle(question.getTitle());
                }
                vo.set("question",querstionByid);//把每个问题设置到一行vo对象中
                vo.set("followCount",followService.getFolloerCount(EntityType.Question,querstionByid.getId()));
                vo.set("user", userService.getUserById(querstionByid.getUserId()));
                vos.add(vo);
            }
            model.addAttribute("vos",vos);
            model.addAttribute("keyword",keyword);//设置我们前端接收到的关键词，用户回显

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("评论失败");
        }
        return "result";
    }
}
