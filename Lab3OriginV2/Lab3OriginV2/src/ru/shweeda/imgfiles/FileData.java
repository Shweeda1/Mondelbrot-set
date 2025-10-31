package ru.shweeda.imgfiles;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileData {
    public static void saveAsImage(BufferedImage img, File file) throws NonImageFormatException, IOException {
        var filename = file.getName();
        var nameParts = filename.split("\\.");
        var ext = nameParts[nameParts.length - 1];
        var writers = ImageIO.getImageWritersByFormatName(ext);
        if (!writers.hasNext()){
            throw new NonImageFormatException("Указанный формат не может быть использован для сохранения изображения");
        }
        var writer = writers.next();
        var param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.98f);
        try (var os = ImageIO.createImageOutputStream(file)) {
            writer.setOutput(os);
            writer.write(
                    null,
                    new IIOImage(img, null, null),
                    param
            );
            writer.dispose();
        }
    }
}
