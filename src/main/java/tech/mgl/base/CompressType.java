package tech.mgl.base;

public  enum CompressType {
    CSS("css"),JS("js"),HTML("html");

    private final String t;

    CompressType(String t) {
        this.t = t;
    }


    public String getT() {
        return t;
    }
}
