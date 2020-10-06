package com.rsupport.media.header;

public interface IModel {
    // public void push(byte[] szBuffer, int nStart);

    // szBuffer내용을 읽어서, 객체에 저장
    public void save(byte[] szBuffer, int nStart);

    // szBuffer내용을 읽어서, 객체에 저장시, 객체내에 dstOffset위치 부터, dstLen 크기만큼 저장한다.
    public void save2(byte[] szBuffer, int nStart, int dstOffset, int dstLen);


    // 객체의 내용을 szBuffer에 저장
    public void push(byte[] szBuffer, int nStart);

    public int size();
}
