package com.superyoudi.common.constants;

public class RedisConstants {

    public static final String AI_SUGGEST_CACHE_KEY = "student:home:ai:suggest:";
    public static final String AI_DIAGNOSE_CACHE_KEY = "student:task:ai:diagnose:";

    /** AI学习建议缓存有效期 6小时 */
    public static final long AI_SUGGEST_CACHE_HOUR = 6L;
    /** AI学情诊断缓存有效期 6小时 */
    public static final long AI_DIAGNOSE_CACHE_HOUR = 6L;
}
