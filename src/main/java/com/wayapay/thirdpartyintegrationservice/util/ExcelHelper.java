package com.wayapay.thirdpartyintegrationservice.util;

import com.wayapay.thirdpartyintegrationservice.dto.BulkBillsPaymentDTO;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequestExcel;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Slf4j
public class ExcelHelper {

    private static DataFormatter dataFormatter = new DataFormatter();
    public static String[] TYPE = {"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel"};
    public static List<String> PAYMENT_HEADER = Arrays.asList("BILLER_ID", "CATEGORY_ID", "PHONE", "AMOUNT", "PAYMENT_METHOD", "CHANNEL", "WALLET_ID", "PLAN", "USER_ID");


    static String SHEET = "BulkBillspayment";
    static Pattern alphabetsPattern = Pattern.compile("^[a-zA-Z]*$");
    static Pattern numericPattern = Pattern.compile("^[0-9]*$");
    static Pattern emailPattern = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");

    static Pattern formatter = Pattern.compile("\\d+\\.\\d+");
//            new DecimalFormat("#0.00");

    public static boolean hasExcelFormat(MultipartFile file) {
        if (!Arrays.asList(TYPE).contains(file.getContentType())) {
            return false;
        }
        return true;
    }

    public static BulkBillsPaymentDTO excelToPaymentRequest(InputStream is, String fileName) throws ThirdPartyIntegrationException {

        try(Workbook workbook = getWorkBook(is, fileName)) {
            if(workbook == null){
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid Excel File Check Extension");
            }
            List<PaymentRequestExcel> models = new ArrayList<>();

            Sheet sheet = workbook.getSheet(SHEET);
            if(sheet == null) throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid Excel File Format Passed, Check Sheet Name");
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()){
                Row currentRow = rows.next();
                Iterator<Cell> cellsInRow = currentRow.iterator();
                PaymentRequestExcel pojo = new PaymentRequestExcel();

                // If First Cell is empty break from loop
                if (currentRow == null || isCellEmpty(currentRow.getCell(0))) {
                    break;
                }

                // Skip header After Check of Header Formats
                if (rowNumber == 0) {
                    List<String> excelColNames = new ArrayList<>();
                    int i = 0;
                    while (cellsInRow.hasNext()) {
                        Cell cell = cellsInRow.next();
                        String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();
                        excelColNames.add(cellValue);
                        i++;
                        if (i == PAYMENT_HEADER.size()) {
                            break;
                        }
                    }
                    boolean value = checkExcelFileValidity(PAYMENT_HEADER, excelColNames);
                    if (!value) {
                        String errorMessage = "Failure, Incorrect File Format";
                        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST,errorMessage);
                    }
                    rowNumber++;
                    continue;
                }

                int cellIdx = 0;
                while (cellsInRow.hasNext()){
                    Cell cell = cellsInRow.next();
                    String colName = CellReference.convertNumToColString(cell.getColumnIndex()).toUpperCase();
                    switch (colName) {
                        case "A":
                            pojo.setBillerId(defaultStringCell(cell));
                            break;
                        case "B":
                            pojo.setCategoryId(defaultStringCell(cell));
                            break;
                        case "C":
                            pojo.setPhone(defaultStringCell(cell));
                            break;
                        case "D":
                            pojo.setAmount(Integer.parseInt(validateStringNumericOnly(cell, cellIdx, rowNumber)));
                            break;
                        case "E":
                            pojo.setPaymentMethod(defaultStringCell(cell));
                            break;
                        case "F":
                            pojo.setChannel(defaultStringCell(cell));
                            break;
                        case "G":
                            pojo.setSourceWalletAccountNumber(defaultStringCell(cell));
                            break;
                        case "H":
                            pojo.setPlan(defaultStringCell(cell));
                        case "I":
                            pojo.setUserId(defaultStringCell(cell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                models.add(pojo);
                rowNumber++;
            }
            return new BulkBillsPaymentDTO(models);
        }catch (Exception ex){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }


    public static ByteArrayInputStream createExcelSheet(List<String> HEADERS) throws ThirdPartyIntegrationException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet(SHEET);
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS.get(col));
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new ThirdPartyIntegrationException( HttpStatus.EXPECTATION_FAILED, "Error in Forming Excel: " + e.getMessage());
        }
    }

    private static Workbook getWorkBook(InputStream is, String fileName) {
        Workbook workbook = null;
        try {
            String extension = fileName.substring(fileName.lastIndexOf("."));
            if(extension.equalsIgnoreCase(".xls")){
                workbook = new HSSFWorkbook(is);
            }
            else if(extension.equalsIgnoreCase(".xlsx")){
                workbook = new XSSFWorkbook(is);
            }
        }
        catch(Exception ex) {
            log.error("An Error has Occurred while Getting WorkBook File: {}", ex.getMessage());
        }
        return workbook;
    }

    private static boolean isCellEmpty(final Cell cell) {
        String cellValue = dataFormatter.formatCellValue(cell).trim();
        return cellValue.isEmpty();
    }

    private static boolean checkExcelFileValidity(List<String> one, List<String> two) {
        if (one == null && two == null)
            return true;

        if ((one == null && two != null) || (one != null && two == null) || (one.size() != two.size())) {
            return false;
        }
        one = new ArrayList<>(one);
        two = new ArrayList<>(two);

        return one.equals(two);
    }

    private static String defaultStringCell(final Cell cell) {
        return dataFormatter.formatCellValue(cell).trim();
    }

    private static String validateAndPassStringValue(Cell cell, int cellNumber, int rowNumber) throws ThirdPartyIntegrationException {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        boolean val = alphabetsPattern.matcher(cellValue).find();
        if(!cellValue.isBlank() && val && cellValue.length() >= 2){
            return cellValue;
        }
        String errorMessage = String.format("Invalid Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED,errorMessage);
    }

    private static String validateStringIsEmail(Cell cell, int cellNumber, int rowNumber) throws ThirdPartyIntegrationException {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        Matcher matcher = emailPattern.matcher(cellValue);
        if(!matcher.matches()){
            String errorMessage = String.format("Invalid Email Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED,errorMessage);
        }
        return cellValue;
    }

    private static String validateStringNumericOnly(Cell cell, int cellNumber, int rowNumber) throws ThirdPartyIntegrationException {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        boolean val = numericPattern.matcher(cellValue).find();
        if(!val) {
            String errorMessage = String.format("Invalid Numeric Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED,errorMessage);
        }
        return cellValue;
    }

    private static String validateStringDoubleOnly(Cell cell, int cellNumber, int rowNumber) throws ThirdPartyIntegrationException {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        boolean val = formatter.matcher(cellValue).find();
        if(!val) {
            String errorMessage = String.format("Invalid Numeric Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED,errorMessage);
        }
        return cellValue;
    }

}
