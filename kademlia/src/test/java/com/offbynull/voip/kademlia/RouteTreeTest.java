package com.offbynull.voip.kademlia;

import static com.offbynull.voip.kademlia.TestUtils.verifyActivityChangeSetAdded;
import static com.offbynull.voip.kademlia.TestUtils.verifyActivityChangeSetCounts;
import static com.offbynull.voip.kademlia.TestUtils.verifyActivityChangeSetRemoved;
import static com.offbynull.voip.kademlia.TestUtils.verifyPrefixMatches;
import java.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class RouteTreeTest {
    
    private static final Node NODE_0000 = new Node(Id.createFromLong(0x00L, 4), "0"); // 0000
    private static final Node NODE_0001 = new Node(Id.createFromLong(0x01L, 4), "1");
    private static final Node NODE_0010 = new Node(Id.createFromLong(0x02L, 4), "2");
    private static final Node NODE_0011 = new Node(Id.createFromLong(0x03L, 4), "3");
    private static final Node NODE_0100 = new Node(Id.createFromLong(0x04L, 4), "4");
    private static final Node NODE_0101 = new Node(Id.createFromLong(0x05L, 4), "5");
    private static final Node NODE_0110 = new Node(Id.createFromLong(0x06L, 4), "6");
    private static final Node NODE_0111 = new Node(Id.createFromLong(0x07L, 4), "7");
    private static final Node NODE_1000 = new Node(Id.createFromLong(0x08L, 4), "8"); // 0000
    private static final Node NODE_1001 = new Node(Id.createFromLong(0x09L, 4), "9");
    private static final Node NODE_1010 = new Node(Id.createFromLong(0x0AL, 4), "A");
    private static final Node NODE_1011 = new Node(Id.createFromLong(0x0BL, 4), "B");
    private static final Node NODE_1100 = new Node(Id.createFromLong(0x0CL, 4), "C");
    private static final Node NODE_1101 = new Node(Id.createFromLong(0x0DL, 4), "D");
    private static final Node NODE_1110 = new Node(Id.createFromLong(0x0EL, 4), "E");
    private static final Node NODE_1111 = new Node(Id.createFromLong(0x0FL, 4), "F");
    
    private static final Instant BASE_TIME = Instant.ofEpochMilli(0L);
    
    private RouteTree fixture;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    public RouteTreeTest() {
        SimpleRouteTreeSpecificationSupplier specSupplier = new SimpleRouteTreeSpecificationSupplier(NODE_0000.getId(), 2, 2, 2);
        fixture = new RouteTree(NODE_0000.getId(), specSupplier, specSupplier);        
    }

    @Test
    public void mustAddNodesToProperBuckets() throws Throwable {
        RouteTreeChangeSet res;
        
        res = fixture.touch(BASE_TIME.plusMillis(1L), NODE_0001);
        verifyPrefixMatches(res.getKBucketPrefix(), "0001");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 0, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0001);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
        
        res = fixture.touch(BASE_TIME.plusMillis(2L), NODE_0010);
        verifyPrefixMatches(res.getKBucketPrefix(), "001");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 0, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0010);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
        
        res = fixture.touch(BASE_TIME.plusMillis(3L), NODE_0011);
        verifyPrefixMatches(res.getKBucketPrefix(), "001");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 0, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0011);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
        
        res = fixture.touch(BASE_TIME.plusMillis(4L), NODE_0100);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 0, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0100);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
        
        res = fixture.touch(BASE_TIME.plusMillis(5L), NODE_0101);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 0, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0101);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
        
        res = fixture.touch(BASE_TIME.plusMillis(6L), NODE_0110);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 0, 0, 0);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 1, 0, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getCacheChangeSet(), NODE_0110);
        
        res = fixture.touch(BASE_TIME.plusMillis(7L), NODE_0111);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 0, 0, 0);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 1, 0, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getCacheChangeSet(), NODE_0111);
    }

    @Test
    public void mustProperlyReplaceStaleNodesWithCacheIfAvailable() throws Throwable {
        fixture.touch(BASE_TIME.plusMillis(1L), NODE_0001);
        fixture.touch(BASE_TIME.plusMillis(2L), NODE_0010);
        fixture.touch(BASE_TIME.plusMillis(3L), NODE_0011);
        fixture.touch(BASE_TIME.plusMillis(4L), NODE_0100);
        fixture.touch(BASE_TIME.plusMillis(5L), NODE_0101);
        fixture.touch(BASE_TIME.plusMillis(6L), NODE_0110);
        fixture.touch(BASE_TIME.plusMillis(7L), NODE_0111);
        
        RouteTreeChangeSet res;
        res = fixture.stale(NODE_0100);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 1, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0111);
        verifyActivityChangeSetRemoved(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0100);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 1, 0);
        verifyActivityChangeSetRemoved(res.getKBucketChangeSet().getCacheChangeSet(), NODE_0111);
        
        res = fixture.stale(NODE_0101);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 1, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0110);
        verifyActivityChangeSetRemoved(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0101);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 1, 0);
        verifyActivityChangeSetRemoved(res.getKBucketChangeSet().getCacheChangeSet(), NODE_0110);
    }

    @Test
    public void mustProperlyReplaceStaleNodesWithCacheWhenAvailable() throws Throwable {
        fixture.touch(BASE_TIME.plusMillis(1L), NODE_0001);
        fixture.touch(BASE_TIME.plusMillis(2L), NODE_0010);
        fixture.touch(BASE_TIME.plusMillis(3L), NODE_0011);
        fixture.touch(BASE_TIME.plusMillis(4L), NODE_0100);
        fixture.touch(BASE_TIME.plusMillis(5L), NODE_0101);
        
        RouteTreeChangeSet res;
        res = fixture.stale(NODE_0100);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 0, 0, 0);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
        
        res = fixture.stale(NODE_0101);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 0, 0, 0);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
        
        res = fixture.touch(BASE_TIME.plusMillis(6L), NODE_0110);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 1, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0110);
        verifyActivityChangeSetRemoved(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0100);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
        
        res = fixture.touch(BASE_TIME.plusMillis(7L), NODE_0111);
        verifyPrefixMatches(res.getKBucketPrefix(), "01");
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getBucketChangeSet(), 1, 1, 0);
        verifyActivityChangeSetAdded(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0111);
        verifyActivityChangeSetRemoved(res.getKBucketChangeSet().getBucketChangeSet(), NODE_0101);
        verifyActivityChangeSetCounts(res.getKBucketChangeSet().getCacheChangeSet(), 0, 0, 0);
    }
    
    @Test
    public void mustRejectIfTouchingSelfId() throws Throwable {
        expectedException.expect(IllegalArgumentException.class);
        fixture.touch(BASE_TIME.plusMillis(1L), NODE_0000);
    }

    @Test
    public void mustRejectIfStalingSelfId() throws Throwable {
        expectedException.expect(IllegalArgumentException.class);
        fixture.stale(NODE_0000);
    }
}
