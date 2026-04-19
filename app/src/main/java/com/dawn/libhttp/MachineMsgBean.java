package com.dawn.libhttp;

public class MachineMsgBean {
    private int code;
    private String address;
    private String sn;
    private CMSetting cm_setting;//相关参数设置

    public int getCode() {
        return code;
    }

    public String getAddress() {
        return address;
    }

    public String getSn() {
        return sn;
    }

    public CMSetting getCm_setting() {
        return cm_setting;
    }

    public class CMSetting{
        private String img_template_1;//背景1
        private String img_template_2;//背景2
        private String img_template_3;//背景3
        private String img_template_4;//背景4
        private String img_template_5;//背景5
        private String img_template_6;//背景6
        private String img_template_7;//背景6
        private String img_template_8;//背景6
        private String img_template_9;//背景6
        private String img_template_10;//背景6

        public String getImg_template_1() {
            return img_template_1;
        }

        public String getImg_template_2() {
            return img_template_2;
        }

        public String getImg_template_3() {
            return img_template_3;
        }

        public String getImg_template_4() {
            return img_template_4;
        }

        public String getImg_template_5() {
            return img_template_5;
        }

        public String getImg_template_6() {
            return img_template_6;
        }

        public String getImg_template_7() {
            return img_template_7;
        }

        public String getImg_template_8() {
            return img_template_8;
        }

        public String getImg_template_9() {
            return img_template_9;
        }

        public String getImg_template_10() {
            return img_template_10;
        }
    }
}
