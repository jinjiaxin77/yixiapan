package com.jinjiaxin.yixiapan.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class ScaleFilter {

    public static void createCover4Video(File sourceFile, Integer width, File targetFile){
        try{
            String cmd = "ffmpeg -i %s -y -vframes 1 -vf scale=%d:%d/a %s";
            ProcessUtils.executeCommand(String.format(cmd,sourceFile.getAbsoluteFile(),width,width,targetFile.getAbsoluteFile()),false);
        }catch (Exception e){
            log.error("生成视频封面失败",e);
        }
    }

    public static Boolean createThumbnailWidthFFmpeg(File file, int thumbnailWidth, File targetFile, Boolean delSource){
        try{
            BufferedImage src = ImageIO.read(file);
            int sorceW = src.getWidth();
            int sorceH = src.getHeight();
            if(sorceW <= thumbnailWidth){
                return false;
            }
            compressImage(file,thumbnailWidth,targetFile,delSource);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void compressImage(File file, Integer width, File targetFile, Boolean delSource){
        try{
            String cmd = "ffmpeg -i %s -vf scale=%d:-1 %s -y";
            ProcessUtils.executeCommand(String.format(cmd,file.getAbsoluteFile(),width,targetFile.getAbsoluteFile()),false);
            if(delSource){
                FileUtils.forceDelete(file);
            }
        }catch(Exception e){
            log.error("压缩图片失败");
        }
    }

}
