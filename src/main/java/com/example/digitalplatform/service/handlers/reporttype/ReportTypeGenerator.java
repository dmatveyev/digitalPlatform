package com.example.digitalplatform.service.handlers.reporttype;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.dto.ReportData;

public interface ReportTypeGenerator {

    ReportType getReportType();

    byte[] generate(User owner, ReportData data);
}
