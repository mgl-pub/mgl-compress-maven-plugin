package tech.mgl.closure

import java.io.File
import javax.annotation.processing.FilerException

class NewFileChecker {
    fun showListFile(dir: File): ArrayList<String> {
        val list = ArrayList<String>(0);
        //查找参数文件是否存在,只检查第一个入参
        if(!dir.exists()) {
            throw FilerException("找不到文件");
        }
        //如果是目录那么进行递归调用
        if(dir.isDirectory) {
            //获取目录下的所有文件
            val f = dir.listFiles();
            //进行递归调用,最后总会返回一个list
        }else {//不是目录直接添加进去
            list.add(dir.toString());
        }
        return list;
    }
}