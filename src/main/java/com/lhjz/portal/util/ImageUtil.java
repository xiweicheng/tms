package com.lhjz.portal.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 图片处理工具类.
 *
 * @author xiweicheng
 * @version 1.0
 * @creation 2014年4月14日 上午9:56:55
 * @modification 2014年4月14日 上午9:56:55
 * @company
 */
@Slf4j
public final class ImageUtil {

    public static final String STR_DOT = ".";

    private ImageUtil() {
    }


    /**
     * 缩放图像（按高度和宽度缩放）
     *
     * @param srcImageFile 源图像文件地址
     * @param result       缩放后的图像地址
     * @param height       缩放后的高度
     * @param width        缩放后的宽度
     * @param bb           比例不对时是否需要补白：true为补白; false为不补白;
     */
    public static void scale(String srcImageFile, String result,
                             int height, int width, boolean bb) {

        try {
            double ratio; // 缩放比例
            File f = new File(srcImageFile);
            BufferedImage bi = ImageIO.read(f);
            Image temp = bi.getScaledInstance(width, height,
                    Image.SCALE_SMOOTH);

            // 计算比例
            if ((bi.getHeight() > height) || (bi.getWidth() > width)) {

                if (bi.getHeight() > bi.getWidth()) {
                    ratio = (new Integer(height)).doubleValue()
                            / bi.getHeight();
                } else {
                    ratio = (new Integer(width)).doubleValue() / bi.getWidth();
                }

                AffineTransformOp op = new AffineTransformOp(
                        AffineTransform.getScaleInstance(ratio, ratio), null);
                temp = op.filter(bi, null);
            }

            // 补白
            if (bb) {
                BufferedImage image = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, width, height);

                if (width == temp.getWidth(null)) {
                    g.drawImage(temp, 0, (height - temp.getHeight(null)) / 2,
                            temp.getWidth(null), temp.getHeight(null),
                            Color.white, null);
                } else {
                    g.drawImage(temp, (width - temp.getWidth(null)) / 2, 0,
                            temp.getWidth(null), temp.getHeight(null),
                            Color.white, null);
                }

                g.dispose();
                temp = image;
            }
            ImageIO.write((BufferedImage) temp, "JPEG", new File(result));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 判断文件是否为图片
     *
     * @param fileName 文件名
     * @return 是否为图片
     * @author xiweicheng
     * @creation 2014年4月14日 下午1:28:45
     * @modification 2014年4月14日 下午1:28:45
     */
    public static boolean isImage(String fileName) {
        // 文件名称为空的场合
        if (StringUtil.isEmpty(fileName)) {
            return false;
        }

        if (fileName.lastIndexOf(STR_DOT) == -1) {
            return false;
        }

        // 获得文件后缀名
        String suffix = fileName.substring(fileName.lastIndexOf(STR_DOT) + 1);

        Set<String> typeSet = new HashSet<>();
        typeSet.add("bmp");
        typeSet.add("gif");
        typeSet.add("jpeg");
        typeSet.add("jpg");
        typeSet.add("png");
        typeSet.add("tiff");

        return typeSet.contains(suffix.toLowerCase());
    }

    /**
     * base64字符串转化成图片
     *
     * @param base64      图片base64
     * @param imgFilePath 图片路径
     */
    public static void decodeBase64ToImage(String base64,
                                           String imgFilePath) { // 对字节数组字符串进行Base64解码并生成图片

        // 图像数据为空
        if (base64 == null) {
            return;
        }

        Base64 decoder = new Base64();

        try {
            // Base64解码
            byte[] b = decoder.decode(base64);

            for (int i = 0; i < b.length; ++i) {
                // 调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            // 生成jpeg图片
            try (OutputStream out = new FileOutputStream(imgFilePath)) {
                out.write(b);
                out.flush();
            }
        } catch (Exception e) {
            log.error("base64字符串转化成图片error:" + e.getMessage(), e);
        }
    }

}
