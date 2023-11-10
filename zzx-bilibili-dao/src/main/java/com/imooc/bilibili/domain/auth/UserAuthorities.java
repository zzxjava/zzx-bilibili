package com.imooc.bilibili.domain.auth;

import java.util.List;

/**
 * 存储用户拥有的权限
 * 目前是操作级权限和页面菜单权限，如果有新的权限可以自己再添加
 */
public class UserAuthorities {

    List<AuthRoleElementOperation> roleElementOperationList;//操作级的权限

    List<AuthRoleMenu> roleMenuList;//页面菜单相关的权限

    public List<AuthRoleElementOperation> getRoleElementOperationList() {
        return roleElementOperationList;
    }

    public void setRoleElementOperationList(List<AuthRoleElementOperation> roleElementOperationList) {
        this.roleElementOperationList = roleElementOperationList;
    }

    public List<AuthRoleMenu> getRoleMenuList() {
        return roleMenuList;
    }

    public void setRoleMenuList(List<AuthRoleMenu> roleMenuList) {
        this.roleMenuList = roleMenuList;
    }
}
