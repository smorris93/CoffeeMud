package com.planet_ink.miniweb.interfaces;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;

/*
Copyright 2012-2013 Bo Zimmerman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/**
 * Manages multibyte byte buffers as an interator objects
 * @author Bo Zimmerman
 *
 */
public interface DataBuffers extends Iterator<ByteBuffer>
{

	/**
	 * Close out and clear all internal data buffers
	 * This is a required operation, which is done automatically
	 * if all data is iterated through, but should be done
	 * manually otherwise. 
	 */
	public void close();
	
	
	/**
	 * Flushes all internet bytebuffers to a single one.
	 * This can be an expensive operation, but will call
	 * close() on completion.
	 * @return these buffers flushed to one
	 */
	public ByteBuffer flushToBuffer();
	
	/**
	 * Return the length of all bytes buffers here
	 * @return an overall size
	 */
	public int getLength();

	/**
	 * Return the last modified date of the data content
	 * @return a date
	 */
	public Date getLastModified();

	/**
	 * Add a new ByteBuffer to this set.
	 * @param buf the buffer to add
	 * @param lastModifiedTime the last modified date of the data, or 0 to ignore
	 */
	public void add(final ByteBuffer buf, final long lastModifiedTime);

	/**
	 * Add a new byte array to this set.
	 * @param buf the byte array to add
	 * @param lastModifiedTime the last modified date of the data, or 0 to ignore
	 */
	public void add(final byte[] buf, final long lastModifiedTime);

	/**
	 * Add a new input stream to this set.
	 * @param stream the input stream to add
	 * @param length the input stream length
	 * @param lastModifiedTime the last modified date of the data, or 0 to ignore
	 */
	public void add(final InputStream stream, final int length, final long lastModifiedTime);

	/**
	 * Add a new ByteBuffer to the top of this set.
	 * @param buf the buffer to add
	 * @param lastModifiedTime the last modified date of the data, or 0 to ignore
	 */
	public void insertTop(final ByteBuffer buf, final long lastModifiedTime);

	/**
	 * Add a new byte array to the top of this set.
	 * @param buf the byte array to add
	 * @param lastModifiedTime the last modified date of the data, or 0 to ignore
	 */
	public void insertTop(final byte[] buf, final long lastModifiedTime);

	/**
	 * Add a new input stream to the top of this set.
	 * @param stream the input stream to add
	 * @param length the input stream length
	 * @param lastModifiedTime the last modified date of the data, or 0 to ignore
	 */
	public void insertTop(final InputStream stream, final int length, final long lastModifiedTime);
	
	/**
	 * Skip ahead to the given position in these buffers.
	 * @param to the position to skip ahead to
	 */
	public void skip(long to);
	
	/**
	 * Trim, cut off, or prevent reading past the given position.
	 * @param to the position to skip ahead to
	 */
	public void limit(long at);
}