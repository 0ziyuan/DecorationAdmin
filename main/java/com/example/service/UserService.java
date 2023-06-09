package com.example.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Permission;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionService permissionService;

    public User login(User user) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()).eq(User::getPassword, user.getPassword());
        User one = getOne(queryWrapper);
        if (one == null) {
            throw new CustomException("-1", "账号或密码错误");
        }
        one.setPermission(getPermissions(one.getId()));
        List<Permission> permissions = getPermissions(one.getId());
        List<Permission> permissions1= new ArrayList<>();
        List<Permission> permissions2= new ArrayList<>();
        List<Permission> permissions3= new ArrayList<>();
        List<Permission> permissions4= new ArrayList<>();
        List<Permission> permissions5= new ArrayList<>();
        List<Permission> permissions0= new ArrayList<>();


        if(permissions != null)
        {
            for (Permission permission : permissions)
            {
                if(permission!=null )
                {
                if (permission.getId() < 6)
                    permissions0.add(permission);
                else if (permission.getId() < 15)
                    permissions1.add(permission);
                else if (permission.getId() < 30)
                    permissions2.add(permission);
                else if (permission.getId() < 50)
                    permissions3.add(permission);
                else if (permission.getId() < 70)
                    permissions4.add(permission);
                else
                    permissions5.add(permission);
                }
            }

            one.setPermission0(permissions0);
            one.setPermission1(permissions1);
            one.setPermission2(permissions2);
            one.setPermission3(permissions3);
            one.setPermission4(permissions4);
            one.setPermission5(permissions5);
        }

        return one;

    }

    public User register(User user) {
        User one = getOne((Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername())));
        if (one != null) {
            throw new CustomException("-1", "用户已注册");
        }
        if (user.getPassword() == null) {
            user.setPassword("123456");
        }
        user.setRole(CollUtil.newArrayList(2L));  // 默认普通用户角色
        save(user);
        return getOne((Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername())));
    }

    public User reset(User user){
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername());
        User one = getOne(queryWrapper);
        if (one== null){
            throw new CustomException("-1", "用户不存在");
        }
        LambdaQueryWrapper<User> queryWrapper1 = Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()).eq(User::getQuestion, user.getQuestion()).eq(User::getAnswers,user.getAnswers());
        one = getOne(queryWrapper1);
        if (one== null) {
            throw new CustomException("-1", "密保问题或答案错误");
        }
        update(user,queryWrapper1);
        return one;
    }

    public User changePassword(User user){
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()).eq(User::getPassword,user.getPassword());
        User one = getOne(queryWrapper);
        System.out.println(one);
        if (one==null){
            throw new CustomException("-1","密码错误");
        }
        if (user.getNewPassword()==null){
            System.out.println(user);
            throw new CustomException("-1","请输入新密码");
        }
        user.setPassword(user.getNewPassword());
        user.setNewPassword("");
        update(user,queryWrapper);
        return one;
    }


    /**
     * 设置权限
     * @param userId
     * @return
     */
    public List<Permission> getPermissions(Long userId) {
        User user = getById(userId);
        List<Permission> permissions = new ArrayList<>();
        List<Long> role = user.getRole();
        if (role != null) {
            for (Object roleId : role) {
                Role realRole = roleService.getById((int) roleId);
                if (CollUtil.isNotEmpty(realRole.getPermission())) {
                    for (Object permissionId : realRole.getPermission()) {
                        Permission permission = permissionService.getById((int) permissionId);
                        if (permission != null && permissions.stream().noneMatch(p -> p.getPath().equals(permission.getPath()))) {
                            permissions.add(permission);
                        }
                    }
                }
            }
            user.setPermission(permissions);
        }
        return permissions;
    }

    public User getbyUsername(String username) {
        User one = getOne((Wrappers.<User>lambdaQuery().eq(User::getUsername, username)));
        one.setPermission(getPermissions(one.getId()));
        one.setPassword("");
        List<Permission> permissions = getPermissions(one.getId());
        List<Permission> permissions1= new ArrayList<>();
        List<Permission> permissions2= new ArrayList<>();
        List<Permission> permissions3= new ArrayList<>();
        List<Permission> permissions4= new ArrayList<>();
        List<Permission> permissions5= new ArrayList<>();
        List<Permission> permissions0= new ArrayList<>();


        if(permissions != null)
        {
            for (Permission permission : permissions)
            {
                if(permission!=null )
                {
                    if (permission.getId() < 6)
                        permissions0.add(permission);
                    else if (permission.getId() < 15)
                        permissions1.add(permission);
                    else if (permission.getId() < 30)
                        permissions2.add(permission);
                    else if (permission.getId() < 50)
                        permissions3.add(permission);
                    else if (permission.getId() < 70)
                        permissions4.add(permission);
                    else
                        permissions5.add(permission);
                }
            }

            one.setPermission0(permissions0);
            one.setPermission1(permissions1);
            one.setPermission2(permissions2);
            one.setPermission3(permissions3);
            one.setPermission4(permissions4);
            one.setPermission5(permissions5);
        }
        return one;
    }
}
