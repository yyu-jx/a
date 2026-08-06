package com.superyoudi.common.exception;

/**
 * 学生重复加入课程异常
 */
public class RepeatException extends BaseException {

    public RepeatException(String message) {
        super(message);
    }
}