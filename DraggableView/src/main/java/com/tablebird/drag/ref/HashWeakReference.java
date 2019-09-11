package com.tablebird.drag.ref;



import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * @author tablebird
 * @date 2019/7/14
 */
public class HashWeakReference<T> extends WeakReference<T> {
    private static final String NULL = "NULL";
    public HashWeakReference(T referent) {
        super(referent);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (get() != null) {
            return get().hashCode();
        }
        return NULL.hashCode();
    }
}
