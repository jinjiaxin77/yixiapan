package com.jinjiaxin.yixiapan.service;

public interface EmailCodeService {
    void sendEmailCode(String email, Integer type);

    boolean checkCode(String email, String code);
}
