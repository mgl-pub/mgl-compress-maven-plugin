package tech.mgl.closure.compiler;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.*;
import tech.mgl.base.BaseMojo;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.google.javascript.jscomp.CommandLineRunner.getDefaultExterns;

/**
 * js 压缩支持类 基于cclosure compiler
 * @author mgl.tech
 * @date 2020-05
 */
public abstract class CompressJs extends BaseMojo {
    private final CompilerOptions compilerOptions = new CompilerOptions();
    //最简单的压缩不做任何改动 只是移除注释 清空空格 最安全的压缩了
    private final CompilationLevel compilationLevel = CompilationLevel.WHITESPACE_ONLY;
    protected String compressJS(String source) throws Exception {
        Compiler compiler = new Compiler();
        compilerOptions.setLineBreak(true);
        compilerOptions.setEmitUseStrict(false);
        compilationLevel.setOptionsForCompilationLevel(compilerOptions);
        compiler.disableThreads();
        compiler.setLoggingLevel(Level.SEVERE);
        StringWriter writer = new StringWriter();
        try {
            List<SourceFile> externsList = getDefaultExterns();
            List<SourceFile> input = new ArrayList<>();
            input.add(SourceFile.fromCode("source.js", source));
            /*externsList.forEach(sourceFile -> {
                System.out.println(sourceFile.getName());
            });*/
            Result result = compiler.compile(externsList, input, compilerOptions);
            if (result.success) {
                writer.write(compiler.toSource());
            } else {
                writer.write(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.flush();
            writer.close();
        }
        return writer.toString();
    }
}
