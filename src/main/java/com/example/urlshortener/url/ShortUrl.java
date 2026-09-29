package com.example.urlshortener.url;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="short_urls")
public class ShortUrl {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, columnDefinition="TEXT")
    private String url;
    @Column(name="short_code", nullable=false, unique=true, length=10)
    private String shortCode;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    @Column(name="access_count", nullable=false) private long accessCount=0;
    protected ShortUrl(){}
    public ShortUrl(String url,String code){
        this.url=url; this.shortCode=code;
        this.createdAt=Instant.now(); this.updatedAt=Instant.now();
    }
    public Long getId(){return id;}
    public String getUrl(){return url;}
    public void setUrl(String u){url=u;}
    public String getShortCode(){return shortCode;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(Instant t){updatedAt=t;}
    public long getAccessCount(){return accessCount;}
    public void setAccessCount(long c){accessCount=c;}
}