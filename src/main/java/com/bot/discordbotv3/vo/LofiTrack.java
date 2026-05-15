package com.bot.discordbotv3.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class LofiTrack {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("videoId")
    private String videoId;
    @JsonProperty("titleName")
    private String titleName;
    @JsonProperty("url")
    private String url;

    public LofiTrack(Long id, String videoId, String titleName, String url) {
        this.id = id;
        this.videoId = videoId;
        this.titleName = titleName;
        this.url = url;
    }

    public LofiTrack() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
