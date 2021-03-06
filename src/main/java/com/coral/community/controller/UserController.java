package com.coral.community.controller;

import com.coral.community.annotation.LoginRequired;
import com.coral.community.entity.User;
import com.coral.community.service.FollowService;
import com.coral.community.service.LikeService;
import com.coral.community.service.UserService;
import com.coral.community.util.CommunityConstant;
import com.coral.community.util.CommunityUtil;
import com.coral.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;


@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static  final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private  String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public  String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHead(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","you have not select picture");
            return "/site/setting";
        }
        // file name cant be the same , the postfix need to be the same
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));  // get substring from the last .
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","file format is incorrect");
            return "/site/setting";
        }
        //generate random filename
        String fileName = CommunityUtil.generateUUID() + suffix;
        // confirm the file saving directory
        File dest = new File(uploadPath+"/"+fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("upload file fail"+ e.getMessage());
            throw new RuntimeException("upload file fail,server exception",e);
        }
        // update user headerUrl(web http)
        //http://localhost:8080/community/user/Header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    // get image
    @RequestMapping(path = "/header/{fileName}" ,method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName , HttpServletResponse response){
        // get header from sever saving directory
        fileName = uploadPath + "/" + fileName;
        // file suffix
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //response the picture(Header)
        response.setContentType("image/"+ suffix);
        try( FileInputStream fis = new FileInputStream(fileName);
             ServletOutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int b =0;
            while((b =fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("read Header fail" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public String updatePassword(String oldPassword,String newPassword,
                                 String rePassword,Model model,@CookieValue("ticket") String ticket){
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword, rePassword);
        if(map==null|| map.isEmpty()){
            userService.logout(ticket);
            return "redirect:/login";
        }else{
            model.addAttribute("olderror",map.get("olderror"));
            model.addAttribute("newerror",map.get("newerror"));
            model.addAttribute("reerror",map.get("reerror"));
            return "/site/setting";
        }
    }
    //personal profile
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public  String getProfilePage(@PathVariable("userId") int userId , Model model){
        User user = userService.findUserByID(userId);
        if(user ==null){
            throw new RuntimeException(" user doesnt exist");
        }
        // user info
        model.addAttribute("user",user);
        // like count
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //followee count
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //follower count
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        // do i follow this user
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

}
