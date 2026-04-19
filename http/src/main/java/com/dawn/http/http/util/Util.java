package com.dawn.http.http.util;

import android.text.TextUtils;
import com.dawn.http.http.net.NameValuePair;
import java.net.URLEncoder;
import java.util.List;
import okhttp3.Credentials;

public class Util {
    /**
     * 获取文件名称
     *
     * @param filename 文件路径
     * @return 文件名（不含路径）
     */
    public static String getFileName(String filename) {
        if (TextUtils.isEmpty(filename)) {
            return null;
        }
        // 兼容 "/" 和 "\" 路径分隔符
        int start = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (start != -1) {
            return filename.substring(start + 1);
        } else {
            return filename;
        }
    }

    /**
     * 拼接公共参数
     *
     * @param url
     * @param commonField
     * @return
     */
    public static String getMosaicParameter(String url, List<NameValuePair> commonField) {
        if (TextUtils.isEmpty(url))
            return "";
        if (commonField == null || commonField.isEmpty()) {
            return url;
        }
        if (url.endsWith("?") || url.endsWith("&")) {
            // URL已经以分隔符结尾，直接拼接
        } else if (url.contains("?")) {
            url = url + "&";
        } else {
            url = url + "?";
        }
        url += getCommonFieldString(commonField);
        return url;
    }

    /**
     * @param commonField
     * @return
     */
    private static String getCommonFieldString(List<NameValuePair> commonField) {
        StringBuilder sb = new StringBuilder();
        try {
            int i = 0;
            for (NameValuePair item : commonField) {
                if (item.getName() == null || item.getValue() == null) {
                    continue;
                }
                if (i > 0) {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(item.getName(), "utf-8"));
                sb.append('=');
                sb.append(URLEncoder.encode(item.getValue(), "utf-8"));
                i++;
            }
        } catch (Exception e) {
            // ignore encoding exception
        }
        return sb.toString();
    }

    public static String getAuthorization(String AUTH_UM, String AUTH_PW) {
        return Credentials.basic(AUTH_UM, AUTH_PW);
    }
}
