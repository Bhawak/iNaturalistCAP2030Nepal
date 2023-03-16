package org.inaturalist.android.capnepal;

import com.google.firebase.database.PropertyName;
import com.google.gson.annotations.Expose;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
public class Quest implements Serializable {
    @PropertyName("area")
    @Expose
    private String area;
    @PropertyName("challange")
    @Expose
    private String challange;
    @PropertyName("created_at")
    @Expose
    private String createdAt;
    @PropertyName("description")
    @Expose
    private String description;
    @PropertyName("id")
    @Expose
    private Integer id;
    @PropertyName("image")
    @Expose
    private String image;
    @PropertyName("local_name")
    @Expose
    private String localName;
    @PropertyName("scientific_name")
    @Expose
    private String scientificName;
    @PropertyName("valid_until")
    @Expose
    private String validUntil;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getChallange() {
        return challange;
    }

    public void setChallange(String challange) {
        this.challange = challange;
    }
    @PropertyName("created_at")
    public String getCreatedAt() {
        return createdAt;
    }
    @PropertyName("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    @PropertyName("local_name")
    public String getLocalName() {
        return localName;
    }
    @PropertyName("local_name")
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    @PropertyName("scientific_name")
    public String getScientificName() {
        return scientificName;
    }
    @PropertyName("scientific_name")
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
    @PropertyName("valid_until")
    public String getValidUntil() {
        return validUntil;
    }
    @PropertyName("valid_until")
    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }
    public Integer getDueInt(){
//        int dateDifference = (int) getDateDiff(new SimpleDateFormat("dd/MM/yyyy"), "29/05/2017", "31/05/2017");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        int dateDifference = (int) getDateDiff(new SimpleDateFormat("dd/MM/yyyy"), date, validUntil);
//        int dateDifference = (int) getDateDiff(new SimpleDateFormat("dd/MM/yyyy"), "15/10/2021", "1/1/2022");
//        int remains = -1;
//        if(dateDifference < 0) {
//            remains =
            return dateDifference;
//        }
//        return dateDifference + " days remaining";
    }
    //https://stackoverflow.com/questions/21285161/android-difference-between-two-dates
    public static long getDateDiff(SimpleDateFormat format, String oldDate, String newDate) {
        try {
            return TimeUnit.DAYS.convert(format.parse(newDate).getTime() - format.parse(oldDate).getTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
