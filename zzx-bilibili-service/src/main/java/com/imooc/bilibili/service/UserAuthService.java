package com.imooc.bilibili.service;

import com.imooc.bilibili.domain.auth.*;
import com.imooc.bilibili.domain.constant.AuthRoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {

    /*用户角色关联*/
    @Autowired
    private UserRoleService userRoleService;

    /*角色权限关联*/
    @Autowired
    private AuthRoleService authRoleService;

    /**
     * 根据用户ID获取用户权限
     *
     * @param userId
     * @return 根据用户ID获取用户权限，首先通过userRoleService获取用户的角色，
     * 然后根据角色ID获取角色的按钮操作权限和菜单权限，
     * 最后将获取的信息封装到UserAuthorities对象中返回。
     */
    public UserAuthorities getUserAuthorities(Long userId) {
        //先根据用户ID获取用户角色
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //上面已经取出userRoleList用户角色信息，所以获取用户ID不需要再次访问数据库查询，
        /*采用stream方法只需要操作内存中的数据，效率高，而且不存在网络问题，更加可靠*/
        /*获取角色ID列表*/
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());

        //查找按钮操作权限和页面操作权限
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);
        List<AuthRoleMenu> authRoleMenuList = authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);

        //创建一个用户权限对象，去设置用户目前的权限
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        //将用户权限存储再userAuthorities对象里面，然后返回该对象
        return userAuthorities;
    }

    public void addUserDefaultRole(Long id) {
        UserRole userRole = new UserRole();
        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
        userRole.setUserId(id);
        userRole.setRoleId(role.getId());
        userRoleService.addUserRole(userRole);
    }
}
