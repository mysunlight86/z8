package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_binary;

import java.io.InputStream;

public class BinaryField extends Field {

    public static class CLASS<T extends BinaryField> extends Field.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(BinaryField.class);
            setAttribute(Native, BinaryField.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new BinaryField(container);
        }
    }

    public BinaryField(IObject container) {
        super(container);
        setDefault(new binary());
    }

    public binary z8_getDefault() {
        return (binary) super.getDefault();
    }

    @Override
    public primary getDefault() {
        return z8_getDefault();
    }

    @Override
    public FieldType type() {
        return FieldType.Binary;
    }

    @Override
    public primary get() {
        return z8_get();
    }

    public void set(InputStream inputStream){
        set(new binary(inputStream));
    }

    public binary z8_get() {
        return (binary) internalGet();
    }

    @Override
    public String sqlType(DatabaseVendor vendor) {
        String name = type().vendorType(vendor);

        if (vendor == DatabaseVendor.SqlServer) {
            return name + "(MAX)";
        }
        return name;
    }

    public sql_binary sql_binary() {
        return new sql_binary(new SqlField(this));
    }


}