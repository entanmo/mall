package org.linlinjava.litemall.db.util;

public class OrderEtmHandleOption {
    private boolean cancel = false;      // 取消操作
    private boolean delete = false;      // 删除操作
    private boolean pay = false;         // 支付操作
    private boolean confirm = false;    // 确认操作
    private boolean verify = false;     //
    private boolean reject = false;     //
    public boolean isReject() {
        return reject;
    }

    public void setReject(boolean reject) {
        this.reject = reject;
    }


    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isPay() {
        return pay;
    }

    public void setPay(boolean pay) {
        this.pay = pay;
    }


    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    public boolean isVerify() {
        return verify;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

}
