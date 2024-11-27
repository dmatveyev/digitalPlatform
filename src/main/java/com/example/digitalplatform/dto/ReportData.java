package com.example.digitalplatform.dto;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.db.model.ReportModel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportData {

    String reportName;
    ReportType reportType;
    LocalDateTime startDate;
    LocalDateTime endDate;
    List<ReportModel> data;
}
