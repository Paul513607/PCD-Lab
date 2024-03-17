package org.example;

public class ReadDto {
    private byte[] message;
    private long bytesRead;
    private int numMessages;

    public ReadDto(byte[] message, long bytesRead, int numMessages) {
        this.message = message;
        this.bytesRead = bytesRead;
        this.numMessages = numMessages;
    }

    public byte[] getMessage() {
        return message;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public int getNumMessages() {
        return numMessages;
    }
}
