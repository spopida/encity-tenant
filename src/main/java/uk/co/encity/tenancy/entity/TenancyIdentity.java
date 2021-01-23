package uk.co.encity.tenancy.entity;

import org.bson.types.ObjectId;

public class TenancyIdentity {
    private String tenancyId;
    private String tenancyName;

    public TenancyIdentity(String name) {
        this.tenancyId = new ObjectId().toHexString();
        this.tenancyName = name;
    }

    public String getIdentity() { return this.tenancyId; }
    public String getTenancyName() { return this.tenancyName; }
}
