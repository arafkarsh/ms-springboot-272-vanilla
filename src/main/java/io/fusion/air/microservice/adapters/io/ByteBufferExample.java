package io.fusion.air.microservice.adapters.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
public class ByteBufferExample {

    public static void main(String[] args) throws Exception {

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN); 		                // Set to Big-Endian
        buffer.putInt(1234567890);
        buffer.flip();

        int value1 = buffer.getInt();
        System.out.println(value1);

        buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN); 	                    // Set to Little-Endian
        buffer.putInt(1234567890);
        buffer.flip();

        int value2 = buffer.getInt();
        System.out.println(value2);

        buffer.clear();

    }
}
