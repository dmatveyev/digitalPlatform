package com.example.digitalplatform.controller;

import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.dto.ReportData;
import com.example.digitalplatform.service.ReportService;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWithSerializerProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class ReportController {
    ReportService reportService;
    UserRepository userRepository;

    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('TEACHER')")
    public String getAll(
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam(value = "type", required = false) ReportType reportType,
            Model model, Principal principal) {
        int start = page.orElse(1);
        int pageSize = size.orElse(5);
        PageRequest pageRequest = PageRequest.of(start - 1, pageSize);
        String name = principal.getName();
        User byLogin = userRepository.findByLogin(name);
        List<ReportModel> reportModels = reportService.findByPrincipalAndReportType(byLogin, reportType);

        int end = Math.min((start - 1 + pageRequest.getPageSize()), reportModels.size());
        List<ReportModel> pageContent = reportModels.isEmpty() ? Collections.emptyList() :
                reportModels.subList(start - 1, end);
        Page<ReportModel> result = new PageImpl<>(pageContent, pageRequest, reportModels.size());

        model.addAttribute("reportData", result);
        RoleType code = byLogin.getRole().getCode();

        ReportType[] availableRoles = switch (code) {
            case ADMIN -> ReportType.values();
            case TEACHER -> new ReportType[]{ReportType.BY_SUBJECT_AREA};
            default -> new ReportType[0];
        };
        model.addAttribute("reportTypes", availableRoles);
        model.addAttribute("reportType", reportType);
        return switch (reportType) {
            case BY_TEACHERS -> "reports/byTeachers";
            case BY_SUBJECT_AREA -> "reports/bySubjectArea";
            case BY_TEACHES_AND_SUBJECT_AREAS -> "reports/byTeacherAndSubjectArea";
        };
    }

    public static UUID convertBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    @GetMapping(
            value = "/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void getFile(
            @RequestParam("type") ReportType reportType,
            @RequestParam(value = "startDate",required = false) Long startDate,
                        Model model, Principal principal,
                        HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        User byLogin = userRepository.findByLogin(principal.getName());
        ReportData data = new ReportData();
        data.setReportName(reportType.getDesc());
        data.setReportType(reportType);
        data.setStartDate(LocalDateTime.now().minusDays(30));
        data.setEndDate(LocalDateTime.now());
        byte[] generate = reportService.generate(byLogin, data);
        response.setHeader("Content-Disposition", "attachment; filename=" + reportType.name().toLowerCase().concat(".xlsx"));
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(generate);
        outputStream.close();

    }

}