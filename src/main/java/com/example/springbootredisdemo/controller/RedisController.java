package com.example.springbootredisdemo.controller;

import com.example.springbootredisdemo.utils.RedisConstants;
import com.example.springbootredisdemo.utils.RedisUtils;
import com.example.springbootredisdemo.utils.StateParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @version 0.0.1
 * @program: springbootredisdemo
 * @description: 控制层进行测试
 * @packname: com.example.springbootredisdemo.controller
 * @author: wzp
 * @create: 2019-11-05 15:33
 */
@Controller
@RequestMapping(value = "/redis")
public class RedisController extends BaseController {
    public  static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    RedisUtils redisUtils;
    @RequestMapping(value = "getRedis",method = RequestMethod.POST)
    @ResponseBody
    public ModelMap getRedis(){
        redisUtils.set("20182018","这是一条测试数据", RedisConstants.datebase1);
        Long resExpire = redisUtils.expire("20182018", 60, RedisConstants.datebase1);//设置key过期时间
        logger.info("resExpire="+resExpire);
        String res = redisUtils.get("20182018", RedisConstants.datebase1);
        return getModelMap(StateParameter.SUCCESS, res, "执行成功");
    }

    private ModelMap getModelMap(Object success, String res, String 执行成功) {
        ModelMap modelMap = new ModelMap();
        modelMap.addAttribute(success);
        modelMap.addAttribute(res);
        return modelMap;
    }

}
