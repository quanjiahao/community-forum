package com.coral.community.service;

import com.coral.community.dao.DiscussPostMapper;
import com.coral.community.entity.DiscussPost;
import com.coral.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }
    public int finDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public  int addDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw  new IllegalArgumentException("parameter cant be null");
        }
        //escape character
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // filter banned word
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        return discussPostMapper.insertDiscussPost(discussPost);
    }


}
