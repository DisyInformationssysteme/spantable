package net.disy.oss.spantable;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CellSpanTest {
    @Test
    public void nonIntersectingSpans() {
        var first = new CellSpan(0,0,2,2);
        var second = new CellSpan(2,2,2,2);
        assertThat(first.intersects(second))
                .isFalse();
    }

    @Test
    public void intersectingSpans() {
        var first = new CellSpan(0, 0, 2, 2);
        var second = new CellSpan(1, 1,2,2);
        assertThat(first.intersects(second))
                .isTrue();
    }
}
