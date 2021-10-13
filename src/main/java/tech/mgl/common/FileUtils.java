package tech.mgl.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FileUtils {


    /**
     * 快速扫描 文件夹及文件 优化性能
     * @param f
     */
    public static void scanPath(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                File[] fileArray = f.listFiles();
                if (fileArray != null) {
                    for (int i = 0; i < fileArray.length; i++) {
                        // 递归调用
                        scan(fileArray[i]);
                    }
                }
            } else {
                System.out.println(f);
            }
        }
    }


    /**
     * 扫描具体类型的文件
     * @param f
     */
    public static void scan(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                File[] fileArray = f.listFiles();
                if (fileArray != null) {
                    for (int i = 0; i < fileArray.length; i++) {
                        // 递归调用
                        scan(fileArray[i]);
                    }
                }
            } else {

                if ((f.getName().substring(f.getName().lastIndexOf(".") + 1, f
                        .getName().length())).equals("java")) {
                    FileReader reader;
                    try {
                        Thread.sleep(3000);
                        reader = new FileReader(f);
                        BufferedReader Bufferedreader = new BufferedReader(
                                reader);
                        String content = "";
                        while ((content = Bufferedreader.readLine()) != null) {
                            System.out.println(content);
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                    }

                }
            }
        }
    }
}
