package com.example.digitalplatform.service.handlers.reporttype;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.controller.dto.ReportData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public interface ReportTypeGenerator {

    ReportType getReportType();

    byte[] generate(ReportData data);

    default XSSFCellStyle getHeaderStyle(XSSFWorkbook workbook) {
        XSSFFont firstTitle = workbook.createFont();
        firstTitle.setFontName("Arial");
        firstTitle.setFontHeightInPoints((short) 14);
        firstTitle.setBold(true);

        XSSFCellStyle firstHeaderStyle = workbook.createCellStyle();
        firstHeaderStyle.setFont(firstTitle);
        return firstHeaderStyle;
    }

   default XSSFCellStyle getTableHeaderStyle(XSSFWorkbook workbook) {
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
    default XSSFCellStyle getTableDataStyle(XSSFWorkbook workbook) {
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


    default Row createRow(Sheet sheet, int rowStart) {
        Row headerTable = sheet.createRow(rowStart);
        headerTable.setHeight((short) 350);
        return headerTable;
    }

    default void createCellBorder(Sheet sheet, Cell dataCell1) {
        setRegionBorderWithThin(new CellRangeAddress(
                        dataCell1.getRowIndex(),
                        dataCell1.getRowIndex(),
                        dataCell1.getColumnIndex(),
                        dataCell1.getColumnIndex()),
                sheet);
    }

    default void createCellBorderMedium(Sheet sheet, Cell dataCell1) {
        setRegionBorderWithMedium(new CellRangeAddress(
                        dataCell1.getRowIndex(),
                        dataCell1.getRowIndex(),
                        dataCell1.getColumnIndex(),
                        dataCell1.getColumnIndex()),
                sheet);
    }

    default void setRegionBorderWithMedium(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region,sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region,sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region,sheet);
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region,sheet);

    }

    default void setRegionBorderWithThin(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderBottom(BorderStyle.THIN, region,sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region,sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region,sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, region,sheet);

    }
}
