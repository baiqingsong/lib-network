package com.dawn.http.http.net;

public class NameValuePair {
    private String name;//请求名称
    private String value;//请求值
    private boolean isFile = false;//是否是文件

    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public NameValuePair(String name, String value, boolean isFile) {
        this.name = name;
        this.value = value;
        this.isFile = isFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    @Override
    public String toString() {
        return "NameValuePair{name='" + name + "', value='" + value + "', isFile=" + isFile + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NameValuePair that = (NameValuePair) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
