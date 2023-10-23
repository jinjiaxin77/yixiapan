package com.jinjiaxin.yixiapan.utils;

import com.jinjiaxin.yixiapan.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;


@Slf4j
public class ProcessUtils {

    public static String executeCommand(String cmd, Boolean outprintLog) throws BusinessException{
        if(StringUtils.isEmpty(cmd)){
            log.error("--- 指令执行失败，因为要执行的FFmpeg指令为空！ ---");
            return null;
        }

        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try{
            process = Runtime.getRuntime().exec(cmd);
            //执行ffmpeg指令
            //取出输出流和错误流的信息
            //注意，必须要去除ffmpeg在执行命令过程中产生的输出信息，如果不取的话当输出流信息填满jvm存储输出流信息的缓冲区时，线程会阻塞
            PrintStream errorStream = new PrintStream(process.getErrorStream());
            PrintStream inputStream = new PrintStream(process.getInputStream());
            errorStream.start();
            inputStream.start();

            process.waitFor();

            String result = errorStream.stringBuffer.append(inputStream.stringBuffer + "\n").toString();

            if(outprintLog){
                log.info("执行命令：{}，已执行完毕，执行结果：{}",cmd,result);
            }else{
                log.info("执行命令：{}，已执行完毕",cmd);
            }
            return result;

        }catch(Exception e){
            e.printStackTrace();
            throw new BusinessException("视频转换失败");
        }finally {
            if(process != null){
                ProcessKiller killer = new ProcessKiller(process);
                runtime.addShutdownHook(killer);
            }
        }
    }

    private static class ProcessKiller extends Thread{
        private Process process;

        public ProcessKiller(Process process){
            this.process = process;
        }

        @Override
        public void run(){
            this.process.destroy();
        }
    }

    static class PrintStream extends Thread{
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();

        public PrintStream(InputStream inputStream){
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try{
                if(inputStream == null){
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    stringBuffer.append(line);
                }
            } catch (IOException e) {
                log.error("读取输入流出错了！错误信息：" + e.getMessage());
            }finally {
                try{
                    if(bufferedReader != null){
                        bufferedReader.close();
                    }
                    if(inputStream != null){
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("调用PrintStream读取输出流后，关闭流时出错！");
                }
            }
        }
    }

}
