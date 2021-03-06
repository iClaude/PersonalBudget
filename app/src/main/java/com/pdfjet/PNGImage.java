/**
 *  PNGImage.java
 *
Copyright (c) 2013, Innovatics Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and / or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.pdfjet;

import java.io.*;
import java.util.*;


/**
 * Used to embed PNG images in the PDF document.
 *
 */
public class PNGImage {

    int w;                  // Image width in pixels
    int h;                  // Image height in pixels

    byte[] data;            // The compressed data in the IDAT chunk
    byte[] inflated;        // The decompressed image data
    byte[] image;           // The reconstructed image data
    byte[] deflated;        // The deflated reconstructed image data
    byte[] rgb;             // The palette data
    byte[] alphaForPalette; // The alpha for the palette data
    byte[] deflatedAlpha;   // The deflated alpha channel data

    protected byte bitDepth = 8;
    protected int colorType = 0;


    /**
     * Used to embed PNG images in the PDF document.
     *
     */
    public PNGImage(InputStream inputStream) throws Exception {
        validatePNG(inputStream);

        List<Chunk> chunks = new ArrayList<Chunk>();
        processPNG(chunks, inputStream);

        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (chunk.type[0] == 'I'
                    && chunk.type[1] == 'H'
                    && chunk.type[2] == 'D'
                    && chunk.type[3] == 'R') {

                this.w = toIntValue(chunk.getData(), 0);    // Width
                this.h = toIntValue(chunk.getData(), 4);    // Height
                this.bitDepth = chunk.getData()[8];         // Bit Depth
                this.colorType = chunk.getData()[9];        // Color Type

                // System.out.println(
                //         "Bit Depth == " + chunk.getData()[ 8 ] );
                // System.out.println(
                //         "Color Type == " + chunk.getData()[ 9 ] );
                // System.out.println( chunk.getData()[ 10 ] );
                // System.out.println( chunk.getData()[ 11 ] );
                // System.out.println( chunk.getData()[ 12 ] );

            }
            else if (chunk.type[0] == 'I'
                    && chunk.type[1] == 'D'
                    && chunk.type[2] == 'A'
                    && chunk.type[3] == 'T') {
                data = appendIdatChunk(data, chunk.getData());
            }
            else if (chunk.type[0] == 'P'
                    && chunk.type[1] == 'L'
                    && chunk.type[2] == 'T'
                    && chunk.type[3] == 'E') {
                rgb = chunk.getData();
                if (rgb.length % 3 != 0) {
                    throw new Exception("Incorrect palette length.");
                }
            }
            else if (chunk.type[0] == 'g'
                    && chunk.type[1] == 'A'
                    && chunk.type[2] == 'M'
                    && chunk.type[3] == 'A') {
                // TODO:
                // System.out.println("gAMA chunk found!");
            }
            else if (chunk.type[0] == 't'
                    && chunk.type[1] == 'R'
                    && chunk.type[2] == 'N'
                    && chunk.type[3] == 'S') {
                // System.out.println("tRNS chunk found!");
                if (colorType == 3) {
                    alphaForPalette = new byte[this.w * this.h];
                    Arrays.fill(alphaForPalette, (byte) 0xff);
                    byte[] alpha = chunk.getData();
                    for (int j = 0; j < alpha.length; j++) {
                        alphaForPalette[j] = alpha[j];
                    }
                }
            }
            else if (chunk.type[0] == 'c'
                    && chunk.type[1] == 'H'
                    && chunk.type[2] == 'R'
                    && chunk.type[3] == 'M') {
                // TODO:
                // System.out.println("cHRM chunk found!");
            }
            else if (chunk.type[0] == 's'
                    && chunk.type[1] == 'B'
                    && chunk.type[2] == 'I'
                    && chunk.type[3] == 'T') {
                // TODO:
                // System.out.println("sBIT chunk found!");
            }
            else if (chunk.type[0] == 'b'
                    && chunk.type[1] == 'K'
                    && chunk.type[2] == 'G'
                    && chunk.type[3] == 'D') {
                // TODO:
                // System.out.println("bKGD chunk found!");
            }

        }

        inflated = getDecompressedData();

        if (colorType == 0) {
            // Grayscale Image
            if (bitDepth == 16) {
                image = getImageColorType0BitDepth16();
            }
            else if (bitDepth == 8) {
                image = getImageColorType0BitDepth8();
            }
            else if (bitDepth == 4) {
                image = getImageColorType0BitDepth4();
            }
            else if (bitDepth == 2) {
                image = getImageColorType0BitDepth2();
            }
            else if (bitDepth == 1) {
                image = getImageColorType0BitDepth1();
            }
            else {
                throw new Exception("Image with unsupported bit depth == " + bitDepth);
            }
        }
        else if (colorType == 6) {
            image = getImageColorType6BitDepth8();
        }
        else {
            // Color Image
            if (rgb == null) {
                // Trucolor Image
                if (bitDepth == 16) {
                    image = getImageColorType2BitDepth16();
                }
                else {
                    image = getImageColorType2BitDepth8();
                }
            }
            else {
                // Indexed Image
                if (bitDepth == 8) {
                    image = getImageColorType3BitDepth8();
                }
                else if (bitDepth == 4) {
                    image = getImageColorType3BitDepth4();
                }
                else if (bitDepth == 2) {
                    image = getImageColorType3BitDepth2();
                }
                else if (bitDepth == 1) {
                    image = getImageColorType3BitDepth1();
                }
                else {
                    throw new Exception("Image with unsupported bit depth == " + bitDepth);
                }
            }
        }

        deflated = deflateReconstructedData();
    }


    protected int getWidth() {
        return this.w;
    }


    protected int getHeight() {
        return this.h;
    }


    protected byte[] getData() {
        return this.deflated;
    }


    protected byte[] getAlpha() {
        return this.deflatedAlpha;
    }


    private void processPNG(
            List< Chunk> chunks, java.io.InputStream inputStream )
        throws Exception {

        while ( true ) {
            Chunk chunk = getChunk( inputStream );
            chunks.add( chunk );
            if ( chunk.type[0] == 'I'
                    && chunk.type[1] == 'E'
                    && chunk.type[2] == 'N'
                    && chunk.type[3] == 'D' ) {
                break;
            }
        }

    }


    private void validatePNG(InputStream inputStream) throws Exception {
        byte[] buf = new byte[8];
        if (inputStream.read(buf, 0, buf.length) == -1) {
            throw new Exception("File is too short!");
        }

        if ((buf[0] & 0xFF) == 0x89 &&
                buf[1] == 0x50 &&
                buf[2] == 0x4E &&
                buf[3] == 0x47 &&
                buf[4] == 0x0D &&
                buf[5] == 0x0A &&
                buf[6] == 0x1A &&
                buf[7] == 0x0A) {
            // The PNG signature is correct.
        }
        else {
            throw new Exception("Wrong PNG signature.");
        }
    }


    private Chunk getChunk(InputStream inputStream) throws Exception {
        Chunk chunk = new Chunk();
        chunk.setLength(getLong( inputStream));     // The length of the data chunk.
        chunk.setType(getBytes(inputStream, 4));    // The chunk type.
        chunk.setData(getBytes(inputStream, chunk.getLength()));    // The chunk data.
        chunk.setCrc(getLong(inputStream));         // CRC of the type and data chunks.
        if (!chunk.hasGoodCRC()) {
            throw new Exception( "Chunk has bad CRC." );
        }
        return chunk;
    }


    private long getLong(InputStream inputStream) throws Exception {
        byte[] buf = getBytes(inputStream, 4);
        return (toIntValue(buf, 0) & 0x00000000ffffffffL);
    }


    private byte[] getBytes(InputStream inputStream, long length) throws Exception {
        byte[] buf = new byte[(int) length];
        if (inputStream.read(buf, 0, buf.length) == -1) {
            throw new Exception("Error reading input stream!");
        }
        return buf;
    }


    private int toIntValue(byte[] buf, int off) {
        long val = 0L;

        val |= (long) buf[off] & 0xff;
        val <<= 8;
        val |= (long) buf[1 + off] & 0xff;
        val <<= 8;
        val |= (long) buf[2 + off] & 0xff;
        val <<= 8;
        val |= (long) buf[3 + off] & 0xff;

        return (int) val;
    }


    // Truecolor Image with Bit Depth == 16
    private byte[] getImageColorType2BitDepth16() {

        int j = 0;
        byte[] image = new byte[ inflated.length - this.h ];

        byte filter = 0x00;
        int scanLineLength = 6 * this.w;

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % ( scanLineLength + 1 ) == 0 ) {
                filter = inflated[ i ];
                continue;
            }

            image[ j ] = inflated[ i ];

            int a = 0;
            int b = 0;
            int c = 0;

            if ( j % scanLineLength >= 6 ) {
                a = ( image[ j - 6 ] & 0x000000ff );
            }

            if ( j >= scanLineLength ) {
                b = ( image[ j - scanLineLength ] & 0x000000ff );
            }

            if ( j % scanLineLength >= 6 && j >= scanLineLength) {
                c = ( image[ j - ( scanLineLength + 6 ) ] & 0x000000ff );
            }

            applyFilters( filter, image, j, a, b, c );

            j++;
        }

        return image;
    }


    // Truecolor Image with Bit Depth == 8
    private byte[] getImageColorType2BitDepth8() {

        int j = 0;
        byte[] image = new byte[ inflated.length - this.h ];

        byte filter = 0x00;
        int scanLineLength = 3 * this.w;

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % ( scanLineLength + 1 ) == 0 ) {
                filter = inflated[ i ];
                continue;
            }

            image[ j ] = inflated[ i ];

            int a = 0;
            int b = 0;
            int c = 0;

            if ( j % scanLineLength >= 3 ) {
                a = ( image[ j - 3 ] & 0x000000ff );
            }

            if ( j >= scanLineLength ) {
                b = ( image[ j - scanLineLength ] & 0x000000ff );
            }

            if ( j % scanLineLength >= 3 && j >= scanLineLength ) {
                c = ( image[ j - ( scanLineLength + 3 ) ] & 0x000000ff );
            }

            applyFilters( filter, image, j, a, b, c );

            j++;
        }

        return image;
    }


    // Truecolor Image with Alpha Transparency
    private byte[] getImageColorType6BitDepth8() throws Exception {
        int j = 0;
        byte[] image = new byte[4 * this.w * this.h];

        byte filter = 0x00;
        int scanLineLength = 4 * this.w;

        for (int i = 0; i < inflated.length; i++) {
            if (i % (scanLineLength + 1) == 0) {
                filter = inflated[i];
                continue;
            }

            image[j] = inflated[i];

            int a = 0;
            int b = 0;
            int c = 0;

            if (j % scanLineLength >= 4) {
                a = (image[j - 4] & 0x000000ff);
            }
            if (j >= scanLineLength) {
                b = (image[j - scanLineLength] & 0x000000ff);
            }
            if (j % scanLineLength >= 4 && j >= scanLineLength) {
                c = (image[j - (scanLineLength + 4)] & 0x000000ff);
            }

            applyFilters(filter, image, j, a, b, c);
            j++;
        }

        byte[] idata = new byte[3 * this.w * this.h];   // Image data
        byte[] alpha = new byte[this.w * this.h];       // Alpha values

        int k = 0;
        int n = 0;
        for (int i = 0; i < image.length; i += 4) {
            idata[k]     = image[i];
            idata[k + 1] = image[i + 1];
            idata[k + 2] = image[i + 2];
            alpha[n]     = image[i + 3];

            k += 3;
            n += 1;
        }

        Compressor compressor = new Compressor(alpha);
        deflatedAlpha = compressor.getCompressedData();

        return idata;
    }


    // Indexed Image with Bit Depth == 8
    private byte[] getImageColorType3BitDepth8() {
        int j = 0;
        int n = 0;

        byte[] alpha = null;
        if (alphaForPalette != null) {
            alpha = new byte[this.w * this.h];
        }

        byte[] image = new byte[3*(inflated.length - this.h)];
        int scanLineLength = this.w + 1;
        for (int i = 0; i < inflated.length; i++) {
            if (i % scanLineLength == 0) {
                // Skip the filter byte
                continue;
            }

            int k = ((int) inflated[i] & 0x000000ff);
            image[j++] = rgb[3*k];
            image[j++] = rgb[3*k + 1];
            image[j++] = rgb[3*k + 2];

            if (alphaForPalette != null) {
                alpha[n++] = alphaForPalette[k];
            }
        }

        if (alphaForPalette != null) {
            Compressor compressor = new Compressor(alpha);
            deflatedAlpha = compressor.getCompressedData();
        }

        return image;
    }


    // Indexed Image with Bit Depth == 4
    private byte[] getImageColorType3BitDepth4() {

        int j = 0;
        int k;

        byte[] image = new byte[ 6 * ( inflated.length - this.h ) ];
        int scanLineLength = this.w / 2 + 1;
        if ( this.w % 2 > 0 ) {
            scanLineLength += 1;
        }

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % scanLineLength == 0 ) {
                // Skip the filter byte.
                continue;
            }

            int l = (int) inflated[i];

            k = 3 * ((l >> 4) & 0x0000000f);
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * (l & 0x0000000f);
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

        }

        return image;
    }


    // Indexed Image with Bit Depth == 2
    private byte[] getImageColorType3BitDepth2() {
        int j = 0;
        int k;

        byte[] image = new byte[ 12 * ( inflated.length - this.h ) ];
        int scanLineLength = this.w / 4 + 1;
        if ( this.w % 4 > 0 ) {
            scanLineLength += 1;
        }

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % scanLineLength == 0 ) {
                // Skip the filter byte.
                continue;
            }

            int l = ( int ) inflated[ i ];

            k = 3 * ( ( l >> 6 ) & 0x00000003 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * ( ( l >> 4 ) & 0x00000003 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * ( ( l >> 2 ) & 0x00000003 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * (l & 0x00000003);
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

        }

        return image;
    }


    // Indexed Image with Bit Depth == 1
    private byte[] getImageColorType3BitDepth1() {
        int j = 0;
        int k;

        byte[] image = new byte[ 24 * ( inflated.length - this.h ) ];
        int scanLineLength = this.w / 8 + 1;
        if ( this.w % 8 > 0 ) {
            scanLineLength += 1;
        }

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % scanLineLength == 0 ) {
                // Skip the filter byte.
                continue;
            }

            int l = ( int ) inflated[ i ];

            k = 3 * ( ( l >> 7 ) & 0x00000001 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * ( ( l >> 6 ) & 0x00000001 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * ( ( l >> 5 ) & 0x00000001 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * ( ( l >> 4 ) & 0x00000001 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * ( ( l >> 3 ) & 0x00000001 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * ( ( l >> 2 ) & 0x00000001 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * ( ( l >> 1 ) & 0x00000001 );
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

            if ( j % ( 3 * this.w ) == 0 ) continue;

            k = 3 * (l & 0x00000001);
            image[ j++ ] = rgb[ k ];
            image[ j++ ] = rgb[ k + 1 ];
            image[ j++ ] = rgb[ k + 2 ];

        }

        return image;
    }


    // Grayscale Image with Bit Depth == 16
    private byte[] getImageColorType0BitDepth16() {

        int j = 0;
        byte[] image = new byte[ inflated.length - this.h ];

        byte filter = 0x00;
        int scanLineLength = 2 * this.w;

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % ( scanLineLength + 1 ) == 0 ) {
                filter = inflated[ i ];
                continue;
            }

            image[ j ] = inflated[ i ];

            int a = 0;
            int b = 0;
            int c = 0;

            if ( j % scanLineLength >= 2 ) {
                a = ( image[ j - 2 ] & 0x000000ff );
            }

            if ( j >= scanLineLength ) {
                b = ( image[ j - scanLineLength ] & 0x000000ff );
            }

            if ( j % scanLineLength >= 2 && j >= scanLineLength) {
                c = ( image[ j - ( scanLineLength + 2 ) ] & 0x000000ff );
            }

            applyFilters( filter, image, j, a, b, c );

            j++;
        }

        return image;
    }


    // Grayscale Image with Bit Depth == 8
    private byte[] getImageColorType0BitDepth8() {

        int j = 0;
        byte[] image = new byte[ inflated.length - this.h ];

        byte filter = 0x00;
        int scanLineLength = this.w;

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % ( scanLineLength + 1 ) == 0 ) {
                filter = inflated[ i ];
                continue;
            }

            image[ j ] = inflated[ i ];

            int a = 0;
            int b = 0;
            int c = 0;

            if ( j % scanLineLength >= 1 ) {
                a = ( image[ j - 1 ] & 0x000000ff );
            }

            if ( j >= scanLineLength ) {
                b = ( image[ j - scanLineLength ] & 0x000000ff );
            }

            if ( j % scanLineLength >= 1 && j >= scanLineLength) {
                c = ( image[ j - ( scanLineLength + 1 ) ] & 0x000000ff );
            }

            applyFilters( filter, image, j, a, b, c );

            j++;
        }

        return image;
    }


    // Grayscale Image with Bit Depth == 4
    private byte[] getImageColorType0BitDepth4() {

        int j = 0;
        byte[] image = new byte[ inflated.length - this.h ];

        int scanLineLength = this.w / 2 + 1;
        if ( this.w % 2 > 0 ) {
            scanLineLength += 1;
        }

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % scanLineLength == 0 ) {
                continue;
            }

            image[ j++ ] = inflated[ i ];
        }

        return image;
    }


    // Grayscale Image with Bit Depth == 2
    private byte[] getImageColorType0BitDepth2() {

        int j = 0;
        byte[] image = new byte[ inflated.length - this.h ];

        int scanLineLength = this.w / 4 + 1;
        if ( this.w % 4 > 0 ) {
            scanLineLength += 1;
        }

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % scanLineLength == 0 ) {
                continue;
            }

            image[ j++ ] = inflated[ i ];
        }

        return image;
    }


    // Grayscale Image with Bit Depth == 1
    private byte[] getImageColorType0BitDepth1() {

        int j = 0;
        byte[] image = new byte[ inflated.length - this.h ];

        int scanLineLength = this.w / 8 + 1;
        if ( this.w % 8 > 0 ) {
            scanLineLength += 1;
        }

        for ( int i = 0; i < inflated.length; i++ ) {

            if ( i % scanLineLength == 0 ) {
                continue;
            }

            image[ j++ ] = inflated[ i ];
        }

        return image;
    }


    private void applyFilters( byte filter, byte[] image, int j, int a, int b, int c ) {

        if ( filter == 0x00 ) {             // None
            // Nothing to do.
        }
        else if ( filter == 0x01 ) {        // Sub
            image[ j ] += ( byte ) a;
        }
        else if ( filter == 0x02 ) {        // Up
            image[ j ] += ( byte ) b;
        }
        else if ( filter == 0x03 ) {        // Average
            image[ j ] += ( byte ) Math.floor( ( double ) ( a + b ) / 2 );
        }
        else if ( filter == 0x04 ) {        // Paeth
            int pr;
            int p = a + b - c;
            int pa = Math.abs( p - a );
            int pb = Math.abs( p - b );
            int pc = Math.abs( p - c );
            if ( pa <= pb && pa <= pc ) {
                pr = a;
            }
            else if ( pb <= pc ) {
                pr = b;
            }
            else {
                pr = c;
            }

            image[ j ] += ( byte ) ( pr & 0x000000ff );
        }

    }


    private byte[] getDecompressedData() throws Exception {
        Decompressor decompressor = new Decompressor(data);
        return decompressor.getDecompressedData();
    }


    private byte[] deflateReconstructedData() throws Exception {
        Compressor compressor = new Compressor(image);
        return compressor.getCompressedData();
    }


    private byte[] appendIdatChunk(byte[] array1, byte[] array2) {
        if (array1 == null) {
            return array2;
        } else if (array2 == null) {
            return array1;
        }
        byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }


    public static void main(String[] args) throws Exception {

        FileInputStream fis = new FileInputStream(args[0]);
        BufferedInputStream bis = new BufferedInputStream(fis);

        PNGImage png = new PNGImage(bis);
        byte[] data = png.getData();
        byte[] alpha = png.getAlpha();
        int w = png.getWidth();
        int h = png.getHeight();
        int b = png.bitDepth;
        int c = png.colorType;
        bis.close();

        if (alpha != null) {
            throw new Exception("PNG images with Alpha Channel are not supported.");
        }

        int index = args[0].lastIndexOf("/") + 1;
        String fileName = args[0].substring(index, args[0].indexOf(".", index));
        StringBuilder buf = new StringBuilder();
        buf.append(fileName);
        if (c == 0) {
            buf.append(".grayscale.");
        }
        else {
            buf.append(".rgb.");
        }
        buf.append(w);
        buf.append("x");
        buf.append(h);
        buf.append("x");
        buf.append(b);
        buf.append(".raw");

        FileOutputStream fos = new FileOutputStream(buf.toString());
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(data);

        bos.flush();
        bos.close();
    }

}   // End of PNGImage.java
