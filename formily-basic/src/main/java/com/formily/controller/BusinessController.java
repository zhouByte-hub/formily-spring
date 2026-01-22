package com.formily.controller;

import com.formily.model.OptionItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = {"/business", "/api/business"})
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BusinessController {

    private static final List<OptionItem> BUSINESS_OPTIONS = List.of(
            new OptionItem("低代码/无代码表单构建", "asset_a"),
            new OptionItem("企业数据收集", "asset_b"),
            new OptionItem("流程自动化", "asset_c")
    );

    /**
     * 关联资产下拉框选项
     */
    @GetMapping("/list")
    public ResponseEntity<List<OptionItem>> list() {
        return ResponseEntity.ok(BUSINESS_OPTIONS);
    }

}
