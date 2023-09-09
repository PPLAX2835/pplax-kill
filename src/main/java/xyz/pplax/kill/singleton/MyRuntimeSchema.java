package xyz.pplax.kill.singleton;

import io.protostuff.runtime.RuntimeSchema;
import xyz.pplax.kill.entity.PPLAXKill;

public class MyRuntimeSchema {
    private static MyRuntimeSchema ourInstance = new MyRuntimeSchema();

    private RuntimeSchema<PPLAXKill> goodsRuntimeSchema;


    public static MyRuntimeSchema getInstance() {
        return ourInstance;
    }

    private MyRuntimeSchema() {
        RuntimeSchema<PPLAXKill> seckillSchemaVar = RuntimeSchema.createFrom(PPLAXKill.class);
        setGoodsRuntimeSchema(seckillSchemaVar);
    }

    public RuntimeSchema<PPLAXKill> getGoodsRuntimeSchema() {
        return goodsRuntimeSchema;
    }

    private void setGoodsRuntimeSchema(RuntimeSchema<PPLAXKill> goodsRuntimeSchema) {
        this.goodsRuntimeSchema = goodsRuntimeSchema;
    }
}
