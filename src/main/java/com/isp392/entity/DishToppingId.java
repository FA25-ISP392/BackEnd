// DishToppingId.java
package com.isp392.entity;

import java.io.Serializable;
import java.util.Objects;

public class DishToppingId implements Serializable {
    private long dishId;
    private long toppingId;

    public DishToppingId() {}
    public DishToppingId(long dishId, long toppingId) {
        this.dishId = dishId;
        this.toppingId = toppingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DishToppingId)) return false;
        DishToppingId that = (DishToppingId) o;
        return dishId == that.dishId && toppingId == that.toppingId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, toppingId);
    }
}
