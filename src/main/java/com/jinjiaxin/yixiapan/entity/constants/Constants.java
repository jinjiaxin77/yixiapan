package com.jinjiaxin.yixiapan.entity.constants;

/**
 * @author jjx
 * @Description
 * @create 2023/9/14 14:40
 */

public class Constants {

    public static final String CHECK_CODE_KEY = "check_code_key";

    public static final String CHECK_CODE_KEY_EMAIL = "check_code_key_email";

    public static final Integer LENGTH_5 = 5;

    public static final Integer LENGTH_15 = 15;

    public static final Integer LENGTH_10 = 10;

    public static final Integer LENGTH_20 = 20;

    public static final Integer LENGTH_30 = 30;

    public static final Integer ZERO = 0;

    public static final Integer ONE = 1;

    public static final String TYPE_STRING = "java.lang.String";

    public static final String TYPE_INTEGER = "java.lang.Integer";

    public static final String TYPE_LONG = "java.lang.Long";

    public static final String TYPE_BOOLEAN = "java.lang.Boolean";

    public static final String SESSION_KEY = "session_key";

    public static final String FILE_FOLDER_FILE = "/file/";

    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String FILE_FOLDER_TEMP = "/temp/";

    public static final String AVATAR_SUFFIX = ".jpg";

    public static final String DEFAULT_AVATAR = "avatar_default.jpg";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";

    public static final String REDIS_KEY_SYS_SETTING = "yixiapan:syssetting:";

    public static final String REDIS_KEY_USER_SPACE_USED = "yixiapan:user:spaceuse:";

    public static final String REDIS_KEY_FILE_TEMP_SIZE = "yixiapan:user:file:temp:size:";

    public static final Integer REDIS_KEY_EXPIRES_MINUTE = 60;

    public static final Integer REDIS_KEY_EXPIRES_HOUR = 60 * REDIS_KEY_EXPIRES_MINUTE;

    public static final Integer REDIS_KEY_EXPIRES_DAY = 24 * REDIS_KEY_EXPIRES_HOUR;

    public static final Long MB = 1024 * 1024L;

    public static final String VIEW_OBJ_RESULT_KEY = "result";
    public static final Object TS_NAME = "index.ts";
}
