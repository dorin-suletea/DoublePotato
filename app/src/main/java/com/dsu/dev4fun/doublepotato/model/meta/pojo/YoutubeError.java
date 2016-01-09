package com.dsu.dev4fun.doublepotato.model.meta.pojo;


public enum YoutubeError {
    NONE(0),
    TOO_BIG(1),
    FAIL_DOWNLOAD(2),
    FAIL_CONVERT(3),
    FILE_MISSING(4);

    private YoutubeError(int code) {
        errorCode = code;
    }

    private int errorCode;

    public static YoutubeError getErrorByCode(int code){
        switch (code){
            case 0:
                return NONE;
            case 1:
                return TOO_BIG;
            case 2:
                return FAIL_DOWNLOAD;
            case 3:
                return FAIL_CONVERT;
            case 4:
                return FILE_MISSING;
        }
        return NONE;
    }

    public int getErrorCode(){
        return errorCode;
    }
}
