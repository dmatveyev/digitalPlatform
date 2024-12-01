package com.example.digitalplatform.controller.dto;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.db.model.ReportModel;
import com.example.digitalplatform.db.model.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportData {

    User initiator;
    String reportName;
    ReportType reportType;
    LocalDate startDate;
    LocalDate endDate;
    List<ReportModel> data;
}
