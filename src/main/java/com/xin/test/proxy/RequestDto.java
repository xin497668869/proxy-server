package com.xin.test.proxy;

import lombok.Data;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
@Data
public class RequestDto {
    private String  requestLine;
    private String  method;
    private String  connectHost;
    private Integer connectPort;
    private String  url;
    private String  httpVersion;


    public RequestDto(String content) {
        try {
            String firstLine = content.split("\r\n")[0];

            String[] split = firstLine.split(" ");
            this.method = split[0];
            if (split[1].matches("[\\s\\S]*:[0-9]+$")) {
                this.url = split[1].substring(0, split[1].lastIndexOf(":"));
                this.connectPort = Integer.valueOf(split[1].substring(split[1].lastIndexOf(":") + 1, split[1].length()));
            } else {
                this.url = split[1];
                this.connectPort = 80;
            }
            this.url = this.url.replace("http://", "").replace("https://", "");
            connectHost = this.url.replaceAll("/[\\s\\S]*", "");
            this.url = this.url.replace(connectHost, "");
            this.httpVersion = split[2].trim();
            if (method.equals("CONNECT")) {
                requestLine = null;
            } else {
                requestLine = this.method + " " + this.url + " " + this.httpVersion + content.substring(content.indexOf("\r\n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
