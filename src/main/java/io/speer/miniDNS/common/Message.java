package io.speer.miniDNS.common;

public class Message {

    public static final String NOT_ALLOWED = "This host type is not supported!";
    public static final String NULL_VALUE = "Oh no! You have not entered a value.";
    public static final String NOT_FOUND = "Sorry! No data found matching your request.";
    public static final String EXIST_CNAME = "Unable to add CName record with existing record entry.";
    public static final String EXIST_RECORD = "Unable to add a record with existing CName entry.";
    public static final String INVL_LNK = "Yikes!! Cannot persist record as circular link detected";

    public static final String ERR_FORM = "Yikes!! Invalid form field value(s) entered.";
    public static final String ERR_EXIST = "This record already exists.";
}
