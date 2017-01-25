package com.lhjz.portal.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * XML工具类.
 * 
 * @creation 2014年3月25日 上午9:55:48
 * @modification 2014年3月25日 上午9:55:48
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
public class XmlUtil {

	private static Logger logger = Logger.getLogger(XmlUtil.class);

	/**
	 * 将xml转化为java bean. 注意: class中的field当前只支持String类型.
	 * 
	 * @author xiweicheng
	 * @creation 2014年3月25日 上午9:54:20
	 * @modification 2014年3月25日 上午9:54:20
	 * @param xml
	 * @param cls
	 * @return
	 */
	public static <T> T toBean(String xml, Class<T> cls) {

		try {
			T newInstance = cls.newInstance();

			SAXReader reader = new SAXReader();
			Document document = reader.read(new StringReader(xml));

			Element rootE = document.getRootElement();

			Iterator<?> elementIterator = rootE.elementIterator();

			while (elementIterator.hasNext()) {
				Element next = (Element) elementIterator.next();

				try {
					String methodName = "set" + String.valueOf(next.getName());
					Method method = cls.getMethod(methodName, String.class);
					method.invoke(newInstance, next.getData());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
					continue;
				}
			}

			return newInstance;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 格式化XML.
	 * 
	 * @author xiweicheng
	 * @creation 2014年3月25日 上午9:49:52
	 * @modification 2014年3月25日 上午9:49:52
	 * @param xml
	 * @return
	 */
	public static String format(String xml) {
		SAXReader saxReader = new SAXReader();

		String result = null;

		try {
			Document document = saxReader.read(new ByteArrayInputStream(xml.getBytes()));
			// 创建输出格式
			OutputFormat format = OutputFormat.createPrettyPrint();
			// 制定输出xml的编码类型
			format.setEncoding("utf-8");

			StringWriter writer = new StringWriter();
			// 创建一个文件输出流
			XMLWriter xmlwriter = new XMLWriter(writer, format);
			// 将格式化后的xml串写入到文件
			xmlwriter.write(document);
			result = writer.toString();
			writer.close();
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		// 返回编译后的字符串格式
		return result;
	}
}
