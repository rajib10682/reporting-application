package com.reporting.dataservice.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelTemplateService {
    
    public byte[] generateAdjustmentTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Adjustment Template");
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "goc", "account", "currency", "year",
                "jan_amt", "feb_amt", "mar_amt", "apr_amt",
                "may_amt", "jun_amt", "jul_amt", "aug_amt",
                "sep_amt", "oct_amt", "nov_amt", "dec_amt"
            };
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            Row sampleRow = sheet.createRow(1);
            String[] sampleData = {
                "abc123", "346781", "USD", "2025",
                "10.00", "20.00", "30.00", "40.00",
                "50.00", "60.00", "70.00", "80.00",
                "90.00", "100.00", "110.00", "120.00"
            };
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            
            for (int i = 0; i < sampleData.length; i++) {
                Cell cell = sampleRow.createCell(i);
                if (i >= 4) {
                    cell.setCellValue(Double.parseDouble(sampleData[i]));
                } else {
                    cell.setCellValue(sampleData[i]);
                }
                cell.setCellStyle(dataStyle);
            }
            
            Sheet instructionsSheet = workbook.createSheet("Instructions");
            Row instructionRow1 = instructionsSheet.createRow(0);
            instructionRow1.createCell(0).setCellValue("Adjustment File Upload Instructions");
            
            Row instructionRow2 = instructionsSheet.createRow(2);
            instructionRow2.createCell(0).setCellValue("1. Fill in the 'Adjustment Template' sheet with your data");
            
            Row instructionRow3 = instructionsSheet.createRow(3);
            instructionRow3.createCell(0).setCellValue("2. Save the file as .txt with tilde (~) delimited format");
            
            Row instructionRow4 = instructionsSheet.createRow(4);
            instructionRow4.createCell(0).setCellValue("3. Upload the .txt file through the adjustment upload interface");
            
            Row instructionRow5 = instructionsSheet.createRow(6);
            instructionRow5.createCell(0).setCellValue("Column Descriptions:");
            
            Row instructionRow6 = instructionsSheet.createRow(7);
            instructionRow6.createCell(0).setCellValue("- goc: General Operating Company code");
            
            Row instructionRow7 = instructionsSheet.createRow(8);
            instructionRow7.createCell(0).setCellValue("- account: Account identifier (must be leaf account)");
            
            Row instructionRow8 = instructionsSheet.createRow(9);
            instructionRow8.createCell(0).setCellValue("- currency: Currency code (USD, EUR, GBP, etc.)");
            
            Row instructionRow9 = instructionsSheet.createRow(10);
            instructionRow9.createCell(0).setCellValue("- year: Fiscal year");
            
            Row instructionRow10 = instructionsSheet.createRow(11);
            instructionRow10.createCell(0).setCellValue("- *_amt: Monthly amounts (Jan through Dec)");
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            instructionsSheet.autoSizeColumn(0);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
