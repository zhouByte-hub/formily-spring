package com.formily.controller;

import com.formily.model.OptionItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = {"/system", "/api/system"})
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SystemController {
    private static final List<OptionItem> SYSTEM_OPTIONS = List.of(
            new OptionItem("智能表单设计与数据采集平台", "Formily_Studio"),
            new OptionItem("跨系统业务流程集成中枢", "NexusFlow"),
            new OptionItem("统一数据资产治理与服务平台", "DataHive")
    );

    private static final List<OptionItem> OPERATOR_SYSTEM_OPTIONS = List.of(
            new OptionItem("Windows", "windows"),
            new OptionItem("Ubuntu", "ubuntu"),
            new OptionItem("CentOS", "centos"),
            new OptionItem("Debian", "debian"),
            new OptionItem("macOS", "macos")
    );

    /**
     * 一级系统下拉框选项
     */
    @GetMapping("list")
    public ResponseEntity<List<OptionItem>> list() {
        return ResponseEntity.ok(SYSTEM_OPTIONS);
    }

    /**
     * 操作系统下拉框选项
     */
    @GetMapping({"/operator-system", "/operator_system", "/os"})
    public ResponseEntity<List<OptionItem>> listOperatorSystems() {
        return ResponseEntity.ok(OPERATOR_SYSTEM_OPTIONS);
    }

}
