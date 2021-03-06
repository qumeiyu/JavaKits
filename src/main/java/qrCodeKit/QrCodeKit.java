package qrCodeKit;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Random;

/**
 * <p>二维码工具类</p>
 *
 * @author NewGr8Player
 * @version 0.1 (2017/11/22)
 */
public class QrCodeKit {
    private static final String CHARSET = "utf-8";/* 包含文字的编码 */
    private static final String FORMAT = "PNG";/* 图片格式 */

    private static final int QRCODE_SIZE = 300;/* 二维码尺寸 */
    private static final int LOGO_WIDTH = 60;/* LOGO宽度 */
    private static final int LOGO_HEIGHT = 60;/* LOGO高度 */

    /**
     * <p>创建图片(private)</p>
     * @param content 内容
     * @param logoPath 路径
     * @param needCompress 是否压缩
     * @return BufferedImage对象
     */
    private static BufferedImage createImage(String content, String logoPath, boolean needCompress) throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE,
                hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        if (logoPath == null || "".equals(logoPath)) {
            return image;
        }
        /* 插入图片 */
        QrCodeKit.insertImage(image, logoPath, needCompress);
        return image;
    }

    /**
     * <p>插入LOGO</p>
     *
     * @param source       二维码图片
     * @param logoPath     LOGO图片地址
     * @param needCompress 是否压缩
     */
    private static void insertImage(BufferedImage source, String logoPath, boolean needCompress) throws Exception {
        File file = new File(logoPath);
        if (!file.exists()) {
            throw new Exception("logo file not found.");
        }
        Image src = ImageIO.read(new File(logoPath));
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        if (needCompress) { // 压缩LOGO
            if (width > LOGO_WIDTH) {
                width = LOGO_WIDTH;
            }
            if (height > LOGO_HEIGHT) {
                height = LOGO_HEIGHT;
            }
            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); /* 绘制缩小后的图 */
            g.dispose();
            src = image;
        }
        /* 插入LOGO */
        Graphics2D graph = source.createGraphics();
        int x = (QRCODE_SIZE - width) / 2;
        int y = (QRCODE_SIZE - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    /**
     * <p>生成二维码(内嵌LOGO)</p>
     * <p>二维码文件名随机，文件名可能会有重复</p>
     *
     * @param content      内容
     * @param logoPath     LOGO地址
     * @param destPath     存放目录
     * @param needCompress 是否压缩LOGO
     */
    public static String encode(String content, String logoPath, String destPath, boolean needCompress) throws Exception {
        BufferedImage image = QrCodeKit.createImage(content, logoPath, needCompress);
        mkdirs(destPath);
        String fileName = new Random().nextInt(99999999) + "." + FORMAT.toLowerCase();
        ImageIO.write(image, FORMAT, new File(destPath + "/" + fileName));
        return fileName;
    }

    /**
     * <p>生成二维码(内嵌LOGO)</p>
     * <p>调用者指定二维码文件名</p>
     *
     * @param content      内容
     * @param logoPath     LOGO地址
     * @param destPath     存放目录
     * @param fileName     二维码文件名
     * @param needCompress 是否压缩LOGO
     */
    public static String encode(String content, String logoPath, String destPath, String fileName, boolean needCompress) throws Exception {
        BufferedImage image = QrCodeKit.createImage(content, logoPath, needCompress);
        mkdirs(destPath);
        fileName = fileName.substring(0, fileName.indexOf(".") > 0 ? fileName.indexOf(".") : fileName.length())
                + "." + FORMAT.toLowerCase();
        ImageIO.write(image, FORMAT, new File(destPath + "/" + fileName));
        return fileName;
    }

    /**
     * <p>当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．</p>
     * <p>(mkdir如果父目录不存在则会抛出异常)</p>
     *
     * @param destPath 存放目录
     */
    public static void mkdirs(String destPath) {
        File file = new File(destPath);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    /**
     * <p>生成二维码(内嵌LOGO)</p>
     *
     * @param content  内容
     * @param logoPath LOGO地址
     * @param destPath 存储地址
     */
    public static String encode(String content, String logoPath, String destPath) throws Exception {
        return QrCodeKit.encode(content, logoPath, destPath, false);
    }

    /**
     * <p>生成二维码</p>
     *
     * @param content      内容
     * @param destPath     存储地址
     * @param needCompress 是否压缩LOGO
     */
    public static String encode(String content, String destPath, boolean needCompress) throws Exception {
        return QrCodeKit.encode(content, null, destPath, needCompress);
    }

    /**
     * <p>生成二维码</p>
     *
     * @param content  内容
     * @param destPath 存储地址
     */
    public static String encode(String content, String destPath) throws Exception {
        return QrCodeKit.encode(content, null, destPath, false);
    }

    /**
     * <p>生成二维码(内嵌LOGO)</p>
     *
     * @param content      内容
     * @param logoPath     LOGO地址
     * @param output       输出流
     * @param needCompress 是否压缩LOGO
     */
    public static void encode(String content, String logoPath, OutputStream output, boolean needCompress)
            throws Exception {
        BufferedImage image = QrCodeKit.createImage(content, logoPath, needCompress);
        ImageIO.write(image, FORMAT, output);
    }

    /**
     * 生成二维码
     *
     * @param content 内容
     * @param output  输出流
     */
    public static void encode(String content, OutputStream output) throws Exception {
        QrCodeKit.encode(content, null, output, false);
    }

    /**
     * <p>解析二维码</p>
     *
     * @param file 二维码图片
     * @return 解析后的字符串
     */
    public static String decode(File file) throws Exception {
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
        result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }

    /**
     * 解析二维码
     *
     * @param path 二维码图片地址
     * @return 解析后的字符串
     */
    public static String decode(String path) throws Exception {
        return QrCodeKit.decode(new File(path));
    }

    public static void main(String[] args) throws Exception {
        String text = "http://www.bing.com/";
        String logo_path = "D:\\testQrCode\\logo.jpg";
        /* 不含Logo */
        QrCodeKit.encode(text, null, "d:\\testQrCode\\", true);
        /* 含Logo，不指定二维码图片名 */
        QrCodeKit.encode(text, logo_path, "d:\\testQrCode\\", true);
        /* 含Logo，指定二维码图片名 */
        QrCodeKit.encode(text, logo_path, "d:\\testQrCode\\", "qrcode_logoed", true);
        /* 测试二维码解释 */
        System.out.println(QrCodeKit.decode("d:\\testQrCode\\qrcode_logoed.png"));
    }
}
