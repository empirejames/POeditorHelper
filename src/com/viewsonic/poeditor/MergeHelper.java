package com.viewsonic.poeditor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MergeHelper {

	public ArrayList<String> getFiles(String path) {
		ArrayList<String> files = new ArrayList<String>();
		File file = new File(path);
		File[] tempList = file.listFiles();

		for (int i = 0; i < tempList.length; i++) {
			if (tempList[i].isFile()) {
				if (!tempList[i].toString().contains("~$")) {
					if (getExtension(tempList[i]).equals("xlsx") || getExtension(tempList[i]).equals("xls")) {
						if (!tempList[i].toString().contains("merge")) {
							files.add(tempList[i].toString());
						}
					}
				}
			}
			if (tempList[i].isDirectory()) {
				// System.out.println("資料夾：" + tempList[i]);
			}
		}
		return files;
	}

	public String getExtension(File file) {
		int startIndex = file.getName().lastIndexOf(46) + 1;
		int endIndex = file.getName().length();
		return file.getName().substring(startIndex, endIndex);
	}

	public String getFileLang(File file) {
		int startIndex = file.getName().lastIndexOf("_") + 1;
		int endIndex = file.getName().lastIndexOf(46);
		return file.getName().substring(startIndex, endIndex);
	}

	public List<List<String>> readColContentFromExcel(String filePath) {
		ArrayList<String> rowData = new ArrayList<String>();
		List<List<String>> tempRowData = new ArrayList<List<String>>();
		File excelFile = new File(filePath);
		String lang = getFileLang(excelFile);
		Workbook workbook = null;
		Sheet sheet = null;
		try {
			FileInputStream fis = new FileInputStream(excelFile);
			if (fis.equals(null)) {
				System.out.println("File not found");
			} else {
				workbook = new XSSFWorkbook(fis);
			}
			sheet = workbook.getSheetAt(0);// 取得檔案分頁名稱
			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				Row row = sheet.getRow(i);
				if (i == 0) {
					rowData.add(lang); //加上語系判斷在第一行
				}
				if (row.getCell(1) == null) { // cell會有空的可能
					rowData.add("");
				} else {
					rowData.add(row.getCell(1).toString()); // 直接讀取第一列
				}
			}
			fis.close();
			tempRowData.add(rowData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tempRowData;
	}

	public void writeExcel(int nmOfFile, List<List<String>> listOfRowData) {
		String filePath = "merge.xlsx";
		BufferedOutputStream outPutStream = null;
		Workbook workbook = null;
		try {
			workbook = getWorkBook(filePath);
			XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);
			System.out.println();
			for (int i = 0; i < listOfRowData.get(0).size(); i++) {
				XSSFRow row = sheet.createRow(i);
				for (int j = 0; j < nmOfFile; j++) {
					Cell cell = row.createCell(j);
					cell.setCellValue(listOfRowData.get(j).get(i).toString());
				}
			}
			outPutStream = new BufferedOutputStream(new FileOutputStream(filePath));
			workbook.write(outPutStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outPutStream != null) {
				try {
					outPutStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static XSSFWorkbook getWorkBook(String filePath) {
		XSSFWorkbook workbook = null;
		try {
			File fileXlsxPath = new File(filePath);
			BufferedOutputStream outPutStream = new BufferedOutputStream(new FileOutputStream(fileXlsxPath));
			workbook = new XSSFWorkbook();
			workbook.createSheet("合併翻譯檔");
			workbook.write(outPutStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workbook;
	}
}
