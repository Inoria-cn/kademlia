package com.offbynull.kademlia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BitStringTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void mustCreateFromString() {
        BitString bitString1 = BitString.createFromString("100001011111");
        assertEquals("100001011111", bitString1.toString());
    }

    @Test
    public void mustFailConstructingFromStringIfSpacesInString() {
        expectedException.expect(IllegalArgumentException.class);
        BitString.createFromString("1000 0101 1111");
    }

    @Test
    public void mustCreateTheSameBitStringUsingAllConstructors() {
        BitString bitString1 = BitString.createLogicalOrder(new byte[] { (byte) 0x1F, (byte) 0xFF, (byte) 0xF0, 0x00 }, 0, 32);
        BitString bitString2 = BitString.createReadOrder(new byte[] { (byte) 0xF8, (byte) 0xFF, (byte) 0x0F, 0x00 }, 0, 32);
        
        assertEquals(bitString1, bitString2);
    }

    @Test
    public void mustCreateTheSameBitStringUsingAllConstructorsWhenUnaligned() {
        BitString bitString1 = BitString.createLogicalOrder(new byte[] { (byte) 0xAB, (byte) 0xCD, (byte) 0x0F }, 4, 8);
        BitString bitString2 = BitString.createReadOrder(new byte[] { (byte) 0xD5, (byte) 0xB3, (byte) 0x0F }, 4, 8);
        
        assertEquals(bitString1, bitString2);
    }

    @Test
    public void mustFailWhenConstructingWhenOffsetOutOfBounds() {
        expectedException.expect(IllegalArgumentException.class);
        BitString.createLogicalOrder(new byte[] { (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 33, 0);
    }

    @Test
    public void mustFailWhenConstructingWhenLengthOutOfBounds() {
        expectedException.expect(IllegalArgumentException.class);
        BitString.createLogicalOrder(new byte[] { (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 0, 33);
    }
    
    @Test
    public void mustGetIndividualBits() {
        BitString bitString = BitString.createLogicalOrder(toBytes(0x3C5A000000000000L), 0, 16);

        assertFalse(bitString.getBit(0));
        assertFalse(bitString.getBit(1));
        assertTrue(bitString.getBit(2));
        assertTrue(bitString.getBit(3));

        assertTrue(bitString.getBit(4));
        assertTrue(bitString.getBit(5));
        assertFalse(bitString.getBit(6));
        assertFalse(bitString.getBit(7));

        assertFalse(bitString.getBit(8));
        assertTrue(bitString.getBit(9));
        assertFalse(bitString.getBit(10));
        assertTrue(bitString.getBit(11));

        assertTrue(bitString.getBit(12));
        assertFalse(bitString.getBit(13));
        assertTrue(bitString.getBit(14));
        assertFalse(bitString.getBit(15));
    }

    @Test
    public void mustGetGroupsOfBits() {
        BitString bitString = BitString.createLogicalOrder(toBytes(0x3C5A000000000000L), 0, 16);
        
        BitString expected = BitString.createLogicalOrder(toBytes(0x5000000000000000L), 3, 4);
        BitString actual = bitString.getBits(8, 4);

        assertEquals(expected, actual);
    }

    @Test
    public void mustGetGroupsOfBitsAsLong() {
        BitString bitString = BitString.createLogicalOrder(toBytes(0x3CFA000000000000L), 0, 16);
        
        long expected = 0x5L;
        long actual = bitString.getBitsAsLong(8, 4);

        assertEquals(expected, actual);
    }

    @Test
    public void mustSetIndividualBits() {
        BitString bitString = BitString.createLogicalOrder(toBytes(0x0000000000000000L), 48, 16);

        bitString = bitString.setBit(0, false);
        bitString = bitString.setBit(1, false);
        bitString = bitString.setBit(2, true);
        bitString = bitString.setBit(3, true);

        bitString = bitString.setBit(4, true);
        bitString = bitString.setBit(5, true);
        bitString = bitString.setBit(6, false);
        bitString = bitString.setBit(7, false);

        bitString = bitString.setBit(8, false);
        bitString = bitString.setBit(9, true);
        bitString = bitString.setBit(10, false);
        bitString = bitString.setBit(11, true);

        bitString = bitString.setBit(12, true);
        bitString = bitString.setBit(13, false);
        bitString = bitString.setBit(14, true);
        bitString = bitString.setBit(15, false);
        
        assertEquals(BitString.createLogicalOrder(toBytes(0x0000000000003C5AL), 48, 16), bitString);
    }

    @Test
    public void mustSetGroupsOfBitsTo1() {
        BitString bitString = BitString.createLogicalOrder(toBytes(0x3C5A000000000000L), 0, 16);
        BitString modifier  = BitString.createLogicalOrder(toBytes(0xF000000000000000L), 4, 4);
        
        BitString actual = bitString.setBits(8, modifier);
        BitString expected = BitString.createLogicalOrder(toBytes(0x3C5F000000000000L), 0, 16);

        assertEquals(expected, actual);
    }

    @Test
    public void mustAppendGroupsOfBits() {
        BitString bitString = BitString.createLogicalOrder(toBytes(0x3C5A000000000000L), 0, 16);
        BitString modifier  = BitString.createLogicalOrder(toBytes(0xF000000000000000L), 4, 4);
        
        BitString actual = bitString.appendBits(modifier);
        BitString expected = BitString.createLogicalOrder(toBytes(0x3C5A0F0000000000L), 0, 20);

        assertEquals(expected, actual);
    }

    @Test
    public void mustSetGroupsOfBitsTo0() {
        BitString bitString = BitString.createLogicalOrder(toBytes(0x3C5A000000000000L), 0, 16);
        BitString modifier  = BitString.createLogicalOrder(toBytes(0x0000000000000000L), 4, 4);
        
        BitString actual = bitString.setBits(8, modifier);
        BitString expected = BitString.createLogicalOrder(toBytes(0x3C50000000000000L), 0, 16);

        assertEquals(expected, actual);
    }
    
    @Test
    public void mustFlipBits() {
        BitString bitString = BitString.createLogicalOrder(toBytes(0x3C5AL), 48, 16);

        // 3
        bitString = bitString.flipBit(0);
        bitString = bitString.flipBit(1);
        bitString = bitString.flipBit(2);
        bitString = bitString.flipBit(3);
        
        // 9
        bitString = bitString.flipBit(4);
        bitString = bitString.flipBit(5);
        bitString = bitString.flipBit(6);
        bitString = bitString.flipBit(7);
        
        // 5
        bitString = bitString.flipBit(8);
        bitString = bitString.flipBit(9);
        bitString = bitString.flipBit(10);
        bitString = bitString.flipBit(11);

        // A
        bitString = bitString.flipBit(12);
        bitString = bitString.flipBit(13);
        bitString = bitString.flipBit(14);
        bitString = bitString.flipBit(15);
        
        assertEquals(BitString.createLogicalOrder(toBytes(0xC3A5L), 48, 16), bitString);
    }

    @Test
    public void mustNotIdentifyExcessBitsAsCommonPrefix() {
        BitString bitString1 = BitString.createFromString("1");
        BitString bitString2 = BitString.createFromString("1001");
        
        assertEquals(1, bitString1.getSharedPrefixLength(bitString2));
    }

    @Test
    public void mustIdentifyCommonPrefixLength() {
        BitString baseBitString = BitString.createLogicalOrder(toBytes(0x000000000000A2F0L), 48, 12);
        BitString noMatchBitString = BitString.createLogicalOrder(toBytes(0x0000000000000100L), 48, 12);
        BitString partialMatchBitString = BitString.createLogicalOrder(toBytes(0x0000000000002200L), 48, 12);
        BitString fullMatchBitString = BitString.createLogicalOrder(toBytes(0x000000000000A2F0L), 48, 12);
        
        assertEquals(0, baseBitString.getSharedPrefixLength(noMatchBitString));
        assertEquals(7, baseBitString.getSharedPrefixLength(partialMatchBitString));
        assertEquals(12, baseBitString.getSharedPrefixLength(fullMatchBitString));
    }

    @Test
    public void mustIdentifyCommonPrefixLengthOnSmallerSizes() {
        BitString baseBitString = BitString.createLogicalOrder(toBytes(0x000000000000A2F0L), 48, 12);
        BitString noMatchBitString = BitString.createLogicalOrder(toBytes(0x0000000000000100L), 48, 1);
        BitString partialMatchBitString1 = BitString.createLogicalOrder(toBytes(0x0000000000002200L), 48, 9);
        BitString partialMatchBitString2 = BitString.createLogicalOrder(toBytes(0x000000000000A2C0L), 48, 9);
        
        assertEquals(0, baseBitString.getSharedPrefixLength(noMatchBitString));
        assertEquals(7, baseBitString.getSharedPrefixLength(partialMatchBitString1));
        assertEquals(9, baseBitString.getSharedPrefixLength(partialMatchBitString2));
    }

    @Test
    public void mustIdentifyCommonPrefixLengthOnLargerSizes() {
        BitString baseBitString = BitString.createLogicalOrder(toBytes(0x000000000000A2F0L), 48, 12);
        BitString noMatchBitString = BitString.createLogicalOrder(toBytes(0x0000000000000100L), 48, 16);
        BitString partialMatchBitString1 = BitString.createLogicalOrder(toBytes(0x0000000000002200L), 48, 16);
        BitString partialMatchBitString2 = BitString.createLogicalOrder(toBytes(0x000000000000A22CL), 48, 16);
        
        assertEquals(0, baseBitString.getSharedPrefixLength(noMatchBitString));
        assertEquals(7, baseBitString.getSharedPrefixLength(partialMatchBitString1));
        assertEquals(10, baseBitString.getSharedPrefixLength(partialMatchBitString2));
    }
    
    @Test
    public void mustIdentifyCommonPrefixLengthWhenDealingWithEdgeBits() {
        // impelemtnation uses bytes ... groups of 8 bits
        BitString baseBitString = BitString.createFromString("100000000");
        
        assertEquals(9, BitString.createFromString("100000000").getSharedPrefixLength(baseBitString));
        assertEquals(8, BitString.createFromString("10000000").getSharedPrefixLength(baseBitString));
        assertEquals(7, BitString.createFromString("1000000").getSharedPrefixLength(baseBitString));
        assertEquals(6, BitString.createFromString("100000").getSharedPrefixLength(baseBitString));
        assertEquals(5, BitString.createFromString("10000").getSharedPrefixLength(baseBitString));
        assertEquals(4, BitString.createFromString("1000").getSharedPrefixLength(baseBitString));
        assertEquals(3, BitString.createFromString("100").getSharedPrefixLength(baseBitString));
        assertEquals(2, BitString.createFromString("10").getSharedPrefixLength(baseBitString));
        assertEquals(1, BitString.createFromString("1").getSharedPrefixLength(baseBitString));
        assertEquals(0, BitString.createFromString("").getSharedPrefixLength(baseBitString));

    }

    @Test
    public void mustIdentifyCommonSuffixLength() {
        BitString baseBitString = BitString.createFromString("000000000001");
        BitString noMatchBitString = BitString.createFromString("000000000000");
        BitString partialMatchBitString = BitString.createFromString("000010000001");
        BitString fullMatchBitString = BitString.createFromString("000000000001");
        
        assertEquals(0, baseBitString.getSharedSuffixLength(noMatchBitString));
        assertEquals(7, baseBitString.getSharedSuffixLength(partialMatchBitString));
        assertEquals(12, baseBitString.getSharedSuffixLength(fullMatchBitString));
    }

    @Test
    public void mustIdentifyCommonSuffixLengthOnSmallerSizes() {
        BitString baseBitString = BitString.createFromString("000000000001");
        BitString noMatchBitString = BitString.createFromString("0");
        BitString partialMatchBitString1 = BitString.createFromString("110000001");
        BitString partialMatchBitString2 = BitString.createFromString("000000001");
        
        assertEquals(0, baseBitString.getSharedSuffixLength(noMatchBitString));
        assertEquals(7, baseBitString.getSharedSuffixLength(partialMatchBitString1));
        assertEquals(9, baseBitString.getSharedSuffixLength(partialMatchBitString2));
    }

    @Test
    public void mustIdentifyCommonSuffixLengthOnLargerSizes() {
        BitString baseBitString = BitString.createFromString("000000000001");
        BitString noMatchBitString = BitString.createFromString("0000000000000000");
        BitString partialMatchBitString1 = BitString.createFromString("0000000010000001");
        BitString partialMatchBitString2 = BitString.createFromString("0000010000000001");
        
        assertEquals(0, baseBitString.getSharedSuffixLength(noMatchBitString));
        assertEquals(7, baseBitString.getSharedSuffixLength(partialMatchBitString1));
        assertEquals(10, baseBitString.getSharedSuffixLength(partialMatchBitString2));
    }

    @Test
    public void mustIdentifyCommonSuffixLengthWhenDealingWithEdgeBits() {
        // impelemtnation uses bytes ... groups of 8 bits
        BitString baseBitString = BitString.createFromString("000000001");
        
        assertEquals(9, BitString.createFromString("000000001").getSharedSuffixLength(baseBitString));
        assertEquals(8, BitString.createFromString("00000001").getSharedSuffixLength(baseBitString));
        assertEquals(7, BitString.createFromString("0000001").getSharedSuffixLength(baseBitString));
        assertEquals(6, BitString.createFromString("000001").getSharedSuffixLength(baseBitString));
        assertEquals(5, BitString.createFromString("00001").getSharedSuffixLength(baseBitString));
        assertEquals(4, BitString.createFromString("0001").getSharedSuffixLength(baseBitString));
        assertEquals(3, BitString.createFromString("001").getSharedSuffixLength(baseBitString));
        assertEquals(2, BitString.createFromString("01").getSharedSuffixLength(baseBitString));
        assertEquals(1, BitString.createFromString("1").getSharedSuffixLength(baseBitString));
        assertEquals(0, BitString.createFromString("").getSharedSuffixLength(baseBitString));

    }
    
    private static byte[] toBytes(long data) { // returns in big endian format
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            int shiftAmount = 56 - (i * 8);
            bytes[i] = (byte) (data >>> shiftAmount);
        }
        return bytes;
    }
}
