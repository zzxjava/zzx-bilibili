package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.UserFollowingDao;
import com.imooc.bilibili.domain.FollowingGroup;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserFollowing;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.constant.UserConstant;
import com.imooc.bilibili.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {

    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;

    /*添加用户分组
     * 先看一下关注的用户是否存在，看看他的关注分组id
     *如果未null，就设置到默认分组,如果不为null,就查询对应的分组id,然后设置给关注表中的关注分组id
     *
     * */
    /*@Transactional  添加事务的处理*/
    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();//获取用户关注的分组id

        if (groupId == null) {//如果用户分组id为null.所以没有选择分组，系统默认分配默认分组
            //根据默认分组，查询出默认分组的信息
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            //将默认分组的id,存入到用户关注表中的，关注分组id
            userFollowing.setGroupId(followingGroup.getId());
        } else {
            //说明他此时是有分组的，直接获取当前分组的id
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if (followingGroup == null) {
                throw new ConditionException("关注分组不存在！");
            }
        }


        Long followingId = userFollowing.getFollowingId();//获取关注用户的id
        User user = userService.getUserById(followingId);//根据关注用户id,查询该用户是否存在
        if (user == null) {
            throw new ConditionException("关注的用户不存在！");
        }
        //这条代码的作用就是，检查之前该用户是否被关注过，如果关注过，就把之前的关注删除，再重新关注一次
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), followingId);
        //把用户关注信息传进来存到数据库中
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
    }


    /**
     * 获取用户的关注列表
     *1、获取关注的用户列表
     * 2、根据用户关注的id查询关注用户的基本信息
     * 3、将关注用户按关注分组进行分类
     * @param userId：传入的参数是用户的id
     * @return
     */
    public List<FollowingGroup> getUserFollowings(Long userId) {
        List<UserFollowing> list = userFollowingDao.getUserFollowings(userId);//拿着当前用户id，直接去数据库查和当前用户id相同的关注列表

        /*这是一个使用Java 8 Stream API的操作，它的作用是从列表中映射出userFollowing对象的followingId属性，然后将其存入一个Set集合中。*/
        //从list列表里面映射出关注用户的id，简单来说就是取出集合中的followingId，存入到集合中
        Set<Long> followingIdSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());

        List<UserInfo> userInfoList = new ArrayList<>();
        if (followingIdSet.size() > 0) {
            /*通过映射出来的关注用户id，查询对应的用户信息，主要是想看看，该用户是否也关注了我，相当于看看他是不是当前用户的粉丝*/
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }

        /*两个列表遍历比较*/
        for (UserFollowing userFollowing : list) {
            for (UserInfo userInfo : userInfoList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userFollowing.setUserInfo(userInfo);//将用户信息关联到用户分组中，因为用户关注分组中没有保存用户ID，需要遍历对比，如果一样，就把用户信息插入
                }
            }
        }

        //根据用户ID，把用户关注的所以分组列表全部查出来。
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);
        List<FollowingGroup> result = new ArrayList<>();
        result.add(allGroup);

        for (FollowingGroup group : groupList) {
            List<UserInfo> infoList = new ArrayList<>();
            for (UserFollowing userFollowing : list) {
                if (group.getId().equals(userFollowing.getGroupId())) {
                    infoList.add(userFollowing.getUserInfo());
                }

            }
            group.setFollowingUserInfoList(infoList);
            result.add(group);
        }
        return result;
    }

    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);//用户自定义分组
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }


    // 第一步：获取当前用户的粉丝列表
    // 第二步：根据粉丝的用户id查询基本信息
    // 第三步：查询当前用户是否已经关注该粉丝,(互粉)
    public List<UserFollowing> getUserFans(Long userId) {
        List<UserFollowing> fanList = userFollowingDao.getUserFans(userId);
        /*把粉丝的id都抽取出来*/
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());

        List<UserInfo> userInfoList = new ArrayList<>();
        if (fanIdSet.size() > 0) {
            /*通过粉丝的id获取粉丝的信息*/
            userInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }

        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId);
        for (UserFollowing fan : fanList) {
            for (UserInfo userInfo : userInfoList) {
                if (fan.getUserId().equals(userInfo.getUserId())) {
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            for (UserFollowing following : followingList) {
                if (following.getFollowingId().equals(fan.getUserId())) {
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fanList;
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    /**
     *
     * @param userInfoList:这个是通过分页查询出来的当前用户的关注者的详细信息
     * @param userId
     * @return
     */
    public List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId) {
        //查询一下当前用户关注的列表
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);
        for (UserInfo userInfo : userInfoList) {
            userInfo.setFollowed(false);
            for (UserFollowing userFollowing : userFollowingList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userInfo.setFollowed(true);//说明是互粉状态
                }
            }
        }
        return userInfoList;
    }
}
