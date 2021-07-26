package org.point.segment;

import java.io.Serializable;
import java.util.List;

/**
 * 分段对象
 */
public class Segment<T> implements Serializable {

    private static final long serialVersionUID = 2029863093925127740L;

    private List<T> data;

    public List<T> getData() {
        return data;
    }

    public Segment<T> build(List<T> data) {
        this.data = data;
        return this;
    }
}
