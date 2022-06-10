package tech.mgl.exception;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

@Slf4j
public class MGLErrorReport implements ErrorReporter {

    @Override
    public void warning(String s, String s1, int i, String s2, int i1) {
        log.warn(s,s1,s2,i,i1);
    }

    @Override
    public void error(String s, String s1, int i, String s2, int i1) {
        log.error("error:" ,s);
        log.error("error:" ,s1);
        log.error("error:" ,s2);
        log.error("error:" ,i);
        log.error("error:" ,i1);
    }

    @Override
    public EvaluatorException runtimeError(String s, String s1, int i, String s2, int i1) {
        log.error("runtimeError",s,s1,s2,i,i1);
        return null;
    }
}
