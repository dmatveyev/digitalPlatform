package com.example.digitalplatform.service.handlers.reporttype;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.db.model.ReportModel;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.dto.ReportData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public byte[] generate(User owner, ReportData data) {
        byte[] result = new byte[0];
        Map<?,?> headerData = new HashMap<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Отчет");
            sheet.setColumnWidth(0, 10000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 3000);
            sheet.setColumnWidth(3, 3000);
            sheet.setColumnWidth(4, 3000);
            sheet.setColumnWidth(5, 3000);
            sheet.setColumnWidth(6, 3000);
            //#region Header
            int nextRow = createHeader(data, workbook, sheet);
            //#endregion
            //#region Header body
            fillBoby(data.getData(), workbook, sheet, nextRow);
            //#endregion
            workbook.write(baos);

            result = baos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        }
        return result;
    }

    private int createHeader(ReportData headerData, XSSFWorkbook workbook, Sheet sheet) {
        Row header0 = sheet.createRow(0);
        XSSFCellStyle firstHeaderStyle = getHeaderStyle(workbook);
        Cell titleCell = header0.createCell(0);
        titleCell.setCellValue(headerData.getReportName());
        titleCell.setCellStyle(firstHeaderStyle);

        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);
        Row row3 = sheet.createRow(3);
        getHeaderStyle(workbook);
        XSSFFont dataFont = workbook.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 12);
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setFont(dataFont);
        dataStyle.setWrapText(true);
        Cell cell10 = row1.createCell(0);
        cell10.setCellValue("За период:");
        cell10.setCellStyle(dataStyle);
        Cell cell11 = row1.createCell(1);
        cell11.setCellStyle(dataStyle);
        Cell cell20 = row2.createCell(0);
        String startDate = headerData.getStartDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String endDate = headerData.getEndDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        cell20.setCellValue(startDate.concat(" - ").concat(endDate));
        cell20.setCellStyle(dataStyle);

        return 5;
    }

    private XSSFCellStyle getHeaderStyle(XSSFWorkbook workbook) {
        XSSFFont firstTitle = workbook.createFont();
        firstTitle.setFontName("Arial");
        firstTitle.setFontHeightInPoints((short) 14);
        firstTitle.setBold(true);

        XSSFCellStyle firstHeaderStyle = workbook.createCellStyle();
        firstHeaderStyle.setFont(firstTitle);
        return firstHeaderStyle;
    }

    private XSSFCellStyle getTableHeaderStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    private XSSFCellStyle getTableDataStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private int fillBoby(List<ReportModel> data, XSSFWorkbook workbook, Sheet sheet, int rowStart) {
        int rowNum = rowStart;
        if (true) {


            Row headerTable = sheet.createRow(rowStart++);
            //set style
            Cell cell0 = headerTable.createCell(0);
            cell0.setCellStyle(getTableHeaderStyle(workbook));
            cell0.setCellValue("Преподаватель");
            createCellBorderMedium(sheet, cell0);
            Cell cell1 = headerTable.createCell(1);
            cell1.setCellStyle(getTableHeaderStyle(workbook));
            cell1.setCellValue("Институт");
            createCellBorderMedium(sheet, cell1);
            Cell cell2 = headerTable.createCell(2);
            cell2.setCellStyle(getTableHeaderStyle(workbook));
            cell2.setCellValue("Завершено");
            createCellBorderMedium(sheet, cell2);
            Cell cell3 = headerTable.createCell(3);
            cell3.setCellStyle(getTableHeaderStyle(workbook));
            cell3.setCellValue("Отклонено");
            createCellBorderMedium(sheet, cell3);
            Cell cell4 = headerTable.createCell(4);
            cell4.setCellStyle(getTableHeaderStyle(workbook));
            cell4.setCellValue("Просрочено");
            createCellBorderMedium(sheet, cell4);
            Cell cell5 = headerTable.createCell(5);
            cell5.setCellStyle(getTableHeaderStyle(workbook));
            cell5.setCellValue("Всего");
            //create tableData
            int startRowIndex = cell0.getRowIndex();
            int startCollIndex = cell0.getColumnIndex();

            int startRow = startRowIndex;
            for (ReportModel datum : data) {
                Row dataRow = sheet.createRow(rowStart++);
                String lastName = datum.getLastName();
                String firstName = datum.getFirstName();
                String degree = datum.getDegree();
                String institute = datum.getInstitute();
                long countDone = datum.getCountDone();
                long countDeclined = datum.getCountDeclined();
                long countExpired = datum.getCountExpired();
                long countAssigned = datum.getCountAssigned();
                Cell dataCell0 = dataRow.createCell(0);

                dataCell0.setCellValue(degree + " " + lastName + " " + firstName);
                XSSFCellStyle tableDataStyle = getTableDataStyle(workbook);
                tableDataStyle.setAlignment(HorizontalAlignment.LEFT);
                dataCell0.setCellStyle(tableDataStyle);
                createCellBorder(sheet, dataCell0);
                Cell dataCell1 = dataRow.createCell(1);
                dataCell1.setCellValue(institute);
                dataCell1.setCellStyle(getTableDataStyle(workbook));
                createCellBorder(sheet, dataCell1);
                Cell dataCell2 = dataRow.createCell(2);
                dataCell2.setCellValue(countDone);
                dataCell2.setCellStyle(getTableDataStyle(workbook));
                createCellBorder(sheet, dataCell2);
                Cell dataCell3 = dataRow.createCell(3);
                dataCell3.setCellStyle(getTableDataStyle(workbook));
                dataCell3.setCellValue(countDeclined);
                createCellBorder(sheet, dataCell3);
                Cell dataCell4 = dataRow.createCell(4);
                dataCell4.setCellStyle(getTableDataStyle(workbook));
                dataCell4.setCellValue(countExpired);
                createCellBorder(sheet, dataCell4);
                Cell dataCell5 = dataRow.createCell(5);
                dataCell5.setCellValue(countAssigned);
                dataCell5.setCellStyle(getTableDataStyle(workbook));
                createCellBorder(sheet, dataCell5);
                startRow++;
            }

            setRegionBorderWithMedium(new CellRangeAddress(cell5.getRowIndex(), cell5.getRowIndex(), cell5.getColumnIndex(), cell5.getColumnIndex()),
                    sheet);
            setRegionBorderWithMedium(new CellRangeAddress(startRowIndex, startRow, startCollIndex, startCollIndex + 5),
                    sheet);
        }

        return rowStart;
    }

    private void createCellBorder(Sheet sheet, Cell dataCell1) {
        setRegionBorderWithThin(new CellRangeAddress(
                        dataCell1.getRowIndex(),
                        dataCell1.getRowIndex(),
                        dataCell1.getColumnIndex(),
                        dataCell1.getColumnIndex()),
                sheet);
    }

    private void createCellBorderMedium(Sheet sheet, Cell dataCell1) {
        setRegionBorderWithMedium(new CellRangeAddress(
                        dataCell1.getRowIndex(),
                        dataCell1.getRowIndex(),
                        dataCell1.getColumnIndex(),
                        dataCell1.getColumnIndex()),
                sheet);
    }

    public void setRegionBorderWithMedium(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region,sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region,sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region,sheet);
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region,sheet);

    }

    public void setRegionBorderWithThin(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderBottom(BorderStyle.THIN, region,sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region,sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region,sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, region,sheet);

    }
}
