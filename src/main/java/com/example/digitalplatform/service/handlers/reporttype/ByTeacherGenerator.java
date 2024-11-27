package com.example.digitalplatform.service.handlers.reporttype;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.db.model.ReportModel;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.service.ReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ByTeacherGenerator implements ReportTypeGenerator {

    @Override
    public ReportType getReportType() {
        return ReportType.BY_TEACHERS;
    }

    @Override
    public byte[] generate(User owner, List<ReportModel> data) {
        byte[] result = new byte[0];
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.createSheet("Отчет");
            //#region

            //#endregion
            workbook.write(baos);

            result = baos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        }
        return result;
    }
}
