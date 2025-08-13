package io.speer.miniDNS.common.enums;

public enum TypeEnum {
    A(0, "A"),
    CNAME(1, "CNAME");

    private int typeId;
    private String typeName;

    TypeEnum(int typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public int getTypeId(){
        return this.typeId;
    }
    public String getTypeName() {
        return this.typeName;
    }
}
