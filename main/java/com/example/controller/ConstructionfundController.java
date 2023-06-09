package com.example.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.Constructionfund;
import com.example.service.ConstructionfundService;
import com.example.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import com.example.exception.CustomException;
import cn.hutool.core.util.StrUtil;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/constructionfund")
public class ConstructionfundController {
    @Resource
    private ConstructionfundService constructionfundService;
    @Resource
    private HttpServletRequest request;

    public User getUser() {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomException("-1", "请登录");
        }
        return user;
    }

    @PostMapping
    public Result<?> save(@RequestBody Constructionfund constructionfund) {
        return Result.success(constructionfundService.save(constructionfund));
    }

    @PutMapping
    public Result<?> update(@RequestBody Constructionfund constructionfund) {
        return Result.success(constructionfundService.updateById(constructionfund));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        constructionfundService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable Long id) {
        return Result.success(constructionfundService.getById(id));
    }

    @GetMapping
    public Result<?> findAll() {
        return Result.success(constructionfundService.list());
    }

    @GetMapping("/page")
    public Result<?> findPage(@RequestParam(required = false, defaultValue = "") String name,
                                                @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<Constructionfund> query = Wrappers.<Constructionfund>lambdaQuery().orderByDesc(Constructionfund::getId);
        if (StrUtil.isNotBlank(name)) {
            query.like(Constructionfund::getName, name);
        }
        return Result.success(constructionfundService.page(new Page<>(pageNum, pageSize), query));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {

        List<Map<String, Object>> list = CollUtil.newArrayList();

        List<Constructionfund> all = constructionfundService.list();
        for (Constructionfund obj : all) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("工程款id", obj.getId());
            row.put("日期", obj.getDate());
            row.put("项目代码", obj.getProjectid());
            row.put("增减金额", obj.getMoney());
            row.put("增减内容", obj.getContent());
            row.put("备用", obj.getName());

            list.add(row);
        }

        // 2. 写excel
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(list, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("工程款信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(System.out);
    }

    @GetMapping("/upload/{fileId}")
    public Result<?> upload(@PathVariable String fileId) {
        String basePath = System.getProperty("user.dir") + "/src/main/resources/static/file/";
        List<String> fileNames = FileUtil.listFileNames(basePath);
        String file = fileNames.stream().filter(name -> name.contains(fileId)).findAny().orElse("");
        List<List<Object>> lists = ExcelUtil.getReader(basePath + file).read(1);
        List<Constructionfund> saveList = new ArrayList<>();
        for (List<Object> row : lists) {
            Constructionfund obj = new Constructionfund();
            obj.setDate((String) row.get(1));
            obj.setProjectid(Integer.valueOf((String) row.get(2)));
            obj.setMoney(Integer.valueOf((String) row.get(3)));
            obj.setContent((String) row.get(4));
            obj.setName((String) row.get(5));

            saveList.add(obj);
        }
        constructionfundService.saveBatch(saveList);
        return Result.success();
    }

}
