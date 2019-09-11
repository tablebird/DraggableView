package com.tablebird.drag.ref;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tablebird.drag.DraggableView;

/**
 * @author tablebird
 * @date 2019/7/14
 */
public class DraggableViewWeakReference extends HashWeakReference<DraggableView> implements Comparable<DraggableViewWeakReference> {

    public static final DraggableViewWeakReference NULL = new DraggableViewWeakReference(null);

    public DraggableViewWeakReference(DraggableView referent) {
        super(referent);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        DraggableView draggableView = get();
        if (draggableView == null) {
            if (obj instanceof DraggableViewWeakReference && ((DraggableViewWeakReference) obj).get() == null) {
                return true;
            }
            return false;
        }
        DraggableView d = null;
        if (obj instanceof DraggableView) {
            d = (DraggableView) obj;
        } else if (obj instanceof DraggableViewWeakReference) {
            d = ((DraggableViewWeakReference) obj).get();
        }
        return draggableView.equals(d);
    }

    @Override
    public int compareTo(@NonNull DraggableViewWeakReference draggableViewWeakReference) {
        DraggableView draggableView1 = get();
        DraggableView draggableView2 = draggableViewWeakReference.get();
        if (draggableView1 == null && draggableView2 == null) {
            return 0;
        } else if (draggableView1 == null) {
            return -1;
        } else if (draggableView2 == null) {
            return 1;
        } else {
            return draggableView1.compareTo(draggableView2);
        }
    }
}
