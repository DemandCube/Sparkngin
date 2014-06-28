package com.neverwinterdp.sparkngin;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SendAck {
  static public enum Status {
    OK, ERROR, NOT_AVAIBLE
  }

  private Object messageId ;
  private Status status;
  private String message;

  public Object getMessageId() { return messageId; }
  public void setMessageId(Object objectId) {
    this.messageId = objectId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
