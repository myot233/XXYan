package com.github;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SimpleGeometricImageProvider {
    public static BufferedImage apply(String key) {
        try {
            String[] args = key.split(" ");
            String type = args[0];
            if (type.equals("矩形")) {
                String[] sizeParts = args[1].split(",");
                int width = Integer.parseInt(sizeParts[0]);
                int height = Integer.parseInt(sizeParts[1]);

                String[] colorParts = args[2].split(",");
                String colorType = colorParts[0];
                Paint paint;
                if (colorType.contains("渐变")) {
                    Color colorFrom = new Color(Integer.parseInt(colorParts[1]),
                            Integer.parseInt(colorParts[2]),
                            Integer.parseInt(colorParts[3]),
                            Integer.parseInt(colorParts[4]));
                    Color colorTo = new Color(Integer.parseInt(colorParts[5]),
                            Integer.parseInt(colorParts[6]),
                            Integer.parseInt(colorParts[7]),
                            Integer.parseInt(colorParts[8]));
                    if (colorType.equals("上下渐变")) {
                        paint = new GradientPaint((int)(width / 2), 0, colorFrom, (int)(width / 2), height, colorTo);
                    } else if (colorType.equals("左右渐变")) {
                        paint = new GradientPaint(0, (int)(height / 2), colorFrom, width, (int)(height / 2), colorTo);
                    } else {
                        paint = new GradientPaint((int)(width / 2), 0, colorFrom, (int)(width / 2), height, colorTo);
                    }
                } else {
                    paint = new Color(Integer.parseInt(colorParts[0]),
                            Integer.parseInt(colorParts[1]),
                            Integer.parseInt(colorParts[2]),
                            Integer.parseInt(colorParts[3]));
                }

                BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D graphics = result.createGraphics();
                graphics.setPaint(paint);
                graphics.fillRect(0, 0, result.getWidth(), result.getHeight());
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("处理key=%s时异常", key), e);
        }
        throw new RuntimeException(String.format("无法处理key=%s", key));
    }
}
