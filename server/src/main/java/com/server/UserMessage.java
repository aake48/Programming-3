package com.server;

import java.time.Instant;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UserMessage {

    private String locationName;
    private String locationDescription;
    private String locationCity;
    private String locationCountry;
    private String locationStreetAddress;
    private String originalPoster;
    private String latitude;
    private String longitude;
    private ZonedDateTime originalPostingTime;

    public UserMessage(String location, String description, String city, String country, String address,
            String originalPoster2, String postingTime, String latitude2, String longitude2) {
        this.locationName = location;
        this.locationDescription = description;
        this.locationCity = city;
        this.locationCountry = country;
        this.locationStreetAddress = address;
        this.originalPoster = originalPoster2;
        this.latitude = latitude2;
        this.longitude = longitude2;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        originalPostingTime = ZonedDateTime.parse(postingTime, formatter);
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public ZonedDateTime getOriginalPostingTime() {
        return originalPostingTime;
    }

    public void setOriginalPostingTime(ZonedDateTime originalPostingTime) {
        this.originalPostingTime = originalPostingTime;
    }

    public String getLocationCountry() {
        return locationCountry;
    }

    public void setLocationCountry(String locationCountry) {
        this.locationCountry = locationCountry;
    }

    public String getLocationStreetAddress() {
        return locationStreetAddress;
    }

    public void setLocationStreetAddress(String locationStreetAddress) {
        this.locationStreetAddress = locationStreetAddress;
    }

    public String getOriginalPoster() {
        return originalPoster;
    }

    public void setOriginalPoster(String originalPoster) {
        this.originalPoster = originalPoster;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * Converts the original posting time to a long value representing the
     * milliseconds since the epoch.
     *
     * 
     * @return A long value representing the original posting time in milliseconds.
     */
    public long dateAsInt() {
        return originalPostingTime.toInstant().toEpochMilli();
    }

    /**
     * Sets the original posting time based on the provided epoch time in
     * milliseconds.
     *
     * @param epoch The epoch time in milliseconds to set as the original posting
     *              time.
     */
    public void setSent(long epoch) {
        originalPostingTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }
}
