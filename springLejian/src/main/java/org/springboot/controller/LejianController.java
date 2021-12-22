package org.springboot.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.io.FileUtils;
import org.springboot.common.Result;
import org.springboot.entity.User;
import org.springboot.mapper.UserMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
@RequestMapping("/lejian")
public class LejianController {

    @Resource
    UserMapper userMapper;

    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) throws Exception {

        //白名单
        User white = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()));

        if (white == null) {
            return Result.error("-1", "您不在白名单内，请联系作者");
        }

        //请求登录
        String loginUrl = "https://cpes.legym.cn/authorization/user/manage/login";

        JSONObject data = new JSONObject();
        data.put("entrance", 1);
        data.put("userName", user.getUsername());
        data.put("password", user.getPassword());

        String post = HttpUtil.createPost(loginUrl).contentType("application/json").body(String.valueOf(data)).execute().body();
        JSONObject js = JSONUtil.parseObj(post);

        if ((int) js.get("code") != 0) {
            return Result.error("-1", "用户名或密码错误");
        }

        JSONObject dat = JSONUtil.parseObj(js.get("data"));
        user.setRealname((String) dat.get("realName"));
        if (white.getRealname() == null) {
            userMapper.update(user, Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()));
        }

        //打印一下
        System.out.println(user.getRealname());

        //请求跑步
        String accessToken = (String) dat.get("accessToken");
        String runUrl = "https://cpes.legym.cn/running/app/uploadRunningDetails";
        ClassPathResource resource;
        // 区分南充校区和成都校区读取不同的json文件
        String school = (String) dat.get("schoolName");
        if ("西南石油大学".equals(school)) {
            resource = new ClassPathResource("json/fake.json");
        } else {
            resource = new ClassPathResource("json/fake2.json");
        }

        //可以优化
        InputStream inputStream = resource.getInputStream();
        File somethingFile = File.createTempFile("fake", ".json");
        FileUtils.copyInputStreamToFile(inputStream, somethingFile);
        String result = FileUtils.readFileToString(somethingFile);
        JSONObject res = JSONUtil.parseObj(result);
        FileUtil.del(somethingFile);

        Date now = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //二十分钟
        long time = 20 * 60 * 1000;
        Date beforeDate = new Date(now.getTime() - time);

        res.put("paceNumber", 402 * 3);
        res.put("effectiveMileage", 3);
        res.put("gpsMileage", 3);
        res.put("totalMileage", 3);
        res.put("calorie", 180 * 3);
        res.put("startTime", ft.format(beforeDate));
        res.put("endTime", ft.format(now));

        post = HttpUtil.createPost(runUrl).contentType("application/json").bearerAuth(accessToken).body(String.valueOf(res)).execute().body();

//        打印返回结果
//        js = JSONUtil.parseObj(post);
//        System.out.println(js);
        return Result.success();
    }
}
