package com.labdatahub.component.collect.adapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.labdatahub.component.collect.CollectionPoint;

/**
 * 连续地址合并：把一批「起始地址 + 结束地址」的点位按协议单次读上限聚成若干块
 *
 * <p>同属一个采集单元（同从站/同数据区）的点位，只要地址重叠、相邻（或空档不超过配置的
 * mergeGap）且整块不超过协议上限，就合成<b>一次读取</b>；读回来再按各点位自己的地址区间切片，
 * 上报内容与旧链路逐点读完全一致。
 *
 * <p>mergeGap 默认 0：只合并重叠/相邻的地址，绝不往中间补读设备未实现的地址
 * （补读会让整块读失败，一个空档毁掉一批点位）。
 */
final class RangeMerge {

    private RangeMerge() {
        throw new UnsupportedOperationException("该类为静态工具类，禁止实例化");
    }

    /**
     * 一个点位的地址区间
     */
    static final class Span {

        /** 起始地址（含） */
        final int start;

        /** 结束地址（含） */
        final int end;

        /** 所属点位 */
        final CollectionPoint point;

        Span(int start, int end, CollectionPoint point) {
            this.start = start;
            this.end = end;
            this.point = point;
        }

        /**
         * 区间长度（地址个数）
         *
         * @return 长度
         */
        int size() {
            return end - start + 1;
        }
    }

    /**
     * 一个读取块
     */
    static final class Cluster {

        /** 块起始地址（含） */
        int start;

        /** 块结束地址（含） */
        int end;

        /** 块内点位区间 */
        final List<Span> spans = new ArrayList<>();

        Cluster(Span span) {
            this.start = span.start;
            this.end = span.end;
            this.spans.add(span);
        }

        /**
         * 块长度（地址个数）
         *
         * @return 长度
         */
        int size() {
            return end - start + 1;
        }

        /**
         * 能否并入下一个区间
         *
         * @param span 待并入区间
         * @param cap 单次读上限
         * @param gap 允许的地址空档
         * @return true=可以合并
         */
        boolean canTake(Span span, int cap, int gap) {
            int newStart = Math.min(start, span.start);
            int newEnd = Math.max(end, span.end);
            // 区间已按起始地址排序，空档只出现在「本块尾」与「新区间头」之间；包含关系时空档为负，按 0 算
            int hole = Math.max(0, span.start - end - 1);
            return newEnd - newStart + 1 <= cap && hole <= gap;
        }

        /**
         * 并入一个区间
         *
         * @param span 区间
         */
        void add(Span span) {
            start = Math.min(start, span.start);
            end = Math.max(end, span.end);
            spans.add(span);
        }
    }

    /**
     * 按起始地址排序后贪心合并
     *
     * @param spans 点位区间
     * @param cap 单次读取的地址数上限
     * @param gap 允许跳过的地址空档
     * @return 读取块列表
     */
    static List<Cluster> cluster(List<Span> spans, int cap, int gap) {
        List<Span> sorted = new ArrayList<>(spans);
        sorted.sort(Comparator.comparingInt(span -> span.start));
        List<Cluster> clusters = new ArrayList<>();
        Cluster current = null;
        for (Span span : sorted) {
            if (current != null && current.canTake(span, cap, gap)) {
                current.add(span);
                continue;
            }
            // 单点位自身就超过上限时仍然单独成块：让读取按协议原样报错，与旧链路逐点读的表现一致
            current = new Cluster(span);
            clusters.add(current);
        }
        return clusters;
    }
}
