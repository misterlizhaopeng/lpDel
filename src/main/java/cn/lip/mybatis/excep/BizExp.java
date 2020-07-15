package cn.lip.mybatis.excep;

public class BizExp extends RuntimeException {

    private Integer code;

    private String errMsg;

    public BizExp(){}
    public BizExp(Integer code, String errMsg) {
        super();
        this.code = code;
        this.errMsg = errMsg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
