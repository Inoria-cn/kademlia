/*
 * Copyright (c) 2015, Kasra Faghihi, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.offbynull.voip.core;

import com.offbynull.voip.core.ChangeSet.UpdatedEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.lang3.Validate;

public final class LeastRecentlySeenSet {
    private final Id baseId;
    private final LinkedList<Entry> entries;

    private int maxSize;

    public LeastRecentlySeenSet(Id baseId, int maxSize) {
        Validate.notNull(baseId);
        Validate.isTrue(maxSize >= 0);
        
        this.baseId = baseId;
        this.maxSize = maxSize;

        this.entries = new LinkedList<>();
    }
    
    public ChangeSet touch(Instant time, Node node) throws EntryConflictException {
        Validate.notNull(time);
        Validate.notNull(node);
        
        Id nodeId = node.getId();
        String nodeLink = node.getLink();
        
        Validate.isTrue(nodeId.getBitLength() == baseId.getBitLength());
        Validate.isTrue(!nodeId.equals(baseId));
        
        // TODO: You can make this way more efficient if you used something like MultiTreeSet (guava) and sorted based on entry time

        // Remove existing entry
        Entry oldEntry = null;
        ListIterator<Entry> it = entries.listIterator();
        while (it.hasNext()) {
            Entry entry = it.next();

            Id entryId = entry.getNode().getId();
            String entryLink = entry.getNode().getLink();

            if (entryId.equals(nodeId)) {
                if (!entryLink.equals(nodeLink)) {
                    // if ID exists but link for ID is different
                    throw new EntryConflictException(entry);
                }

                // remove
                it.remove();
                oldEntry = entry;
                break;
            }
        }

        
        // Add entry
        Entry newEntry = new Entry(node, time);
        it = entries.listIterator();
        boolean added = false;
        while (it.hasNext()) {
            Entry entry = it.next();

            if (entry.getLastSeenTime().isAfter(time)) {
                it.previous(); // move back 1 space, we want to add to element just before entry
                it.add(newEntry);
                added = true;
                break;
            }
        }

        if (!added) { // special case where newEntry needs to be added at the end of entries, not handled by loop above
            entries.addLast(newEntry);
        }

        
        // Set has become too large, remove the item with the latest time
        Entry discardedEntry = null;
        if (entries.size() > maxSize) {
            // if the node removed with the latest time is the one we just added, then report that node couldn't be added
            discardedEntry = entries.removeLast();
            if (discardedEntry.equals(newEntry)) {
                return ChangeSet.NO_CHANGE;
            }
        }

        
        // Add successful
        if (oldEntry != null) {
            // updated existing node
            Validate.validState(discardedEntry == null); // sanity check, must not have discarded anything
            return ChangeSet.updated(new UpdatedEntry(newEntry.getNode(), oldEntry.getLastSeenTime(), newEntry.getLastSeenTime()));
        } else {
            // added new node
            Validate.validState(oldEntry == null); // sanity check, node being touched must not have already existed
            Collection<Entry> addedEntries = Collections.singletonList(newEntry);
            Collection<Entry> removedEntries = discardedEntry == null ? Collections.emptyList() : Collections.singletonList(discardedEntry);
            Collection<UpdatedEntry> updatedEntries = Collections.emptyList();
            return new ChangeSet(addedEntries, removedEntries, updatedEntries);
        }
    }

    public ChangeSet remove(Node node) throws EntryConflictException {
        Validate.notNull(node);
        
        Id nodeId = node.getId();
        String nodeLink = node.getLink();
        
        ListIterator<Entry> it = entries.listIterator();
        while (it.hasNext()) {
            Entry entry = it.next();

            Id entryId = entry.getNode().getId();
            String entryLink = entry.getNode().getLink();

            if (entryId.equals(nodeId)) {
                if (!entryLink.equals(nodeLink)) {
                    // if ID exists but link for ID is different
                    throw new EntryConflictException(entry);
                }

                // remove
                it.remove();
                return ChangeSet.removed(entry);
            }
        }
        
        return ChangeSet.NO_CHANGE;
    }
    
    public ChangeSet resize(int maxSize) {
        Validate.isTrue(maxSize >= 0);
        
        int discardCount = this.maxSize - maxSize;
        
        List<Entry> removed = new LinkedList<>();
        for (int i = 0; i < discardCount; i++) {
            Entry removedEntry = entries.removeFirst(); // remove node that hasn't been touched the longest
            removed.add(removedEntry);
        }
        
        this.maxSize = maxSize;
        
        return ChangeSet.removed(removed);
    }
    
    public List<Entry> dump() {
        return new ArrayList<>(entries);
    }
    
    public int size() {
        return entries.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public String toString() {
        return "LeastRecentlySeenSet{" + "baseId=" + baseId + ", entries=" + entries + ", maxSize=" + maxSize + '}';
    }



}
