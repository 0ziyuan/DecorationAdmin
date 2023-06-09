package com.example.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.Materialpurchase;
import com.example.service.MaterialpurchaseService;
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
@RequestMapping("/api/materialpurchase")
public class MaterialpurchaseController {
    @Resource
    private MaterialpurchaseService materialpurchaseService;
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
    public Result<?> save(@RequestBody Materialpurchase materialpurchase) {
        return Result.success(materialpurchaseService.save(materialpurchase));
    }

    @PutMapping
    public Result<?> update(@RequestBody Materialpurchase materialpurchase) {
        return Result.success(materialpurchaseService.updateById(materialpurchase));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        materialpurchaseService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable Long id) {
        return Result.success(materialpurchaseService.getById(id));
    }

    @GetMapping
    public Result<?> findAll() {
        return Result.success(materialpurchaseService.list());
    }

    @GetMapping("/page")
    public Result<?> findPage(@RequestParam(required = false, defaultValue = "") String name,
                                                @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<Materialpurchase> query = Wrappers.<Materialpurchase>lambdaQuery().orderByDesc(Materialpurchase::getId);
        if (StrUtil.isNotBlank(name)) {
            query.like(Materialpurchase::getName, name);
        }
        return Result.success(materialpurchaseService.page(new Page<>(pageNum, pageSize), query));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {

        List<Map<String, Object>> list = CollUtil.newArrayList();

        List<Materialpurchase> all = materialpurchaseService.list();
        for (Materialpurchase obj : all) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("材料单号", obj.getId());
            row.put("材料价格", obj.getMoney());
            row.put("材料名称", obj.getName());
            row.put("项目id", obj.getProjectid());
            row.put("供应商id", obj.getSupplierid());

            list.add(row);
        }

        // 2. 写excel
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(list, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("材料采购信息", "UTF-8");
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
        List<Materialpurchase> saveList = new ArrayList<>();
        for (List<Object> row : lists) {
            Materialpurchase obj = new Materialpurchase();
            obj.setMoney(Integer.valueOf((String) row.get(1)));
            obj.setName((String) row.get(2));
            obj.setProjectid(Integer.valueOf((String) row.get(3)));
            obj.setSupplierid(Integer.valueOf((String) row.get(4)));

            saveList.add(obj);
        }
        materialpurchaseService.saveBatch(saveList);
        return Result.success();
    }

}
