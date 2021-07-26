package org.point.segment;

import java.util.ArrayList;
import java.util.List;

/**
 * 分段-工具类
 * 1.一般用于多线程分割任务时使用
 */
public class SegmentUtil {

    // 分段阈值(1千)
    public static final int segmentSize = 1_000;

    /**
     * 分段操作
     * @param segments 返回按阈值分段记录集合
     * @param list 总记录集合
     * @param start 开始索引
     * @param <T> 泛型
     * @return List<org.point.segment.Segment<T>>
     */
    public static <T> List<Segment<T>> segment(List<Segment<T>> segments, List<T> list, int start) {
        if ( segments == null ) {
            segments = new ArrayList<>();
        }
        // 总记录数
        int size = list.size();
        // 总记录数 == 开始索引:说明记录已经读完,直接返回结果
        if ( size == start ) {
            return segments;
        }
        // 记录余量
        int remain = size - start;
        if ( remain >= segmentSize) {
            // 余量 >= 分段阈值
            int end = start + segmentSize;
            List<T> sub = list.subList(start, end);
            segments.add( new Segment<T>().build(sub) );
            // 开始索引+阈值
            start = end;
            // 递归分段
            return segment(segments, list, start);
        } else {
            // 余量 < 分段阈值
            List<T> sub = list.subList(start, size);
            segments.add( new Segment<T>().build(sub) );
            return segments;
        }
    }

    private SegmentUtil() {}
}
