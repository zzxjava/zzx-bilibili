package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.AuthRoleDao;
import com.imooc.bilibili.domain.auth.AuthRole;
import com.imooc.bilibili.domain.auth.AuthRoleElementOperation;
import com.imooc.bilibili.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 角色权限服务类，提供了
 * 获取角色按钮操作权限
 * 和菜单权限的方法，
 * 以及根据角色代码获取角色信息的方法。
 */
@Service
public class AuthRoleService {

    @Autowired
    private AuthRoleDao authRoleDao;

    /*查询操作权限*/
    @Autowired
    private AuthRoleElementOperationService authRoleElementOperationService;

    /*查询页面关联权限*/
    @Autowired
    private AuthRoleMenuService authRoleMenuService;

    /**
     * 获取角色按钮操作权限
     *
     * @param roleIdSet
     * @return
     */
    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationsByRoleIds(roleIdSet);
    }

    /**
     * 获取菜单权限的方法，
     */
    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuService.getAuthRoleMenusByRoleIds(roleIdSet);
    }

    public AuthRole getRoleByCode(String code) {
        return authRoleDao.getRoleByCode(code);
    }
}
