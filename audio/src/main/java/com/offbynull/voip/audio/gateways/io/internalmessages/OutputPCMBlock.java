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
package com.offbynull.voip.audio.gateways.io.internalmessages;

import java.util.Arrays;
import org.apache.commons.lang3.Validate;

/**
 * A block of PCM audio data to write out to the opened output audio device.
 * <p>
 * PCM data must conform to the following ...
 * <ul>
 * <li>16000Hz sample rate</li>
 * <li>8-bit sample size (signed)</li>
 * <li>1 channel (mono)</li>
 * <li>at least 800 samples (800 bytes / 50ms of data)</li>
 * </ul>
 * <p>
 * This class is immutable.
 * @author Kasra Faghihi
 */
public final class OutputPCMBlock {
    private final byte[] data;

    /**
     * Constructs an {@link OutputPCMBlock} object.
     * @param data PCM data
     * @throws NullPointerException if any argument is {@code null}
     */
    public OutputPCMBlock(byte[] data) {
        Validate.notNull(data);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Get PCM data.
     * @return PCM data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
