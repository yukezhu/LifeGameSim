package ca.sfu.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class MessageCompressor {

	public static final int BufferSize = 165536;
	public static boolean compressed = false;

	public static byte[] compress(final byte[] input)
	{
		if(compressed)
		{
			return QuickLZ.compress(input, 1);
		} else
		{
			return input;
		}
	}

	public static byte[] decompress(final byte[] input)
	{
		if(compressed)
		{
			return QuickLZ.decompress(input);
		} else
		{
			return input;
		}
	}

	public static byte[] compressWithDataStream(final byte[] input)
	{
		// Create the compressor with highest level of compression
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);

		// Give the compressor the data to compress
		compressor.setInput(input);
		compressor.finish();

		// Create an expandable byte array to hold the compressed data.
		// You cannot use an array that's the same size as the orginal because
		// there is no guarantee that the compressed data will be smaller than
		// the uncompressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

		// Compress the data
		byte[] buf = new byte[65536];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}
		try {
			bos.close();
		} catch (IOException e) {
		}

		// Get the compressed data
		byte[] compressedData = bos.toByteArray();
		return compressedData;
	}

	public static byte[] decompressWithDataStream(final byte[] compressedData)
	{
		// Create the decompressor and give it the data to compress
		Inflater decompressor = new Inflater();
		decompressor.setInput(compressedData);

		// Create an expandable byte array to hold the decompressed data
		ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);

		// Decompress the data
		byte[] buf = new byte[65536];
		while (!decompressor.finished()) {
			try {
				int count = decompressor.inflate(buf);
				bos.write(buf, 0, count);
			} catch (DataFormatException e) {
			}
		}
		try {
			bos.close();
		} catch (IOException e) {
		}

		// Get the decompressed data
		byte[] decompressedData = bos.toByteArray();
		return decompressedData;
	}

}
