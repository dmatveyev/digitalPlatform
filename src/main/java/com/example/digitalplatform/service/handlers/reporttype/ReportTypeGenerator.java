package com.example.digitalplatform.service.handlers.reporttype;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.db.model.ReportModel;
import com.example.digitalplatform.db.model.User;

import java.util.List;

public interface ReportTypeGenerator {

    ReportType getReportType();

    byte[] generate(User owner, List<ReportModel> data);
}
