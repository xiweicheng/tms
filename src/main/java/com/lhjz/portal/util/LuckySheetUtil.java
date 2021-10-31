/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiweicheng
 * @date 2021/9/20 8:53 上午
 */
public class LuckySheetUtil {

    private LuckySheetUtil() {
    }

    /***
     * 基于POI解析 从0开始导出xlsx文件，
     * @param newFileDir 保存的文件夹名
     * @param newFileName 保存的文件名
     * @param excelData luckysheet 表格数据
     */
    public static void exportLuckySheetXlsxByPOI(String newFileDir, String newFileName, String excelData) {
        //去除luckysheet中 &#xA 的换行
        excelData = excelData.replace("&#xA;", "\\r\\n");
        //获取luckysheet数据  sheet:[{name:sheet1, ...},{}, ...]
        JSONArray jsonArray = (JSONArray) JSONObject.parse(excelData);

        //创建操作Excel的XSSFWorkbook对象
        XSSFWorkbook excel = new XSSFWorkbook();

        for (Object o : jsonArray) {
            //获取sheet
            JSONObject jsonObject = (JSONObject) o;
            JSONArray celldataObjectList = jsonObject.getJSONArray("celldata");//获取所有单元格（坐标，内容，字体类型，字体大小...）
            JSONArray rowObjectList = jsonObject.getJSONArray("visibledatarow");
            JSONArray colObjectList = jsonObject.getJSONArray("visibledatacolumn");
            JSONObject columnlenObject = jsonObject.getJSONObject("config").getJSONObject("columnlen");//表格列宽
            JSONObject rowlenObject = jsonObject.getJSONObject("config").getJSONObject("rowlen");//表格行高
            JSONArray borderInfoObjectList = jsonObject.getJSONObject("config").getJSONArray("borderInfo");//边框样式

            //参考：https://blog.csdn.net/jdtugfcg/article/details/84100315
            excel.createCellStyle();
            //创建XSSFSheet对象
            XSSFSheet sheet = excel.createSheet(jsonObject.getString("name"));

            //我们都知道excel是表格，即由一行一行组成的，那么这一行在java类中就是一个XSSFRow对象，我们通过XSSFSheet对象就可以创建XSSFRow对象
            //如：创建表格中的第一行（我们常用来做标题的行)  XSSFRow firstRow = sheet.createRow(0); 注意下标从0开始
            //根据luckysheet创建行列
            //创建行和列
            if (rowObjectList != null && rowObjectList.size() > 0) {

                for (int i = 0; i < rowObjectList.size(); i++) {

                    XSSFRow row = sheet.createRow(i);//创建行

                    try {
                        row.setHeightInPoints(Float.parseFloat(rowlenObject.get(i) + ""));//行高px值
                    } catch (Exception e) {
                        row.setHeightInPoints(20f);//默认行高
                    }

                    if (colObjectList != null && colObjectList.size() > 0) {

                        for (int j = 0; j < colObjectList.size(); j++) {

                            if (columnlenObject != null && columnlenObject.getInteger(j + "") != null) {
                                sheet.setColumnWidth(j, columnlenObject.getInteger(j + "") * 42);//列宽px值
                            }
                            row.createCell(j);//创建列
                        }
                    }
                }
            }

            //设置值,样式
            setCellValue(celldataObjectList, borderInfoObjectList, sheet, excel);

            // 判断路径是否存在
            File dir = new File(newFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            try (OutputStream out = new FileOutputStream(newFileDir + newFileName)) {
                excel.write(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 合并单元格与填充单元格颜色
     *
     * @param jsonObjectValue jsonObjectValue
     * @param sheet           sheet
     * @param style           style
     */
    private static void setMergeAndColorByObject(JSONObject jsonObjectValue, XSSFSheet sheet, XSSFCellStyle style) {

        JSONObject mergeObject = (JSONObject) jsonObjectValue.get("mc");

        //合併單元格
        if (mergeObject != null) {

            int r = (int) (mergeObject.get("r"));
            int c = (int) (mergeObject.get("c"));

            if ((mergeObject.get("rs") != null && (mergeObject.get("cs") != null))) {

                int rs = (int) (mergeObject.get("rs"));
                int cs = (int) (mergeObject.get("cs"));

                CellRangeAddress region = new CellRangeAddress(r, r + rs - 1, (short) (c), (short) (c + cs - 1));
                sheet.addMergedRegion(region);
            }
        }
        //填充顏色
        if (jsonObjectValue.getString("bg") != null) {
            // 设置填充方案
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // 设置填充颜色
            style.setFillForegroundColor(new XSSFColor(toColor(jsonObjectValue.getString("bg"))));
        }

    }

    private static Color toColor(String colorBg) {

        Color color = null;

        if (colorBg.startsWith("rgb(")) {
            colorBg = colorBg.replace("rgb(", "").replace(")", "");
            String[] split = colorBg.split(",");
            int r = Integer.parseInt(split[0].trim());
            int g = Integer.parseInt(split[1].trim());
            int b = Integer.parseInt(split[2].trim());
            color = new Color(r, g, b);
        } else if (colorBg.startsWith("#")) {
            colorBg = colorBg.replace("#", "");
            color = new Color(Integer.parseInt(colorBg, 16));
        }

        return color;
    }

    private static void setBorder(JSONArray borderInfoObjectList, XSSFSheet sheet) {

        //设置边框样式map
        Map<Integer, BorderStyle> bordMap = new HashMap<>();
        bordMap.put(1, BorderStyle.THIN);
        bordMap.put(2, BorderStyle.HAIR);
        bordMap.put(3, BorderStyle.DOTTED);
        bordMap.put(4, BorderStyle.DASHED);
        bordMap.put(5, BorderStyle.DASH_DOT);
        bordMap.put(6, BorderStyle.DASH_DOT_DOT);
        bordMap.put(7, BorderStyle.DOUBLE);
        bordMap.put(8, BorderStyle.MEDIUM);
        bordMap.put(9, BorderStyle.MEDIUM_DASHED);
        bordMap.put(10, BorderStyle.MEDIUM_DASH_DOT);
        bordMap.put(11, BorderStyle.MEDIUM_DASH_DOT_DOT);
        bordMap.put(12, BorderStyle.SLANTED_DASH_DOT);
        bordMap.put(13, BorderStyle.THICK);

        //一定要通过 cell.getCellStyle()  不然的话之前设置的样式会丢失
        //设置边框
        if (borderInfoObjectList != null && borderInfoObjectList.size() > 0) {

            for (Object o : borderInfoObjectList) {

                JSONObject borderInfoObject = (JSONObject) o;

                if ("cell".equals(borderInfoObject.get("rangeType"))) {//单个单元格

                    JSONObject borderValueObject = borderInfoObject.getJSONObject("value");

                    JSONObject l = borderValueObject.getJSONObject("l");
                    JSONObject r = borderValueObject.getJSONObject("r");
                    JSONObject t = borderValueObject.getJSONObject("t");
                    JSONObject b = borderValueObject.getJSONObject("b");


                    int row = borderValueObject.getInteger("row_index");
                    int col = borderValueObject.getInteger("col_index");

                    XSSFCell cell = sheet.getRow(row).getCell(col);


                    if (l != null) {
                        cell.getCellStyle().setBorderLeft(bordMap.get((int) l.get("style"))); //左边框
                        cell.getCellStyle().setLeftBorderColor(new XSSFColor(toColor(l.getString("color"))));//左边框颜色
                    }
                    if (r != null) {
                        cell.getCellStyle().setBorderRight(bordMap.get((int) r.get("style"))); //右边框
                        cell.getCellStyle().setRightBorderColor(new XSSFColor(toColor(r.getString("color"))));//右边框颜色
                    }
                    if (t != null) {
                        cell.getCellStyle().setBorderTop(bordMap.get((int) t.get("style"))); //顶部边框
                        cell.getCellStyle().setTopBorderColor(new XSSFColor(toColor(t.getString("color"))));//顶部边框颜色
                    }
                    if (b != null) {
                        cell.getCellStyle().setBorderBottom(bordMap.get((int) b.get("style"))); //底部边框
                        cell.getCellStyle().setBottomBorderColor(new XSSFColor(toColor(b.getString("color"))));//底部边框颜色
                    }
                } else if ("range".equals(borderInfoObject.get("rangeType"))) {//选区
                    int style = borderInfoObject.getInteger("style");

                    JSONObject rangObject = (JSONObject) ((JSONArray) (borderInfoObject.get("range"))).get(0);

                    JSONArray rowList = rangObject.getJSONArray("row");
                    JSONArray columnList = rangObject.getJSONArray("column");

                    for (int row = rowList.getInteger(0); row < rowList.getInteger(rowList.size() - 1) + 1; row++) {
                        for (int col = columnList.getInteger(0); col < columnList.getInteger(columnList.size() - 1) + 1; col++) {
                            XSSFCell cell = sheet.getRow(row).getCell(col);

                            Color color = toColor(borderInfoObject.getString("color"));

                            cell.getCellStyle().setBorderLeft(bordMap.get(style)); //左边框
                            cell.getCellStyle().setLeftBorderColor(new XSSFColor(color));//左边框颜色
                            cell.getCellStyle().setBorderRight(bordMap.get(style)); //右边框
                            cell.getCellStyle().setRightBorderColor(new XSSFColor(color));//右边框颜色
                            cell.getCellStyle().setBorderTop(bordMap.get(style)); //顶部边框
                            cell.getCellStyle().setTopBorderColor(new XSSFColor(color));//顶部边框颜色
                            cell.getCellStyle().setBorderBottom(bordMap.get(style)); //底部边框
                            cell.getCellStyle().setBottomBorderColor(new XSSFColor(color));//底部边框颜色 }
                        }
                    }
                }
            }
        }
    }

    /**
     * 設置值和樣式
     *
     * @param jsonObjectList       jsonObjectList
     * @param borderInfoObjectList borderInfoObjectList
     * @param sheet                sheet
     * @param workbook             workbook
     */
    private static void setCellValue(JSONArray jsonObjectList, JSONArray borderInfoObjectList, XSSFSheet
            sheet, XSSFWorkbook workbook) {
        //设置字体大小和颜色
//        Map<Integer, String> fontMap = new HashMap<>();
//        fontMap.put(-1, "Arial");
//        fontMap.put(0, "Times New Roman");
//        fontMap.put(1, "Arial");
//        fontMap.put(2, "Tahoma");
//        fontMap.put(3, "Verdana");
//        fontMap.put(4, "微软雅黑");
//        fontMap.put(5, "宋体");
//        fontMap.put(6, "黑体");
//        fontMap.put(7, "楷体");
//        fontMap.put(8, "仿宋");
//        fontMap.put(9, "新宋体");
//        fontMap.put(10, "华文新魏");
//        fontMap.put(11, "华文行楷");
//        fontMap.put(12, "华文隶书");

        // 遍歷每一個單元格（先遍歷行，再遍歷列）
        for (int index = 0; index < jsonObjectList.size(); index++) {

            XSSFCellStyle style = workbook.createCellStyle();//样式
            XSSFFont font = workbook.createFont();//字体样式
            //獲取單元格
            JSONObject object = jsonObjectList.getJSONObject(index);
            //str_ = 行坐標+列坐標=內容
            String str = object.get("r") + "_" + object.get("c") + "=" + ((JSONObject) object.get("v")).get("v") + "\n";
            JSONObject jsonObjectValue = ((JSONObject) object.get("v"));//獲取單元格樣式
            //單元格內容
            String value = "";
            if (jsonObjectValue != null && jsonObjectValue.get("v") != null) {
                value = jsonObjectValue.getString("v");
            }

            if (sheet.getRow((int) object.get("r")) != null && sheet.getRow((int) object.get("r")).getCell((int) object.get("c")) != null) {
                XSSFCell cell = sheet.getRow((int) object.get("r")).getCell((int) object.get("c"));
                //設置公式 注意：luckysheet与Java的公式可能存在不匹配问题，例如js的Int(data)
                if (jsonObjectValue != null && jsonObjectValue.get("f") != null) {//如果有公式，设置公式
                    value = jsonObjectValue.getString("f");
                    cell.setCellFormula(value.substring(1));//不需要=符号,例：INT(12.3)
                }
                //合并单元格与填充单元格颜色
                assert jsonObjectValue != null;
                setMergeAndColorByObject(jsonObjectValue, sheet, style);
                //填充值
                cell.setCellValue(value);
                sheet.getRow((int) object.get("r"));

                //设置垂直水平对齐方式
                int vt = jsonObjectValue.getInteger("vt") == null ? 1 : jsonObjectValue.getInteger("vt");//垂直对齐	 0 中间、1 上、2下
                int ht = jsonObjectValue.getInteger("ht") == null ? 1 : jsonObjectValue.getInteger("ht");//0 居中、1 左、2右
                switch (vt) {
                    case 0:
                        style.setVerticalAlignment(VerticalAlignment.CENTER);
                        break;
                    case 1:
                        style.setVerticalAlignment(VerticalAlignment.TOP);
                        break;
                    case 2:
                        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        break;
                }
                switch (ht) {
                    case 0:
                        style.setAlignment(HorizontalAlignment.CENTER);
                        break;
                    case 1:
                        style.setAlignment(HorizontalAlignment.LEFT);
                        break;
                    case 2:
                        style.setAlignment(HorizontalAlignment.RIGHT);
                        break;
                }

                //设置合并单元格的样式有问题
                String ff = jsonObjectValue.getString("ff");//0 Times New Roman、 1 Arial、2 Tahoma 、3 Verdana、4 微软雅黑、5 宋体（Song）、6 黑体（ST Heiti）、7 楷体（ST Kaiti）、 8 仿宋（ST FangSong）、9 新宋体（ST Song）、10 华文新魏、11 华文行楷、12 华文隶书
                int fs = jsonObjectValue.getInteger("fs") == null ? 14 : jsonObjectValue.getInteger("fs");//字体大小
                int bl = jsonObjectValue.getInteger("bl") == null ? 0 : jsonObjectValue.getInteger("bl");//粗体	0 常规 、 1加粗
                int it = jsonObjectValue.getInteger("it") == null ? 0 : jsonObjectValue.getInteger("it");//斜体	0 常规 、 1 斜体
                String fc = jsonObjectValue.getString("fc") == null ? "" : jsonObjectValue.getString("fc");//字体颜色
//                font.setFontName(fontMap.get(ff));//字体名字
                //字體顏色
                if (fc.length() > 0) {
                    font.setColor(new XSSFColor(toColor(fc)));
                }
                font.setFontName(ff);//字体名字
                font.setFontHeightInPoints((short) fs);//字体大小
                //是否粗體
                if (bl == 1) {
                    font.setBold(true);//粗体显示
                }
                //是否斜體
                font.setItalic(it == 1);//斜体

                style.setFont(font);
                style.setWrapText(true);//设置自动换行
                cell.setCellStyle(style);

            } else {
                System.out.println("错误的=" + index + ">>>" + str);
            }

        }
        //设置边框
        setBorder(borderInfoObjectList, sheet);

    }
}
