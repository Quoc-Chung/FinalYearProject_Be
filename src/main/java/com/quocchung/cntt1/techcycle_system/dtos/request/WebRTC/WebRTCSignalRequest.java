package com.quocchung.cntt1.techcycle_system.dtos.request.WebRTC;

public class WebRTCSignalRequest {
  private String type;       // "offer" | "answer" | "ice-candidate"
  private String sdp;        // cho offer/answer

  private String candidate;

  private String  sdpMid;

  private String  sdpMLineIndex;

  //https://claude.ai/chat/32c3cb8b-b409-44fe-9270-1a619cca404b
}
